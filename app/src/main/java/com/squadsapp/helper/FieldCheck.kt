package com.squadsapp.helper

object FieldCheck {

    fun checkName(name: String): Boolean {
        val toChar = name.toCharArray()
        var count = 0
        for (i in toChar) {
            if (i in '0'..'9' || i in 'a'..'z' || i in 'A'..'Z' || i == ' ') {
                count++
            }
        }
        if (toChar.size < 3) {
            return false
        } else if (count == toChar.size) {
            return true
        }
        return false
    }

    fun checkPhoneNumber(number: String): Boolean {
        val toChar = number.toCharArray()
        var count = 0
        for (i in toChar) {
            if (i in '0'..'9') {
                count++
            }
        }
        if (toChar.size != 9 && toChar.size != 10 || count != toChar.size) {
            return false
        }
        return true
    }

    fun checkUsername(username: String): Boolean {
        val toChar = username.toLowerCase().toCharArray()
        var countLetter = 0
        var countNumber = 0
        for (i in toChar) {
            if (i in '0'..'9') {
                countNumber++
            } else if (i in 'a'..'z') {
                countLetter++
            }
        }
        if (toChar.size < 3) {
            return false
        } else if (countLetter == 0 || countLetter < 3) {
            return false
        } else if ((countLetter + countNumber) == toChar.size) {
            return true
        }
        return false
    }
}