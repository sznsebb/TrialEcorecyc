package com.capstone.ecorecyc.marketplace

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.capstone.ecorecyc.R
import com.capstone.ecorecyc.UserInteractionTracker
import com.capstone.ecorecyc.cart.ItemDetails
import com.capstone.ecorecyc.data.model.Data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import android.util.Log

class MarketplaceAdapter(private var itemList: MutableList<Data.Item>) :
    RecyclerView.Adapter<MarketplaceAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemPrice: TextView = view.findViewById(R.id.itemPrice)
        val itemUsername: TextView = view.findViewById(R.id.byUser)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_marketplace, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        // Set item details
        holder.itemName.text = item.name
        holder.itemPrice.text = item.price
        holder.itemUsername.text = "by ${item.displayName}"
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.itemImage)

        // Show delete button only if the current user is the owner.
        val currentUserId = auth.currentUser?.uid
        if (item.userId == currentUserId) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                deleteItem(holder.itemView, item, position)
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }

        // When a user clicks on an item, record the interaction and navigate to ItemDetails.
        holder.itemView.setOnClickListener {
            // Track the interaction by updating the category count.
            UserInteractionTracker.updateUserPreference(item.category)

            // Start the ItemDetails activity.
            val context = holder.itemView.context
            val intent = Intent(context, ItemDetails::class.java).apply {
                putExtra("name", item.name)
                putExtra("price", item.price)
                putExtra("imageUrl", item.imageUrl)
                putExtra("description", item.description)
                putExtra("condition", item.condition)
                putExtra("location", item.location)
                putExtra("category", item.category)
                putExtra("username", item.username)
                putExtra("displayName", item.displayName)
                putExtra("sellerId", item.userId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun updateItems(newItems: List<Data.Item>) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun deleteItem(view: View, item: Data.Item, position: Int) {
        db.collection("items")
            .document(item.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(view.context, "Item deleted successfully!", Toast.LENGTH_SHORT).show()
                itemList.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener { e ->
                Log.e("MarketplaceAdapter", "Error deleting document", e)
                Toast.makeText(view.context, "Failed to delete item.", Toast.LENGTH_SHORT).show()
            }
    }
}
