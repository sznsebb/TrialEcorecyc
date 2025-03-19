package com.capstone.ecorecyc.marketplace

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.ecorecyc.data.model.Data
import com.capstone.ecorecyc.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class Marketplace : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBox: EditText
    private lateinit var sellItemBtn: ImageButton
    private lateinit var locationTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var categoryButton: TextView
    private lateinit var categorySpinner: Spinner

    private val firestore = FirebaseFirestore.getInstance()
    private val itemList = mutableListOf<Data.Item>()
    private lateinit var adapter: MarketplaceAdapter

    // A token to track the current query; when a new query is initiated, this value is updated.
    private var currentQueryToken: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marketplace)

        recyclerView = findViewById(R.id.recyclerView)
        searchBox = findViewById(R.id.searchbox)
        sellItemBtn = findViewById(R.id.sellItemBtn)
        locationTextView = findViewById(R.id.locationTextView)
        categoryButton = findViewById(R.id.categoryButton)
        categorySpinner = findViewById(R.id.categorySpinner)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = MarketplaceAdapter(itemList)
        recyclerView.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        // Back button functionality.
        val backButton = findViewById<ImageButton>(R.id.marketplace_back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Set up the spinner with category options.
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.category_options,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter
        categorySpinner.visibility = View.INVISIBLE

        // Open the spinner when the category button is clicked.
        categoryButton.setOnClickListener {
            categorySpinner.performClick()
        }

        // Listener for spinner selections.
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                val selectedCategory = parent.getItemAtPosition(position).toString()
                if (selectedCategory == "All") {
                    fetchItems()
                } else {
                    filterItemsByCategory(selectedCategory)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed.
            }
        }

        // Setup search functionality.
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchItems(query)
                } else {
                    fetchItems()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Launch SellAnItem activity.
        sellItemBtn.setOnClickListener {
            val intent = Intent(this, SellAnItem::class.java)
            startActivity(intent)
        }

        // Initially fetch all items.
        fetchItems()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    updateLocationUI(it)
                }
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val street = address.thoroughfare ?: address.subLocality ?: "Unknown Street"
            val city = address.locality ?: address.subAdminArea ?: "Unknown City"
            locationTextView.text = "$street, $city"
        } else {
            locationTextView.text = "Location not found"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        }
    }

    private fun fetchItems() {
        // Update the query token for this new query.
        currentQueryToken = System.currentTimeMillis()
        val queryToken = currentQueryToken

        firestore.collection("items")
            .get()
            .addOnSuccessListener { documents ->
                // Ignore if a new query has started.
                if (queryToken != currentQueryToken) return@addOnSuccessListener

                itemList.clear()
                for (document in documents) {
                    val item = document.toObject(Data.Item::class.java)
                    item.id = document.id
                    if (item.userId.isNotEmpty()) {
                        // Pass the query token to fetchUsername.
                        fetchUsername(item.userId, item, queryToken)
                    } else {
                        if (queryToken == currentQueryToken) {
                            itemList.add(item)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Marketplace", "Error getting documents: ", exception)
            }
    }

    // Modified fetchUsername() now accepts a queryToken.
    private fun fetchUsername(userId: String, item: Data.Item, queryToken: Long) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                // If this callback is from an old query, do nothing.
                if (queryToken != currentQueryToken) return@addOnSuccessListener

                if (document.exists()) {
                    val username = document.getString("username") ?: "Unknown User"
                    val displayName = document.getString("displayName") ?: username
                    item.username = username
                    item.displayName = displayName
                }
                if (queryToken == currentQueryToken) {
                    itemList.add(item)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Marketplace", "Error fetching username: ", exception)
                if (queryToken != currentQueryToken) return@addOnFailureListener
                itemList.add(item)
                adapter.notifyDataSetChanged()
            }
    }

    private fun searchItems(query: String) {
        // Update the query token for this new search.
        currentQueryToken = System.currentTimeMillis()
        val queryToken = currentQueryToken

        firestore.collection("items")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                if (queryToken != currentQueryToken) return@addOnSuccessListener

                itemList.clear()
                for (document in documents) {
                    val item = document.toObject(Data.Item::class.java)
                    item.id = document.id
                    if (item.userId.isNotEmpty()) {
                        fetchUsername(item.userId, item, queryToken)
                    } else {
                        if (queryToken == currentQueryToken) {
                            itemList.add(item)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Marketplace", "Error searching documents: ", exception)
            }
    }

    // Query Firestore to get items for a specific category.
    private fun filterItemsByCategory(category: String) {
        // For simplicity, category filtering is kept separate since it doesn't use asynchronous username lookups.
        firestore.collection("items")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                val filteredItems = mutableListOf<Data.Item>()
                for (document in documents) {
                    val item = document.toObject(Data.Item::class.java)
                    item.id = document.id
                    filteredItems.add(item)
                }
                adapter.updateItems(filteredItems)
            }
            .addOnFailureListener { exception ->
                Log.e("Marketplace", "Error filtering items by category: ", exception)
            }
    }
}
