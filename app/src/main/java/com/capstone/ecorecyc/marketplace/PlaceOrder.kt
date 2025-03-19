package com.capstone.ecorecyc.marketplace

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.capstone.ecorecyc.R
import com.bumptech.glide.Glide

class PlaceOrder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_order)

        // Retrieve views
        val itemImage: ImageView = findViewById(R.id.itemImage)
        val itemName: TextView = findViewById(R.id.second_hand_item_name)
        val itemPrice: TextView = findViewById(R.id.second_hand_item_price)

        // Retrieve data passed from cart
        val imageUrl = intent.getStringExtra("image_url")
        val name = intent.getStringExtra("item_name")
        val price = intent.getStringExtra("item_price")

        // Set retrieved data
        itemName.text = name ?: "Item Name"
        itemPrice.text = "₱${price ?: "0.00"}"

        // Load image using Glide (ensure you have Glide dependency in build.gradle)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).into(itemImage)
        } else {
            itemImage.setImageResource(R.drawable.ic_launcher_background) // Default image
        }
        Log.d("PlaceOrder", "Image URL: $imageUrl, Name: $name, Price: $price")

    }
}
