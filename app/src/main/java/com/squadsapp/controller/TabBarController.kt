package com.squadsapp.controller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.database.*
import com.squadsapp.R
import com.squadsapp.model.Member
import com.squadsapp.model.Trip
import com.squadsapp.model.TripRequest
import com.squadsapp.model.User
import kotlinx.android.synthetic.main.activity_tab_bar.*
import org.json.JSONArray

class TabBarController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    private lateinit var mainFragment: Fragment
    private lateinit var standard: SharedPreferences
    private lateinit var update: SharedPreferences.Editor
    private var navItemIndex = 0

    var sortedIndex = 0
    var userId = ""
    var name = ""
    var username = ""
    var phoneNumber = ""
    var email = ""
    var country = ""
    var profileUrl = ""
    var rate = 0.0
    var totalTrip = 0
    var gender = ""
    var tripRequestList = mutableListOf<TripRequest>()

    private var isGPS = false
    private var isNetwork = false
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    lateinit var userLocation: Location

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_bar)
        initializeData()
        handleRetrieveUserInfo()
        getTripRequestList()
        handleSavingUser()
        navigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener)
        if (savedInstanceState == null) {
            navigation.menu.getItem(0).setIcon(R.drawable.home_click)
            navItemIndex = 0
            loadFragment()
        }
        checkPermission()
    }

    @SuppressLint("CommitPrefEdits")
    private fun initializeData() {
        userId = intent.getStringExtra("userId")
        country = intent.getStringExtra("userCountry")
        profileUrl = intent.getStringExtra("userProfileUrl")
        sortedIndex = intent.getIntExtra("sortedIndex", 0)
        standard = this.getSharedPreferences("login", 0)
        update = standard.edit()
    }

    private val navigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        navigation.menu.getItem(0).setIcon(R.drawable.home)
        navigation.menu.getItem(1).setIcon(R.drawable.yourtrip)
        navigation.menu.getItem(2).setIcon(R.drawable.navigate)
        navigation.menu.getItem(3).setIcon(R.drawable.more)
        when (item.itemId) {
            R.id.navigation_main -> {
                item.setIcon(R.drawable.home_click)
                navItemIndex = 0
                loadFragment()
            }
            R.id.navigation_yourtrip -> {
                item.setIcon(R.drawable.yourtrip_click)
                navItemIndex = 1
                loadFragment()
            }
            R.id.navigation_navigate -> {
                item.setIcon(R.drawable.navigate_click)
                navItemIndex = 2
                loadFragment()
            }
            R.id.navigation_more -> {
                item.setIcon(R.drawable.more_click)
                navItemIndex = 3
                loadFragment()
            }
        }
        false
    }

    private fun loadFragment() {
        mainFragment = getFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainFrame, mainFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun getFragment(): Fragment {
        when (navItemIndex) {
            0 -> {
                val homeController = HomeController()
                homeController.tabBarController = this
                homeController.userId = this.userId
                homeController.profileUrl = this.profileUrl
                homeController.country = this.country
                return homeController
            }
            1 -> {
                val yourTripController = YourTripController()
                yourTripController.tabBarController = this
                yourTripController.userID = this.userId
                return yourTripController
            }
            2 -> {
                val navigateController = NavigateController()
                navigateController.tabBarController = this
                navigateController.userId = this.userId
                return navigateController
            }
            3 -> {
                val moreController = MoreController()
                moreController.tabBarController = this
                moreController.userId = this.userId
                return moreController
            }
            else -> {
                val homeController = HomeController()
                homeController.tabBarController = this
                homeController.userId = this.userId
                homeController.profileUrl = this.profileUrl
                homeController.country = this.country
                return homeController
            }
        }
    }

    private fun handleRetrieveUserInfo() {
        userRef.child(userId).addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)!!
                name = user.name
                username = user.username
                country = user.country
                phoneNumber = user.phoneNumber
                email = user.email
                rate = user.rate
                totalTrip = user.totalTrip
                gender = user.gender
                profileUrl = user.profileUrl
            }
        })
    }

    fun handleSavingSortedIndex() {
        update.putInt("sortedIndex", sortedIndex)
        update.apply()
    }

    private fun handleSavingUser() {
        update.putBoolean("userLogin", true)
        update.putString("userId", userId)
        update.putString("userCountry", country)
        update.putString("userProfileUrl", profileUrl)
        update.apply()
    }

    fun handleLogoutUser() {
        update.putBoolean("userLogin", false)
        update.putString("userId", null)
        update.putString("userCountry", null)
        update.putString("userProfileUrl", null)
        update.putInt("sortedIndex", 0)
        update.apply()
        val intent = Intent(this, LoginController::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.left_to_right_open, R.anim.left_to_right_close)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == -1) {
            Log.e("CheckPermission", "We cannot get the location $requestCode")
            return
        } else if (requestCode == 888) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.isNotEmpty()) {
                handleGeoLocation()
            } else {
                Log.e("CheckPermission", "We cannot get the location $requestCode")
            }
            return
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 888)
                Log.e("CheckPermission", "We are requesting the permission!")
                return
            } else {
                Log.e("CheckPermission", "No need to check permission!")
                handleGeoLocation()
            }
        }
    }

    private fun handleGeoLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        locationListener = object: LocationListener {
            override fun onLocationChanged(location: Location?) {
                userLocation = location!!
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }
            override fun onProviderEnabled(provider: String?) {
            }
            override fun onProviderDisabled(provider: String?) {
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (isGPS) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10f, locationListener)
                userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                Log.e("CheckProtocol", "GPS")
            } else if (isNetwork) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10f, locationListener)
                userLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                Log.e("CheckProtocol", "Network")
            }
            Log.e("CheckCoordination", "I want to know the latitude: ${userLocation.latitude} and longitude: ${userLocation.longitude}")
        }
    }

    private fun getTripRequestList() {
        tripRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                tripRequestList.clear()
                if(p0.exists()){
                    var count = 0
                    for (i in p0.children){

                        val trip = i.getValue(Trip::class.java)!!
                        val owner = trip.memberList[0]
                        val requestValue = i.child("requestList").value
                        if (requestValue != null && owner.id == userId) {
                            count++
                            val jsonArray = JSONArray(requestValue.toString())
                            val memberList = mutableListOf<Member>()
                            for (j in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(j)
                                val memberId = jsonObject.get("id").toString()
                                memberList.add(Member(memberId))
                            }
                            val tripRequest = TripRequest(trip.id, trip.dateCreated, trip.timeCreated, trip.location, trip.destination, trip.date, trip.time, trip.members, memberList)
                            tripRequestList.add(tripRequest)
//                            refreshAdapter()
                        }
                        //Handle
                        Log.e("CheckCount", count.toString())
                        Log.e("CheckChild", tripRequestList.size.toString())
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}
