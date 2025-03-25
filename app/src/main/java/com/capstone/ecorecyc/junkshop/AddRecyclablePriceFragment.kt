package com.capstone.ecorecyc.junkshop

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.ecorecyc.R

class AddRecyclablePriceFragment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_recyclable_price)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle Back Button Click
        val backButton: ImageButton = findViewById(R.id.add_reyclable_prices_back_button)
        backButton.setOnClickListener {
            finish() // Close activity
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // Smooth fade-out transition
        }
    }
}
