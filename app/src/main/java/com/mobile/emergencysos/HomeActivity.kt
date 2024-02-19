package com.mobile.emergencysos

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.ClipData.Item
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var requestRefer: DatabaseReference
    private val handler = Handler(Looper.getMainLooper())
    private val intervalMillis = 5000
    private lateinit var progressBar2 : ProgressBar
    private lateinit var statusTextView: TextView

    @SuppressLint("MissingInflatedId", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        startRepeatingTask()
        statusTextView = findViewById(R.id.statusTextView)

        database = FirebaseDatabase.getInstance().reference.child("users")
        requestRefer = FirebaseDatabase.getInstance().reference.child("request")

        progressBar2 = findViewById(R.id.progressBar)
        progressBar2.visibility = View.GONE




//        progressBar.visibility = View.VISIBLE
//
//        handler2.post(object : Runnable {
//            override fun run() {
//                // Update your UI here
//
//                progressBar.visibility = View.GONE
//                // Schedule the next update after the specified interval
//                handler.postDelayed(this, 6000)
//            }
//        })

        findViewById<TextView>(R.id.helpMe).setOnClickListener {
            showConfirmationDialog()
        }
    }
    private fun startRepeatingTask() {
        // Post the initial task with a delay
        handler.postDelayed(runnable, intervalMillis.toLong())
    }

    private val runnable = object : Runnable {
        override fun run() {
            // Call your function here
            checkRequests()
            // Repeat the task after the specified interval
            handler.postDelayed(this, intervalMillis.toLong())
        }
    }


    private fun checkRequests() {
        requestRefer.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    if (userSnapshot.hasChild("confirm") && userSnapshot.hasChild("complete")) {
                        val isConfirm = userSnapshot.child("confirm").getValue(Boolean::class.java)
                        val isComplete = userSnapshot.child("complete").getValue(Boolean::class.java)

                        if (isComplete == false && isConfirm == false) {
                            handleHelpRequest(userSnapshot)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        database.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("key")) {
                    handleUserRequest(snapshot)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun handleHelpRequest(userSnapshot: DataSnapshot) {
        val key = userSnapshot.key.toString()
        showCustomNotification("Someone needs help in your area", key)
        Toast.makeText(this@HomeActivity, key, Toast.LENGTH_SHORT).show()
        Log.d("hehe", userSnapshot.toString())
    }

    private fun handleUserRequest(snapshot: DataSnapshot) {
        val keyValue = snapshot.child("key").value.toString()

        requestRefer.child(keyValue).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("confirm").getValue(Boolean::class.java) == true && snapshot.hasChild("helper")) {
                    startChatActivity(keyValue)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showConfirmationDialog() {


//        val threatLevels = arrayOf("Emergency Situation", "Need Help ASAP", "Other")
//
//        val builder2 = AlertDialog.Builder(this)
//        builder2.setTitle("Please select threat level:")
//            .setItems(threatLevels) { _, which ->
//                // Handle the selected option
//                val selectedThreatLevel = threatLevels[which]
//                // You can perform actions based on the selected threat level
//            }
//            .setPositiveButton("OK") { dialog, _ ->
//                // Handle the "OK" button click if needed
//                findViewById<TextView>(R.id.helpMe).text = "Waiting..."
//                progressBar2.visibility = View.VISIBLE
//                startFirstAnimation()
//                updateDatabase(requestKey)
//                showCustomNotification("Someone needs help in your area", requestKey.toString())
//                dialog.dismiss()
//            }
//
//        val dialog = builder2.create()
//        dialog.show()



        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_layout)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)

        // Set up "OK" button click listener
        btnOk.setOnClickListener {
            // Get the selected threat level
            val requestRef = requestRefer.push()
            val requestKey = requestRef.key
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            val selectedRadioButton = dialog.findViewById<RadioButton>(selectedRadioButtonId)

            if (selectedRadioButton != null) {
                val selectedThreatLevel = selectedRadioButton.text.toString()
                findViewById<TextView>(R.id.helpMe).text = "Waiting..."
                progressBar2.visibility = View.VISIBLE
                startFirstAnimation()
                updateDatabase(requestKey)
//                showCustomNotification("Someone needs help in your area", requestKey.toString())
                showFCMNotification("Someone needs help in your area", requestKey.toString())
            }
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        // Show the dialog
        dialog.show()

    }

    private fun showFCMNotification(Message: String, request_key: String) {

        var fcmTokens : MutableList<String> = mutableListOf()
        val database = FirebaseDatabase.getInstance().reference
        val usersRef = database.child("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (userSnapshot in snapshot.children) {
                    val token = userSnapshot.child("fcmToken").getValue(String::class.java)
                    token?.let { fcmTokens.add(it) }
                }

                // Now 'fcmTokens' contains all FCM tokens for your users
                // You can use this list to send notifications
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })

        val jsonObject = JSONObject()
        val notificationJSONObject = JSONObject()

        // Add key-value pairs
        notificationJSONObject.put("title", "Emergency")
        notificationJSONObject.put("body", Message)


        jsonObject.put("notification",notificationJSONObject)
        jsonObject.put("data",request_key)
        jsonObject.put("to",fcmTokens)

        // Convert JSON object to a string
        val json = jsonObject.toString()
        val serverUrl = "https://fcm.googleapis.com/fcm/send"

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url(serverUrl)
            .addHeader("Authorization", "key=AAAASZeQF-4:APA91bG4DKeJKFUAyo9fSOywzXJgzIeo05XF2vONGOD-WdNSDXlNVvyXBQoShbESGojiJ7AwVtX5CcIYtSKHaVqIcBL19rhErbFI6k7CT1TW-axBiZYtf4IqqnJdCmXj-NF95-a0KBUs")
            .post(requestBody)
            .build()

        // Execute HTTP request asynchronously
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "Failed to send message to server: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("MainActivity", "Message sent successfully")
                // Handle the server's response if needed
            }
        })

    }

    private fun startFirstAnimation() {
        val fadeOutAnimator = ObjectAnimator.ofFloat(statusTextView, View.ALPHA, 1f, 0f)
        fadeOutAnimator.duration = 500 // Set the duration in milliseconds

        // Set a listener for the end of the animation
        fadeOutAnimator.addListener(object : AnimatorListenerAdapter() {
            @SuppressLint("SetTextI18n")
            override fun onAnimationEnd(animation: Animator) {
                // Change the text and start the second animation
                statusTextView.text = "Recording your situation..."
                startSecondAnimation()
            }
        })

        fadeOutAnimator.start()
    }

    private fun startSecondAnimation() {
        val fadeInAnimator = ObjectAnimator.ofFloat(statusTextView, View.ALPHA, 0f, 1f)
        fadeInAnimator.duration = 1000 // Set the duration in milliseconds

        // Set a listener for the end of the animation
        fadeInAnimator.addListener(object : AnimatorListenerAdapter() {
            @SuppressLint("SetTextI18n")
            override fun onAnimationEnd(animation: Animator) {
                statusTextView.text = "Waiting for approval..."
                // Animation ended, you can perform additional actions if needed
            }
        })

        // Make the TextView visible before starting the animation
        statusTextView.visibility = View.VISIBLE
        fadeInAnimator.start()
    }

    private fun updateDatabase(requestKey: String?) {
        val updateMap = mapOf(
            "confirm" to false,
            "complete" to false,
            "victim" to FirebaseAuth.getInstance().currentUser?.uid.toString()
        )
        requestRefer.child(requestKey.toString()).updateChildren(updateMap)

        val map = mapOf("key" to requestKey)
        database.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).updateChildren(map)
    }

    private fun startChatActivity(keyValue: String) {
        val intent = Intent(this@HomeActivity, ChatActivity::class.java)
        intent.putExtra("requestKey", keyValue)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showCustomNotification(value: String?, key: String) {


        val intent = Intent(this, welcome::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("requestKey", key)
        Log.d("singleHEHE", key)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "channelId")
            .setContentTitle("Emergency")
            .setContentText(value)
            .setSmallIcon(R.drawable.baseline_notification_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        createNotificationChannel()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "channelId"
            val channelName = "Channel Name"
            val channelDescription = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun stopRepeatingTask() {
        // Remove any callbacks to stop the repeating task
        handler.removeCallbacks(runnable)
    }
    override fun onDestroy() {
        super.onDestroy()
        stopRepeatingTask()
    }
}