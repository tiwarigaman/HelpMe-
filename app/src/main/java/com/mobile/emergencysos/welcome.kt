package com.mobile.emergencysos

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class welcome : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        val chatBtn : Button = findViewById(R.id.chatKaro)

        chatBtn.isVisible = false
        database = FirebaseDatabase.getInstance().reference.child("request")

        val stringValue = intent.getStringExtra("requestKey").toString()
        val otherUser = intent.getStringExtra("uid").toString()
        Log.d("doubleHEHE",stringValue)

        if(stringValue.isNotEmpty()){
            database.child(stringValue).
            addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val builder = AlertDialog.Builder(this@welcome)
                        builder.setTitle("Confirmation")
                        builder.setMessage("Are you sure you want to request to the helpers?")
                        builder.setPositiveButton("Yes") { _, _ ->
                            if (snapshot.child("complete").getValue(Boolean::class.java)==false &&
                                snapshot.child("confirm").getValue(Boolean::class.java)==false
                            ){
                                val map = mapOf(
                                    "confirm" to true,
                                    "helper" to FirebaseAuth.getInstance().currentUser?.uid.toString()
                                )
                                database.child(stringValue).updateChildren(map)
                                val intent = Intent(this@welcome,MainActivity2::class.java)
                                intent.putExtra("requestKey", stringValue)
                                intent.putExtra("uid", otherUser)
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(this@welcome,"The request is accepted by someone",
                                    Toast.LENGTH_LONG).show()
                                val intent = Intent(this@welcome,HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                        builder.setNegativeButton("No"){_,_ ->
                            val intent = Intent(this@welcome,HomeActivity::class.java)
                            startActivity(intent)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                            finish()
                        }
                        builder.show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        }
    }
}