package com.squadsapp.helper

import android.content.Context
import android.widget.Toast

object SingleToast {

    private var toast: Toast? = null

    fun show(context: Context, text: String, duration: Int) {
        if (toast != null) toast!!.cancel()
        toast = Toast.makeText(context, text, duration)
        toast!!.show()
    }
}