package lensopedia.myapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterLink: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        // Standard findViewById compatible with mobile AIDE environments
        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)
        progressBar = findViewById(R.id.progressBarLogin)

        btnLogin.setOnClickListener {
            performLogin()
        }

        tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            etEmail.error = "Please enter your email"
            etEmail.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.error = "Please enter your password"
            etPassword.requestFocus()
            return
        }

        // Show loading progress
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                Toast.makeText(this, "Welcome back to Lensopedia!", Toast.LENGTH_SHORT).show()

                // Redirect to HomeActivity and clear previous activities
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                val errorMessage = exception.localizedMessage ?: "Authentication failed"
                Toast.makeText(this, "Login Error: $errorMessage", Toast.LENGTH_LONG).show()
            }
    }
}
