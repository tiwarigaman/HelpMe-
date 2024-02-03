package com.mobile.emergencysos

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumber: EditText
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        phoneNumber = findViewById(R.id.editTextPhoneNumber)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Verifying phone number...")
        progressDialog.setCancelable(false)

        val loginButton: Button = findViewById(R.id.buttonLogin)
        loginButton.setOnClickListener {
            val enteredPhoneNumber = phoneNumber.text.toString().trim()

            val spinner: Spinner = findViewById(R.id.spinnerCountryCode)
            val selectedCountryItem = spinner.selectedItem.toString()
            val selectedCountryCode = selectedCountryItem.split(" ")[0]

            val fullPhoneNumber = "$selectedCountryCode$enteredPhoneNumber"

            if (enteredPhoneNumber.isNotEmpty()) {
                // Start the phone number verification process
                startPhoneNumberVerification(fullPhoneNumber)
            } else {
                // Handle empty phone number
                phoneNumber.error = "Phone number is required"
            }
        }

    }
    private fun startPhoneNumberVerification(phoneNumber: String) {
        progressDialog.show()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    progressDialog.dismiss()
                    // Automatically verify the phone number when SMS code is detected
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressDialog.dismiss()
                    Log.e("VerificationFailed", e.message.toString())
                    Toast.makeText(this@LoginActivity, e.message.toString(),Toast.LENGTH_LONG).show()

                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    progressDialog.dismiss()
                    // Save the verification ID for later use
                    val intent = Intent(this@LoginActivity, VerifyActivity::class.java)
                    intent.putExtra("verificationId", verificationId)
                    Toast.makeText(this@LoginActivity,"Code sent",Toast.LENGTH_SHORT).show()
                    runOnUiThread {
                        startActivity(intent)
                    }
                }
            }
        )
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Successfully signed in
                    val user = task.result?.user
                    Log.d("SignInSuccess", user?.uid ?: "")
                } else {
                    // Sign-in failed
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.e("SignInFailed", "Invalid verification code")
                    } else {
                        Log.e("SignInFailed", task.exception?.message ?: "")
                    }
                }
            }
    }
}