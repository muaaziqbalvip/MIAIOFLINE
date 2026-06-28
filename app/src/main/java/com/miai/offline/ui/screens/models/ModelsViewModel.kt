package com.miai.offline.ui.screens.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miai.offline.data.model.AiModel
import com.miai.offline.data.model.DeviceProfile
import com.miai.offline.data.model.StorageChoice
import com.miai.offline.data.repository.ModelCatalog
import com.miai.offline.device.DeviceAnalyzer
import com.miai.offline.device.ModelRecommender
import com.miai.offline.download.DownloadState
import com.miai.offline.download.ModelDownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

data class ModelUiState(
    val model: AiModel,
    val recommendation: ModelRecommender.Recommendation,
    val downloadProgress: Int? = null,   // null = not downloading
    val isDownloaded: Boolean = false
)

class ModelsViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceAnalyzer = DeviceAnalyzer(application)
    private val recommender = ModelRecommender()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val downloadManager = ModelDownloadManager(application, httpClient)

    private val _deviceProfile = MutableStateFlow<DeviceProfile?>(null)
    val deviceProfile: StateFlow<DeviceProfile?> = _deviceProfile.asStateFlow()

    private val _modelStates = MutableStateFlow<List<ModelUiState>>(emptyList())
    val modelStates: StateFlow<List<ModelUiState>> = _modelStates.asStateFlow()

    var storageChoice: StorageChoice = StorageChoice.INTERNAL
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val profile = deviceAnalyzer.analyze()
            _deviceProfile.value = profile

            val states = ModelCatalog.starterModels.map { model ->
                val rec = recommender.classify(model, profile)
                val downloaded = downloadManager.isModelDownloaded(model, storageChoice)
                ModelUiState(model = model, recommendation = rec, isDownloaded = downloaded)
            }
            _modelStates.value = states
        }
    }

    fun setStorageChoice(choice: StorageChoice) {
        storageChoice = choice
        refresh()
    }

    fun downloadModel(model: AiModel) {
        viewModelScope.launch {
            downloadManager.downloadModel(model, storageChoice).collect { state ->
                when (state) {
                    is DownloadState.Progress -> updateProgress(model.id, state.percent)
                    is DownloadState.Completed -> markDownloaded(model.id)
                    is DownloadState.Failed -> updateProgress(model.id, null)
                }
            }
        }
    }

    fun deleteModel(model: AiModel) {
        downloadManager.deleteModel(model, storageChoice)
        refresh()
    }

    private fun updateProgress(modelId: String, percent: Int?) {
        _modelStates.value = _modelStates.value.map {
            if (it.model.id == modelId) it.copy(downloadProgress = percent) else it
        }
    }

    private fun markDownloaded(modelId: String) {
        _modelStates.value = _modelStates.value.map {
            if (it.model.id == modelId) it.copy(downloadProgress = null, isDownloaded = true) else it
        }
    }
}
