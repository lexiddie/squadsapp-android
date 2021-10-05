package com.squadsapp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squadsapp.controller.TripInfoController
import com.squadsapp.model.MemberInfo
import kotlinx.android.synthetic.main.member_cell.view.*

class MemberAdapter(val context: Context, val layout: Int, private val memberList: List<MemberInfo>): RecyclerView.Adapter<MemberAdapter.ViewHolder>() {

    private val tripInfoController = context as TripInfoController

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val viewCell = LayoutInflater.from(p0.context).inflate(layout, p0, false)
        return ViewHolder(viewCell)
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        if (memberList[p1].profileUrl != "Not Defined") {
            Glide.with(tripInfoController)
                .load(memberList[p1].profileUrl)
                .apply(RequestOptions().circleCrop())
                .apply(RequestOptions().override(200, 200))
                .into(p0.memberProfile)
        }
        p0.memberNo.text = (memberList.size - (p1)).toString()
        p0.memberName.text = memberList[p1].name
        p0.memberPhoneNumber.text = memberList[p1].phoneNumber
        p0.memberRate.text = String.format("%.2f", memberList[p1].rate)
        p0.memberTotalTrip.text = memberList[p1].totalTrip.toString()

    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val memberNo = view.txtMemberNo!!
        val memberName = view.txtMemberName!!
        val memberPhoneNumber = view.txtMemberPhoneNumber!!
        val memberRate = view.txtMemberRate!!
        val memberTotalTrip = view.txtMemberTotalTrip!!
        val memberProfile = view.imgMember!!
    }
}