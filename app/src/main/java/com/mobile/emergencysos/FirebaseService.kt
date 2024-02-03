package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class FirebaseService : Service() {

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("notification")

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "channelId")
            .setContentTitle("Firebase Listener")
            .setContentText("Listening for changes...")
            .setSmallIcon(R.drawable.baseline_notification_24)
            .build()

        startForeground(1, notification)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                showNotification(value)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "channelId",
                "Firebase Listener Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun showNotification(value: String?) {
        val notification = NotificationCompat.Builder(this, "channelId")
            .setContentTitle("Firebase Update")
            .setContentText(value)
            .setSmallIcon(R.drawable.baseline_notification_24)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)
    }
}
