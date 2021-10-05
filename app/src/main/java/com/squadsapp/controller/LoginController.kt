package com.squadsapp.controller

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squadsapp.R
import com.squadsapp.helper.Internet
import com.squadsapp.helper.Keyboard
import com.squadsapp.helper.LoadDialog
import com.squadsapp.helper.SingleToast
import com.squadsapp.model.User


class LoginController : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance().getReference("User")

    private lateinit var txtUsername: EditText
    private lateinit var txtPassword: EditText
    private val keyboard = Keyboard
    private val singleToast = SingleToast
    private val internet = Internet
    private val loadingDialog = LoadDialog()
    private lateinit var standard: SharedPreferences
    private var sortedIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        viewLayout()
        standard = this.getSharedPreferences("login", 0)
        sortedIndex = standard.getInt("sortedIndex", 0)
    }

    private fun viewLayout() {
        txtUsername = findViewById(R.id.txtUsername)
        txtPassword = findViewById(R.id.txtPassowrd)
    }

    fun handleLogin(view: View) {
        loadingDialog.show(this)
        if (!internet.isConnected(this)) {
            singleToast.show(this, "The internet connection is unavailable", Toast.LENGTH_LONG)
            loadingDialog.dismiss()
            return
        } else if (txtUsername.text.isNullOrEmpty() || txtPassword.text.isNullOrEmpty()) {
            singleToast.show(this, "Username or Password must not be empty", Toast.LENGTH_LONG)
            loadingDialog.dismiss()
            return
        } else {
            userRef.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (i in p0.children) {
                        val user = i.getValue(User::class.java)!!
                        if (txtUsername.text.toString().toLowerCase() == user.username && txtPassword.text.toString() == user.password) {
                            singleToast.show(applicationContext, "Login successful", Toast.LENGTH_LONG)
                            val intent = Intent(applicationContext, TabBarController::class.java)
                            intent.putExtra("userId", user.id)
                            intent.putExtra("userCountry", user.country)
                            intent.putExtra("userProfileUrl", user.profileUrl)
                            intent.putExtra("sortedIndex", sortedIndex)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
                            loadingDialog.dismiss()
                            return
                        }
                    }
                    loadingDialog.dismiss()
                    singleToast.show(applicationContext, "Username or Password is Incorrect", Toast.LENGTH_LONG)
                    return
                }

            })
        }
    }

    fun handleSignUp(view: View) {
        val intent = Intent(this, SignUpController::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.right_to_left_open, R.anim.right_to_left_close)
    }

    fun handleKeyboard(view: View) {
        keyboard.hideKeyboard(this)
    }

}
