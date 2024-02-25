package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.ExecutionException


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService: FirebaseMessagingService() {

        override fun onMessageReceived(remoteMessage: RemoteMessage) {
            Log.d("FCM", "From: ${remoteMessage.from}")
            remoteMessage.notification?.let { it ->
                // Handle the notification here
                val title = it.title
                val body = it.body
                remoteMessage.data.isNotEmpty().let {
                    if (it) {
                        try {
                            // Code that interacts with Google Play services APIs
                            Log.d("MainActivity",remoteMessage.data.toString())
                            val customData1 = remoteMessage.data["requestKey"]
                            val customData2 = remoteMessage.data["uid"]
                            // Pass custom data to the notification function
                            showNotification(title, body,customData1,customData2)
                        } catch (e: ExecutionException) {
                            // Handle the exception
                            Log.e("error found",  e.toString())
                        }
                    }
                }
            }
        }

        private fun showNotification(title: String?, body: String?, key : String?,uid: String?) {
            val intent = Intent(this, welcome::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("requestKey",key)
            intent.putExtra("uid",uid)

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                else
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificationBuilder = NotificationCompat.Builder(this, "channelId")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create Notification Channel (for Android O and above)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "channelId",
                    "Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0, notificationBuilder.build())
        }

}