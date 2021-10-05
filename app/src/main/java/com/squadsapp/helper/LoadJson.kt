package com.squadsapp.helper


import android.content.Context
import org.json.JSONArray
import org.json.JSONException

object LoadJson {

    fun loadJsonFromAsset(context: Context, fileName: String): JSONArray? {
        val jsonArray: JSONArray?
        val json: String?
        try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
            jsonArray = JSONArray(json)
        } catch (err: Exception) {
            err.printStackTrace()
            return null
        } catch (err: JSONException) {
            err.printStackTrace()
            return null
        }
        return jsonArray
    }
}