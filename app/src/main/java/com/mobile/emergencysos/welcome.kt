package com.mobile.emergencysos

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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

            database.child(stringValue).
                addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.child("confirm").getValue(Boolean::class.java)==true &&
                            snapshot.hasChild("helper")){
                            chatBtn.isVisible = true
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
            })


            chatBtn.setOnClickListener {
                val intent = Intent(this,ChatActivity::class.java)
                intent.putExtra("requestKey", stringValue)
                startActivity(intent)
                finish()
            }
        }

    }
}