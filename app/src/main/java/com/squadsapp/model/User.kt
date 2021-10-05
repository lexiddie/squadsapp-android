package com.squadsapp.model

class User(var id: String, var name: String, var username: String, var phoneNumber: String, var email: String, var password: String, var country: String, var profileUrl: String, var rate: Double, var totalTrip: Int, var gender: String) {
    constructor() : this ("", "", "", "", "", "", "", "", 0.0, 0, "")
}