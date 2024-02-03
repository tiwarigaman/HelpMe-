package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class VerifyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        auth = FirebaseAuth.getInstance()

        val instructionsTextView: TextView = findViewById(R.id.textViewInstructions)
        val verifyButton: Button = findViewById(R.id.buttonVerify)

        val digit: EditText = findViewById(R.id.editTextOtp)


        verificationId = intent.getStringExtra("verificationId") ?: ""



        verifyButton.setOnClickListener {
            val enteredOtp = digit.text.toString()

            if (enteredOtp.isNotEmpty() && verificationId.isNotEmpty()) {
                // Verify the entered code with the received verification ID
                val credential = PhoneAuthProvider.getCredential(verificationId, enteredOtp)
                signInWithPhoneAuthCredential(credential)
            } else {
                // Handle empty verification code or missing verification ID
                instructionsTextView.error = "Verification code is required"
            }
        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Successfully signed in
                    val user = task.result?.user
                    // TODO: Handle successful sign-in (e.g., navigate to the main activity)
                    navigateToMainActivity()
                } else {
                    // Sign-in failed
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // Handle invalid verification code
                        // You can display an error message, change UI, etc.
                    } else {
                        // Handle other exceptions
                        // You can display an error message, change UI, etc.
                    }
                }
            }
    }

    private fun navigateToMainActivity() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the VerifyActivity
    }
}
