package dev.adriele.adolescare.chatbot.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.R
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.chatbot.MessageType
import dev.adriele.adolescare.chatbot.ResponseType
import dev.adriele.adolescare.database.entities.Conversations
import dev.adriele.adolescare.ui.PdfViewerActivity

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
        @SuppressLint("SetTextI18n")
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

            // Show sources only for bot messages
            if (viewType == MessageType.LEFT.type) {
                val llSources: ViewGroup = binding.findViewById(R.id.ll_sources)
                llSources.removeAllViews()

                if (!response.sources.isNullOrEmpty()) {
                    llSources.visibility = View.VISIBLE

                    val shownTitles = mutableSetOf<String>()

                    response.sources.forEach { source ->
                        val title = source.title
                        val finalTitle = if (title?.isEmpty() == true) {
                            source.producer
                        } else {
                            title ?: "Unknown Source"
                        }

                        if (shownTitles.contains(finalTitle)) return@forEach // Skip if already shown
                        shownTitles.add(finalTitle) // Track this finalTitle

                        val fullText = "📎 Source: $finalTitle"
                        val startIndex = fullText.indexOf(finalTitle)
                        val endIndex = startIndex + finalTitle.length

                        val spannable = SpannableString(fullText).apply {
                            setSpan(
                                UnderlineSpan(),
                                startIndex,
                                endIndex,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        val tvSource = TextView(binding.context).apply {
                            text = spannable
                            setTextColor(binding.resources.getColor(R.color.textColorLink, null))
                            textSize = 14f
                            setPadding(0, 4, 0, 4)
                            setOnClickListener {
                                val intent = Intent(context, PdfViewerActivity::class.java).apply {
                                    val modifiedSource = source.source
                                    putExtra("module_url", modifiedSource)
                                    putExtra("module_category", extractReadableModuleTitle(modifiedSource).lowercase())
                                }
                                context.startActivity(intent)
                            }
                        }
                        llSources.addView(tvSource)
                    }
                } else {
                    llSources.visibility = View.GONE
                }
            }
        }
    }

    fun extractReadableModuleTitle(sourcePath: String): String {
        val parts = sourcePath.split("/")
        val folderName = parts.getOrNull(2) ?: return "Unknown Module"
        return folderName.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
    }
}