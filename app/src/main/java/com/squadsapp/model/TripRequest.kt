package com.squadsapp.model

data class TripRequest(var tripId: String, var dateCreated: String, var timeCreated: String, var location: String, var destination: String, var date: String, var time: String, var members: Int, var requestList: List<Member>)