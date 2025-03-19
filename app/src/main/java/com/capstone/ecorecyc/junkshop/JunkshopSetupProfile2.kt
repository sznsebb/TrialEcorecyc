package com.capstone.ecorecyc.junkshop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.ecorecyc.R

class JunkshopSetupProfile2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_junkshop_setup_profile2)

        val finishButton = findViewById<Button>(R.id.junkshop_submit_btn)
        finishButton.setOnClickListener {
            val intent = Intent(this, JunkShopDashboard::class.java) // Use the correct class name
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear previous activities
            startActivity(intent)
        }
    }
}
