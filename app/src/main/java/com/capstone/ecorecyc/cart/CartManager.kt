package com.capstone.ecorecyc.cart

import com.capstone.ecorecyc.data.model.Data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object CartManager {
    private val cartItems = mutableListOf<Data.Item>()
    private val db = FirebaseFirestore.getInstance()

    fun addItem(item: Data.Item) {
        cartItems.add(item)
        saveToFirestore(item)
    }

    fun getCartItems(): List<Data.Item> = cartItems

    private fun saveToFirestore(item: Data.Item) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            db.collection("users").document(it).collection("cart").add(item)
        }
    }

    fun deleteItemFromFirestore(item: Data.Item, onItemDeleted: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("cart")
                .whereEqualTo("name", item.name).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("users").document(userId).collection("cart")
                            .document(document.id).delete()
                            .addOnSuccessListener {
                                cartItems.remove(item)
                                onItemDeleted()
                            }
                    }
                }
        }
    }

    fun loadCartItems(onItemsLoaded: (List<Data.Item>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("cart").get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val items = documents.mapNotNull { it.toObject(Data.Item::class.java) }
                        cartItems.clear()
                        cartItems.addAll(items)
                        onItemsLoaded(items)
                    } else {
                        onItemsLoaded(emptyList())
                    }
                }
                .addOnFailureListener {
                    onItemsLoaded(emptyList())
                }
        } else {
            onItemsLoaded(emptyList())
        }
    }
}