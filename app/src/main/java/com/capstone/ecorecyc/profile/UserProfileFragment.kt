package com.capstone.ecorecyc.profile

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.capstone.ecorecyc.R
import com.capstone.ecorecyc.login.Login
import com.capstone.ecorecyc.orders.MyListings
import com.capstone.ecorecyc.orders.MyOrders
import com.capstone.ecorecyc.settings.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get references to TextViews and ImageView
        profileName = view.findViewById(R.id.profile_name)
        profileEmail = view.findViewById(R.id.profile_email)
        profileImageView = view.findViewById(R.id.profile_image)

        // Load the current user profile
        loadUserProfile()

        // Settings button to navigate to Settings activity
        val settingsBtn: ImageButton = view.findViewById(R.id.settings_btn)
        settingsBtn.setOnClickListener {
            val intent = Intent(activity, Settings::class.java)
            startActivity(intent)
        }

        // Logout button to sign out the user
        val logoutBtn: ImageButton = view.findViewById(R.id.logout_btn)
        logoutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            activity?.finish()
        }

        // My Orders button to navigate to My Orders activity
        val myOrdersBtn: ImageButton = view.findViewById(R.id.my_orders_btn)
        myOrdersBtn.setOnClickListener {
            val intent = Intent(activity, MyOrders::class.java)
            startActivity(intent)
        }

        // My Listings button to navigate to My Listings activity
        val myListingsBtn: ImageButton = view.findViewById(R.id.my_listings_btn)
        myListingsBtn.setOnClickListener {
            val intent = Intent(activity, MyListings::class.java) // Open MyListings activity
            startActivity(intent)
        }



        // Rate Us button (ImageButton and Text Button)
        val rateUsImageButton: ImageButton = view.findViewById(R.id.rateus_btn)
        val rateUsTextButton: Button = view.findViewById(R.id.rateus_btn_text)

        // Set click listeners for Rate Us
        rateUsImageButton.setOnClickListener { showRateUsDialog() }
        rateUsTextButton.setOnClickListener { showRateUsDialog() }

        return view
    }

    private fun showRateUsDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_rate_us)

        val star1: ImageView = dialog.findViewById(R.id.star1)
        val star2: ImageView = dialog.findViewById(R.id.star2)
        val star3: ImageView = dialog.findViewById(R.id.star3)
        val star4: ImageView = dialog.findViewById(R.id.star4)
        val star5: ImageView = dialog.findViewById(R.id.star5)
        val submitButton: Button = dialog.findViewById(R.id.submitButton)

        var rating = 0

        // Handle star clicks
        val stars = listOf(star1, star2, star3, star4, star5)
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                rating = index + 1
                updateStars(stars, rating)
            }
        }

        // Handle submit button click
        submitButton.setOnClickListener {
            if (rating > 0) {
                Toast.makeText(requireContext(), "Thank you for your $rating-star rating!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please select a rating", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                star.setImageResource(R.drawable.ic_star_filled) // Filled star
            } else {
                star.setImageResource(R.drawable.ic_star_outline) // Outline star
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile() // Reload user profile whenever the fragment is resumed
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        currentUser?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        profileEmail.text = document.getString("email") ?: it.email
                        val displayName = document.getString("displayName") ?: "No Name"
                        profileName.text = displayName

                        val photoUrl = document.getString("photoUrl")
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(photoUrl)
                                .transform(CircleCrop())
                                .into(profileImageView)
                        } else {
                            Glide.with(this)
                                .load(R.drawable.img_5) // Default image if no profile photo
                                .transform(CircleCrop())
                                .into(profileImageView)
                        }
                    } else {
                        Log.d("UserProfileFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("UserProfileFragment", "Error getting documents: ", exception)
                }
        }
    }
}