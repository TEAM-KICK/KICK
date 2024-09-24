package com.example.kick.alarm

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    var isActive: Boolean
)
