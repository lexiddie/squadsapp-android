package com.squadsapp.model

class Trip(var id: String, var dateCreated: String, var timeCreated: String, var sharedType: String, var country: String, var isLocked: Boolean, var isEnded: Boolean, var location: String, var destination: String, var date: String, var time: String, var announcement: String, var members: Int, var memberList: List<Member>, var tripLocation: Location) {
    constructor() : this ("", "", "", "", "",false, false,"", "", "", "", "",0 , emptyList(), Location())
}