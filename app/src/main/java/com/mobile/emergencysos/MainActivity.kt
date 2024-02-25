package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        databaseReference.addValueEventListener(object :  ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
//                val value = dataSnapshot.getValue(String::class.java)

                if (snapshot.hasChild("content")){
                    val newValue : String = snapshot.child("content").value.toString()

                    Toast.makeText(this@MainActivity, newValue, Toast.LENGTH_LONG).show()
                    showCustomNotification(newValue)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun showCustomNotification(value: String?) {
        // Create an explicit intent for a BroadcastReceiver in your app
        val intent = Intent("com.mobile.emergencysos.POPUP_ACTION").apply {
            putExtra("message", value) // Pass the message to the PopupReceiver
        }
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, "channelId")
            .setContentTitle("Firebase Update")
            .setContentText(value)
            .setSmallIcon(R.drawable.baseline_notification_24)
            .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
            .setAutoCancel(true) // Automatically removes the notification when the user taps it
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)

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

//    override fun onDestroy() {
//        // Unregister the BroadcastReceiver and ValueEventListener to avoid memory leaks
//        unregisterReceiver(receiver)
//        databaseReference.removeEventListener(valueEventListener)
//
//        super.onDestroy()
//    }

}

