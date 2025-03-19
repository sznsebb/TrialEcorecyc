// ChatConversation.kt
package com.capstone.ecorecyc.chat

data class ChatConversation(
    var conversationId: String = "",
    var participants: List<String> = listOf(),
    var lastMessage: String = "",
    var timestamp: Long = 0,

    // These two fields will be populated after we fetch the partner’s info
    var partnerDisplayName: String = "",
    var partnerPhotoUrl: String = ""
)
