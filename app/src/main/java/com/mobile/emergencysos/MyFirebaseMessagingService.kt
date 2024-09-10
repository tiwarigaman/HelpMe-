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

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Variables for title and body
        var title: String? = null
        var body: String? = null

        // Check if there's a notification payload (title, body)
        remoteMessage.notification?.let {
            title = it.title
            body = it.body
            Log.d("FCM", "Notification payload: Title: $title, Body: $body")
        }

        // Check if there's a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")

            // Extract title and body from data payload if notification payload is missing
            title = remoteMessage.data["title"] ?: title
            body = remoteMessage.data["body"] ?: body

            val customData1 = remoteMessage.data["requestKey"]
            val customData2 = remoteMessage.data["uid"]

            // Show the notification with the extracted title, body, and custom data
            showNotification(title, body, customData1, customData2)
        }
    }


    // Check if the message contains a notification payload
//        remoteMessage.notification?.let {
//            Log.d("FCM", "Message Notification Body: ${it.body}")
//            showNotification(it.title, it.body, null, null)
//        }
//    }

//    private fun showNotification(title: String?, body: String?, key: String?, uid: String?) {
//        val intent = Intent(this, welcome::class.java).apply {
//            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            putExtra("requestKey", key)
//            putExtra("uid", uid)
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            else PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        val notificationBuilder = NotificationCompat.Builder(this, "channelId")
//            .setSmallIcon(R.drawable.app_logo)  // Update with your app's logo
//            .setContentTitle(title)
//            .setContentText(body)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Create Notification Channel for Android O and above
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "channelId",
//                "Channel Name",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        notificationManager.notify(0, notificationBuilder.build())
//    }
    //override fun onMessageReceived(remoteMessage: RemoteMessage) {
    //        Log.d("FCM", "From: ${remoteMessage.from}")
    //        remoteMessage.notification?.let { it ->
    //            // Handle the notification here
    //            val title = it.title
    //            val body = it.body
    //            remoteMessage.data.isNotEmpty().let {
    //                if (it) {
    //                    try {
    //                        // Code that interacts with Google Play services APIs
    //                        Log.d("MainActivity",remoteMessage.data.toString())
    //                        val customData1 = remoteMessage.data["requestKey"]
    //                        val customData2 = remoteMessage.data["uid"]
    //                        // Pass custom data to the notification function
    //                        showNotification(title, body,customData1,customData2)
    //                    } catch (e: ExecutionException) {
    //                        // Handle the exception
    //                        Log.e("error found",  e.toString())
    //                    }
    //                }
    //            }
    //        }
    //    }
    //
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
                .setSmallIcon(R.drawable.app_logo)
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
