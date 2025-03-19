package com.capstone.ecorecyc.marketplace

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.ecorecyc.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class SellAnItem : AppCompatActivity() {

    private lateinit var uploadBtn: Button
    private lateinit var confirmBtn: Button
    private lateinit var prodName: EditText
    private lateinit var price: EditText
    private lateinit var description: EditText
    private lateinit var condition: EditText
    private lateinit var location: EditText
    private lateinit var categoryButton: Button
    private lateinit var categorySpinner: Spinner

    private var imageUri: Uri? = null
    private val storage = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    private var selectedCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_an_item)

        // Initialize views
        uploadBtn = findViewById(R.id.upload_btn)
        confirmBtn = findViewById(R.id.confirm_sell_item_btn)
        prodName = findViewById(R.id.prod_Name)
        price = findViewById(R.id.price)
        description = findViewById(R.id.description)
        condition = findViewById(R.id.condition)
        location = findViewById(R.id.market_Location)
        categoryButton = findViewById(R.id.categoryButton)
        categorySpinner = findViewById(R.id.categorySpinner)

        // Category spinner logic
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedCategory = parent.getItemAtPosition(position).toString()
                categoryButton.text = selectedCategory
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = ""
            }
        }

        categoryButton.setOnClickListener {
            categorySpinner.performClick()
        }

        // Select image
        uploadBtn.setOnClickListener {
            selectImage()
        }

        // Confirm upload
        confirmBtn.setOnClickListener {
            uploadItem()
        }
    }

    // Launch image picker
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadItem() {
        val productName = prodName.text.toString().trim()
        val productPrice = price.text.toString().trim()
        val productDescription = description.text.toString().trim()
        val productCondition = condition.text.toString().trim()
        val productLocation = location.text.toString().trim()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (productName.isEmpty() || productPrice.isEmpty() || productDescription.isEmpty() ||
            productCondition.isEmpty() || productLocation.isEmpty() || selectedCategory.isEmpty() || imageUri == null
        ) {
            Toast.makeText(this, "Please fill all fields, select a category, and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // If user is not logged in, userId is null -> can't proceed
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = UUID.randomUUID().toString()
        val imageRef = storage.child("items/$fileName.jpg")

        imageUri?.let { uri ->
            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Create a map with item details
                    val itemData = hashMapOf(
                        "name" to productName,
                        "price" to productPrice,
                        "description" to productDescription,
                        "condition" to productCondition,
                        "location" to productLocation,
                        "imageUrl" to downloadUrl.toString(),
                        "userId" to userId,  // <-- Important for ownership
                        "category" to selectedCategory
                    )

                    firestore.collection("items")
                        .add(itemData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item uploaded successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to upload item", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}
