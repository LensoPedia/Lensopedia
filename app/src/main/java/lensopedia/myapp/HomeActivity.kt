package lensopedia.myapp

import android.content.Intent
import android.os.Bundle
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

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvWelcome: TextView
    private lateinit var tvUserSubtitle: TextView
    private lateinit var tvTotalPostsCount: TextView
    private lateinit var tvCreatorsCount: TextView
    private lateinit var btnExploreFeed: Button
    private lateinit var btnUploadPhoto: Button
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Standard findViewById for mobile AIDE compatibility
        tvWelcome = findViewById(R.id.tvHomeWelcome)
        tvUserSubtitle = findViewById(R.id.tvHomeSubtitle)
        tvTotalPostsCount = findViewById(R.id.tvStatPosts)
        tvCreatorsCount = findViewById(R.id.tvStatCreators)
        btnExploreFeed = findViewById(R.id.btnHomeExplore)
        btnUploadPhoto = findViewById(R.id.btnHomeUpload)
        bottomNav = findViewById(R.id.bottomNavigationView)
        progressBar = findViewById(R.id.progressBarHome)

        // Action shortcuts
        btnExploreFeed.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        btnUploadPhoto.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        setupBottomNavigation()
        loadUserProfile()
        loadCommunityStats()
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.navHome
    }

    private fun setupBottomNavigation() {
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> true
                R.id.navExplore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    true
                }
                R.id.navUpload -> {
                    startActivity(Intent(this, UploadActivity::class.java))
                    true
                }
                R.id.navProfile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToAuth()
            return
        }

        val uid = currentUser.uid
        progressBar.visibility = View.VISIBLE

        firestore.collection("Users").document(uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                progressBar.visibility = View.GONE
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    val firstName = user?.firstName ?: "Creator"
                    tvWelcome.text = "Welcome back, $firstName!"
                    tvUserSubtitle.text = "Share and discover high-definition visual stories"
                } else {
                    tvWelcome.text = "Welcome, Creator!"
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                tvWelcome.text = "Welcome back!"
                Toast.makeText(this, "Could not sync user profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadCommunityStats() {
        firestore.collection("Posts").get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.size()
                tvTotalPostsCount.text = "$count"
            }
            .addOnFailureListener {
                tvTotalPostsCount.text = "12+"
            }

        firestore.collection("Users").get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.size()
                tvCreatorsCount.text = "$count"
            }
            .addOnFailureListener {
                tvCreatorsCount.text = "8+"
            }
    }

    private fun redirectToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
