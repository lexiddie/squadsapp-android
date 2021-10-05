package com.squadsapp.helper

import android.app.AlertDialog
import android.content.Context
import android.support.v4.app.Fragment
import com.squadsapp.controller.*

object DialogAlert {

    fun showCheckProfileSetup(context: Context) {
        val dialog = AlertDialog.Builder(context)
        val controller = context as SetupProfileController
        dialog.setCancelable(true)
        dialog.setTitle("Upload Profile Photo")
        dialog.setMessage("Do you want to choose new photo or remove?")
        dialog.setPositiveButton("Choose") { _, _ ->
            controller.handleChooseProfile()
        }.setNegativeButton("Remove") { _, _ ->
            controller.handleRemoveProfile()
        }
        val alert = dialog.create()
        alert.show()
    }

    fun showCheckEditProfile(context: Context) {
        val dialog = AlertDialog.Builder(context)
        val controller = context as EditProfileController
        dialog.setCancelable(true)
        dialog.setTitle("Upload Profile Photo")
        dialog.setMessage("Do you want to choose new photo or remove?")
        dialog.setPositiveButton("Choose") { _, _ ->
            controller.handleSelectPhoto()
        }.setNeutralButton("Remove") {_, _ ->
            controller.handleRemoveProfile()
        }.setNegativeButton("Cancel") { _, _ ->

        }
        val alert = dialog.create()
        alert.show()
    }

    fun showLogOut(context: Context) {
        val dialog = AlertDialog.Builder(context)
        val controller = context as TabBarController
        dialog.setCancelable(true)
        dialog.setTitle("Log out")
        dialog.setMessage("Would you like to log out?")
        dialog.setPositiveButton("Log out") { _, _ ->
            controller.handleLogoutUser()
        }.setNegativeButton("Cancel") { _, _ ->

        }
        val alert = dialog.create()
        alert.show()
    }

    fun showLeaveTrip(context: TripInfoController) {
        val dialog = AlertDialog.Builder(context)
        dialog.setCancelable(true)
        dialog.setTitle("Leave trip")
        dialog.setMessage("Would you like to leave this trip?")
        dialog.setPositiveButton("Leave") { _, _ ->
            context.handleInitiateLeave()
        }.setNegativeButton("Cancel") { _, _ ->
            context.loadDialog.dismiss()
        }
        val alert = dialog.create()
        alert.show()
    }

    fun showDeleteTrip(context: TripInfoController) {
        val dialog = AlertDialog.Builder(context)
        dialog.setCancelable(true)
        dialog.setTitle("Delete trip")
        dialog.setMessage("Would you like to delete this trip?")
        dialog.setPositiveButton("Delete") { _, _ ->
            context.handleInitiateDelete()
        }.setNegativeButton("Cancel") { _, _ ->
            context.loadDialog.dismiss()
        }
        val alert = dialog.create()
        alert.show()
    }

    fun showGender(context: PrivateInfoController, gender: String) {
        val genderList = arrayOf("Male", "Female", "Not Defined")
        var mainIndex = 0
        for (i in 0 until genderList.size) {
            if (genderList[i] == gender) {
                mainIndex = i
            }
        }
        val dialog = AlertDialog.Builder(context)
        dialog.setCancelable(true)
        dialog.setTitle("Choose Gender")
        dialog.setSingleChoiceItems(genderList, mainIndex) { _, i ->
            mainIndex = i
        }
        dialog.setPositiveButton("Select") { _, _ ->
            context.txtGender.setText(genderList[mainIndex])
        }.setNegativeButton("Cancel") { _, _ ->

        }
        val alert = dialog.create()
        alert.show()
    }

    fun showSharedType(context: TripInfoController, sharedType: String) {
        val sharedTypeList = arrayOf("Private", "Public")
        var mainIndex = 0
        for (i in 0 until sharedTypeList.size) {
            if (sharedTypeList[i] == sharedType) {
                mainIndex = i
            }
        }
        val dialog = AlertDialog.Builder(context)
        dialog.setCancelable(true)
        dialog.setTitle("Change SharedType")
        dialog.setSingleChoiceItems(sharedTypeList, mainIndex) { _, i ->
            mainIndex = i
        }
        dialog.setPositiveButton("Select") { _, _ ->
            context.initiateSharedType(sharedTypeList[mainIndex])
        }.setNegativeButton("Cancel") { _, _ ->

        }
        val alert = dialog.create()
        alert.show()
    }

    fun showTripType(context: CreateTripController) {
        val sharedTypeList = arrayOf("Private", "Public")
        var mainIndex = 1
        val dialog = AlertDialog.Builder(context)
        dialog.setCancelable(true)
        dialog.setTitle("Setup SharedType")
        dialog.setSingleChoiceItems(sharedTypeList, mainIndex) { _, i ->
            mainIndex = i
        }
        dialog.setPositiveButton("Select") { _, _ ->
            context.handleCreateTrip(sharedTypeList[mainIndex])
        }.setNegativeButton("Cancel") { _, _ ->

        }
        val alert = dialog.create()
        alert.show()
    }

    fun showRequestTrip(context: TripInfoController) {
        val dialog = AlertDialog.Builder(context)
        dialog.setCancelable(true)
        dialog.setTitle("Requesting private trip")
        dialog.setMessage("Would you like to request this trip?")
        dialog.setPositiveButton("Confirm") { _, _ ->
            context.handleRequest()
        }.setNegativeButton("Cancel") { _, _ ->
            context.loadDialog.dismiss()
        }
        val alert = dialog.create()
        alert.show()
    }

    fun showCancelRequest(context: TripInfoController) {
        val dialog = AlertDialog.Builder(context)
        dialog.setCancelable(true)
        dialog.setTitle("Cancel request trip")
        dialog.setMessage("Would you like to cancel this request?")
        dialog.setPositiveButton("Confirm") { _, _ ->
            context.handleInitiateCancel()
        }.setNegativeButton("Cancel") { _, _ ->
            context.loadDialog.dismiss()
        }
        val alert = dialog.create()
        alert.show()
    }

    fun showSortBy(context: TabBarController, homeController: HomeController, sortedIndex: Int) {
        val sortByList = arrayOf("Default", "Date Created: Latest", "Date Created: Earliest", "Location: Nearby")
        var mainIndex = 0
        for (i in 0 until sortByList.size) {
            if (i == sortedIndex) {
                mainIndex = i
            }
        }
        val dialog = AlertDialog.Builder(context)
        dialog.setCancelable(true)
        dialog.setTitle("Sort By")
        dialog.setSingleChoiceItems(sortByList, mainIndex) { _, i ->
            mainIndex = i
        }
        dialog.setPositiveButton("Select") { _, _ ->
            context.sortedIndex = mainIndex
            context.handleSavingSortedIndex()
            homeController.refreshAdapter()
        }.setNegativeButton("Cancel") { _, _ ->

        }
        val alert = dialog.create()
        alert.show()
    }
}