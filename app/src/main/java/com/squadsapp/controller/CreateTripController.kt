package com.squadsapp.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
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
import com.google.firebase.database.FirebaseDatabase
import com.squadsapp.R
import com.squadsapp.helper.*
import com.squadsapp.model.Member
import com.squadsapp.model.Trip
import java.text.SimpleDateFormat
import java.util.*

class CreateTripController : AppCompatActivity(), OnMapReadyCallback {

    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    private val loadingDialog = LoadDialog()
    private val dialogAlert = DialogAlert
    private val singleToast = SingleToast
    private val keyboard = Keyboard
    private val displayDate = DisplayDate

    @SuppressLint("SimpleDateFormat")
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
    private val calendar = Calendar.getInstance()
    private val setupCalendar = Calendar.getInstance()
    private lateinit var txtLocation: EditText
    private lateinit var txtDestination: EditText
    private lateinit var txtDate: EditText
    private lateinit var txtTime: EditText
    private lateinit var txtMember: EditText
    private lateinit var mapFragment: SupportMapFragment

    private var isCreated = false
    private val requestMap = 5
    private var tripId = ""
    private var userId = ""
    private var profileUrl = ""
    private var country = ""
    private var startLocation = ""
    private var destination = ""
    private var date = ""
    private var time = ""
    private var members = 0
    private val memberList = mutableListOf<Member>()

    private lateinit var googleView: GoogleMap
    private var isGPS = false
    private var isNetwork = false
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var userLocation: Location
    private var tripCoordination: LatLng? = null


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun finish() {
        super.finish()
        when(isCreated) {
            false -> overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
            true -> overridePendingTransition(R.anim.left_to_right_open, R.anim.left_to_right_close)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_trip)
        initializeLayout()
        handleGeoLocation()
        txtDate.setOnClickListener {
            setupCalendar.time
            val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateIntoTextField()
            }
            val displayDateDialog = DatePickerDialog(this, R.style.DialogTheme, date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
            displayDateDialog.datePicker.minDate = setupCalendar.timeInMillis
            displayDateDialog.show()
        }
        txtTime.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(this, R.style.DialogTheme, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                txtTime.setText(String.format("%02d:%02d", hour, minute)) }, hour, minute, true)
            timePicker.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (this.requestMap == requestCode && Activity.RESULT_OK == resultCode) {
            val lat = data!!.getDoubleExtra("tripLat", 0.0)
            val long = data.getDoubleExtra("tripLong", 0.0)
            tripCoordination = LatLng(lat, long)
            Log.e("CheckResultCoordination", tripCoordination.toString())
            initializeCameraMap()
        }
    }

    private fun updateIntoTextField(){
        date = simpleDateFormat.format(calendar.time)
        txtDate.setText(displayDate.updatingDisplay(date))
    }

    private fun initializeLayout() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.handleMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        tripId = tripRef.push().key!!
        userId = intent.getStringExtra("userId")
        country = intent.getStringExtra("userCountry")
        profileUrl = intent.getStringExtra("profileUrl")
        txtLocation = findViewById(R.id.txtLocation)
        txtDestination = findViewById(R.id.txtDestination)
        txtDate = findViewById(R.id.txtDate)
        txtTime = findViewById(R.id.txtTime)
        txtMember = findViewById(R.id.txtMembers)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleView = googleMap
        googleView.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.googlemap_style))
        googleView.uiSettings.setAllGesturesEnabled(false)
        googleView.setOnMapClickListener {
            val intent = Intent(this, PreviewLocationController::class.java)
            intent.putExtra("isCreating", true)
            intent.putExtra("tripLat", tripCoordination?.latitude)
            intent.putExtra("tripLong", tripCoordination?.longitude)
            startActivityForResult(intent, requestMap)
            overridePendingTransition(R.anim.left_to_right_open, R.anim.left_to_right_close)
        }
        initializeCameraMap()
    }

    fun handleKeyboard(view: View) {
        keyboard.hideKeyboard(this)
    }

    fun handleDismiss(view: View) {
        finish()
    }

    fun handleDone(view: View) {
        if (txtLocation.text.toString().isEmpty() || txtDestination.text.toString().isEmpty() || txtTime.text.toString().isEmpty() || txtDate.text.toString().isEmpty() || txtMember.text.toString().isEmpty()){
            singleToast.show(applicationContext, "Please fill the information", Toast.LENGTH_LONG)
        } else if (tripCoordination == null) {
            singleToast.show(applicationContext, "Please setup meeting location", Toast.LENGTH_LONG)
        } else {
            dialogAlert.showTripType(this)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun handleCreateTrip(sharedType: String) {
        isCreated = true
        loadingDialog.show(this)
        startLocation = txtLocation.text.toString()
        destination = txtDestination.text.toString()
        time = txtTime.text.toString()
        members = txtMember.text.toString().toInt()
        val member = Member(userId)
        memberList.add(member)
        val calendar = Calendar.getInstance()
        val dateCreated = simpleDateFormat.format(calendar.time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val timeCreated = (String.format("%02d:%02d:%02d", hour, minute, second))
        val tripLocation = com.squadsapp.model.Location(tripCoordination!!.latitude, tripCoordination!!.longitude)
        val trip = Trip(tripId, dateCreated, timeCreated, sharedType, country, false, false, startLocation, destination, date, time, "", members - 1, memberList, tripLocation)
        tripRef.child(tripId).setValue(trip).addOnCompleteListener{
            singleToast.show(applicationContext, "Trip has been created", Toast.LENGTH_LONG)
            val intent = Intent(this, TripInfoController::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("tripId", tripId)
            startActivity(intent)
            loadingDialog.dismiss()
            finish()
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

    private fun initializeCameraMap() {
        when(tripCoordination) {
            null -> googleView.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(userLocation.latitude, userLocation.longitude), 16f))
            else -> {
                googleView.clear()
                googleView.moveCamera(CameraUpdateFactory.newLatLngZoom(tripCoordination, 16f))
                Glide.with(this).asBitmap().apply(RequestOptions().override(80, 80)).load(R.drawable.coordination)
                    .into(object: SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            googleView.addMarker(
                                MarkerOptions()
                                    .position(tripCoordination!!)
                                    .title("Meeting location")
                                    .icon(BitmapDescriptorFactory.fromBitmap(resource))
                            )
                        }
                    })
            }
        }
    }
}
