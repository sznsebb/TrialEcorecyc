package com.capstone.ecorecyc.cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.capstone.ecorecyc.Chat
import com.capstone.ecorecyc.R
import com.capstone.ecorecyc.data.model.Data
import com.google.firebase.auth.FirebaseAuth

class ItemDetails : AppCompatActivity() {

    private lateinit var itemImage: ImageView
    private lateinit var itemName: TextView
    private lateinit var itemPrice: TextView
    private lateinit var itemDescription: TextView
    private lateinit var itemCondition: TextView
    private lateinit var itemLocation: TextView
    private lateinit var addToCartButton: Button
    private lateinit var buyNowButton: Button
    private lateinit var contactSellerButton: Button
    private lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        // Initialize views
        itemImage = findViewById(R.id.itemImage)
        itemName = findViewById(R.id.itemName)
        itemPrice = findViewById(R.id.itemPrice)
        itemDescription = findViewById(R.id.itemDescription)
        itemCondition = findViewById(R.id.itemCondition)
        itemLocation = findViewById(R.id.itemLocation)
        addToCartButton = findViewById(R.id.addToCartButton)
        buyNowButton = findViewById(R.id.buyNowButton)
        backBtn = findViewById(R.id.backBtn)
        contactSellerButton = findViewById(R.id.itemContactSeller)

        // Get the item details from the intent
        val name = intent.getStringExtra("name") ?: ""
        val price = intent.getStringExtra("price") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val condition = intent.getStringExtra("condition") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val sellerId = intent.getStringExtra("sellerId") ?: ""

        // Set the retrieved data to the views
        itemName.text = name
        itemPrice.text = "PHP $price"
        itemDescription.text = description
        itemCondition.text = "Condition: $condition"
        itemLocation.text = location

        // Load the image using Glide
        Glide.with(this).load(imageUrl).into(itemImage)

        // "Add to Cart" logic (example)
        addToCartButton.setOnClickListener {
            val item = Data.Item(
                name = name,
                price = price,
                imageUrl = imageUrl,
                description = description,
                condition = condition,
                location = location
            )
             CartManager.addItem(item)
            Toast.makeText(this, "$name added to cart", Toast.LENGTH_SHORT).show()
        }

        buyNowButton.setOnClickListener {
            // Handle "Buy Now"
        }

        backBtn.setOnClickListener {
            finish()
        }

        contactSellerButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "You must be logged in to chat", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Prevent seller from contacting themselves
            if (currentUser.uid == sellerId) {
                Toast.makeText(this, "This is your item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Generate a unique conversation ID
            val chatId = if (currentUser.uid < sellerId) "${currentUser.uid}-$sellerId" else "$sellerId-${currentUser.uid}"

            // Launch the Chat activity
            val intentChat = Intent(this, Chat::class.java).apply {
                putExtra("receiverId", sellerId)
                putExtra("chatId", chatId)
            }
            startActivity(intentChat)

            Log.d("ItemDetailsActivity", "Sender ID: ${FirebaseAuth.getInstance().currentUser?.uid}, Receiver ID: $sellerId")

        }
    }
}
