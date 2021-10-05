package com.squadsapp.controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.squadsapp.R
import com.squadsapp.helper.Keyboard
import com.squadsapp.helper.SingleToast

class SetupPasswordController : AppCompatActivity() {

    private val singleToast = SingleToast
    private val keyboard = Keyboard
    private lateinit var txtPassword: EditText
    private lateinit var txtConfirmPassword: EditText
    private var name = ""
    private var phoneNumber = ""
    private var username = ""

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_to_right_open, R.anim.left_to_right_close)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_password)
        initializeLayout()
    }

    private fun initializeLayout() {
        txtPassword = findViewById(R.id.txtPassword)
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword)
        name = intent.getStringExtra("Name")
        phoneNumber = intent.getStringExtra("PhoneNumber")
        username = intent.getStringExtra("Username")
    }

    fun handleNext(view: View) {
        if (txtPassword.text.isNullOrEmpty() || txtConfirmPassword.text.isNullOrEmpty()) {
            singleToast.show(this, "Fields must not empty", Toast.LENGTH_LONG)
        } else if (txtPassword.text.toString() != txtConfirmPassword.text.toString()) {
            singleToast.show(this, "Passwords don't match", Toast.LENGTH_LONG)
        } else {
            val intent = Intent(this, SetupProfileController::class.java)
            intent.putExtra("Name", name)
            intent.putExtra("PhoneNumber", phoneNumber)
            intent.putExtra("Username", username)
            intent.putExtra("Password", txtPassword.text.toString())
            startActivity(intent)
            overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }
    }

    fun handleKeyboard(view: View) {
        keyboard.hideKeyboard(this)
    }
}
