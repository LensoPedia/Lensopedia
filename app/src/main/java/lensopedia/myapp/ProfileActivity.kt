package lensopedia.myapp

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lensopedia.myapp.models.User
import java.util.Date

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvJoinedDate: TextView
    private lateinit var tvUserPostsCount: TextView
    private lateinit var btnLogout: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Standard findViewById for mobile AIDE compatibility
        tvFullName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        tvJoinedDate = findViewById(R.id.tvProfileJoined)
        tvUserPostsCount = findViewById(R.id.tvProfilePostsCount)
        btnLogout = findViewById(R.id.btnLogout)
        progressBar = findViewById(R.id.progressBarProfile)
        bottomNav = findViewById(R.id.bottomNavigationViewProfile)

        btnLogout.setOnClickListener {
            handleLogout()
        }

        setupBottomNavigation()
        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.navProfile
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
                R.id.navUpload -> {
                    startActivity(Intent(this, UploadActivity::class.java))
                    finish()
                    true
                }
                R.id.navProfile -> true
                else -> false
            }
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            handleLogout()
            return
        }

        val uid = currentUser.uid
        progressBar.visibility = View.VISIBLE

        // 1. Fetch user metadata from Users/{userId}
        firestore.collection("Users").document(uid)
            .get()
            .addOnSuccessListener { docSnapshot ->
                progressBar.visibility = View.GONE
                if (docSnapshot.exists()) {
                    val user = docSnapshot.toObject(User::class.java)
                    if (user != null) {
                        tvFullName.text = user.getFullName()
                        tvEmail.text = user.email
                        if (user.createdAt > 0) {
                            val formattedDate = DateFormat.format("MMMM yyyy", Date(user.createdAt)).toString()
                            tvJoinedDate.text = "Member since $formattedDate"
                        } else {
                            tvJoinedDate.text = "Member of Lensopedia"
                        }
                    }
                } else {
                    tvFullName.text = currentUser.email ?: "Creator"
                    tvEmail.text = currentUser.email ?: ""
                    tvJoinedDate.text = "Active Member"
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Profile sync error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // 2. Fetch user's post count
        firestore.collection("Posts")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { query ->
                tvUserPostsCount.text = "${query.size()} Posts Shared"
            }
            .addOnFailureListener {
                tvUserPostsCount.text = "0 Posts Shared"
            }
    }

    private fun handleLogout() {
        // Clear active session and navigate back to AuthActivity
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
