package com.miai.offline.download

import android.content.Context
import com.miai.offline.data.model.AiModel
import com.miai.offline.data.model.StorageChoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

sealed class DownloadState {
    data class Progress(val bytesDownloaded: Long, val totalBytes: Long, val percent: Int) : DownloadState()
    data class Completed(val filePath: String) : DownloadState()
    data class Failed(val error: String) : DownloadState()
}

/**
 * Downloads a model file from HuggingFace to the location the user chose
 * (internal storage or SD card — "data kahan rakhna hai hum idhar rakh"),
 * emitting progress percentage as it goes so the UI can show a live progress bar.
 */
class ModelDownloadManager(
    private val context: Context,
    private val client: OkHttpClient
) {

    fun getModelsDirectory(storageChoice: StorageChoice): File {
        val baseDir = when (storageChoice) {
            StorageChoice.INTERNAL -> context.filesDir
            StorageChoice.SD_CARD -> {
                val externalDirs = context.getExternalFilesDirs(null)
                // Index 0 is always the primary (built-in) storage. A true removable
                // SD card, if present, appears as a later entry in this array.
                externalDirs.drop(1).firstOrNull { dir ->
                    dir != null && android.os.Environment.isExternalStorageRemovable(dir)
                } ?: context.filesDir
            }
        }
        val modelsDir = File(baseDir, "models")
        if (!modelsDir.exists()) modelsDir.mkdirs()
        return modelsDir
    }

    fun downloadModel(
        model: AiModel,
        storageChoice: StorageChoice
    ): Flow<DownloadState> = flow {
        val modelsDir = getModelsDirectory(storageChoice)
        val targetFile = File(modelsDir, model.fileName)
        val tempFile = File(modelsDir, "${model.fileName}.part")

        try {
            // Support resume: if a partial file exists, request a Range continuation
            val startOffset = if (tempFile.exists()) tempFile.length() else 0L

            val requestBuilder = Request.Builder().url(model.downloadUrl)
            if (startOffset > 0) {
                requestBuilder.addHeader("Range", "bytes=$startOffset-")
            }

            val response = client.newCall(requestBuilder.build()).execute()

            if (!response.isSuccessful) {
                emit(DownloadState.Failed("Download failed: HTTP ${response.code}"))
                return@flow
            }

            val totalBytes = (response.body?.contentLength() ?: 0L) + startOffset
            val body = response.body ?: run {
                emit(DownloadState.Failed("Empty response body"))
                return@flow
            }

            FileOutputStream(tempFile, startOffset > 0).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesDownloaded = startOffset
                    var lastEmittedPercent = -1

                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        bytesDownloaded += read

                        val percent = if (totalBytes > 0) {
                            ((bytesDownloaded * 100) / totalBytes).toInt()
                        } else 0

                        // Only emit when percent actually changes — avoids flooding the UI
                        if (percent != lastEmittedPercent) {
                            lastEmittedPercent = percent
                            emit(DownloadState.Progress(bytesDownloaded, totalBytes, percent))
                        }
                    }
                }
            }

            tempFile.renameTo(targetFile)
            emit(DownloadState.Completed(targetFile.absolutePath))

        } catch (e: Exception) {
            emit(DownloadState.Failed(e.message ?: "Unknown download error"))
        }
    }

    fun deleteModel(model: AiModel, storageChoice: StorageChoice): Boolean {
        val file = File(getModelsDirectory(storageChoice), model.fileName)
        return if (file.exists()) file.delete() else false
    }

    fun isModelDownloaded(model: AiModel, storageChoice: StorageChoice): Boolean {
        return File(getModelsDirectory(storageChoice), model.fileName).exists()
    }
}
