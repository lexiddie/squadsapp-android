package com.squadsapp.controller

import android.app.Activity
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
import com.squadsapp.helper.Keyboard
import com.squadsapp.helper.LoadDialog
import com.squadsapp.helper.SingleToast
import com.squadsapp.model.User

class ChangeCountryController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")

    private lateinit var txtCountry: EditText
    private val loadingDialog = LoadDialog()
    private val singleToast = SingleToast
    private val keyboard = Keyboard

    private val requestCode = 0
    private var userID = ""
    private var country = ""

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
        setContentView(R.layout.activity_change_country)
        initializeLayout()
        txtCountry.setOnClickListener {
            val intent = Intent(this, CountryController::class.java)
            startActivityForResult(intent, requestCode)
            overridePendingTransition(com.squadsapp.R.anim.right_to_left_open, com.squadsapp.R.anim.right_to_left_close)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (this.requestCode == requestCode && Activity.RESULT_OK == resultCode) {
            val tempCountry = data!!.getStringExtra("country")
            txtCountry.setText(tempCountry)
        }
    }

    private fun initializeLayout() {
        userID = intent.getStringExtra("userId")
        country = intent.getStringExtra("country")
        txtCountry = findViewById(R.id.txtCountry)
        txtCountry.setText(country)
    }

    fun handleDismiss(view: View) {
        finish()
    }

    fun handleKeyboard(view: View) {
        keyboard.hideKeyboard(this)
    }

    fun handleSave(view: View) {
        if (country == txtCountry.text.toString()) {
            finish()
        } else {
            loadingDialog.show(this)
            userRef.child(userID).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val userInfo = p0.getValue(User::class.java)!!
                    userInfo.country = txtCountry.text.toString()
                    userRef.child(userID).setValue(userInfo)
                    singleToast.show(applicationContext, "Your country content has been updated.", Toast.LENGTH_LONG)
                    loadingDialog.dismiss()
                    finish()
                }

            })
        }
    }
}
