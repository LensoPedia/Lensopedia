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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lensopedia.myapp.models.User
import org.json.JSONObject
import java.util.Random

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etFirstName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etOtpCode: EditText

    private lateinit var btnSendOtp: Button
    private lateinit var btnCreateAccount: Button
    private lateinit var tvLoginLink: TextView
    private lateinit var progressBar: ProgressBar

    // OTP tracking
    private var generatedOtp: String? = null
    private var isOtpSent: Boolean = false

    // EmailJS Configuration
    // Replace with your keys from https://dashboard.emailjs.com/
    private val EMAILJS_SERVICE_ID = "service_lensopedia"
    private val EMAILJS_TEMPLATE_ID = "template_lens_otp"
    private val EMAILJS_USER_ID = "user_lens_public_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Standard findViewById for mobile AIDE compatibility
        etFirstName = findViewById(R.id.etRegFirstName)
        etSurname = findViewById(R.id.etRegSurname)
        etEmail = findViewById(R.id.etRegEmail)
        etPassword = findViewById(R.id.etRegPassword)
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword)
        etOtpCode = findViewById(R.id.etRegOtp)

        btnSendOtp = findViewById(R.id.btnSendOtp)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        tvLoginLink = findViewById(R.id.tvLoginLink)
        progressBar = findViewById(R.id.progressBarRegister)

        btnSendOtp.setOnClickListener {
            handleSendOtp()
        }

        btnCreateAccount.setOnClickListener {
            handleCreateAccount()
        }

        tvLoginLink.setOnClickListener {
            finish() // Return to Login
        }
    }

    private fun handleSendOtp() {
        val firstName = etFirstName.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (TextUtils.isEmpty(firstName)) {
            etFirstName.error = "First name is required"
            etFirstName.requestFocus()
            return
        }

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email address"
            etEmail.requestFocus()
            return
        }

        // Generate 6-digit random verification code
        val random = Random()
        val codeInt = 100000 + random.nextInt(900000)
        generatedOtp = codeInt.toString()

        progressBar.visibility = View.VISIBLE
        btnSendOtp.isEnabled = false

        // Dispatch OTP via EmailJS REST API using Volley
        sendOtpViaEmailJs(email, firstName, generatedOtp!!)
    }

    private fun sendOtpViaEmailJs(toEmail: String, recipientName: String, otp: String) {
        val url = "https://api.emailjs.com/api/v1.0/email/send"

        try {
            val templateParams = JSONObject().apply {
                put("to_email", toEmail)
                put("to_name", recipientName)
                put("otp_code", otp)
                put("app_name", "Lensopedia")
            }

            val body = JSONObject().apply {
                put("service_id", EMAILJS_SERVICE_ID)
                put("template_id", EMAILJS_TEMPLATE_ID)
                put("user_id", EMAILJS_USER_ID)
                put("template_params", templateParams)
            }

            val requestQueue = Volley.newRequestQueue(this)
            val jsonRequest = object : JsonObjectRequest(
                Request.Method.POST, url, body,
                { response ->
                    progressBar.visibility = View.GONE
                    btnSendOtp.isEnabled = true
                    isOtpSent = true
                    btnSendOtp.text = "Resend Code"
                    Toast.makeText(this, "OTP sent to $toEmail. Please check your inbox.", Toast.LENGTH_LONG).show()
                },
                { error ->
                    progressBar.visibility = View.GONE
                    btnSendOtp.isEnabled = true
                    // In case user hasn't set custom EmailJS keys yet, provide fallback display for testing in AIDE
                    isOtpSent = true
                    val errorMsg = error.message ?: "EmailJS service unreachable"
                    Toast.makeText(
                        this,
                        "Testing Mode: Your OTP is $otp (Setup EmailJS keys in strings.xml for live dispatch)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }

            requestQueue.add(jsonRequest)

        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            btnSendOtp.isEnabled = true
            Toast.makeText(this, "Error building OTP request: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCreateAccount() {
        val firstName = etFirstName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val inputOtp = etOtpCode.text.toString().trim()

        if (TextUtils.isEmpty(firstName)) {
            etFirstName.error = "First name is required"
            return
        }

        if (TextUtils.isEmpty(surname)) {
            etSurname.error = "Surname is required"
            return
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.error = "Email is required"
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return
        }

        if (!isOtpSent) {
            Toast.makeText(this, "Please click 'Send Code' to receive OTP verification", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(inputOtp)) {
            etOtpCode.error = "Enter the 6-digit OTP"
            return
        }

        if (inputOtp != generatedOtp) {
            etOtpCode.error = "Invalid OTP code. Please verify again."
            return
        }

        // Validation passed! Proceed to Firebase Authentication
        progressBar.visibility = View.VISIBLE
        btnCreateAccount.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                saveUserProfileToFirestore(uid, firstName, surname, email)
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnCreateAccount.isEnabled = true
                Toast.makeText(this, "Registration failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserProfileToFirestore(uid: String, firstName: String, surname: String, email: String) {
        val user = User(
            uid = uid,
            firstName = firstName,
            surname = surname,
            email = email,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("Users").document(uid)
            .set(user)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Registration successful! Welcome to Lensopedia.", Toast.LENGTH_SHORT).show()

                // Redirect to HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnCreateAccount.isEnabled = true
                Toast.makeText(this, "Profile sync failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
