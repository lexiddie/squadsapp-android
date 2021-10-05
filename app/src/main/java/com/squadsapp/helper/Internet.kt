package com.squadsapp.helper

import android.content.Context
import android.net.ConnectivityManager

object Internet {

    fun isConnected(context: Context):Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null
    }
}