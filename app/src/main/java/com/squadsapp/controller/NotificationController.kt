package com.squadsapp.controller

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import com.squadsapp.R
import com.squadsapp.adapter.TripRequestAdapter

class NotificationController : Fragment() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    lateinit var tabBarController: AppCompatActivity
    private lateinit var parentController: TabBarController
    private lateinit var superView: View
    private lateinit var notificationView: RecyclerView
    private lateinit var adapter: TripRequestAdapter
    private lateinit var handleTripRequest: RelativeLayout
    private lateinit var txtCountRequest: TextView

    var userId = ""

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        superView = inflater.inflate(R.layout.fragment_notification, container, false)
        parentController = tabBarController as TabBarController
        initializeLayout()
        handleTripRequest.setOnClickListener {
            val intent = Intent(parentController, TripRequestController::class.java)
            intent.putExtra("userId", userId)
            tabBarController.startActivity(intent)
            tabBarController.overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }
        txtCountRequest = superView.findViewById(R.id.txtCountRequest)
        txtCountRequest.text = parentController.tripRequestList.size.toString()
        return superView
    }


    private fun initializeLayout() {
        notificationView = superView.findViewById(R.id.notificationView)
        notificationView.layoutManager = LinearLayoutManager(tabBarController, LinearLayout.VERTICAL, false)
        handleTripRequest = superView.findViewById(R.id.handleRequest)
    }


//    private fun refreshAdapter() {
//        adapter = TripRequestAdapter(tabBarController, R.layout.notification_cell, emptyList())
//        notificationView.adapter = adapter
//        adapter.notifyDataSetChanged()
//    }

}
