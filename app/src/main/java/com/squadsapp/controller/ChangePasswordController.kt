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

class ChangePasswordController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")

    private lateinit var txtCurrentPassword: EditText
    private lateinit var txtNewPassword: EditText
    private lateinit var txtConfirmPassword: EditText

    private val loadingDialog = LoadDialog()
    private val singleToast = SingleToast
    private val keyboard = Keyboard

    private var userID = ""

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
        setContentView(R.layout.activity_change_password)
        initializeLayout()
    }

    private fun initializeLayout() {
        userID = intent.getStringExtra("userId")
        txtCurrentPassword = findViewById(R.id.txtCurrentPassword)
        txtNewPassword = findViewById(R.id.txtNewPassword)
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword)
    }

    fun handleDismiss(view: View) {
        finish()
    }

    fun handleKeyboard(view: View) {
        keyboard.hideKeyboard(this)
    }

    fun handleSave(view: View) {
        if (txtCurrentPassword.text.isNullOrEmpty() || txtNewPassword.text.isNullOrEmpty() || txtConfirmPassword.text.isNullOrEmpty()) {
            singleToast.show(this, "The fields must not be empty", Toast.LENGTH_LONG)
        } else if (txtNewPassword.text.toString() != txtConfirmPassword.text.toString()) {
            singleToast.show(this, "New passwords don't match", Toast.LENGTH_LONG)
        } else {
            loadingDialog.show(this)
            userRef.child(userID).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val userInfo = p0.getValue(User::class.java)!!
                    if (userInfo.password != txtCurrentPassword.text.toString()) {
                        singleToast.show(applicationContext, "Your current password is incorrect", Toast.LENGTH_LONG)
                        loadingDialog.dismiss()
                    } else {
                        userInfo.password = txtNewPassword.text.toString()
                        userRef.child(userID).setValue(userInfo)
                        singleToast.show(applicationContext, "Your password has been updated.", Toast.LENGTH_LONG)
                        loadingDialog.dismiss()
                        finish()
                    }

                }

            })
        }
    }
}
