package com.miai.offline.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miai.offline.data.model.StorageChoice
import com.miai.offline.data.repository.ModelCatalog
import com.miai.offline.download.ModelDownloadManager
import com.miai.offline.inference.InferenceEngine
import com.miai.offline.inference.ResponseMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.File

data class ChatUiMessage(
    val role: String,            // "user" | "assistant"
    val content: String,
    val isStreaming: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val inferenceEngine = InferenceEngine()
    private val downloadManager = ModelDownloadManager(application, OkHttpClient())

    private val _messages = MutableStateFlow<List<ChatUiMessage>>(emptyList())
    val messages: StateFlow<List<ChatUiMessage>> = _messages.asStateFlow()

    private val _responseMode = MutableStateFlow(ResponseMode.FAST)
    val responseMode: StateFlow<ResponseMode> = _responseMode.asStateFlow()

    private val _modelReady = MutableStateFlow(false)
    val modelReady: StateFlow<Boolean> = _modelReady.asStateFlow()

    private val _currentModelName = MutableStateFlow("")
    val currentModelName: StateFlow<String> = _currentModelName.asStateFlow()

    fun loadModel(modelId: String) {
        val model = ModelCatalog.findById(modelId) ?: return
        _currentModelName.value = model.displayName

        viewModelScope.launch {
            val internalFile = File(downloadManager.getModelsDirectory(StorageChoice.INTERNAL), model.fileName)
            val sdCardFile = File(downloadManager.getModelsDirectory(StorageChoice.SD_CARD), model.fileName)

            val modelFile = when {
                internalFile.exists() -> internalFile
                sdCardFile.exists() -> sdCardFile
                else -> null
            }

            if (modelFile != null) {
                val loaded = inferenceEngine.loadModel(modelFile.absolutePath)
                _modelReady.value = loaded
                if (loaded) {
                    _messages.value = listOf(
                        ChatUiMessage("assistant", "Salam! Main ${model.displayName} hoon, offline chal raha hoon. Kaise madad karoon?")
                    )
                } else {
                    _messages.value = listOf(
                        ChatUiMessage("assistant", "Model load nahi ho saka. Dobara try karein ya model fir se download karein.")
                    )
                }
            } else {
                _messages.value = listOf(
                    ChatUiMessage("assistant", "Yeh model abhi download nahi hua. Pehle Models screen se download karein.")
                )
            }
        }
    }

    fun toggleMode() {
        _responseMode.value = if (_responseMode.value == ResponseMode.FAST) {
            ResponseMode.DEEP_THINKING
        } else {
            ResponseMode.FAST
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _messages.value = _messages.value + ChatUiMessage("user", text)
        val streamingPlaceholder = ChatUiMessage("assistant", "", isStreaming = true)
        _messages.value = _messages.value + streamingPlaceholder

        viewModelScope.launch {
            val builder = StringBuilder()
            inferenceEngine.chat(text, _responseMode.value).collect { token ->
                builder.append(token)
                updateLastMessage(builder.toString(), isStreaming = true)
            }
            updateLastMessage(builder.toString(), isStreaming = false)
        }
    }

    private fun updateLastMessage(content: String, isStreaming: Boolean) {
        val current = _messages.value.toMutableList()
        if (current.isNotEmpty() && current.last().role == "assistant") {
            current[current.size - 1] = ChatUiMessage("assistant", content, isStreaming)
            _messages.value = current
        }
    }

    override fun onCleared() {
        super.onCleared()
        inferenceEngine.unloadCurrentModel()
    }
}
