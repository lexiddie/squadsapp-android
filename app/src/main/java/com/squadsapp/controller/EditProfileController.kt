package com.squadsapp.controller

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squadsapp.R
import com.squadsapp.helper.*
import com.squadsapp.model.User
import java.io.IOException
import java.util.*

class EditProfileController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val storageRef = FirebaseStorage.getInstance().getReference("profile_images")

    private lateinit var txtName: EditText
    private lateinit var txtUsername: EditText
    private lateinit var imgProfileView: ImageView

    private var userId = ""
    private var name = ""
    private var username = ""
    private var profileUrl = ""
    private var profileUrlUUID = ""
    private var imgProfilePath: Uri? = null
    private val requestImage = 71

    private val loadingDialog = LoadDialog()
    private val singleToast = SingleToast
    private val dialogAlert = DialogAlert
    private val fieldCheck = FieldCheck
    private val keyboard = Keyboard

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
        setContentView(R.layout.activity_edit_profile)
        initializeLayout()

        imgProfileView.setOnClickListener {
            if (profileUrl == "Not Defined" && imgProfilePath == null) {
                handleSelectPhoto()
            } else {
                dialogAlert.showCheckEditProfile(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (this.requestImage == requestCode && Activity.RESULT_OK == resultCode) {
            imgProfilePath = data?.data
            try {
                Glide.with(this)
                    .load(imgProfilePath)
                    .apply(RequestOptions().circleCrop())
                    .apply(RequestOptions().override(400, 400))
                    .into(imgProfileView)
                profileUrlUUID = UUID.randomUUID().toString().toUpperCase()
                handleUploadProfile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun initializeLayout() {
        userId = intent.getStringExtra("userId")
        name = intent.getStringExtra("name")
        username = intent.getStringExtra("username")
        profileUrl = intent.getStringExtra("profileUrl")
        txtName = findViewById(R.id.txtName)
        txtUsername = findViewById(R.id.txtUsername)
        imgProfileView = findViewById(R.id.imgProfileView)
        txtName.setText(name)
        txtUsername.setText(username)
        loadProfile()
        txtName.isCursorVisible = false
        txtUsername.isCursorVisible = false
    }

    private fun loadProfile() {
        if (profileUrl != "Not Defined") {
            Glide.with(this)
                .load(profileUrl)
                .apply(RequestOptions().circleCrop())
                .apply(RequestOptions().override(400, 400))
                .into(imgProfileView)
        }
    }

    fun handleRemoveProfile() {
        if (profileUrl != "Not Defined") {
            val localStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileUrl)
            localStorageRef.delete()
            userRef.child(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    if (imgProfilePath == null) {
                        imgProfileView.setImageResource(R.drawable.user)
                        profileUrl = "Not Defined"
                    }
                    val userInfo = p0.getValue(User::class.java)!!
                    userInfo.profileUrl = "Not Defined"
                    userRef.child(userId).setValue(userInfo)
                }
            })
        }
    }

    fun handleSelectPhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, requestImage)
    }

    fun handleDismiss(view: View) {
        finish()
    }

    fun handleSave(view: View) {
        if (txtName.text.isNullOrEmpty() || txtUsername.text.isNullOrEmpty()) {
            singleToast.show(this, "Name or Username cannot be empty.", Toast.LENGTH_LONG)
        } else if (!fieldCheck.checkName(txtName.text.toString())) {
            singleToast.show(this, "The name must be at least 3 letters in alphabet or with numbers", Toast.LENGTH_LONG)
        } else if (!fieldCheck.checkUsername(txtUsername.text.toString())) {
            singleToast.show(this, "The username must be at least 3 letters in alphabet or with numbers", Toast.LENGTH_LONG)
        } else if (name == txtName.text.toString().trimEnd() && username == txtUsername.text.toString().trim().toLowerCase()) {
            finish()
        } else {
            loadingDialog.show(this)
            userRef.child(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val userInfo = p0.getValue(User::class.java)!!
                    userInfo.name = txtName.text.toString().trimEnd()
                    userInfo.username = txtUsername.text.toString().trim().toLowerCase()
                    userRef.child(userId).setValue(userInfo)
                    singleToast.show(this@EditProfileController, "Your information has been updated.", Toast.LENGTH_LONG)
                    loadingDialog.dismiss()
                    finish()
                }
            })
        }
    }

    private fun handleUploadProfile() {
        loadingDialog.show(this)
        handleRemoveProfile()
        if (imgProfilePath != null) {
            storageRef.child(profileUrlUUID).putFile(imgProfilePath!!).addOnSuccessListener {
                storageRef.child(profileUrlUUID).downloadUrl.addOnSuccessListener {
                    profileUrl = it.toString()
                    imgProfilePath = null
                    userRef.child(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }
                        override fun onDataChange(p0: DataSnapshot) {
                            val userInfo = p0.getValue(User::class.java)!!
                            userInfo.profileUrl = profileUrl
                            userRef.child(userId).setValue(userInfo)
                            loadingDialog.dismiss()
                            Log.e("Check", "Update finished")
                        }
                    })
                }
                Log.e("Check", "Successfully uploaded image: ${it.metadata?.path}")
            }.addOnFailureListener {
                Log.e("Check", "Failed to upload image to storage: ${it.message}")
            }
        }
    }

    fun handleKeyboard(view: View) {
        keyboard.hideKeyboard(this)
    }
}
