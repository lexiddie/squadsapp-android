package com.squadsapp.helper

import java.text.DateFormatSymbols

object DisplayDate {

    fun updatingDisplay(date: String): String {
        val dateSplit = date.split("/")
        val day = dateSplit[0].toInt()
        val month = DateFormatSymbols().months[dateSplit[1].toInt() - 1]
        val year = dateSplit[2].toInt()
        return "$month ${String.format("%02d", day)}, $year"
    }
}