package com.capstone.ecorecyc.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.capstone.ecorecyc.R
import com.capstone.ecorecyc.dashboard.Navbar
import com.capstone.ecorecyc.junkshop.JunkShopDashboard
import com.capstone.ecorecyc.junkshop.JunkshopSetupProfile1
import com.capstone.ecorecyc.junkshop.JunkshopSetupProfile2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressBar: ProgressBar

    private val PREFS_NAME = "LoginPrefs"
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        progressBar = findViewById(R.id.progress_bar)
        val emailField: EditText = findViewById(R.id.email)
        val passwordField: EditText = findViewById(R.id.password)
        val rememberMeCheckBox: CheckBox = findViewById(R.id.remembermeCheckbox)
        val loginButton: Button = findViewById(R.id.btn_login)
        val signupBtn: Button = findViewById(R.id.Signup)
        val forgotPasswordBtn: Button = findViewById(R.id.forgotbtn)

        progressBar.visibility = View.GONE
        restoreLoginDetails(emailField, passwordField, rememberMeCheckBox)
        setUpPasswordVisibilityToggle(passwordField)

        signupBtn.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        forgotPasswordBtn.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    saveLoginDetails(email, password, rememberMeCheckBox.isChecked)
                    navigateToDashboard()
                } else {
                    Toast.makeText(this, task.exception?.localizedMessage ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToDashboard() {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                val role = document.getString("role") ?: "USER"
                val profileSetup1Completed = document.getBoolean("profileSetup1Completed") ?: false
                val profileSetup2Completed = document.getBoolean("profileSetup2Completed") ?: false

                val nextIntent = when {
                    role == "JUNKSHOP_OWNER" && !profileSetup1Completed -> Intent(this, JunkshopSetupProfile1::class.java)
                    role == "JUNKSHOP_OWNER" && !profileSetup2Completed -> Intent(this, JunkshopSetupProfile2::class.java)
                    role == "JUNKSHOP_OWNER" -> Intent(this, JunkShopDashboard::class.java)
                    else -> Intent(this, Navbar::class.java)
                }

                startActivity(nextIntent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
    }

    private fun restoreLoginDetails(emailField: EditText, passwordField: EditText, checkBox: CheckBox) {
        val email = sharedPreferences.getString("remembered_email", null)
        val password = sharedPreferences.getString("remembered_password", null)
        if (email != null && password != null) {
            emailField.setText(email)
            passwordField.setText(password)
            checkBox.isChecked = true
            passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    private fun saveLoginDetails(email: String, password: String, rememberMe: Boolean) {
        val editor = sharedPreferences.edit()
        if (rememberMe) {
            editor.putString("remembered_email", email)
            editor.putString("remembered_password", password)
        } else {
            editor.remove("remembered_email")
            editor.remove("remembered_password")
        }
        editor.apply()
    }

    private fun setUpPasswordVisibilityToggle(passwordField: EditText) {
        val visibilityOnIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.visibility_on)
        val visibilityOffIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.visibility_off)
        val passwordIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.password_icon)

        passwordField.setCompoundDrawablesWithIntrinsicBounds(passwordIcon, null, visibilityOffIcon, null)

        passwordField.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = passwordField.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (passwordField.right - drawableEnd.bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    passwordField.inputType = if (isPasswordVisible) {
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    passwordField.setCompoundDrawablesWithIntrinsicBounds(passwordIcon, null, if (isPasswordVisible) visibilityOnIcon else visibilityOffIcon, null)
                    passwordField.setSelection(passwordField.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}
