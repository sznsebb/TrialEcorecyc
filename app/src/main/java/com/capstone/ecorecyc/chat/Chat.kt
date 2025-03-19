package com.capstone.ecorecyc

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.ecorecyc.chat.ChatAdapter
import com.capstone.ecorecyc.chat.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class Chat : AppCompatActivity() {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messagesList = mutableListOf<Message>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var chatId: String
    private lateinit var receiverId: String
    private lateinit var senderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize views
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        recyclerView = findViewById(R.id.recyclerView)

        // Get IDs from intent
        senderId = auth.currentUser?.uid ?: ""
        receiverId = intent.getStringExtra("receiverId") ?: ""
        chatId = intent.getStringExtra("chatId") ?: ""

        if (senderId.isEmpty() || receiverId.isEmpty() || chatId.isEmpty()) {
            Toast.makeText(this, "Invalid chat details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up RecyclerView
        chatAdapter = ChatAdapter(messagesList, senderId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        // Create or merge the chat doc with participants
        val chatData = mapOf(
            "participants" to listOf(senderId, receiverId),
            "lastMessage" to "",
            "timestamp" to System.currentTimeMillis()
        )
        val chatRef = db.collection("chats").document(chatId)
        chatRef.set(chatData, SetOptions.merge())
            .addOnSuccessListener {
                // Start listening to messages
                setupMessagesListener()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating chat: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Send button
        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupMessagesListener() {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading messages: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    messagesList.clear()
                    for (doc in snapshots.documents) {
                        val msg = doc.toObject(Message::class.java)
                        msg?.let { messagesList.add(it) }
                    }
                    chatAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messagesList.size - 1)
                }
            }
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val message = Message(
            senderId = senderId,
            receiverId = receiverId,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )

        // Add message to subcollection
        db.collection("chats").document(chatId).collection("messages")
            .add(message)
            .addOnSuccessListener {
                messageInput.text.clear()
                // Update chat doc with lastMessage/timestamp
                db.collection("chats").document(chatId).update(
                    mapOf(
                        "lastMessage" to messageText,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        Log.d("ChatActivity", "Sender ID: $senderId, Receiver ID: $receiverId, Chat ID: $chatId")

    }
}
