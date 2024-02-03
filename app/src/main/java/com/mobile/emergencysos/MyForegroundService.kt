package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class MyForegroundService : Service() {

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("notification")

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                // You can do something with the fetched value here
                val notificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Create NotificationCompat.Builder
                val notificationBuilder = NotificationCompat.Builder(applicationContext, "channalid1")
                    .setSmallIcon(R.drawable.baseline_notification_24)
                    .setContentTitle("Simple Notification")
                    .setContentText(value)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Notify with a unique notification ID
                notificationManager.notify(0, notificationBuilder.build())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
        databaseReference.keepSynced(true)

        // Your background task goes here

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}

