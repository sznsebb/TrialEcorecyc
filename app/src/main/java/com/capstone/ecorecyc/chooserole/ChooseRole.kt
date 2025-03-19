package com.capstone.ecorecyc.chooserole

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.capstone.ecorecyc.R

class ChooseRole : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_role)

        // Reference to the buttons (assume they exist in activity_choose_role.xml)
        val userBtn: Button = findViewById(R.id.radio_user)
        val junkOwnerBtn: Button = findViewById(R.id.radio_junkshop_owner)

        // Optionally apply a ripple effect
        applyRippleEffect(userBtn)
        applyRippleEffect(junkOwnerBtn)

        userBtn.setOnClickListener {
            // Return "USER" as the chosen role
            val data = Intent().apply { putExtra("USER_TYPE", "USER") }
            setResult(RESULT_OK, data)
            finish()
        }

        junkOwnerBtn.setOnClickListener {
            // Return "JUNKSHOP_OWNER" as the chosen role
            val data = Intent().apply { putExtra("USER_TYPE", "JUNKSHOP_OWNER") }
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun applyRippleEffect(button: Button) {
        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        button.foreground = getDrawable(outValue.resourceId)
    }
}
