package com.mobile.emergencysos

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0
) {
    constructor() : this("", "", 0)
}


