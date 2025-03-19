package com.capstone.ecorecyc.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.capstone.ecorecyc.R
import com.capstone.ecorecyc.cleanup.CleanupEvents
import com.capstone.ecorecyc.maps.Maps
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.capstone.ecorecyc.marketplace.Marketplace
import com.capstone.ecorecyc.notifications.Notifications
import com.capstone.ecorecyc.recyclinghub.RecyclingHub
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var greetingTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val sharedPrefs by lazy { requireActivity().getSharedPreferences("EcorecycPrefs", Context.MODE_PRIVATE) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize views
        greetingTextView = view.findViewById(R.id.hiusername)
        locationTextView = view.findViewById(R.id.locationTextView)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Check if user has been prompted before
        if (!sharedPrefs.getBoolean("location_prompted", false)) {
            checkLocationPermission()
            sharedPrefs.edit().putBoolean("location_prompted", true).apply()
        }

        // Setup buttons
        setupButtons(view)

        return view
    }

    private fun setupButtons(view: View) {
        // Button navigation setup
        view.findViewById<ImageButton>(R.id.marketplacebutton).setOnClickListener {
            navigateToActivity(Marketplace::class.java, "USER")
        }
        view.findViewById<ImageButton>(R.id.recylinghubbutton).setOnClickListener {
            navigateToActivity(RecyclingHub::class.java, "USER")
        }
        view.findViewById<ImageButton>(R.id.cleanupbtn).setOnClickListener {
            navigateToActivity(CleanupEvents::class.java, "USER")
        }
        view.findViewById<ImageButton>(R.id.notificationBtn).setOnClickListener {
            startActivity(Intent(activity, Notifications::class.java)) // Navigates to Notifications
        }
        view.findViewById<ImageButton>(R.id.locationpin).setOnClickListener {
            startActivity(Intent(activity, Maps::class.java)) // Navigates to Maps
        }
    }

    private fun navigateToActivity(activityClass: Class<*>, userType: String) {
        val intent = Intent(activity, activityClass)
        intent.putExtra("USER_TYPE", userType)
        startActivity(intent)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions granted; fetch location
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { updateLocationUI(it) } ?: run {
                    locationTextView.text = "Location not available"
                }
            }.addOnFailureListener {
                locationTextView.text = "Failed to fetch location"
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                // Extract street name and city
                val street = address.thoroughfare ?: address.subLocality ?: "Unknown Street"
                val city = address.locality ?: address.subAdminArea ?: "Unknown City"

                // Set the formatted address
                locationTextView.text = "$street, $city"
            } else {
                locationTextView.text = "Location not found"
            }
        } catch (e: Exception) {
            locationTextView.text = "Error retrieving location"
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUsername()
        fetchLocation()
    }

    private fun fetchUsername() {
        val userId = auth.currentUser?.uid
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    val username = document?.getString("displayName") ?: "User"
                    greetingTextView.text = "Hi, $username!"
                }
                .addOnFailureListener {
                    greetingTextView.text = "Hi, User!"
                }
        } ?: run {
            greetingTextView.text = "Hi, User!"
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
