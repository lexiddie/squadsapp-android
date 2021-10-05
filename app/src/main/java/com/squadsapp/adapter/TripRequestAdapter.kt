package com.squadsapp.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squadsapp.R
import com.squadsapp.controller.MemberRequestController
import com.squadsapp.controller.TripRequestController
import com.squadsapp.helper.DisplayDate
import com.squadsapp.model.TripRequest
import kotlinx.android.synthetic.main.trip_request_cell.view.*

class TripRequestAdapter(val context: Context, val layout: Int, private val memberRequestList: List<TripRequest>): RecyclerView.Adapter<TripRequestAdapter.ViewHolder>() {

    private val tripRequestController = context as TripRequestController
    private val displayDate = DisplayDate

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val viewCell = LayoutInflater.from(p0.context).inflate(layout, p0, false)
        return ViewHolder(viewCell)
    }

    override fun getItemCount(): Int {
        return memberRequestList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.tripLocation.text = memberRequestList[p1].location
        p0.tripDestination.text = memberRequestList[p1].destination
        p0.tripAvailable.text = memberRequestList[p1].members.toString()
        p0.tripDate.text = displayDate.updatingDisplay(memberRequestList[p1].date)
        p0.tripTime.text = memberRequestList[p1].time
        p0.tripRequesting.text = memberRequestList[p1].requestList.size.toString()
        p0.itemView.setOnClickListener {
            val intent = Intent(tripRequestController, MemberRequestController::class.java)
            intent.putExtra("userId", tripRequestController.userId)
            intent.putExtra("tripId", memberRequestList[p1].tripId)
            intent.putExtra("tripLocation", memberRequestList[p1].location)
            intent.putExtra("tripDestination", memberRequestList[p1].destination)
            tripRequestController.startActivity(intent)
            tripRequestController.overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tripLocation = view.txtLocation!!
        val tripDestination = view.txtDestination!!
        val tripDate = view.txtDate!!
        val tripTime = view.txtTime!!
        val tripAvailable = view.txtAvailable!!
        val tripRequesting = view.txtRequesting!!
    }
}