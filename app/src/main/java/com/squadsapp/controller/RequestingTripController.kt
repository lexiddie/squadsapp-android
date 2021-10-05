package com.squadsapp.controller

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.firebase.database.*
import com.squadsapp.R
import com.squadsapp.adapter.TripAdapter
import com.squadsapp.model.Trip
import com.squadsapp.model.TripInfo
import com.squadsapp.model.User
import org.json.JSONArray


class RequestingTripController : Fragment() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    lateinit var tabBarController: AppCompatActivity
    private lateinit var adapter: TripAdapter
    private lateinit var superView: View
    private lateinit var requestingTripView: RecyclerView
    private var requestingTripList = mutableListOf<TripInfo>()
    private var sortedList = mutableListOf<TripInfo>()
    var userId = ""

    override fun onResume() {
        super.onResume()
        getRequestingTripList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        superView = inflater.inflate(R.layout.fragment_requesting_trip, container, false)
        requestingTripView = superView.findViewById(R.id.handleRequestingView)
        requestingTripView.layoutManager = LinearLayoutManager(tabBarController, LinearLayout.VERTICAL, false)
        return superView
    }

    private fun getRequestingTripList() {
        tripRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                requestingTripList.clear()
                if(p0.exists()){
                    for (i in p0.children){
                        val trip = i.getValue(Trip::class.java)!!
                        val owner = trip.memberList[0]
                        val requestValue = i.child("requestList").value
                        if (requestValue != null) {
                            val jsonArray = JSONArray(requestValue.toString())
                            for (j in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(j)
                                val memberId = jsonObject.get("id").toString()
                                if (userId == memberId) {
                                    userRef.child(owner.id).addListenerForSingleValueEvent(object: ValueEventListener{
                                        override fun onCancelled(p0: DatabaseError) {
                                        }
                                        override fun onDataChange(p0: DataSnapshot) {
                                            val profileUrl = p0.getValue(User::class.java)!!.profileUrl
                                            val tripLocation = trip.tripLocation
                                            val tripInfo = TripInfo(trip.id, trip.dateCreated, trip.timeCreated, profileUrl, trip.location, trip.destination, trip.date, trip.time, trip.members, tripLocation)
                                            requestingTripList.add(tripInfo)
                                            refreshAdapter()
                                        }
                                    })
                                }
                            }
                            refreshAdapter()
                        }
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun refreshAdapter() {
        sortedList = sortLatest()
        adapter = TripAdapter(tabBarController, R.layout.trip_cell, sortedList)
        requestingTripView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun sortLatest(): MutableList<TripInfo> {
        val tempList = requestingTripList
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
