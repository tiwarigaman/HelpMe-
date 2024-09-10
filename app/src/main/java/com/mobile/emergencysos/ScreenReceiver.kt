package com.mobile.emergencysos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenReceiver"
        private var pressCount = 0
        private const val MAX_PRESS_COUNT = 4
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                pressCount++
                Log.d(TAG, "Power button pressed $pressCount times")
                if (pressCount >= MAX_PRESS_COUNT) {
                    pressCount = 0  // Reset counter
                    triggerAppAction(context)
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                // Reset counter if needed or handle accordingly
                pressCount = 0
            }
        }
    }

    private fun triggerAppAction(context: Context) {
        // Trigger action, e.g., simulate button click
        val intent = Intent(context, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

