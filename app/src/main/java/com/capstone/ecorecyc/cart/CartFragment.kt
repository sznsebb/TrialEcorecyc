package com.capstone.ecorecyc.cart

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.ecorecyc.R
import com.capstone.ecorecyc.data.model.Data
import com.capstone.ecorecyc.marketplace.PlaceOrder

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CartAdapter
    private lateinit var editButton: Button
    private lateinit var proceedToCheckoutButton: Button
    private lateinit var shoppingCartText: TextView
    private val cartItems = mutableListOf<Data.Item>()
    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Data.Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        editButton = view.findViewById(R.id.btn_edit_cart)
        proceedToCheckoutButton = view.findViewById(R.id.btn_proceed_to_checkout)
        shoppingCartText = view.findViewById(R.id.shoppingCartText)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CartAdapter(cartItems, isSelectionMode, selectedItems) { updateCheckoutButton() }
        recyclerView.adapter = adapter

        // Load cart items
        CartManager.loadCartItems { items ->
            cartItems.clear()
            cartItems.addAll(items)
            adapter.notifyDataSetChanged()
            updateCartCount()
        }

        setupSwipeToDelete()

        // Handle Edit button click
        editButton.setOnClickListener {
            isSelectionMode = !isSelectionMode
            adapter.updateSelectionMode(isSelectionMode)
            editButton.text = if (isSelectionMode) "Done" else "Edit"
        }

        // Handle Proceed to Checkout click
        proceedToCheckoutButton.setOnClickListener {
            if (selectedItems.isNotEmpty()) {
                val intent = Intent(requireContext(), PlaceOrder::class.java)
                startActivity(intent)
            }
        }

        return view
    }

    // Update the cart item count
    private fun updateCartCount() {
        shoppingCartText.text = "Shopping Cart (${cartItems.size})"
    }

    // Update the checkout button state
    private fun updateCheckoutButton() {
        proceedToCheckoutButton.isEnabled = selectedItems.isNotEmpty()
        val buttonColor = if (selectedItems.isNotEmpty()) R.color.primary_color else R.color.grey
        proceedToCheckoutButton.setBackgroundColor(ContextCompat.getColor(requireContext(), buttonColor))
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val deleteBackground = ColorDrawable(Color.RED)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val itemToDelete = cartItems[position]

                CartManager.deleteItemFromFirestore(itemToDelete) {
                    cartItems.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    selectedItems.remove(itemToDelete)
                    updateCheckoutButton()
                    updateCartCount() // Update count when item is removed
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val alpha = 1.0f - (Math.abs(dX) / recyclerView.width.toFloat()) // Smooth fade effect
                itemView.alpha = alpha

                if (dX < 0) {
                    deleteBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    deleteBackground.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
