package com.capstone.ecorecyc.login;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.capstone.ecorecyc.R;
import com.capstone.ecorecyc.R.drawable.visibility_off;
import com.capstone.ecorecyc.R.drawable.visibility_on;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth;
    private lateinit var firestore: FirebaseFirestore;
    private lateinit var progressBar: ProgressBar;
    private var isPasswordVisible = false;

    private val REQUEST_CODE_CHOOSE_ROLE = 1001;
    private var selectedRole: String? = null;
    private lateinit var roleTextView: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progress_bar);
        progressBar.visibility = View.GONE;

        val usernameField: EditText = findViewById(R.id.username);
        val emailField: EditText = findViewById(R.id.email);
        val passwordField: EditText = findViewById(R.id.password);

        setUpPasswordVisibilityToggle(passwordField);

        val chooseRoleButton: Button = findViewById(R.id.btnChooseRole);
        roleTextView = findViewById(R.id.selectedRoleText);
        roleTextView.text = "No role selected";

        chooseRoleButton.setOnClickListener {
            val intent = Intent(this, com.capstone.ecorecyc.chooserole.ChooseRole::class.java);
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_ROLE);
        }

        val registerButton: Button = findViewById(R.id.btn_login);
        registerButton.setOnClickListener {
            val username = usernameField.text.toString().trim();
            val email = emailField.text.toString().trim();
            val password = passwordField.text.toString().trim();

            if (selectedRole == null) {
                Toast.makeText(this, "Please choose a role", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.visibility = View.VISIBLE;

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE;
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid;
                            val user = hashMapOf(
                                "displayName" to username,
                                "email" to email,
                                "role" to selectedRole,
                                "firstTimeLogin" to (selectedRole == "JUNKSHOP_OWNER") // Set firstTimeLogin only for junkshop owners
                            );
                            userId?.let {
                                firestore.collection("users").document(it).set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                        startActivity(Intent(this, Login::class.java)); // Redirect all users to Login
                                        finish();
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show();
                                    };
                            }
                        } else {
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show();
                        }
                    }
            }
        }

        val login2btn: Button = findViewById(R.id.loginregister);
        login2btn.setOnClickListener {
            startActivity(Intent(this, Login::class.java));
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_ROLE && resultCode == RESULT_OK) {
            selectedRole = data?.getStringExtra("USER_TYPE") ?: "USER";
            roleTextView.text = when (selectedRole) {
                "JUNKSHOP_OWNER" -> "Junkshop";
                else -> "User";
            };
        }
    }

    private fun setUpPasswordVisibilityToggle(passwordField: EditText) {
        val visibilityOnIcon: Drawable? = ContextCompat.getDrawable(this, visibility_on);
        val visibilityOffIcon: Drawable? = ContextCompat.getDrawable(this, visibility_off);
        val passwordIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.password_icon);

        passwordField.setCompoundDrawablesWithIntrinsicBounds(passwordIcon, null, visibilityOffIcon, null);

        passwordField.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = passwordField.compoundDrawables[2];
                if (drawableEnd != null && event.rawX >= (passwordField.right - drawableEnd.bounds.width())) {
                    isPasswordVisible = !isPasswordVisible;
                    if (isPasswordVisible) {
                        passwordField.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(passwordIcon, null, visibilityOnIcon, null);
                    } else {
                        passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD;
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(passwordIcon, null, visibilityOffIcon, null);
                    }
                    passwordField.setSelection(passwordField.text.length);
                    return@setOnTouchListener true;
                }
            }
            false;
        }
    }
}
