package com.squadsapp.controller

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.google.gson.GsonBuilder
import com.squadsapp.R
import com.squadsapp.adapter.CountryAdapter
import com.squadsapp.helper.LoadJson
import com.squadsapp.model.Country
import kotlinx.android.synthetic.main.activity_country.*

class CountryController : AppCompatActivity() {

    var country = ""
    private var loadJson = LoadJson
    private var countryList = mutableListOf<Country>()

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
        setContentView(R.layout.activity_country)
        getCountryList()
        val countryListView = handleCountryView
        countryListView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val adapter = CountryAdapter(this, R.layout.country_cell, countryList)
        countryListView.adapter = adapter
    }

    private fun getCountryList() {
        val gSon = GsonBuilder().create()
        val json = loadJson.loadJsonFromAsset(this, "Json/CountriesData.json")
        if (json != null) {
            val size = json.length()
            var count = 0
            while (count < size) {
                val element = json.getJSONObject(count)
                val country = gSon.fromJson(element.toString(), Country::class.java)
                count += 1
                countryList.add(country)
            }
        }
    }

    fun loadDataBack() {
        val result = Intent()
        result.putExtra("country", country)
        setResult(Activity.RESULT_OK, result)
    }

    fun handleDismiss(view: View) {
        finish()
    }


}
