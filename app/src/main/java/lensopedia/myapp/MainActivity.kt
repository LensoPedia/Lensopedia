package lensopedia.myapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Auto-login session check: If user is authenticated, skip onboarding
        if (auth.currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Inflate splash & onboarding layout
        setContentView(R.layout.activity_main)

        // Standard findViewById compatible with mobile AIDE environments
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val tvSignInPrompt = findViewById<TextView>(R.id.tvSignInPrompt)

        btnGetStarted.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        tvSignInPrompt.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }
}
