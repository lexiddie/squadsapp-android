package com.squadsapp.helper

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.google.gson.GsonBuilder
import com.squadsapp.model.CountryData

class CountryInfo {

    private val loadJson = LoadJson
    private val gson = GsonBuilder().create()

    private fun getCarrierCountry(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryISO = telephonyManager.simCountryIso
        return if (countryISO is String) {
            countryISO.toUpperCase()
        } else {
            ""
        }
    }

    fun getCountry(context: Context): String {
        val defaultCountry = "Thailand"
        val country = getCarrierCountry(context)
        val json = loadJson.loadJsonFromAsset(context, "Json/CountriesData.json")
        if (country != "") {
            for (i in 0 until json!!.length()) {
                val element = json.getJSONObject(i)
                val countryMobile = gson.fromJson(element.toString(), CountryData::class.java)
                if (countryMobile.code == country) {
                    return countryMobile.name
                }
            }
        }
        return defaultCountry
    }
}