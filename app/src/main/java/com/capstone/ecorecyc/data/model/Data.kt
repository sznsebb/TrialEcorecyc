package com.capstone.ecorecyc.data.model

class Data {
    data class Item(
        var id: String = "",         // Firestore document ID
        var userId: String = "",     // UID of the item's owner
        val name: String = "",
        val price: String = "",
        val imageUrl: String = "",
        val description: String = "",
        val condition: String = "",
        val location: String = "",
        val category: String = "",
        var username: String = "",   // Owner's username
        var displayName: String = "" // Owner's display name
    )
}
