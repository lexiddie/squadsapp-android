package com.squadsapp.controller

import android.Manifest
import android.app.Activity
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
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squadsapp.R
import com.squadsapp.helper.SingleToast

class PreviewLocationController : AppCompatActivity(), OnMapReadyCallback {

    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")

    private val singleToast = SingleToast
    private lateinit var handleDismissLeft: Button
    private lateinit var handleDismissRight: Button
    private lateinit var handleDone: Button
    private lateinit var handleEdit: Button
    private lateinit var handleFinish: Button
    private lateinit var handleMapPicker: ImageView
    private lateinit var mapFragment: SupportMapFragment

    private var ownerId = ""
    private var userId = ""
    private var tripID = ""
    private var isEnded = false
    private var isCreating = false

    private lateinit var googleView: GoogleMap
    private var isGPS = false
    private var isNetwork = false
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var userLocation: Location
    private var tripCoordination: LatLng? = null

    override fun finish() {
        super.finish()
        when (isCreating) {
            true -> overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
            false -> overridePendingTransition(R.anim.left_to_right_open, R.anim.left_to_right_close)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun handleDismiss(view: View) {
        finish()
    }

    private fun dynamicNavigationButtons() {
        when (isCreating) {
            true -> {
                handleDismissLeft.visibility = View.INVISIBLE
                handleDone.visibility = View.VISIBLE
            }
            false -> {
                handleDismissRight.visibility = View.INVISIBLE
            }
        }
    }

    private fun dynamicRightButtons() {
        if (isEnded || isCreating) {
            handleEdit.visibility = View.INVISIBLE
        } else if (ownerId == userId) {
            handleEdit.visibility = View.VISIBLE
        }
    }

    private fun loadMapPicker(check: Int) {
        when (check) {
            0 -> handleMapPicker.visibility = View.VISIBLE
            1 -> handleMapPicker.visibility = View.INVISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_location)
        initializeLayout()
        dynamicNavigationButtons()
        dynamicRightButtons()
        handleGeoLocation()
    }

    private fun initializeLayout() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.handleMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        isCreating = intent.getBooleanExtra("isCreating", false)
        val tripLat = intent.getDoubleExtra("tripLat", 0.0)
        val tripLong = intent.getDoubleExtra("tripLong", 0.0)
        tripCoordination = LatLng(tripLat, tripLong)
        previewController()
        handleDismissLeft = findViewById(R.id.handleDismissLeft)
        handleDismissRight = findViewById(R.id.handleDismissRight)
        handleDone = findViewById(R.id.handleDone)
        handleEdit = findViewById(R.id.handleEdit)
        handleFinish = findViewById(R.id.handleFinish)
        handleMapPicker = findViewById(R.id.handleMapPicker)
    }

    private fun previewController() {
        if (!isCreating) {
            userId = intent.getStringExtra("userId")
            ownerId = intent.getStringExtra("ownerId")
            tripID = intent.getStringExtra("tripId")
            isEnded = intent.getBooleanExtra("isEnded", false)
            Log.e("CheckEnd", isEnded.toString())
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        googleView = googleMap
        googleView.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.googlemap_style))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleView.isMyLocationEnabled = true
        }
        googleView.setOnCameraIdleListener {
            val currentCoordination = googleView.cameraPosition.target
            tripCoordination = currentCoordination
            Log.e("CheckMidLatLng", currentCoordination.toString())
        }
        googleView.setOnCameraMoveListener {
            if (isCreating) {
                googleMap.clear()
                loadMapPicker(0)
            }
        }
        initializeTripLocation()
    }

    private fun initializeTripLocation() {
        if (tripCoordination!!.latitude != 0.0 && tripCoordination!!.longitude != 0.0) {
            googleView.moveCamera(CameraUpdateFactory.newLatLngZoom(tripCoordination, 16f))
            Glide.with(this).asBitmap().apply(RequestOptions().override(80, 80)).load(R.drawable.coordination)
                .into(object: SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        googleView.addMarker(MarkerOptions()
                            .position(tripCoordination!!)
                            .title("Meeting location")
                            .icon(BitmapDescriptorFactory.fromBitmap(resource))
                        )
                    }
                })
        } else {
            loadMapPicker(0)
            googleView.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(userLocation.latitude, userLocation.longitude), 16f))
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

    private fun loadDataBack() {
        val result = Intent()
        result.putExtra("tripLat", tripCoordination!!.latitude)
        result.putExtra("tripLong", tripCoordination!!.longitude)
        setResult(Activity.RESULT_OK, result)
    }

    fun handleDone(view: View) {
        loadDataBack()
        finish()
    }

    fun handleEdit(view: View) {
        if (ownerId == userId) {
            handleEdit.visibility = View.INVISIBLE
            handleFinish.visibility = View.VISIBLE
            googleView.clear()
            loadMapPicker(0)
        } else {
            singleToast.show(this, "Only owner can modify the meeting location", Toast.LENGTH_LONG)
        }
    }

    fun handleFinish(view: View) {
        tripRef.child(tripID).child("tripLocation").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                val locationInfo = p0.getValue(com.squadsapp.model.Location::class.java)!!
                locationInfo.latitude = tripCoordination!!.latitude
                locationInfo.longitude = tripCoordination!!.longitude
                tripRef.child(tripID).child("tripLocation").setValue(locationInfo)
                singleToast.show(this@PreviewLocationController, "Trip's meeting location has been updated", Toast.LENGTH_LONG)
            }

        })
        loadDataBack()
        finish()
    }
}
