package com.squadsapp.controller

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squadsapp.R
import com.squadsapp.adapter.TripRequestAdapter
import com.squadsapp.model.*
import org.json.JSONArray

class TripRequestController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    lateinit var tabBarController: AppCompatActivity
    private lateinit var tripRequestView: RecyclerView
    private lateinit var adapter: TripRequestAdapter
    private var tripRequestList = mutableListOf<TripRequest>()
    private var sortedList = mutableListOf<TripRequest>()
    var userId = ""

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
        getTripRequestList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_request)
        userId = intent.getStringExtra("userId")
        tripRequestView = findViewById(R.id.tripRequestView)
        tripRequestView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
    }

    fun handleDismiss(view: View) {
        finish()
    }

    private fun getTripRequestList() {
        tripRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                tripRequestList.clear()
                if(p0.exists()){
                    for (i in p0.children){
                        val trip = i.getValue(Trip::class.java)!!
                        val owner = trip.memberList[0]
                        val requestValue = i.child("requestList").value
                        if (requestValue != null && owner.id == userId) {
                            val jsonArray = JSONArray(requestValue.toString())
                            val memberList = mutableListOf<Member>()
                            for (j in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(j)
                                val memberId = jsonObject.get("id").toString()
                                memberList.add(Member(memberId))
                            }
                            val tripRequest = TripRequest(trip.id, trip.dateCreated, trip.timeCreated, trip.location, trip.destination, trip.date, trip.time, trip.members, memberList)
                            tripRequestList.add(tripRequest)
                            refreshAdapter()
                        }
                    }
                    refreshAdapter()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    private fun refreshAdapter() {
        sortedList = sortLatest()
        adapter = TripRequestAdapter(this, R.layout.trip_request_cell, sortedList)
        tripRequestView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun sortLatest(): MutableList<TripRequest> {
        val tempList = tripRequestList
        for (i in 0 until tempList.size - 1) {
            for (j in 0 until tempList.size - i - 1) {
                val day1 = (tempList[j + 1].dateCreated.split("/")[0])
                val month1 = (tempList[j + 1].dateCreated.split("/")[1])
                val year1 = (tempList[j + 1].dateCreated.split("/")[2])
                val hour1 = (tempList[j + 1].timeCreated.split(":")[0])
                val minute1 = (tempList[j + 1].timeCreated.split(":")[1])
                val day2 = (tempList[j].dateCreated.split("/")[0])
                val month2 = (tempList[j].dateCreated.split("/")[1])
                val year2 = (tempList[j].dateCreated.split("/")[2])
                val hour2 = (tempList[j].timeCreated.split(":")[0])
                val minute2 = (tempList[j].timeCreated.split(":")[1])
                if (year2 < year1 || (year2 == year1 && month2 < month1) || (year2 == year1 && month2 == month1 && day2 < day1) || (year2 == year1 && month2 == month1 && day2 == day1 && hour2 < hour1) || (year2 == year1 && month2 == month1 && day2 == day1 && hour2 == hour1 && minute2 < minute1)) {
                    val temp = tempList[j]
                    tempList[j] = tempList[j + 1]
                    tempList[j + 1] = temp
                }
            }
        }
        return  tempList
    }
}
