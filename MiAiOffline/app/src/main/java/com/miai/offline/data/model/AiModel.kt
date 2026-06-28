package com.miai.offline.data.model

/**
 * Represents one downloadable offline AI model (GGUF format, used by llama.cpp).
 * sizeBytes is shown to user as MB/GB before download, per requirement.
 */
data class AiModel(
    val id: String,                  // e.g. "smollm2-1.7b-instruct-q4"
    val displayName: String,         // e.g. "SmolLM2 1.7B Instruct (Q4_K_M)"
    val huggingFaceRepo: String,      // e.g. "HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF"
    val fileName: String,             // e.g. "smollm2-1.7b-instruct-q4_k_m.gguf"
    val downloadUrl: String,          // direct resolve URL
    val sizeBytes: Long,              // exact size, shown as MB/GB in UI
    val quantization: String,         // Q4_K_M, Q5_K_M, Q8_0, F16 etc
    val paramCount: String,           // "1.7B", "3B", "7B" etc
    val minRamMb: Int,                // minimum device RAM recommended to run this smoothly
    val languages: List<String>,      // ["en"], ["ur","en","hi","ar"] etc
    val category: ModelCategory,
    val description: String,
    val isDownloaded: Boolean = false,
    val localPath: String? = null
)

enum class ModelCategory {
    GENERAL_CHAT,
    URDU_MULTILINGUAL,
    CODING,
    REASONING_DEEP_THINK,
    QUIZ_EDUCATION,
    IMAGE_GENERATION,
    AUDIO_GENERATION,
    SPEECH_TO_TEXT,
    TEXT_TO_SPEECH
}

/** Snapshot of the device's hardware, used to recommend which models will actually run well. */
data class DeviceProfile(
    val totalRamMb: Int,
    val availableRamMb: Int,
    val totalStorageMb: Long,
    val availableInternalStorageMb: Long,
    val availableSdCardStorageMb: Long?,   // null if no SD card present
    val hasSdCard: Boolean,
    val cpuAbi: String,                    // arm64-v8a, armeabi-v7a
    val cpuCoreCount: Int,
    val supportsNeon: Boolean
) {
    /** Simple tier used to filter which models are "Recommended" vs "Not Recommended". */
    fun performanceTier(): DeviceTier = when {
        totalRamMb >= 8000 -> DeviceTier.HIGH
        totalRamMb >= 4000 -> DeviceTier.MEDIUM
        else -> DeviceTier.LOW
    }
}

enum class DeviceTier { LOW, MEDIUM, HIGH }

enum class StorageChoice { INTERNAL, SD_CARD }
