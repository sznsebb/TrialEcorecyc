package com.capstone.ecorecyc.chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.ecorecyc.Chat
import com.capstone.ecorecyc.R

class ChatListAdapter(
    private val chatList: List<ChatConversation>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_conversation, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val conversation = chatList[position]

        // Show the partner's display name and last message
        holder.partnerTextView.text = conversation.partnerDisplayName
        holder.lastMessageTextView.text = conversation.lastMessage

        // Load partner's profile image
        Glide.with(holder.itemView.context)
            .load(conversation.partnerPhotoUrl)
            .into(holder.partnerImageView)

        // Open the Chat activity when clicked
        holder.itemView.setOnClickListener { view ->
            val context = view.context
            // The partnerId is the participant not equal to currentUserId
            val partnerId = conversation.participants.firstOrNull { it != currentUserId } ?: ""
            val intent = Intent(context, Chat::class.java).apply {
                putExtra("receiverId", partnerId)
                putExtra("chatId", conversation.conversationId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = chatList.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val partnerImageView: ImageView = itemView.findViewById(R.id.partnerImageView)
        val partnerTextView: TextView = itemView.findViewById(R.id.partnerTextView)
        val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)
    }
}
