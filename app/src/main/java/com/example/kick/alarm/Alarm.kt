package com.example.kick.alarm

import org.json.JSONObject

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    var isActive: Boolean
) {
    fun toJson(): String {
        val jsonObject = JSONObject()
        jsonObject.put("id", id)
        jsonObject.put("hour", hour)
        jsonObject.put("minute", minute)
        jsonObject.put("isActive", isActive)
        return jsonObject.toString()
    }

    companion object {
        fun fromJson(json: String): Alarm {
            val jsonObject = JSONObject(json)
            return Alarm(
                jsonObject.getInt("id"),
                jsonObject.getInt("hour"),
                jsonObject.getInt("minute"),
                jsonObject.getBoolean("isActive")
            )
        }
    }
}
