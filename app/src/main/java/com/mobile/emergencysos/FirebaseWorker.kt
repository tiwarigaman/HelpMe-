package com.mobile.emergencysos

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.*

class FirebaseWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("notification")

    override fun doWork(): Result {
        // Fetch data from Firebase
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

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}
