package dev.adriele.adolescare.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dev.adriele.adolescare.api.RetrofitInstance
import dev.adriele.adolescare.api.request.InsightsRequest
import dev.adriele.adolescare.chatbot.ResponseType
import dev.adriele.adolescare.contracts.IChatBot
import dev.adriele.adolescare.database.entities.Conversations
import dev.adriele.adolescare.database.repositories.ChatBotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatBotViewModel(
    private val repository: ChatBotRepository,
    private val userId: String
) : ViewModel() {
    val messages = repository.getAllConversations(userId).asLiveData()

    fun saveMessage(conversations: Conversations) = viewModelScope.launch {
        repository.insertConversations(conversations)
    }

    fun deleteMessage(resType: ResponseType) = viewModelScope.launch {
        repository.deleteConversations(resType)
    }

    fun clearMessages() = viewModelScope.launch {
        repository.clearMessages(userId)
    }

    fun sendQueryToBot(query: String, iChatBot: IChatBot) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getResponse(query)
                withContext(Dispatchers.Main) {
                    if(response.isSuccessful) {
                        val answer = response.body()?.answer
                        val sources = response.body()?.sources

                        val result = answer?.result ?: "AdolesCare ChatBot Error: Empty result"
                        iChatBot.onResult(result, sources)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AdolesCare ChatBot Error:", e.message, e)
                }
            }
        }
    }

    fun getTodayTips(iChatBot: IChatBot.Tips) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getTodayTip()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            iChatBot.onResult(it)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AdolesCare Tip Error", e.message ?: "Unknown error", e)
                }
            }
        }
    }

    fun getInsights(insightsRequest: InsightsRequest, iChatBot: IChatBot.Insight) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getInsights(insightsRequest)
                withContext(Dispatchers.Main) {
                    if(response.isSuccessful) {
                        response.body()?.let {
                            iChatBot.onResult(it)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AdolesCare Insight Error:", e.message, e)
                }
            }
        }
    }

}