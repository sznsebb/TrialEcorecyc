package com.capstone.ecorecyc.cart

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.ecorecyc.R
import com.capstone.ecorecyc.data.model.Data
import com.capstone.ecorecyc.marketplace.PlaceOrder

class CartAdapter(
    private val cartItems: List<Data.Item>,
    private var isSelectionMode: Boolean,
    private val selectedItems: MutableSet<Data.Item>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        notifyDataSetChanged()
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        private val itemName: TextView = itemView.findViewById(R.id.itemName)
        private val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        private val checkBox: CheckBox = itemView.findViewById(R.id.cartItemCheckbox)

        fun bind(item: Data.Item) {
            // Set item details
            itemName.text = item.name
            itemPrice.text = "₱${item.price}"
            Glide.with(itemView.context).load(item.imageUrl).into(itemImage)

            // Handle selection mode
            checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            checkBox.isChecked = selectedItems.contains(item)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(item)
                } else {
                    selectedItems.remove(item)
                }
                onSelectionChanged()
            }
        }
    }
}
