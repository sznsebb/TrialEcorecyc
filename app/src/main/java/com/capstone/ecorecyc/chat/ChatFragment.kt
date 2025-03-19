package com.capstone.ecorecyc.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.ecorecyc.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatListAdapter: ChatListAdapter
    private val chatList = mutableListOf<ChatConversation>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.chatListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatListAdapter = ChatListAdapter(chatList, auth.currentUser?.uid ?: "")
        recyclerView.adapter = chatListAdapter

        loadConversations()
        return view
    }

    private fun loadConversations() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("ChatFragment", "Error loading chats: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    chatList.clear()

                    // We'll fetch user profiles after loading each conversation
                    for (doc in snapshots.documents) {
                        val conversation = doc.toObject(ChatConversation::class.java)
                        conversation?.conversationId = doc.id

                        if (conversation != null) {
                            chatList.add(conversation)
                        }
                    }

                    // For each conversation, fetch the partner's user profile
                    fetchPartnerProfiles(currentUserId)
                }
            }
    }

    /**
     * For each conversation, find the partner's UID and fetch their display name/photo from Firestore.
     */
    private fun fetchPartnerProfiles(currentUserId: String) {
        val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

        for (conversation in chatList) {
            // The partner is the participant who isn't the current user
            val partnerId = conversation.participants.firstOrNull { it != currentUserId }
            if (!partnerId.isNullOrEmpty()) {
                val task = db.collection("users").document(partnerId).get()
                    .addOnSuccessListener { userDoc ->
                        if (userDoc.exists()) {
                            conversation.partnerDisplayName = userDoc.getString("displayName") ?: "Unknown"
                            // Or if you store "username" in the doc, fetch that
                            // conversation.partnerDisplayName = userDoc.getString("username") ?: "Unknown"

                            conversation.partnerPhotoUrl = userDoc.getString("photoUrl") ?: ""
                        }
                    }
                tasks.add(task)
            }
        }

        // When all partner profiles have been fetched, update the adapter
        com.google.android.gms.tasks.Tasks.whenAllComplete(tasks).addOnSuccessListener {
            chatListAdapter.notifyDataSetChanged()
        }
    }
}
