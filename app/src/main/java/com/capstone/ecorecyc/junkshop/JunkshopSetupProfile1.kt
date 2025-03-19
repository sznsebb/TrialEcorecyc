package com.capstone.ecorecyc.junkshop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.ecorecyc.R

class JunkshopSetupProfile1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_junkshop_setup_profile1)

        val nextButton = findViewById<Button>(R.id.next_junkshop_setup_btn)
        nextButton.setOnClickListener {
            val intent = Intent(this, JunkshopSetupProfile2::class.java)
            startActivity(intent)
        }
    }
}
