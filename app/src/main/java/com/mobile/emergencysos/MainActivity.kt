package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("notification")
    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Handle data changes
            val value = dataSnapshot.getValue(String::class.java)

            // Trigger custom notification
            if (value != null) {
                Toast.makeText(this@MainActivity, value, Toast.LENGTH_LONG).show()
                showCustomNotification(value)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Handle errors
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Handle the database change, and show the custom notification
            databaseReference.addValueEventListener(valueEventListener)
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register ValueEventListener
//        val myWorkRequest = OneTimeWorkRequestBuilder<FirebaseWorker>().build()
//        WorkManager.getInstance(this).enqueue(myWorkRequest)
//        databaseReference.addValueEventListener(valueEventListener)
//
//        val filter = IntentFilter("database_change")
//        registerReceiver(receiver, filter, RECEIVER_EXPORTED)


//        val serviceIntent2 = Intent(this, MyForegroundService::class.java)
//        startService(serviceIntent2)

        val serviceIntent = Intent(this, FirebaseService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

    }

    private fun showCustomNotification(message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create NotificationCompat.Builder
        val notificationBuilder = NotificationCompat.Builder(this, "channalid1")
            .setSmallIcon(R.drawable.baseline_notification_24)
            .setContentTitle("Simple Notification")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Notify with a unique notification ID
        notificationManager.notify(0, notificationBuilder.build())
    }


    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel("channalid1")
            Log.d("MainActivity", "Channel Importance: ${channel?.importance}")
            channel?.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            true
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotificationPermissionDialog() {
        Log.d("MainActivity", "Showing notification permission dialog")

        runOnUiThread {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Notification Permission")
                .setMessage("Notification permission is required to show notifications. Would you like to open settings?")
                .setPositiveButton("Yes") { _, _ ->
                    openNotificationSettings()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

            val alertDialog: AlertDialog = builder.create()

            alertDialog.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where the settings activity is not found
            // You can provide an alternative action here, such as showing a dialog or a Toast
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        // Unregister the BroadcastReceiver and ValueEventListener to avoid memory leaks
        unregisterReceiver(receiver)
        databaseReference.removeEventListener(valueEventListener)

        super.onDestroy()
    }

}

