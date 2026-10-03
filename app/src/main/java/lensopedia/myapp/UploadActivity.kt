package lensopedia.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import lensopedia.myapp.models.Post
import lensopedia.myapp.models.User

class UploadActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var ivSelectedImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var etPostTitle: EditText
    private lateinit var etPostDescription: EditText
    private lateinit var btnPublish: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvUploadStatus: TextView
    private lateinit var bottomNav: BottomNavigationView

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Standard findViewById for mobile AIDE compatibility
        ivSelectedImage = findViewById(R.id.ivUploadPreview)
        btnSelectImage = findViewById(R.id.btnChoosePhoto)
        etPostTitle = findViewById(R.id.etUploadTitle)
        etPostDescription = findViewById(R.id.etUploadDescription)
        btnPublish = findViewById(R.id.btnPublishPost)
        progressBar = findViewById(R.id.progressBarUpload)
        tvUploadStatus = findViewById(R.id.tvUploadStatus)
        bottomNav = findViewById(R.id.bottomNavigationViewUpload)

        btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        btnPublish.setOnClickListener {
            handlePublishPost()
        }

        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.navUpload
    }

    private fun setupBottomNavigation() {
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navExplore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    finish()
                    true
                }
                R.id.navUpload -> true
                R.id.navProfile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            ivSelectedImage.setImageURI(selectedImageUri)
            ivSelectedImage.visibility = View.VISIBLE
            btnSelectImage.text = "Change Photo"
        }
    }

    private fun handlePublishPost() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please choose an image to share", Toast.LENGTH_SHORT).show()
            return
        }

        val title = etPostTitle.text.toString().trim()
        val description = etPostDescription.text.toString().trim()

        if (TextUtils.isEmpty(title)) {
            etPostTitle.error = "Please enter a title for your photo"
            etPostTitle.requestFocus()
            return
        }

        if (TextUtils.isEmpty(description)) {
            etPostDescription.error = "Please write a brief description"
            etPostDescription.requestFocus()
            return
        }

        // Start upload process
        progressBar.visibility = View.VISIBLE
        tvUploadStatus.visibility = View.VISIBLE
        tvUploadStatus.text = "Uploading image to Firebase Storage..."
        btnPublish.isEnabled = false
        btnSelectImage.isEnabled = false

        val userId = currentUser.uid
        val timestamp = System.currentTimeMillis()
        val storageRef = storage.reference.child("uploads/$userId/$timestamp.jpg")

        // 1. Upload selected image file to Firebase Storage
        storageRef.putFile(selectedImageUri!!)
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                tvUploadStatus.text = "Uploading: $progress%"
            }
            .addOnSuccessListener {
                // 2. Obtain the public download URL
                tvUploadStatus.text = "Finalizing metadata in Firestore..."
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val downloadUrl = downloadUri.toString()
                    fetchAuthorNameAndSavePost(userId, downloadUrl, title, description, timestamp)
                }.addOnFailureListener { e ->
                    handleUploadFailure("Could not obtain image URL: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                handleUploadFailure("Storage upload failed: ${e.localizedMessage}")
            }
    }

    private fun fetchAuthorNameAndSavePost(
        userId: String,
        downloadUrl: String,
        title: String,
        description: String,
        timestamp: Long
    ) {
        firestore.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val authorName = if (doc.exists()) {
                    val user = doc.toObject(User::class.java)
                    user?.getFullName() ?: "Photographer"
                } else {
                    "Photographer"
                }

                // 3. Post information in Firestore Posts collection
                savePostToFirestore(userId, authorName, downloadUrl, title, description, timestamp)
            }
            .addOnFailureListener {
                // Save anyway with default name
                savePostToFirestore(userId, "Photographer", downloadUrl, title, description, timestamp)
            }
    }

    private fun savePostToFirestore(
        userId: String,
        authorName: String,
        downloadUrl: String,
        title: String,
        description: String,
        timestamp: Long
    ) {
        val newPostRef = firestore.collection("Posts").document()
        val postId = newPostRef.id

        val post = Post(
            postId = postId,
            userId = userId,
            authorName = authorName,
            imageUrl = downloadUrl,
            title = title,
            description = description,
            timestamp = timestamp
        )

        newPostRef.set(post)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                tvUploadStatus.visibility = View.GONE
                Toast.makeText(this, "Photo published successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to Explore Feed to see the new post
                val intent = Intent(this, ExploreActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                handleUploadFailure("Could not publish post: ${e.localizedMessage}")
            }
    }

    private fun handleUploadFailure(message: String) {
        progressBar.visibility = View.GONE
        tvUploadStatus.visibility = View.GONE
        btnPublish.isEnabled = true
        btnSelectImage.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
