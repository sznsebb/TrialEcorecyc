package com.capstone.ecorecyc.cleanup

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.ecorecyc.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventAdapter(private val eventList: MutableList<Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.cleanupName.text = event.cleanupName
        holder.location.text = event.location
        holder.description.text = event.description
        holder.date.text = event.date
        holder.time.text = event.time

        Glide.with(holder.itemView.context)
            .load(event.imageUrl)
            .into(holder.eventImage)

        // Set visibility of delete button based on creator
        holder.deleteButton.visibility =
            if (event.creatorId == currentUser?.uid) View.VISIBLE else View.GONE

        holder.deleteButton.setOnClickListener {
            firestore.collection("cleanup_events").document(event.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Event deleted", Toast.LENGTH_SHORT).show()
                    eventList.removeAt(position)
                    notifyItemRemoved(position)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(holder.itemView.context, "Failed to delete event", Toast.LENGTH_SHORT).show()
                }
        }

        holder.joinButton.setOnClickListener {
            val context: Context = holder.itemView.context
            val intent = Intent(context, VolunteerCleanupEvent::class.java).apply {
                putExtra("IMAGE_URL", event.imageUrl)
                putExtra("EVENT_ID", event.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = eventList.size

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventImage: ImageView = itemView.findViewById(R.id.event_image)
        val cleanupName: TextView = itemView.findViewById(R.id.cleanup_name)
        val location: TextView = itemView.findViewById(R.id.location)
        val description: TextView = itemView.findViewById(R.id.description)
        val date: TextView = itemView.findViewById(R.id.date)
        val time: TextView = itemView.findViewById(R.id.time)
        val joinButton: Button = itemView.findViewById(R.id.joinEvent)
        val deleteButton: Button = itemView.findViewById(R.id.deleteEvent)
    }
}
