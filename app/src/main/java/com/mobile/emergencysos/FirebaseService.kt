package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
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
                Log.d("FirebaseService", "Received value from Firebase: $value")
                showCustomNotification(value)
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

    @SuppressLint("LaunchActivityFromNotification")
    private fun showCustomNotification(value: String?) {
        // Create an explicit intent for a BroadcastReceiver in your app
        Log.d("FirebaseService", "showCustomNotification called with value: $value")

        if (value.isNullOrEmpty()) {
            Log.d("FirebaseService", "Value from Firebase is null or empty.")
            return
        }
        val intent = Intent("com.mobile.emergencysos.POPUP_ACTION").apply {
            putExtra("message", value) // Pass the message to the PopupReceiver
        }

        // Add FLAG_IMMUTABLE to the PendingIntent
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "channelId")
            .setContentTitle("Firebase Update")
            .setContentText(value)
            .setSmallIcon(R.drawable.baseline_notification_24)
            .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
            .setAutoCancel(true) // Automatically removes the notification when the user taps it
            .build()


        Log.d("FirebaseService", "Creating and showing notification...")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)
    }


}