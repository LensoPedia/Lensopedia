package lensopedia.myapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import lensopedia.myapp.adapters.PostAdapter
import lensopedia.myapp.models.Post

class ExploreActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyFeed: TextView
    private lateinit var bottomNav: BottomNavigationView

    private val postList = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        firestore = FirebaseFirestore.getInstance()

        // Standard findViewById for mobile AIDE compatibility
        recyclerView = findViewById(R.id.rvExplorePosts)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshExplore)
        progressBar = findViewById(R.id.progressBarExplore)
        tvEmptyFeed = findViewById(R.id.tvEmptyFeed)
        bottomNav = findViewById(R.id.bottomNavigationViewExplore)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(this, postList)
        recyclerView.adapter = postAdapter

        swipeRefreshLayout.setOnRefreshListener {
            loadExplorePosts()
        }

        setupBottomNavigation()
        loadExplorePosts()
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.navExplore
    }

    private fun setupBottomNavigation() {
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navExplore -> true
                R.id.navUpload -> {
                    startActivity(Intent(this, UploadActivity::class.java))
                    finish()
                    true
                }
                R.id.navProfile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadExplorePosts() {
        if (!swipeRefreshLayout.isRefreshing) {
            progressBar.visibility = View.VISIBLE
        }

        firestore.collection("Posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                postList.clear()

                for (doc in querySnapshot.documents) {
                    val post = doc.toObject(Post::class.java)
                    if (post != null) {
                        postList.add(post)
                    }
                }

                postAdapter.notifyDataSetChanged()

                if (postList.isEmpty()) {
                    tvEmptyFeed.visibility = View.VISIBLE
                } else {
                    tvEmptyFeed.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                val msg = exception.localizedMessage ?: "Failed to retrieve posts"
                Toast.makeText(this, "Feed Error: $msg", Toast.LENGTH_SHORT).show()

                if (postList.isEmpty()) {
                    tvEmptyFeed.visibility = View.VISIBLE
                    tvEmptyFeed.text = "No posts found or network error.\nPull down to retry."
                }
            }
    }
}
