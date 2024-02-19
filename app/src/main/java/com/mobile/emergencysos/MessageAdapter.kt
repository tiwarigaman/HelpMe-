package com.mobile.emergencysos

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private var messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val myLay : LinearLayout = itemView.findViewById(R.id.myLay)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.senderTextView.text = message.senderId

        when(message.senderId) {
            FirebaseAuth.getInstance().currentUser?.uid.toString()->{
                holder.senderTextView.text = "You"
            }
            "system"->{
                holder.senderTextView.text = "System"
            }
            else->{
                holder.senderTextView.text = "Anonymous"
            }
        }

        holder.messageTextView.text = message.text
        val formattedTimestamp = SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(Date(message.timestamp))
        holder.timestampTextView.text = formattedTimestamp
        val gravity = if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid.toString()) {
            Gravity.END // Right for current user
        } else {
            Gravity.START // Left for others
        }

//        val layoutParams: LinearLayout.LayoutParams = if ((message.senderId == FirebaseAuth.getInstance().currentUser?.uid.toString()) ) {
//            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
//                leftMargin = 70
//            }
//        } else {
//            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
//                rightMargin = 70
//            }
//        }
//
//        holder.messageTextView.layoutParams = layoutParams

        //Apply gravity to the message text
        if (message.senderId != "system"){
            holder.myLay.gravity = gravity
            holder.timestampTextView.gravity = gravity
        }else{
            holder.myLay.gravity = Gravity.CENTER
            holder.timestampTextView.gravity = Gravity.CENTER
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}

