package com.squadsapp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squadsapp.controller.CountryController
import com.squadsapp.model.Country
import kotlinx.android.synthetic.main.country_cell.view.*

class CountryAdapter(val context: Context, val layout: Int, private val countryList: List<Country>): RecyclerView.Adapter<CountryAdapter.ViewHolder>() {

    private val countryController: CountryController = context as CountryController

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val viewCell = LayoutInflater.from(p0.context).inflate(layout, p0, false)
        return ViewHolder(viewCell)
    }

    override fun getItemCount(): Int {
        return countryList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.countryName.text = countryList[p1].name
        p0.itemView.setOnClickListener {
            countryController.country = countryList[p1].name
            countryController.loadDataBack()
            countryController.finish()
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val countryName = view.txtCountry!!
    }
}