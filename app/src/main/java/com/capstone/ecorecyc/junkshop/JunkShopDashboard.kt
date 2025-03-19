package com.capstone.ecorecyc.junkshop

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.ecorecyc.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class JunkShopDashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_junk_shop_dashboard)

        // Find the FloatingActionButton
        val fab: FloatingActionButton = findViewById(R.id.addButton)

        // Set click listener to open AddRecyclablePrice activity
        fab.setOnClickListener {
            val intent = Intent(this, AddRecyclablePrice::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // Smooth fade-in transition
        }

        // Find the Profile Layout
        val profileLayout: LinearLayout = findViewById(R.id.profileLayout)

        // Set click listener to open JunkshopOwnerProfile activity
        profileLayout.setOnClickListener {
            val intent = Intent(this, JunkshopOwnerProfile::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // Smooth fade-in transition
        }
    }
}