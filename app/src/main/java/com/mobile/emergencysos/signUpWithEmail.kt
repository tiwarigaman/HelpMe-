package com.mobile.emergencysos

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class signUpWithEmail : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_with_email)
        val emailEditText : EditText = findViewById(R.id.signUpEmail)
        val passwordEditText : EditText = findViewById(R.id.signUpPass)

        progressDialog = ProgressDialog(this)

        findViewById<TextView>(R.id.loginPageButton).setOnClickListener{
            startActivity(Intent(this,LoginWithEmail::class.java))
            finish()
        }

        findViewById<TextView>(R.id.signUpGo).setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() and password.isNotEmpty()){
                registerUser(email, password)
            }else{
                Toast.makeText(this,"Please Enter email address and password."
                    ,Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun registerUser(email: String, password: String) {
        progressDialog?.setMessage("Registering...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    sendVerificationEmail()
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }
            }
    }

    private fun sendVerificationEmail() {
        val user = auth.currentUser

        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Email sent successfully, now store user details in the database
                    saveUserToDatabase()
                } else {
                    // Email not sent, handle the error
                    // You can check task.exception?.message for details
                    Toast.makeText(this, "Email verification failed", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }
            }
    }

    private fun saveUserToDatabase() {
        val user = auth.currentUser
        val userId = user?.uid

        if (userId != null) {
            val userReference = database.reference.child("users").child(userId)

            val userData = hashMapOf(
                "email" to user.email,
                "verified" to false // Initially set to false, will be updated after email verification
            )

            userReference.setValue(userData)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // User registration and details saving completed
                        Toast.makeText(this, "Registration successful. Please verify your email.", Toast.LENGTH_SHORT).show()
                        progressDialog?.dismiss()
                    } else {
                        // Handle the error
                        // You can check task.exception?.message for details
                        Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show()
                        progressDialog?.dismiss()
                    }
                }
        }
    }
}