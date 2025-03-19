package com.capstone.ecorecyc

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object UserInteractionTracker {

    private const val TAG = "UserInteractionTracker"
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Updates the user's interaction count for the given category.
     * Firestore automatically creates the collection/document if they don't exist.
     *
     * @param category The category of the item the user interacted with.
     */
    fun updateUserPreference(category: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "No user is logged in.")
            return
        }
        val userPrefRef = db.collection("user_preferences").document(userId)

        userPrefRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Increment the count for the specified category.
                    userPrefRef.update(category, FieldValue.increment(1))
                        .addOnSuccessListener {
                            Log.d(TAG, "Updated category '$category' for user $userId")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating category '$category' for user $userId", e)
                        }
                } else {
                    // Document does not exist; create a new one.
                    val data = hashMapOf(category to 1)
                    userPrefRef.set(data)
                        .addOnSuccessListener {
                            Log.d(TAG, "Created user preference for $userId with category '$category'")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error creating user preference for $userId", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting document for user $userId", e)
            }
    }
}
