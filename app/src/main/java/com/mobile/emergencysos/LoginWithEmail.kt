package com.mobile.emergencysos

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginWithEmail : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var database : DatabaseReference
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_with_email)
        val emailEditText : EditText = findViewById(R.id.loginEmail)
        val passwordEditText : EditText = findViewById(R.id.loginPass)
        progressDialog = ProgressDialog(this)
        database = FirebaseDatabase.getInstance().reference.child("exception")


        findViewById<TextView>(R.id.signUpButton).setOnClickListener{
            startActivity(Intent(this,signUpWithEmail::class.java))
        }


        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() and password.isNotEmpty()){
                loginUser(email, password)
            }else{
                Toast.makeText(this,"Please Enter email address and password."
                    ,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        progressDialog?.setMessage("Signing in...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Check if the email is verified
                    if (auth.currentUser!!.isEmailVerified) {
                        // Login successful, user is verified
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this,HomeActivity::class.java))
                        finish()
                        progressDialog?.dismiss()
                    } else {
                        // Email not verified, handle accordingly
                        // You might want to ask the user to verify their email before proceeding
                        Toast.makeText(this, "Please verify your email before logging in", Toast.LENGTH_SHORT).show()
                        progressDialog?.dismiss()
                    }
                } else if(task.exception.toString()==
                    "com.google.firebase.auth.FirebaseAuthInvalidUserException: The user account has been disabled by an administrator."){
                    // Login failed, handle the error
                    // You can check task.exception?.message for details
                    Toast.makeText(this, "Account blocked: suspicious activity found", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }else{
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }
            }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if ((currentUser != null) && currentUser.isEmailVerified) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}