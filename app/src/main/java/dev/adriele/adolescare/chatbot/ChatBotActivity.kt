package dev.adriele.adolescare.chatbot

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.adriele.adolescare.helpers.Utility.getCurrentTime
import dev.adriele.adolescare.chatbot.adapter.ChatBotAdapter
import dev.adriele.adolescare.contracts.IChatBot
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.Conversations
import dev.adriele.adolescare.database.repositories.implementation.ChatBotRepositoryImpl
import dev.adriele.adolescare.databinding.ActivityChatBotBinding
import dev.adriele.adolescare.viewmodel.ChatBotViewModel
import dev.adriele.adolescare.viewmodel.factory.ChatBotViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatBotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBotBinding

    private lateinit var chatBotAdapter: ChatBotAdapter
    private lateinit var chatBotViewModel: ChatBotViewModel
    private lateinit var chatBotViewModelFactory: ChatBotViewModelFactory

    private var userId: String? = null

    private val chats = mutableListOf<Conversations>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //add this to not block the ui when keyboard show
        WindowCompat.setDecorFitsSystemWindows(window, true)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        afterInit()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun afterInit() {
        // Observe chat messages from ViewModel
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Optional: send on keyboard done action
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        binding.etMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.rvChats.postDelayed({
                    binding.rvChats.scrollToPosition(chatBotAdapter.itemCount - 1)
                }, 100)
            }
        }

        binding.rvChats.post {
            binding.rvChats.scrollToPosition(chatBotAdapter.itemCount - 1)
        }

        chatBotViewModel.messages.observe(this) { messages ->
            if(messages.isNotEmpty() && messages != null) {
                chatBotAdapter.setMessage(messages)
                binding.rvChats.scrollToPosition(messages.size - 1)
            } else {
                lifecycleScope.launch {
                    delay(1500)

                    // Add typing indicator
                    val typing = Conversations(userId = userId, resWith = ResponseType.TYPING)
                    chatBotViewModel.saveMessage(typing)
                    chatBotAdapter.notifyDataSetChanged()
                    binding.rvChats.scrollToPosition(chatBotAdapter.itemCount - 1)

                    val botWelcomeMessage = Conversations(userId = userId, resWith = ResponseType.BOT, message = "Welcome to AdolesCare ChatBot!", receivedDate = getCurrentTime())
                    val botWelcomeMessage2 = Conversations(userId = userId, resWith = ResponseType.BOT, message = "How can i help you today?", receivedDate = getCurrentTime())
                    delay(500)
                    chatBotViewModel.saveMessage(botWelcomeMessage)
                    chatBotAdapter.notifyDataSetChanged()
                    binding.rvChats.scrollToPosition(chatBotAdapter.itemCount - 1)
                    chatBotViewModel.deleteMessage(ResponseType.TYPING)
                    delay(500)
                    chatBotViewModel.saveMessage(botWelcomeMessage2)
                    chatBotAdapter.notifyDataSetChanged()
                    binding.rvChats.scrollToPosition(chatBotAdapter.itemCount - 1)
                }
            }
        }
    }

    private fun init() {
        val dao = AppDatabaseProvider.getDatabase(applicationContext).conversationDao()
        val repo = ChatBotRepositoryImpl(dao)
        intent?.getStringExtra("user_id")?.let { id ->
            userId = id
        }
        chatBotViewModelFactory = ChatBotViewModelFactory(repo, userId!!)
        chatBotViewModel = ViewModelProvider(this, chatBotViewModelFactory)[ChatBotViewModel::class]

        chatBotAdapter = ChatBotAdapter()
        val myManager = LinearLayoutManager(this)

        myManager.stackFromEnd = true

        binding.rvChats.apply {
            adapter = chatBotAdapter
            layoutManager = myManager
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sendMessage() {
        val message = binding.etMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            // Add user message
            val userMessage = Conversations(userId = userId, resWith = ResponseType.USER, message = message, sentDate = getCurrentTime())
            chats.add(userMessage)
            chatBotViewModel.saveMessage(userMessage)
            chatBotAdapter.notifyDataSetChanged()
            binding.rvChats.scrollToPosition(chatBotAdapter.itemCount - 1)

            lifecycleScope.launch {
                delay(2000)

                // Add typing indicator
                val typing = Conversations(userId = userId, resWith = ResponseType.TYPING)
                chats.add(typing)
                chatBotViewModel.saveMessage(typing)
                chatBotAdapter.notifyDataSetChanged()
                binding.rvChats.scrollToPosition(chatBotAdapter.itemCount - 1)

                chatBotViewModel.sendQueryToBot(message, object : IChatBot {
                    override fun onResult(result: String) {
                        // Add bot response
                        val botResponse = Conversations(
                            userId = userId,
                            resWith = ResponseType.BOT,
                            message = result,
                            receivedDate = getCurrentTime()
                        )
                        chats.add(botResponse)

                        // Update UI
                        chatBotAdapter.notifyDataSetChanged()
                        binding.rvChats.scrollToPosition(chatBotAdapter.itemCount - 1)

                        Handler(Looper.getMainLooper()).postDelayed({
                            chatBotViewModel.deleteMessage(ResponseType.TYPING)
                            chatBotViewModel.saveMessage(botResponse)
                        }, 1000)
                    }
                })
            }

            binding.etMessage.text?.clear()
        }
    }
}