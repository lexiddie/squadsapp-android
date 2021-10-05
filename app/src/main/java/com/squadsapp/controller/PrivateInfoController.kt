package com.squadsapp.controller

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
import com.squadsapp.helper.*
import com.squadsapp.model.User

class PrivateInfoController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")

    private lateinit var txtEmail: EditText
    private lateinit var txtPhone: EditText
    lateinit var txtGender: EditText

    private var userId = ""
    private var email = ""
    private var phone = ""
    private var gender = ""

    private val loadingDialog = LoadDialog()
    private val singleToast = SingleToast
    private val dialogAlert = DialogAlert
    private val fieldCheck = FieldCheck
    private val keyboard = Keyboard

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
        setContentView(R.layout.activity_private_info)
        initializeLayout()
        txtGender.setOnClickListener {
            dialogAlert.showGender(this, txtGender.text.toString())
        }
    }

    private fun initializeLayout() {
        userId = intent.getStringExtra("userId")
        email = intent.getStringExtra("email")
        phone = intent.getStringExtra("phone")
        gender = intent.getStringExtra("gender")
        txtEmail = findViewById(R.id.txtEmail)
        txtPhone = findViewById(R.id.txtPhoneNumber)
        txtGender = findViewById(R.id.txtGender)
        txtEmail.setText(email)
        txtPhone.setText(phone)
        txtGender.setText(gender)
    }

    fun handleSave(view: View) {
        if (txtEmail.text.isNullOrEmpty() || txtPhone.text.isNullOrEmpty()) {
            singleToast.show(this, "Email or Phone number cannot be empty.", Toast.LENGTH_LONG)
        } else if (!fieldCheck.checkPhoneNumber(txtPhone.text.toString())) {
            singleToast.show(this, "Invalid phone number", Toast.LENGTH_LONG)
        } else if (email == txtEmail.text.toString().trimEnd() && phone == txtPhone.text.toString().trim().toLowerCase() && gender == txtGender.text.toString()) {
            finish()
        } else {
            loadingDialog.show(this)
            userRef.child(userId).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val userInfo = p0.getValue(User::class.java)!!
                    userInfo.email = txtEmail.text.toString().trimEnd()
                    userInfo.phoneNumber = txtPhone.text.toString().trim().toLowerCase()
                    userInfo.gender = txtGender.text.toString()
                    userRef.child(userId).setValue(userInfo)
                    singleToast.show(this@PrivateInfoController, "Your information has been updated.", Toast.LENGTH_LONG)
                    loadingDialog.dismiss()
                    finish()
                }
            })
        }
    }

    fun handleDismiss(view: View) {
        finish()
    }

    fun handleKeyboard(view: View) {
        keyboard.hideKeyboard(this)
    }
}
