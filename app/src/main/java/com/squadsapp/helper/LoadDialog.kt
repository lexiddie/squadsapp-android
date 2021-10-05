package com.squadsapp.helper

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.squadsapp.R
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class LoadDialog() {

    private lateinit var dialog: Dialog

    fun show(context: Context) {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_loading)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        val gifImageView = dialog.findViewById(R.id.custom_loading_imageView) as ImageView
        Glide.with(context)
            .load(R.drawable.loading)
            .apply(RequestOptions().circleCrop())
            .into(gifImageView)
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}