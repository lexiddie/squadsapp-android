package com.squadsapp.controller

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squadsapp.R
import com.squadsapp.adapter.RequestAdapter
import com.squadsapp.helper.DialogAlert
import com.squadsapp.helper.DisplayDate
import com.squadsapp.helper.LoadDialog
import com.squadsapp.helper.SingleToast
import com.squadsapp.model.MemberInfo
import com.squadsapp.model.User
import kotlinx.android.synthetic.main.activity_member_request.*
import org.json.JSONArray

class MemberRequestController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    val singleToast = SingleToast
    private lateinit var tripRequestView: RecyclerView
    private lateinit var adapter: RequestAdapter
    private var memberList = mutableListOf<MemberInfo>()

    var userId = ""
    var tripId = ""
    var tripLocation = ""
    var tripDestination = ""

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_to_right_open, R.anim.left_to_right_close)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
        getMemberList()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_request)
        userId = intent.getStringExtra("userId")
        tripId = intent.getStringExtra("tripId")
        tripLocation = intent.getStringExtra("tripLocation")
        tripDestination = intent.getStringExtra("tripDestination")
        tripRequestView = findViewById(R.id.memberRequestView)
        tripRequestView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        txtRequestHeader.text = "$tripLocation - $tripDestination"
    }

    fun handleDismiss(view: View) {
        finish()
    }

    fun getMemberList() {
        tripRef.child(tripId).child("requestList").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                memberList.clear()
                if(p0.exists()) {
                    val jsonArray = JSONArray(p0.value.toString())
                    for (j in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(j)
                        val memberId = jsonObject.get("id").toString()
                        userRef.child(memberId).addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                            }
                            override fun onDataChange(p0: DataSnapshot) {
                                val member = p0.getValue(User::class.java)!!
                                val memberInfo = MemberInfo(member.id, member.profileUrl, member.name, member.phoneNumber, member.rate, member.totalTrip)
                                memberList.add(memberInfo)
                                refreshAdapter()
                            }
                        })
                    }
                } else {
                    refreshAdapter()
                    finish()
                }

            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    private fun refreshAdapter() {
        adapter = RequestAdapter(this, tripId, R.layout.member_request_cell, memberList)
        tripRequestView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}
