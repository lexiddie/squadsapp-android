package com.squadsapp.controller

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.squadsapp.R


class YourTripController : Fragment() {

    lateinit var tabBarController: AppCompatActivity

    private val upcomingController = UpcomingController()
    private val pastController = PastController()
    private lateinit var handleUpcoming: Button
    private lateinit var handlePast: Button
    private lateinit var superView: View
    private lateinit var mainFragment: Fragment

    var userID = ""
    private var navItemIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        superView = inflater.inflate(R.layout.fragment_your_trip, container, false)
        initializeLayout()

        handleUpcoming.setOnClickListener {
            navItemIndex = 0
            loadFragment()
        }

        handlePast.setOnClickListener {
            navItemIndex = 1
            loadFragment()
        }
        loadFragment()
        return superView
    }

    private fun initializeLayout() {
        handleUpcoming = superView.findViewById(R.id.handleUpcoming)
        handlePast = superView.findViewById(R.id.handlePast)
    }

    private fun dynamicLabelColors() {
        when (navItemIndex) {
            0 -> {
                handleUpcoming.setTextColor(resources.getColor(R.color.colorBlack))
                handlePast.setTextColor(resources.getColor(R.color.colorTextBorder))
            }
            1 -> {
                handleUpcoming.setTextColor(resources.getColor(R.color.colorTextBorder))
                handlePast.setTextColor(resources.getColor(R.color.colorBlack))
            }
        }
    }

    private fun loadFragment() {
        dynamicLabelColors()
        mainFragment = getFragment()
        val fragmentTransaction = tabBarController.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.yourTripFrame, mainFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun getFragment(): Fragment {
        when (navItemIndex) {
            0 -> {
                upcomingController.tabBarController = tabBarController
                upcomingController.userId = this.userID
                return upcomingController
            }
            1 -> {
                pastController.tabBarController = tabBarController
                pastController.userId = this.userID
                return pastController
            }
            else -> {
                upcomingController.tabBarController = tabBarController
                upcomingController.userId = this.userID
                return upcomingController
            }
        }
    }
}
