package com.squadsapp.controller

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.squadsapp.R

class NavigateController : Fragment() {

    lateinit var tabBarController: AppCompatActivity

    private val requestingTripController = RequestingTripController()
    private val notificationController = NotificationController()
    private lateinit var handleRequesting: Button
    private lateinit var handleNotification: Button
    private lateinit var superView: View
    private lateinit var mainFragment: Fragment

    var userId = ""
    private var navItemIndex = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        superView = inflater.inflate(R.layout.fragment_navigate, container, false)
        initializeLayout()

        handleRequesting.setOnClickListener {
            navItemIndex = 0
            loadFragment()
        }
        handleNotification.setOnClickListener {
            navItemIndex = 1
            loadFragment()
        }
        loadFragment()
        return superView
    }

    private fun initializeLayout() {
        handleRequesting = superView.findViewById(R.id.handleRequesting)
        handleNotification = superView.findViewById(R.id.handleNotification)
    }

    private fun dynamicLabelColors() {
        when (navItemIndex) {
            0 -> {
                handleNotification.setTextColor(resources.getColor(R.color.colorTextBorder))
                handleRequesting.setTextColor(resources.getColor(R.color.colorBlack))
            }
            1 -> {
                handleNotification.setTextColor(resources.getColor(R.color.colorBlack))
                handleRequesting.setTextColor(resources.getColor(R.color.colorTextBorder))
            }
        }
    }

    private fun loadFragment() {
        dynamicLabelColors()
        mainFragment = getFragment()
        val fragmentTransaction = tabBarController.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.navigateFrame, mainFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun getFragment(): Fragment {
        when (navItemIndex) {
            0 -> {
                requestingTripController.tabBarController = tabBarController
                requestingTripController.userId = this.userId
                return requestingTripController
            }
            1 -> {
                notificationController.tabBarController = tabBarController
                notificationController.userId = this.userId
                return notificationController
            }
            else -> {
                notificationController.tabBarController = tabBarController
                notificationController.userId = this.userId
                return notificationController
            }
        }
    }

}
