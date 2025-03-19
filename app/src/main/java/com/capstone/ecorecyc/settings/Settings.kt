package com.capstone.ecorecyc.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.ecorecyc.R
import com.capstone.ecorecyc.login.Login
import com.capstone.ecorecyc.profile.EditProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Edit Profile button logic
        val editProf: Button = findViewById(R.id.editProfileBtn)
        editProf.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))

        }


        val backButton = findViewById<Button>(R.id.backButtonsettings)
        backButton.setOnClickListener {
            finish()
        }
        // Account Deletion button logic
        val deleteAccountBtn: Button = findViewById(R.id.requestAccountDeletionBtn)
        deleteAccountBtn.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { dialog, which ->
                deleteAccount()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            val firestore = FirebaseFirestore.getInstance()

            // Remove user data from Firestore
            firestore.collection("users").document(uid).delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // After Firestore data deletion, delete the user from Firebase Authentication
                    user.delete().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
                            // Redirect to Login screen
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Failed to delete account: ${deleteTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Failed to remove user data: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    val backButton = findViewById<Button>(R.id.backButtonsettings)
                    backButton.setOnClickListener {
                        finish()
                    }
                }
            }
        }
    }
}
