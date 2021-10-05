package com.squadsapp.controller

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squadsapp.R
import com.squadsapp.helper.DialogAlert

class MoreController : Fragment() {

    lateinit var tabBarController: AppCompatActivity
    private lateinit var parentController: TabBarController
    private lateinit var handleLogout: Button
    private lateinit var imgProfileView: ImageView
    private lateinit var txtUsername: TextView
    private lateinit var txtName: TextView
    private lateinit var txtPhoneNumber: TextView
    private lateinit var txtUserRate: TextView
    private lateinit var txtUserTotalTrip: TextView
    private lateinit var handleEditProfile: Button
    private lateinit var handlePrivateInfo: Button
    private lateinit var handleChangePassword: Button
    private lateinit var handleChangeCountry: Button
    private lateinit var handleAds: Button
    private lateinit var handlePrivacy: Button
    private lateinit var superView: View
    private val dialogAlert = DialogAlert
    var userId = ""

    override fun onResume() {
        super.onResume()
        handleUpdateShow()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        superView = inflater.inflate(R.layout.fragment_more, container, false)
        initializeLayout()
        handleUpdateShow()

        handleEditProfile.setOnClickListener {
            val intent = Intent(superView.context, EditProfileController::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("name", parentController.name)
            intent.putExtra("username", parentController.username)
            intent.putExtra("profileUrl", parentController.profileUrl)
            startActivity(intent)
            tabBarController.overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }

        handlePrivateInfo.setOnClickListener {
            val intent = Intent(superView.context, PrivateInfoController::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("email", parentController.email)
            intent.putExtra("phone", parentController.phoneNumber)
            intent.putExtra("gender", parentController.gender)
            startActivity(intent)
            tabBarController.overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }

        handleChangePassword.setOnClickListener {
            val intent = Intent(superView.context, ChangePasswordController::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
            tabBarController.overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }

        handleChangeCountry.setOnClickListener {
            val intent = Intent(superView.context, ChangeCountryController::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("country", parentController.country)
            startActivity(intent)
            tabBarController.overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }

        handleLogout.setOnClickListener {
            dialogAlert.showLogOut(tabBarController)
        }

        return superView
    }

    private fun initializeLayout() {
        parentController = tabBarController as TabBarController
        handleLogout = superView.findViewById(R.id.handleLogout)
        imgProfileView = superView.findViewById(R.id.imgProfileView)
        txtUsername = superView.findViewById(R.id.txtUsername)
        txtName = superView.findViewById(R.id.txtName)
        txtPhoneNumber = superView.findViewById(R.id.txtPhoneNumber)
        txtUserRate = superView.findViewById(R.id.txtUserRate)
        txtUserTotalTrip = superView.findViewById(R.id.txtUserTotalTrip)
        handleEditProfile = superView.findViewById(R.id.handleEditProfile)
        handlePrivateInfo = superView.findViewById(R.id.handlePrivateInfo)
        handleChangePassword = superView.findViewById(R.id.handleChangePassword)
        handleChangeCountry = superView.findViewById(R.id.handleChangeCountry)
        handleAds = superView.findViewById(R.id.handleAds)
        handlePrivacy = superView.findViewById(R.id.handlePrivacy)
    }


    private fun handleUpdateShow() {
        txtName.text = parentController.name
        txtUsername.text = parentController.username
        txtPhoneNumber.text = parentController.phoneNumber
        txtUserRate.text = String.format("%.2f", parentController.rate)
        txtUserTotalTrip.text = parentController.totalTrip.toString()
        if (parentController.profileUrl != "Not Defined") {
            Glide.with(tabBarController)
                .load(parentController.profileUrl)
                .apply(RequestOptions().circleCrop())
                .apply(RequestOptions().override(400, 400))
                .into(imgProfileView)
        }
    }
}
