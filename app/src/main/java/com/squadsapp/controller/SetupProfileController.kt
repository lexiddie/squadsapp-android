package com.squadsapp.controller

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import com.squadsapp.R
import com.squadsapp.helper.DialogAlert
import com.squadsapp.helper.SingleToast
import android.os.Build
import android.annotation.SuppressLint
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.util.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squadsapp.helper.CountryInfo
import com.squadsapp.helper.LoadDialog
import com.squadsapp.model.Location
import com.squadsapp.model.User


class SetupProfileController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val storageRef = FirebaseStorage.getInstance().getReference("profile_images")

    private lateinit var txtCountry: TextView
    private lateinit var imgProfileView: ImageView
    private var imgProfilePath: Uri? = null
    private val requestImage = 71
    private val requestCode = 1
    private val singleToast = SingleToast
    private val dialogAlert = DialogAlert
    private val countryInfo = CountryInfo()
    private val loadingDialog = LoadDialog()
    private var userID = ""
    private var country = ""
    private var name = ""
    private var phoneNumber = ""
    private var username = ""
    private var password = ""
    private var profileUrl = "Not Defined"

    override fun onResume() {
        super.onResume()
        txtCountry.text = country
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_to_right_open, R.anim.left_to_right_close)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)
        initializeLayout()
        country = countryInfo.getCountry(this)
        txtCountry.text = country
    }

    private fun initializeLayout() {
        txtCountry = findViewById(R.id.txtCountry)
        imgProfileView = findViewById(R.id.imgProfileView)
        imgProfileView.setImageResource(R.drawable.user)
        userID = userRef.push().key!!
        name = intent.getStringExtra("Name")
        phoneNumber = intent.getStringExtra("PhoneNumber")
        username = intent.getStringExtra("Username")
        password = intent.getStringExtra("Password")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (this.requestCode == requestCode && Activity.RESULT_OK == resultCode) {
            country = data!!.getStringExtra("country")
        } else if (this.requestImage == requestCode && Activity.RESULT_OK == resultCode) {
            imgProfilePath = data?.data
            try {
                Glide.with(this)
                    .load(imgProfilePath)
                    .apply(RequestOptions().circleCrop())
                    .apply(RequestOptions().override(400, 400))
                    .into(imgProfileView)
                profileUrl = UUID.randomUUID().toString().toUpperCase()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun handleSignUp(view: View) {
        loadingDialog.show(this)
        handleUploadProfile()
    }

    private fun handleCreateUser(userProfileUrl: String) {
        val user = User(userID, name, username, phoneNumber, "Not Defined", password, country, userProfileUrl, 5.00, 0, "Not Defined")
        userRef.child(userID).setValue(user).addOnCompleteListener {
            val intent = Intent(applicationContext, TabBarController::class.java)
            intent.putExtra("userId", userID)
            intent.putExtra("userCountry", country)
            intent.putExtra("userProfileUrl", userProfileUrl)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
            loadingDialog.dismiss()
            singleToast.show(applicationContext, "Sign up successful!", Toast.LENGTH_LONG)
        }
    }

    private fun handleUploadProfile() {
            if (imgProfilePath != null) {
    //            val compressImage = CompressImage().getImageUri(this, imgProfilePath!!)
                storageRef.child(profileUrl).putFile(imgProfilePath!!).addOnSuccessListener {
                    storageRef.child(profileUrl).downloadUrl.addOnSuccessListener {
                        val link = it.toString()
                        handleCreateUser(link)
                    }
                    Log.e("Check", "Successfully uploaded image: ${it.metadata?.path}")
                }.addOnFailureListener {
                    Log.e("Check", "Failed to upload image to storage: ${it.message}")
                }
            } else {
            handleCreateUser(profileUrl)
        }
    }

    fun handleCountry(view: View) {
        val intent = Intent(this, CountryController::class.java)
        startActivityForResult(intent, requestCode)
        overridePendingTransition(com.squadsapp.R.anim.right_to_left_open, com.squadsapp.R.anim.right_to_left_close)
    }

    fun handleUploadProfile(view: View?) {
        if (imgProfilePath == null) {
            handleChooseProfile()
        } else {
            dialogAlert.showCheckProfileSetup(this)
        }
    }

    fun handleRemoveProfile() {
        imgProfilePath = null
        imgProfileView.setImageResource(R.drawable.user)
        profileUrl = "Not Defined"
    }

    @SuppressLint("ObsoleteSdkInt")
    fun handleChooseProfile() {
        if (Build.VERSION.SDK_INT < 21) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestImage)
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, requestImage)
        }
    }


}
