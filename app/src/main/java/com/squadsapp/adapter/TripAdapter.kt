package com.squadsapp.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squadsapp.R
import com.squadsapp.controller.TabBarController
import com.squadsapp.controller.TripInfoController
import com.squadsapp.helper.DisplayDate
import com.squadsapp.model.TripInfo
import kotlinx.android.synthetic.main.trip_cell.view.*

class TripAdapter(val context: Context, val layout: Int, private val tripList: MutableList<TripInfo>): RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    private val tabBarController = context as TabBarController
    private val displayDate = DisplayDate

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val viewCell = LayoutInflater.from(p0.context).inflate(layout, p0, false)
        return ViewHolder(viewCell)
    }

    override fun getItemCount(): Int {
        return tripList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        if (tripList[p1].profileUrl != "Not Defined") {
            Glide.with(tabBarController)
                .load(tripList[p1].profileUrl)
                .apply(RequestOptions().circleCrop())
                .apply(RequestOptions().override(200, 200))
                .into(p0.tripProfileView)
        }
        p0.tripLocation.text = tripList[p1].location
        p0.tripDestination.text = tripList[p1].destination
        p0.tripMember.text = tripList[p1].member.toString()
        p0.tripDate.text = displayDate.updatingDisplay(tripList[p1].date)
        p0.tripTime.text = tripList[p1].time
        p0.itemView.setOnClickListener {
            val intent = Intent(context, TripInfoController::class.java)
            intent.putExtra("userId", tabBarController.userId)
            intent.putExtra("tripId", tripList[p1].id)
            tabBarController.startActivity(intent)
            tabBarController.overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tripLocation = view.txtLocation!!
        val tripDestination = view.txtDestination!!
        val tripMember = view.txtMembers!!
        val tripDate = view.txtDate!!
        val tripTime = view.txtTime!!
        val tripProfileView = view.imgProfileView!!
    }
}