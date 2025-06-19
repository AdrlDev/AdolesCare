package dev.adriele.adolescare.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.adriele.adolescare.helpers.Utility.getCurrentTime
import dev.adriele.adolescare.chatbot.ResponseType
import dev.adriele.adolescare.chatbot.adapter.ChatBotAdapter
import dev.adriele.adolescare.contracts.IChatBot
import dev.adriele.adolescare.database.AppDatabaseProvider
import dev.adriele.adolescare.database.entities.Conversations
import dev.adriele.adolescare.database.repositories.implementation.ChatBotRepositoryImpl
import dev.adriele.adolescare.databinding.FragmentChatBotBinding
import dev.adriele.adolescare.viewmodel.ChatBotViewModel
import dev.adriele.adolescare.viewmodel.factory.ChatBotViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val USER_ID = "userID"
private const val USER_NAME = "userName"

class ChatBotFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var userId: String? = null
    private var userName: String? = null

    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatBotAdapter: ChatBotAdapter
    private lateinit var chatBotViewModel: ChatBotViewModel
    private lateinit var chatBotViewModelFactory: ChatBotViewModelFactory

    private val chats = mutableListOf<Conversations>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(USER_ID)
            userName = it.getString(USER_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatBotBinding.inflate(inflater, container, false)

        // Apply insets to the fragment root
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                systemBarsInsets.bottom
            )
            insets
        }

        init()
        afterInit()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        chatBotViewModel.messages.observe(requireActivity()) { messages ->
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

                    val botWelcomeMessage = Conversations(userId = userId, resWith = ResponseType.BOT, message = "Welcome to AdolesCare ChatBot, $userName!", receivedDate = getCurrentTime())
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
        val dao = AppDatabaseProvider.getDatabase(requireActivity()).conversationDao()
        val repo = ChatBotRepositoryImpl(dao)
        chatBotViewModelFactory = ChatBotViewModelFactory(repo, userId!!)
        chatBotViewModel = ViewModelProvider(this, chatBotViewModelFactory)[ChatBotViewModel::class]

        chatBotAdapter = ChatBotAdapter()
        val myManager = LinearLayoutManager(requireContext())

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

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(userId: String, userName: String) =
            ChatBotFragment().apply {
                arguments = Bundle().apply {
                    putString(USER_ID, userId)
                    putString(USER_NAME, userName)
                }
            }
    }
}