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
import com.squadsapp.model.Trip

class AnnouncementController : AppCompatActivity() {

    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    private lateinit var txtAnnouncement: EditText
    private val loadingDialog = LoadDialog()
    private val singleToast = SingleToast
    private val keyboard = Keyboard

    private var tripID = ""
    private var announcement = ""

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
        setContentView(R.layout.activity_announcement)
        initializeLayout()
    }

    private fun initializeLayout() {
        tripID = intent.getStringExtra("tripId")
        announcement = intent.getStringExtra("announcement")
        txtAnnouncement = findViewById(R.id.txtAnnouncement)
        txtAnnouncement.setText(announcement)
    }

    fun handleDismiss(view: View) {
        finish()
    }

    fun handleKeyboard(view: View) {
        keyboard.hideKeyboard(this)
    }

    fun handleSave(view: View) {
        if (txtAnnouncement.text.toString()== announcement) {
            finish()
        } else {
            loadingDialog.show(this)
            tripRef.child(tripID).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val tripInfo = p0.getValue(Trip::class.java)!!
                    tripInfo.announcement = txtAnnouncement.text.toString().trimEnd()
                    tripRef.child(tripID).setValue(tripInfo)
                    singleToast.show(applicationContext, "Your announcement has been updated.", Toast.LENGTH_LONG)
                    loadingDialog.dismiss()
                    loadDataBack()
                    finish()
                }

            })
        }

    }

    fun loadDataBack() {
        val result = Intent()
        result.putExtra("announcement", txtAnnouncement.text.toString().trimEnd())
        setResult(Activity.RESULT_OK, result)
    }

}
