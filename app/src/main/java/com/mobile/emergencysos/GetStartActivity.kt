package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth

class GetStartActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_start)

        findViewById<TextView>(R.id.helpMeButton).setOnClickListener {
            checkAndRequestNotificationPermission(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkAndRequestNotificationPermission(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        if (!notificationManager.areNotificationsEnabled()) {
            // Notifications are not enabled
            showNotificationPermissionDialog(context)
        } else {
            val intent = Intent(this, LoginWithEmail::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotificationPermissionDialog(context: Context) {
        // You can customize the dialog to inform the user and prompt them to enable notifications
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Notification Permission")
            .setMessage("Please enable notifications for this app.")
            .setPositiveButton("Enable") { _, _ ->
                openNotificationSettings(context)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openNotificationSettings(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        context.startActivity(intent)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if ((currentUser != null) && currentUser.isEmailVerified) {
            val notificationManager = NotificationManagerCompat.from(this)
            if(!notificationManager.areNotificationsEnabled()){
                showNotificationPermissionDialog(this)
            }else{
                checkAndRequestNotificationPermission(this)
                val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                val validated = sharedPreferences.getBoolean("isAadharValidated", false)
                if (validated) {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this, AdhaarActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
