package com.mobile.emergencysos

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PopupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle notification click here
        val message = intent?.getStringExtra("message")
        Log.d("PopupReceiver", "Received broadcast")

        // You can open a dialog or perform any action based on the notification content
        if (context != null && message != null) {
            // Example: Show a dialog with the message
            Log.d("PopupReceiver", "Context and message are not null")
            AlertDialog.Builder(context)
                .setTitle("Notification Clicked")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
