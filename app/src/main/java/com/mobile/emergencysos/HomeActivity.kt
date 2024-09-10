package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.messaging
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

@Suppress("SameParameterValue")
class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var requestRefer: DatabaseReference
    private lateinit var progressBar2 : ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var statusTextView2: TextView
    private lateinit var statusTextView3: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis : Long = 5000

    @SuppressLint("MissingInflatedId", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        statusTextView = findViewById(R.id.statusTextView)
        statusTextView2 = findViewById(R.id.isTriggered)
        statusTextView3 = findViewById(R.id.myTextHide)

        database = FirebaseDatabase.getInstance().reference.child("users")
        requestRefer = FirebaseDatabase.getInstance().reference.child("request")

        progressBar2 = findViewById(R.id.progressBar)
        progressBar2.visibility = View.GONE
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Use the token to send messages to this device
                database.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .child("fcmToken").setValue(token.toString())
                Log.d("fcmToken",token)
            }
        }

        findViewById<FrameLayout>(R.id.helpMe).setOnClickListener {
            showConfirmationDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showConfirmationDialog() {

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
            statusTextView3.text = ""

            if (selectedRadioButton != null) {
                val runnable = object : Runnable {
                    override fun run() {
                        startChecking(requestKey.toString())
                        handler.postDelayed(this, delayMillis)
                    }
                }
                handler.post(runnable)

                val selectedThreatLevel = selectedRadioButton.text.toString()
                findViewById<TextView>(R.id.helpMe2).text = "  Waiting"
                progressBar2.visibility = View.VISIBLE
                updateDatabase(requestKey)
//                showCustomNotification("Someone needs help in your area", requestKey.toString())
                showFCMNotification(selectedThreatLevel, requestKey.toString())
            }
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        // Show the dialog
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showFCMNotification(message: String, requestKey: String) {

        statusTextView.text = "Recording your Situation..."
        statusTextView2.visibility = View.VISIBLE
        statusTextView2.text = "SOS Button Triggered"
        val fcmTokens: MutableList<String> = mutableListOf()
        val database = FirebaseDatabase.getInstance().reference
        val usersRef = database.child("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    if(userSnapshot.key.toString()!=FirebaseAuth.getInstance().currentUser?.uid.toString()){
                        val token = userSnapshot.child("fcmToken").getValue(String::class.java)
                        token?.let { fcmTokens.add(it) }
                    }
                }

                fcmTokens.forEach { token ->
//                    sendNotificationToToken(message, requestKey, token)
                    sendNotificationToToken(token, requestKey)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun startChecking(requestKey: String) {
        requestRefer.child(requestKey).addListenerForSingleValueEvent(object : ValueEventListener{
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child("confirm").getValue(Boolean::class.java)==true
                    &&
                    snapshot.hasChild("helper")){
                    statusTextView.text = "Recording your Situation..."


                    startChatActivity(snapshot.child("helper").value.toString(),requestKey)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

//    private fun sendNotificationToToken(token : String, key: String) {
//        val notificationObject = JSONObject().apply {
//            put("title", "Emergency")
//            put("body", "Someone need help in your area!")
//            put("android_channel_id", "id")
//            put("click_action", "FLAG_UPDATE_CURRENT")
//        }
//
//        val datObj = JSONObject().apply {
//            put("requestKey",key)
//            put("uid",FirebaseAuth.getInstance().currentUser?.uid.toString())
//        }
//
//        val json = JSONObject().apply {
//            put("to", token)
//            put("notification", notificationObject)
//            put("data", datObj) // Empty data object, as we already include data in the notification
//
//        }
//
//        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
//        val projectId = FirebaseApp.getInstance().options.projectId
//        Log.d("FCM", "Project ID: $projectId")
//        val request = Request.Builder()
//            .url("https://fcm.googleapis.com/v1/projects/$projectId/messages:send")
//            .addHeader("Authorization", "Bearer AAAASZeQF-4:APA91bG4DKeJKFUAyo9fSOywzXJgzIeo05XF2vONGOD-WdNSDXlNVvyXBQoShbESGojiJ7AwVtX5CcIYtSKHaVqIcBL19rhErbFI6k7CT1TW-axBiZYtf4IqqnJdCmXj-NF95-a0KBUs")
//            .post(requestBody)
//            .build()
//
//        val client = OkHttpClient()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("FCM", "Failed to send notification: ${e.message}")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                Log.d("FCM", "Notification sent successfully $call " +
//                        "\n$response")
//            }
//        })
//    }

    private fun sendNotificationToToken(token: String, key: String) {
//        val notificationObject = JSONObject().apply {
//            put("title", "Emergency")
//            put("body", "Someone need help in your area!")
//        }
//
//        val dataObj = JSONObject().apply {
//            put("requestKey", key)
//            put("uid", FirebaseAuth.getInstance().currentUser?.uid.toString())
//        }
//
//        val json = JSONObject().apply {
//            put("to", token)
//            put("notification", notificationObject)
//            put("data", dataObj) // Include data in the 'data' field
//        }


        val dataObj = JSONObject().apply {
            put("title", "Emergency")
            put("body", "Someone needs help in your area!")
            put("requestKey", key)
            put("uid", FirebaseAuth.getInstance().currentUser?.uid.toString())
            put("click_action", "FLAG_UPDATE_CURRENT")
        }

        val messageObj = JSONObject().apply {
            put("token", token)
            put("data", dataObj)
        }

        val json = JSONObject().apply {
            put("message", messageObj)
        }

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val projectId = FirebaseApp.getInstance().options.projectId
        Log.d("FCM", "Project ID: $projectId")
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/v1/projects/$projectId/messages:send")
            .addHeader("Authorization", "Bearer ya29.a0AcM612ybTE1EwUHruDkfZZymcYfHBIHa_AY71yY1k_7d1iKnK_7RUXjuLMIHmy7-p_bz62NEJaiwvpy0KpdXxvMCyk7IHygNSWeHyDpRWEEStnhEcjgyZYJsENUsuM3FKZ7xGew_v1rjEB5OwMI7c6MnX4HNHhBvbkNg3U0qaCgYKASASARESFQHGX2Mi3vMY4hvgrUcRezfdcjnArw0175")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM", "Failed to send notification: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("FCM", "Notification sent successfully $call " +
                        "\n$response")
            }
        })
    }


    private fun updateDatabase(requestKey: String?) {
        val updateMap = mapOf(
            "confirm" to false,
            "complete" to false,
            "hasQuit" to false,
            "victim" to FirebaseAuth.getInstance().currentUser?.uid.toString()
        )
        requestRefer.child(requestKey.toString()).updateChildren(updateMap)

        val map = mapOf("key" to requestKey)
        database.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).updateChildren(map)
    }

    private fun startChatActivity(user: String, requestKey: String) {
        val intent = Intent(this@HomeActivity, MainActivity2::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra("requestKey", requestKey)
        intent.putExtra("uid", user)
        startActivity(intent)
        finish()
        handler.removeCallbacksAndMessages(null)
    }
}