package com.squadsapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squadsapp.R
import com.squadsapp.controller.InspectProfileController
import com.squadsapp.controller.MemberRequestController
import com.squadsapp.model.Member
import com.squadsapp.model.MemberInfo
import com.squadsapp.model.Trip
import kotlinx.android.synthetic.main.member_request_cell.view.*

class RequestAdapter(val context: Context, private val tripId: String, val layout: Int, private val requestList: List<MemberInfo>):RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

    private val tripRef = FirebaseDatabase.getInstance().getReference("Trip")
    private val memberRequestController = context as MemberRequestController

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val viewCell = LayoutInflater.from(p0.context).inflate(layout, p0, false)
        return ViewHolder(viewCell)
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, @SuppressLint("RecyclerView") p1: Int) {
        val memberId = requestList[p1].id
        p0.txtName.text = requestList[p1].name
        if (requestList[p1].profileUrl != "Not Defined") {
            Glide.with(memberRequestController)
                .load(requestList[p1].profileUrl)
                .apply(RequestOptions().circleCrop())
                .apply(RequestOptions().override(200, 200))
                .into(p0.imgMember)
        }
        p0.handleConfirm.setOnClickListener {
            tripRef.child(tripId).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val trip = p0.getValue(Trip::class.java)!!
                    if (trip.members == 0) {
                        memberRequestController.singleToast.show(memberRequestController, "You cannot add more member.", Toast.LENGTH_LONG)
                        return
                    }
                    val memberList = trip.memberList as MutableList
                    memberList.add(Member(memberId))
                    trip.memberList = memberList
                    trip.members -= 1
                    tripRef.child(tripId).child("memberList").setValue(trip.memberList)
                    tripRef.child(tripId).child("members").setValue(trip.members)
                    tripRef.child(tripId).child("requestList").addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                        }
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                val tempList = mutableListOf<Member>()
                                for (i in p0.children.iterator()) {
                                    val member = i.getValue(Member::class.java)!!
                                    if (member.id != memberId) {
                                        tempList.add(member)
                                    }
                                }
                                tripRef.child(tripId).child("requestList").setValue(tempList)
                                memberRequestController.singleToast.show(memberRequestController, "This user has been added to trip", Toast.LENGTH_LONG)
                                memberRequestController.getMemberList()
                            }
                        }

                    })
                }
            })

        }
        p0.handleDelete.setOnClickListener {
            tripRef.child(tripId).child("requestList").addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val tempList = mutableListOf<Member>()
                        for (i in p0.children.iterator()) {
                            val member = i.getValue(Member::class.java)!!
                            if (member.id != memberId) {
                                tempList.add(member)
                            }
                        }
                        tripRef.child(tripId).child("requestList").setValue(tempList)
                        memberRequestController.singleToast.show(memberRequestController, "This user's request has been deleted", Toast.LENGTH_LONG)
                        memberRequestController.getMemberList()
                    }
                }

            })
        }
        p0.itemView.setOnClickListener {
            val indent = Intent(memberRequestController, InspectProfileController::class.java)
            indent.putExtra("tripId", tripId)
            indent.putExtra("memberId", requestList[p1].id)
            memberRequestController.startActivity(indent)
            memberRequestController.overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
        }
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgMember = view.imgMember!!
        val txtName = view.txtName!!
        val handleConfirm = view.handleConfirm!!
        val handleDelete = view.handleDelete!!
    }
}