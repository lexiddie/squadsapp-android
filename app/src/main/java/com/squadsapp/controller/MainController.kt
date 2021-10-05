package com.squadsapp.controller

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.squadsapp.R


class MainController : AppCompatActivity() {

    private lateinit var standard: SharedPreferences
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        standard = this.getSharedPreferences("login", 0)
        handleCheckUser()
    }

    private fun handleCheckUser() {
        val checkLogin = standard.getBoolean("userLogin", false)
        val userID = standard.getString("userId", null)
        val country = standard.getString("userCountry", null)
        val profileUrl = standard.getString("userProfileUrl", null)
        val sortedIndex = standard.getInt("sortedIndex", 0)
        val runnable = {
            if (checkLogin && userID != null && country != null && profileUrl != null) {
                val intent = Intent(applicationContext, TabBarController::class.java)
                intent.putExtra("userId", userID)
                intent.putExtra("userCountry", country)
                intent.putExtra("userProfileUrl", profileUrl)
                intent.putExtra("sortedIndex", sortedIndex)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                finishAffinity()
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                val intent = Intent(applicationContext, LoginController::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP )
                finishAffinity()
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
        handler.postDelayed(runnable, 1000)
    }
}
