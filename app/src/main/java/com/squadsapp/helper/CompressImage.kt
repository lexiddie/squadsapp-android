package com.squadsapp.helper

import android.graphics.Bitmap
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream


class CompressImage {

    fun getImageUri(context: Context, imageUri: Uri): Uri {
        val bytes = ByteArrayOutputStream()
        val inImage = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, inImage , null, null)
        return Uri.parse(path)
    }
}