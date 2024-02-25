package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class ChatActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var hasQuit : DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var stringValue :String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        auth = FirebaseAuth.getInstance()


        val receivedIntent = intent
        stringValue = receivedIntent.getStringExtra("requestKey").toString()
        database = FirebaseDatabase.getInstance().reference.child("request").child(stringValue)
            .child("chat")

        hasQuit = FirebaseDatabase.getInstance().reference.child("request").child(stringValue)
        findViewById<AppCompatImageView>(R.id.btnBack).setOnClickListener{
            finish()
        }
        findViewById<AppCompatImageView>(R.id.btnCancel).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure you want to skip to help?")
            builder.setPositiveButton("Yes") { _, _ ->
                val map = mapOf(
                    "confirm" to false,
                    "complete" to false,
                    "hasQuit" to true
                )
                FirebaseDatabase.getInstance().reference.child("request").child(stringValue).updateChildren(map)
                val currentUser = auth.currentUser
                val message = Message(currentUser?.uid.toString(), "Anonymous quit this session", System.currentTimeMillis())
                database.push().setValue(message)
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("No"){_,_ ->
                //do nothing...
            }
            builder.show()
        }
        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(emptyList())
        recyclerView.adapter = messageAdapter


        // Start listening for messages
        startListeningForMessages()

        // Example: Sending a message
        findViewById<FrameLayout>(R.id.sendButton).setOnClickListener {
            if(findViewById<EditText>(R.id.messageUser).text.toString().isNotEmpty()
                && findViewById<EditText>(R.id.messageUser).text.toString().trim().isNotEmpty()){
                sendMessage(findViewById<EditText>(R.id.messageUser).text.toString())
            }else{
                Toast.makeText(this,"Can't send empty message",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(messageText: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            findViewById<EditText>(R.id.messageUser).setText("")
            val message = Message(currentUser.uid, messageText, System.currentTimeMillis())
            database.push().setValue(message)
        }
    }

    private fun startListeningForMessages() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()

                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val message = childSnapshot.getValue(Message::class.java)
                        message?.let { messages.add(it) }
                    }
                }else {
                    // Push a default message when no messages exist
                    val defaultMessage = Message("system", "Anonymous is here to help you" +
                            "", System.currentTimeMillis())
                    database.push().setValue(defaultMessage)
                    messages.add(defaultMessage)
                }

                // Handle the received messages (update UI, etc.)
                messageAdapter.setMessages(messages)
                recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

}