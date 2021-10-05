package com.squadsapp.controller

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squadsapp.R
import com.squadsapp.helper.SingleToast
import com.squadsapp.model.Member
import com.squadsapp.model.Trip
import com.squadsapp.model.User

class InspectProfileController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    var memberId = ""
    var tripId = ""

    val singleToast = SingleToast

    private lateinit var handleHide: RelativeLayout
    private lateinit var imgProfile: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtUsername: TextView
    private lateinit var txtPhoneNumber: TextView
    private lateinit var txtRate: TextView
    private lateinit var txtTotalTrips: TextView

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_to_right_open, R.anim.left_to_right_close)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun handleDismiss(view: View) {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspect_profile)
        initializeLayout()
        getUserInfo()
    }

    private fun initializeLayout() {
        memberId = intent.getStringExtra("memberId")
        tripId = intent.getStringExtra("tripId")
        handleHide = findViewById(R.id.handleHide)
        imgProfile = findViewById(R.id.imgProfile)
        txtName = findViewById(R.id.txtName)
        txtUsername = findViewById(R.id.txtUsername)
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber)
        txtRate = findViewById(R.id.txtRate)
        txtTotalTrips = findViewById(R.id.txtTotalTrip)
    }

    private fun dynamicLayout() {
        handleHide.visibility = View.GONE
    }

    private fun getUserInfo() {
        userRef.child(memberId).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)!!
                if (user.profileUrl != "Not Defined") {
                    Glide.with(this@InspectProfileController)
                        .load(user.profileUrl)
                        .apply(RequestOptions().circleCrop())
                        .apply(RequestOptions().override(200, 200))
                        .into(imgProfile)
                }
                txtName.text = user.name
                txtPhoneNumber.text = user.phoneNumber
                txtRate.text = user.rate.toString()
                txtTotalTrips.text = user.totalTrip.toString()
                txtUsername.text = user.username
            }
        })
    }

    fun handleConfirm(view: View) {
        tripRef.child(tripId).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                val trip = p0.getValue(Trip::class.java)!!
                if (trip.members == 0) {
                    singleToast.show(this@InspectProfileController, "You cannot add more member.", Toast.LENGTH_LONG)
                    return
                }
                val memberList = trip.memberList as MutableList
                memberList.add(Member(memberId))
                trip.memberList = memberList
                trip.members -= 1
                tripRef.child(tripId).child("memberList").setValue(trip.memberList)
                tripRef.child(tripId).child("members").setValue(trip.members)
                tripRef.child(tripId).child("requestList").addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            val tempList = mutableListOf<Member>()
                            for (i in p0.children.iterator()) {
                                val member = i.getValue(Member::class.java)!!
                                if (member.id != memberId) {
                                    tempList.add(member)
                                }
                            }
                            tripRef.child(tripId).child("requestList").setValue(tempList)
                            singleToast.show(this@InspectProfileController, "This user has been added to trip", Toast.LENGTH_LONG)
                            dynamicLayout()
                        }
                    }

                })
            }
        })
    }

    fun handleDelete(view: View) {
        tripRef.child(tripId).child("requestList").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val tempList = mutableListOf<Member>()
                    for (i in p0.children.iterator()) {
                        val member = i.getValue(Member::class.java)!!
                        if (member.id != memberId) {
                            tempList.add(member)
                        }
                    }
                    tripRef.child(tripId).child("requestList").setValue(tempList)
                    singleToast.show(this@InspectProfileController, "This user's request has been deleted", Toast.LENGTH_LONG)
                    dynamicLayout()
                }
            }

        })
    }
}
