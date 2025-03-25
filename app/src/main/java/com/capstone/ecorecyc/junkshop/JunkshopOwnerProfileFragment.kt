package com.capstone.ecorecyc.junkshop

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.ecorecyc.R

class JunkshopOwnerProfileFragment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_junkshop_owner_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logoutBtn = findViewById<LinearLayout>(R.id.logout_btn)
        logoutBtn.setOnClickListener {
            logoutAndExit()
        }
    }

    private fun logoutAndExit() {
        // Clear session data if needed
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Navigate back to the dashboard
        val intent = Intent(this, JunkShopDashboardFragment::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}