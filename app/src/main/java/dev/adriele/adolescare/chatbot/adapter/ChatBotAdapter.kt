package dev.adriele.adolescare.chatbot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.R
import dev.adriele.adolescare.Utility
import dev.adriele.adolescare.chatbot.MessageType
import dev.adriele.adolescare.chatbot.ResponseType
import dev.adriele.adolescare.database.entities.Conversations

class ChatBotAdapter: RecyclerView.Adapter<ChatBotAdapter.ViewHolder>() {
    private var arrayList: List<Conversations>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setMessage(message: List<Conversations>) {
        this.arrayList = message
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val root = when (viewType) {
            MessageType.RIGHT.type -> inflater.inflate(R.layout.user_message, parent, false)
            MessageType.TYPING.type -> inflater.inflate(R.layout.chatbot_typing, parent, false)
            else -> inflater.inflate(R.layout.chatbot_response, parent, false)
        }
        return ViewHolder(root, viewType)

    }

    override fun getItemCount(): Int {
        arrayList?.size?.let { size ->
            return size
        }
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrayList?.get(position) ?: Conversations())
    }

    override fun getItemViewType(position: Int): Int {
        return when (arrayList?.get(position)?.resWith) {
            ResponseType.USER -> MessageType.RIGHT.type
            ResponseType.BOT -> MessageType.LEFT.type
            ResponseType.TYPING -> MessageType.TYPING.type
            else -> MessageType.LEFT.type
        }
    }

    inner  class ViewHolder(private val binding: View, private val viewType: Int) : RecyclerView.ViewHolder(binding) {
        fun bind(response: Conversations) {
            if (viewType == MessageType.TYPING.type) {
                Utility.animateTypingDots(
                    binding.findViewById(R.id.dot1),
                    binding.findViewById(R.id.dot2),
                    binding.findViewById(R.id.dot3)
                )
                return
            }

            val tvChat: TextView = binding.findViewById(R.id.tv_chat)
            val tvDate: TextView = binding.findViewById(R.id.tv_date)

            tvChat.text = response.message
            tvDate.text = when (response.resWith) {
                ResponseType.USER -> response.sentDate
                ResponseType.BOT -> response.receivedDate
                else -> "No date"
            }
        }
    }
}