package com.squadsapp.controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squadsapp.R
import com.squadsapp.helper.FieldCheck
import com.squadsapp.helper.Keyboard
import com.squadsapp.helper.SingleToast
import com.squadsapp.model.User

class SignUpController : AppCompatActivity() {

    private val ref = FirebaseDatabase.getInstance().getReference("User")

    private val singleToast = SingleToast
    private val fieldCheck = FieldCheck
    private val keyboard = Keyboard
    private lateinit var txtName: EditText
    private lateinit var txtPhoneNumber: EditText
    private lateinit var txtUsername: EditText

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
        setContentView(R.layout.activity_signup)
        initializeLayout()
    }

    private fun initializeLayout() {
        txtName = findViewById(R.id.txtName)
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber)
        txtUsername = findViewById(R.id.txtUsername)
    }

    fun handleLogin(view: View) {
        onBackPressed()
    }

    fun handleNext(view: View) {
        if (txtName.text.isNullOrEmpty() || txtPhoneNumber.text.isNullOrEmpty() || txtUsername.text.isNullOrEmpty()) {
            singleToast.show(this, "The fields must not empty", Toast.LENGTH_LONG)
        } else {
            if (!fieldCheck.checkName(txtName.text.toString())) {
                singleToast.show(this, "The name must be at least 3 letters and in alphabet with/or with number", Toast.LENGTH_LONG)
            } else if (!fieldCheck.checkPhoneNumber(txtPhoneNumber.text.toString())) {
                singleToast.show(this, "Invalid phone number", Toast.LENGTH_LONG)
            } else if (!fieldCheck.checkUsername(txtUsername.text.toString())) {
                singleToast.show(this, "The username must be alphabet with/or with number", Toast.LENGTH_LONG)
            } else {
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        for (i in p0.children) {
                            val checkUser = i.getValue(User::class.java)
                            if (txtUsername.text.toString().toLowerCase() == checkUser!!.username) {
                                singleToast.show(applicationContext, "This Username already exists!", Toast.LENGTH_LONG)
                                return
                            }
                        }
                        val intent =  Intent(applicationContext, SetupPasswordController::class.java)
                        intent.putExtra("Name", txtName.text.toString())
                        intent.putExtra("PhoneNumber", txtPhoneNumber.text.toString())
                        intent.putExtra("Username", txtUsername.text.toString().toLowerCase())
                        startActivity(intent)
                        overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
                    }

                })
            }
        }

    }

    fun handleKeyboard(view: View) {
        keyboard.hideKeyboard(this)
    }
}
