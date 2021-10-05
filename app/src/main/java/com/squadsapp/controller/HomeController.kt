package com.squadsapp.controller

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.google.firebase.database.*
import com.squadsapp.R
import com.squadsapp.adapter.TripAdapter
import com.squadsapp.helper.DialogAlert
import com.squadsapp.model.Trip
import com.squadsapp.model.TripInfo
import com.squadsapp.model.User


class HomeController : Fragment() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    lateinit var tabBarController: AppCompatActivity
    private lateinit var parentController: TabBarController
    private lateinit var adapter: TripAdapter
    private lateinit var handleCreateTrip: Button
    private lateinit var handleSortBy: Button
    private lateinit var superView: View
    private lateinit var tripListView: RecyclerView
    private var tripInfoList = mutableListOf<TripInfo>()
    private var sortedInfoList = mutableListOf<TripInfo>()
    private val dialogAlert = DialogAlert

    var userId = ""
    var profileUrl = ""
    var country = ""
    lateinit var userLocation: Location

    override fun onResume() {
        super.onResume()
        getTripList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        superView = inflater.inflate(R.layout.fragment_home, container, false)

        parentController = tabBarController as TabBarController

        initializeLayout()

        handleCreateTrip.setOnClickListener {
            val intent = Intent(superView.context, CreateTripController::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("profileUrl", profileUrl)
            intent.putExtra("userCountry", country)
            startActivity(intent)
            tabBarController.overridePendingTransition(R.anim.left_to_right_open, R.anim.left_to_right_close)
        }
        handleSortBy.setOnClickListener {
            dialogAlert.showSortBy(parentController, this, parentController.sortedIndex)
        }
        return superView
    }

    private fun initializeLayout() {
        handleCreateTrip = superView.findViewById(R.id.handleCreateTrip)
        handleSortBy = superView.findViewById(R.id.handleSortBy)
        tripListView = superView.findViewById(R.id.handleHomeView)
        tripListView.layoutManager = LinearLayoutManager(tabBarController, LinearLayout.VERTICAL, false)
    }

    private fun getTripList() {
        tripRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                tripInfoList.clear()
                if(p0.exists()){
                    for (t in p0.children){
                        val trip = t.getValue(Trip::class.java)!!
                        if (trip.country == country && !trip.isEnded && !trip.isLocked) {
                            val memberID = trip.memberList[0]
                            userRef.child(memberID.id).addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {

                                }
                                override fun onDataChange(p0: DataSnapshot) {
                                    val profileUrl = p0.getValue(User::class.java)!!.profileUrl
                                    val tripLocation = trip.tripLocation
                                    val tripInfo = TripInfo(trip.id, trip.dateCreated, trip.timeCreated, profileUrl, trip.location, trip.destination, trip.date, trip.time, trip.members, tripLocation)
                                    tripInfoList.add(tripInfo)
                                    refreshAdapter()
                                }

                            })
                        }
                    }
                    refreshAdapter()
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    fun refreshAdapter() {
        handleSortedList()
        adapter = TripAdapter(tabBarController, R.layout.trip_cell, sortedInfoList)
        tripListView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun handleSortedList() {
        when (parentController.sortedIndex) {
            0 -> {
                sortedInfoList = tripInfoList
            }
            1 -> {
                sortedInfoList = sortLatest()
            }
            2 -> {
                sortedInfoList = sortEarliest()
            }
            3 -> {
                sortedInfoList = sortNearby()
            }
        }
    }

    private fun sortNearby(): MutableList<TripInfo> {
        userLocation = parentController.userLocation
        val tempList = tripInfoList
        for (i in 0 until tempList.size - 1) {
            for (j in 0 until tempList.size - i - 1) {
                val l1 = Location("first")
                l1.latitude = (tempList[j + 1].tripLocation.latitude)
                l1.longitude = (tempList[j + 1].tripLocation.longitude)
                val l2 = Location("second")
                l2.latitude = (tempList[j].tripLocation.latitude)
                l2.longitude = (tempList[j].tripLocation.longitude)
                val firstDistance = l1.distanceTo(userLocation)
                val secondDistance = l2.distanceTo(userLocation)
                if (secondDistance > firstDistance) {
                    val temp = tempList[j]
                    tempList[j] = tempList[j + 1]
                    tempList[j + 1] = temp
                }
            }
        }
        return tempList
    }

    private fun sortLatest(): MutableList<TripInfo> {
        val tempList = tripInfoList
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

    private fun sortEarliest(): MutableList<TripInfo> {
        val tempList = tripInfoList
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
                if (year2 > year1 || (year2 == year1 && month2 > month1) || (year2 == year1 && month2 == month1 && day2 > day1) || (year2 == year1 && month2 == month1 && day2 == day1 && hour2 > hour1) || (year2 == year1 && month2 == month1 && day2 == day1 && hour2 == hour1 && minute2 > minute1)) {
                    val temp = tempList[j]
                    tempList[j] = tempList[j + 1]
                    tempList[j + 1] = temp
                }
            }
        }
        return  tempList
    }
}
