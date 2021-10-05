package com.squadsapp.controller

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squadsapp.R
import com.squadsapp.adapter.MemberAdapter
import com.squadsapp.helper.DialogAlert
import com.squadsapp.helper.DisplayDate
import com.squadsapp.helper.LoadDialog
import com.squadsapp.helper.SingleToast
import com.squadsapp.model.*

class TripInfoController : AppCompatActivity(), OnMapReadyCallback {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    private lateinit var handleInitiate: Button
    private lateinit var handleJoin: Button
    private lateinit var handleCancel: Button
    private lateinit var handleLeave: Button
    private lateinit var handleTripRequest: ImageView
    private lateinit var txtLocation: TextView
    private lateinit var txtDestination: TextView
    private lateinit var txtMember: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtTime: TextView
    private lateinit var imgProfileView: ImageView
    private lateinit var imgSharedType: ImageView
    private lateinit var txtOwnerName: TextView
    private lateinit var txtOwnerPhoneNumber: TextView
    private lateinit var txtOwnerRate: TextView
    private lateinit var txtOwnerTotalTrip: TextView
    private lateinit var txtAnnouncement: EditText
    private lateinit var txtSharedType: TextView
    private lateinit var memberListView: RecyclerView
    private lateinit var adapter: MemberAdapter
    private lateinit var mapFragment: SupportMapFragment

    val loadDialog = LoadDialog()
    private val displayDate = DisplayDate
    private val dialogAlert = DialogAlert
    private val singleToast = SingleToast
    private var memberInfoList = mutableListOf<MemberInfo>()
    private var memberList = mutableListOf<Member>()
    private var requestList = mutableListOf<Member>()
    private var isRequesting = false
    private var isFound = false

    private lateinit var tripInfo: Trip
    private val requestCode = 3
    private val requestMap = 4
    private var userId = ""
    private var tripId = ""
    private var ownerId = ""
    private var isLocked = false
    private var isEnded = false
    private var location = ""
    private var destination = ""
    private var sharedType = ""
    private var announcement = ""

    private lateinit var googleView: GoogleMap
    private lateinit var tripCoordination: LatLng

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
        getTripInfo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_info)
        initializeLayout()
        loadDialog.show(this)
//        getTripInfo()
        loadCheckingInfo()
        dynamicInitiate()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleView = googleMap
        googleView.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.googlemap_style))
        googleView.uiSettings.setAllGesturesEnabled(false)
        googleView.setOnMapClickListener {
            val intent = Intent(this, PreviewLocationController::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("isCreating", false)
            intent.putExtra("ownerId", ownerId)
            intent.putExtra("tripId", tripId)
            intent.putExtra("isEnded", isEnded)
            intent.putExtra("tripLat", tripCoordination.latitude)
            intent.putExtra("tripLong", tripCoordination.longitude)
            startActivityForResult(intent, requestMap)
            overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }
    }


    private fun initializeLayout() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.handleMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        userId = intent.getStringExtra("userId")
        tripId = intent.getStringExtra("tripId")
        memberListView = findViewById(R.id.handleMembersView)
        memberListView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        memberListView.isNestedScrollingEnabled = false
        memberListView.hasFixedSize()
        handleInitiate = findViewById(R.id.handleInitiate)
        handleJoin = findViewById(R.id.handleJoin)
        handleCancel = findViewById(R.id.handleCancel)
        handleLeave = findViewById(R.id.handleLeave)
        handleTripRequest = findViewById(R.id.handleRequest)
        txtLocation = findViewById(R.id.txtLocation)
        txtDestination = findViewById(R.id.txtDestination)
        txtMember = findViewById(R.id.txtMembers)
        txtDate = findViewById(R.id.txtDate)
        txtTime = findViewById(R.id.txtTime)
        imgProfileView = findViewById(R.id.imgProfileView)
        imgSharedType = findViewById(R.id.imgSharedType)
        txtOwnerName = findViewById(R.id.txtName)
        txtOwnerPhoneNumber = findViewById(R.id.txtPhoneNumber)
        txtOwnerRate = findViewById(R.id.txtOwnerRate)
        txtOwnerTotalTrip = findViewById(R.id.txtOwnerTotalTrip)
        txtAnnouncement = findViewById(R.id.txtAnnouncement)
        txtSharedType = findViewById(R.id.txtSharedType)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (this.requestCode == requestCode && Activity.RESULT_OK == resultCode) {
            val tempAnnouncement = data!!.getStringExtra("announcement")
            txtAnnouncement.setText(tempAnnouncement)
        } else if (this.requestMap == requestCode && Activity.RESULT_OK == resultCode) {
            val lat = data!!.getDoubleExtra("tripLat", 0.0)
            val long = data.getDoubleExtra("tripLong", 0.0)
            tripCoordination = LatLng(lat, long)
            initializeCameraMap()
        }
    }

    private fun dynamicInitiate() {
        if (isEnded) {
            handleInitiate.visibility = View.GONE
        } else if (userId == ownerId && !isLocked) {
            handleInitiate.visibility = View.VISIBLE
            handleInitiate.setText(R.string.initiate)
        } else if (userId == ownerId && isLocked) {
            handleInitiate.visibility = View.VISIBLE
            handleInitiate.setText(R.string.cancel)
        } else {
            handleInitiate.visibility = View.GONE
        }
    }

    private fun dynamicRightButtons() {
        when {
            isFound -> {
                handleJoin.visibility = View.INVISIBLE
                handleCancel.visibility = View.INVISIBLE
                handleLeave.visibility = View.VISIBLE
            }
            isRequesting -> {
                handleCancel.visibility = View.VISIBLE
                handleJoin.visibility = View.INVISIBLE
                handleLeave.visibility = View.INVISIBLE
            }
            else -> {
                handleCancel.visibility = View.INVISIBLE
                handleJoin.visibility = View.VISIBLE
                handleLeave.visibility = View.INVISIBLE
            }
        }
    }

    private fun dynamicRequestButton() {
        if (sharedType == "Private" && ownerId == userId) {
            handleTripRequest.visibility = View.VISIBLE
        } else {
            handleTripRequest.visibility = View.INVISIBLE
        }
    }

    private fun dynamicSharedType() {
        if (sharedType == "Public") {
            imgSharedType.setImageResource(R.drawable.global)
        } else {
            imgSharedType.setImageResource(R.drawable.privilege)
        }
        txtSharedType.text = sharedType
    }


    private fun getTripInfo() {
        tripRef.child(tripId).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                val tripInfo = p0.getValue(Trip::class.java)!!
                tripCoordination = LatLng(tripInfo.tripLocation.latitude.toDouble(), tripInfo.tripLocation.longitude.toDouble())
                txtLocation.text = tripInfo.location
                location = tripInfo.location
                txtDestination.text = tripInfo.destination
                destination = tripInfo.destination
                txtMember.text = tripInfo.members.toString()
                txtDate.text = displayDate.updatingDisplay(tripInfo.date)
                txtTime.text = tripInfo.time
                announcement = tripInfo.announcement
                isLocked = tripInfo.isLocked
                isEnded = tripInfo.isEnded
                sharedType = tripInfo.sharedType
                handleMembers(tripInfo.memberList as MutableList<Member>)
                txtAnnouncement.setText(announcement)
                dynamicSharedType()
                initializeCameraMap()
                for (i in tripInfo.memberList) {
                    if (i.id == userId) {
                        isFound = true
                        dynamicRightButtons()
                        break
                    }
                }
            }
        })
    }

    private fun initializeCameraMap() {
        googleView.clear()
        googleView.moveCamera(CameraUpdateFactory.newLatLngZoom(tripCoordination, 17f))
        Glide.with(this).asBitmap().apply(RequestOptions().override(80, 80)).load(R.drawable.coordination)
            .into(object: SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    googleView.addMarker(MarkerOptions()
                        .position(tripCoordination)
                        .title("Meeting location")
                        .icon(BitmapDescriptorFactory.fromBitmap(resource))
                    )
                }
            })
    }

    private fun loadCheckingInfo() {
        tripRef.child(tripId).child("requestList").addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (i in p0.children.iterator()) {
                        val memberInfo = i.getValue(Member::class.java)!!
                        if (memberInfo.id == userId) {
                            isRequesting = true
                        }
                    }
                }
                dynamicRightButtons()
            }
        })
    }

    fun handleCancel(view: View) {
        loadDialog.show(this)
        dialogAlert.showCancelRequest(this)
    }

    fun handleInitiateCancel() {
        requestList.clear()
        tripRef.child(tripId).child("requestList").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    var removeMember = Member(userId)
                    for (i in p0.children.iterator()) {
                        val memberInfo = i.getValue(Member::class.java)!!
                        requestList.add(memberInfo)
                        if (memberInfo.id == userId) {
                            removeMember = memberInfo
                        }
                    }
                    requestList.remove(removeMember)
                    tripRef.child(tripId).child("requestList").setValue(requestList)
                    singleToast.show(applicationContext, "Request has been canceled.", Toast.LENGTH_LONG)
                    isRequesting = false
                    dynamicRightButtons()
                    loadDialog.dismiss()
                }
            }

        })
    }


    private fun handleMembers(members: MutableList<Member>) {
        memberInfoList.clear()
        for (i in 0 until members.size) {
            userRef.child(members[i].id).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val member = p0.getValue(User::class.java)!!
                    val memberInfo = MemberInfo(member.id, member.profileUrl, member.name, member.phoneNumber, member.rate, member.totalTrip)
                    when (i) {
                        0 -> showOwner(memberInfo)
                        else -> {
                            memberInfoList.add(memberInfo)
                            refreshAdapter()
                        }
                    }
                }
            })
        }
        refreshAdapter()
    }

    private fun showOwner(owner: MemberInfo) {
        ownerId = owner.id
        if (owner.profileUrl != "Not Defined") {
            Glide.with(this)
                .load(owner.profileUrl)
                .apply(RequestOptions().circleCrop())
                .apply(RequestOptions().override(400, 400))
                .into(imgProfileView)
        }
        txtOwnerName.text = owner.name
        txtOwnerPhoneNumber.text = owner.phoneNumber
        txtOwnerRate.text = String.format("%.2f", owner.rate)
        txtOwnerTotalTrip.text = owner.totalTrip.toString()
        dynamicInitiate()
        dynamicRequestButton()
    }

    private fun refreshAdapter() {
        dynamicHeight()
        adapter = MemberAdapter(this, R.layout.member_cell, memberInfoList)
        memberListView.adapter = adapter
        adapter.notifyDataSetChanged()
        loadDialog.dismiss()
    }

    private fun dynamicHeight() {
        val params= memberListView.layoutParams
        val dp = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (memberInfoList.size * 90).toFloat(), resources.displayMetrics))
        params.height = dp
        memberListView.layoutParams = params
    }

    fun handleDismiss(view: View) {
        finish()
    }

    fun handleJoin(view: View) {
        loadDialog.show(this)
        when {
            isEnded -> singleToast.show(this, "This trip is already ended!", Toast.LENGTH_LONG)
            userId == ownerId -> singleToast.show(this, "You are the owner of this Trip!", Toast.LENGTH_LONG)
            isLocked -> singleToast.show(this, "This trip has been locked by owner!", Toast.LENGTH_LONG)
            else -> {
                tripRef.child(tripId).addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        tripInfo = p0.getValue(Trip::class.java)!!
                        memberList = tripInfo.memberList as MutableList<Member>
                        for (i in memberList) {
                            if (i.id == userId) {
                                singleToast.show(applicationContext, "You already joined this Trip.", Toast.LENGTH_LONG)
                                loadDialog.dismiss()
                                return
                            }
                        }
                        if (sharedType == "Private" && !tripInfo.isLocked && !tripInfo.isEnded && tripInfo.members > 0) {
                            isRequesting = true
                            dialogAlert.showRequestTrip(this@TripInfoController)
                        } else if (!tripInfo.isLocked && !tripInfo.isEnded && tripInfo.members > 0) {
                            memberList.add(Member(userId))
                            tripInfo.memberList = memberList
                            tripInfo.members -= 1
                            txtMember.text = tripInfo.members.toString()
                            handleMembers(memberList)
                            tripRef.child(tripId).setValue(tripInfo)
                            singleToast.show(applicationContext, "Joined successful.", Toast.LENGTH_LONG)
                            isFound = true
                            dynamicRightButtons()
                            loadDialog.dismiss()
                            return
                        } else {
                            singleToast.show(applicationContext, "This Trip is full or has been closed by owner!", Toast.LENGTH_LONG)
                        }
                        loadDialog.dismiss()
                        return
                    }

                })
            }
        }
        loadDialog.dismiss()
    }

    fun handleRequest() {
        requestList.clear()
        tripRef.child(tripId).child("requestList").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (i in p0.children.iterator()) {
                        val member = i.getValue(Member::class.java)!!
                        requestList.add(member)
                        if (member.id == userId) {
                            singleToast.show(applicationContext, "You already requested this trip.", Toast.LENGTH_LONG)
                            loadDialog.dismiss()
                            return
                        }
                    }

                }
                requestList.add(Member(userId))
                tripRef.child(tripId).child("requestList").setValue(requestList)
                singleToast.show(applicationContext, "Request has been sent.", Toast.LENGTH_LONG)
                loadDialog.dismiss()
                return
            }

        })
    }

    fun handleLeave(view: View) {
        loadDialog.show(this)
        if (isEnded) {
            singleToast.show(this, "This trip is already ended!", Toast.LENGTH_LONG)
        } else {
            tripRef.child(tripId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        tripInfo = p0.getValue(Trip::class.java)!!
                        memberList = tripInfo.memberList as MutableList<Member>
                        if (memberList.size == 1 && ownerId == userId) {
                            dialogAlert.showDeleteTrip(this@TripInfoController)
                        } else {
                            dialogAlert.showLeaveTrip(this@TripInfoController)
                        }
                    } else {
                        finish()
                    }
                }
            })
        }
        loadDialog.dismiss()
    }

    fun handleInitiateLeave() {
        for (j in memberList) {
            if (j.id == userId) {
                memberList.remove(j)
                tripInfo.memberList = memberList
                tripInfo.members += 1
                txtMember.text = tripInfo.members.toString()
                tripRef.child(tripId).setValue(tripInfo)
                singleToast.show(this, "Your trip has been left!", Toast.LENGTH_LONG)
                handleMembers(memberList)
                loadDialog.dismiss()
                isFound = false
                dynamicRightButtons()
                return
            }
        }
        singleToast.show(this, "You didn't join this Trip!", Toast.LENGTH_LONG)
        loadDialog.dismiss()
        return
    }

    fun handleInitiateDelete() {
        tripRef.child(tripId).removeValue()
        singleToast.show(this, "Your trip has been deleted!", Toast.LENGTH_LONG)
        loadDialog.dismiss()
        finish()
    }

    fun handleInitiate(view: View) {
        if (ownerId == userId) {
            tripRef.child(tripId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val downloadTripInfo = p0.getValue(Trip::class.java)!!
                    if (isLocked) {
                        downloadTripInfo.isLocked = false
                        isLocked = false
                        tripRef.child(tripId).setValue(downloadTripInfo)
                        singleToast.show(applicationContext, "Your initiate has been canceled.", Toast.LENGTH_LONG)
                    } else {
                        downloadTripInfo.isLocked = true
                        isLocked = true
                        tripRef.child(tripId).setValue(downloadTripInfo)
                        singleToast.show(applicationContext, "Your trip has been initiated.", Toast.LENGTH_LONG)
                    }
                    dynamicInitiate()
                }
            })
        }
    }

    fun handleAnnouncement(view: View) {
        when {
            isEnded -> singleToast.show(this, "This trip is already ended!", Toast.LENGTH_LONG)
            ownerId == userId -> {
                val intent = Intent(this, AnnouncementController::class.java)
                intent.putExtra("tripId", tripId)
                intent.putExtra("announcement", announcement)
                startActivityForResult(intent, requestCode)
                overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
            }
            else -> singleToast.show(this, "Only the owner of this trip can modify announcement.", Toast.LENGTH_LONG)
        }
    }

    fun handleSharedType(view: View) {
        when {
            isEnded -> singleToast.show(this, "This trip is already ended!", Toast.LENGTH_LONG)
            ownerId == userId -> dialogAlert.showSharedType(this, sharedType)
            else -> singleToast.show(this, "Only the owner of this trip can modify trip's shared type.", Toast.LENGTH_LONG)
        }
    }

    fun initiateSharedType(sharedType: String) {
        if (sharedType != this.sharedType) {
            this.sharedType = sharedType
            tripRef.child(tripId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val downloadTripInfo = p0.getValue(Trip::class.java)!!
                    downloadTripInfo.sharedType = sharedType
                    tripRef.child(tripId).setValue(downloadTripInfo)
                    singleToast.show(applicationContext, "Your trip's shared typed has been changed.", Toast.LENGTH_LONG)
                    dynamicSharedType()
                }
            })
        }
    }


    fun handleRequest(view: View) {
        val intent = Intent(this, MemberRequestController::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("tripId", tripId)
        intent.putExtra("tripLocation", location)
        intent.putExtra("tripDestination", destination)
        startActivity(intent)
        overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
    }
}
