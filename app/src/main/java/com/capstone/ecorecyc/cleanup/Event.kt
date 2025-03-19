package com.capstone.ecorecyc.cleanup

data class Event(
    var id: String = "", // Changed from val to var to allow reassignment
    val cleanupName: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val creatorId: String = ""
)
