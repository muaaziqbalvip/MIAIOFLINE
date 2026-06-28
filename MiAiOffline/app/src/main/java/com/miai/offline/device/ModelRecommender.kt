package com.miai.offline.device

import com.miai.offline.data.model.AiModel
import com.miai.offline.data.model.DeviceProfile
import com.miai.offline.data.model.DeviceTier

/**
 * Compares device RAM/storage against each model's requirements and
 * splits the catalog into Recommended / Usable-but-slow / Not-recommended,
 * exactly as requested: "RAM storage check kre... us hisab se AI model recommend kre".
 */
class ModelRecommender {

    sealed class Recommendation {
        data class Recommended(val model: AiModel) : Recommendation()
        data class Caution(val model: AiModel, val reason: String) : Recommendation()
        data class NotRecommended(val model: AiModel, val reason: String) : Recommendation()
    }

    fun classify(model: AiModel, device: DeviceProfile): Recommendation {
        val sizeMb = model.sizeBytes / (1024 * 1024)

        // Storage check first — must have at least the model size + 500MB buffer free
        val hasEnoughStorage = (device.availableInternalStorageMb > sizeMb + 500) ||
                ((device.availableSdCardStorageMb ?: 0) > sizeMb + 500)

        if (!hasEnoughStorage) {
            return Recommendation.NotRecommended(
                model,
                "Storage kam hai — ${sizeMb}MB chahiye, available storage check karein"
            )
        }

        // RAM check — model generally needs roughly 1.2x its file size in RAM to run
        val estimatedRamNeededMb = (sizeMb * 1.2).toInt()

        return when {
            device.totalRamMb >= model.minRamMb && device.availableRamMb >= estimatedRamNeededMb ->
                Recommendation.Recommended(model)

            device.totalRamMb >= model.minRamMb ->
                Recommendation.Caution(
                    model,
                    "RAM tight hai, chalega lekin thora slow ho sakta hai"
                )

            else ->
                Recommendation.NotRecommended(
                    model,
                    "Is device ki RAM (${device.totalRamMb}MB) is model ke liye kaafi nahi (min ${model.minRamMb}MB chahiye)"
                )
        }
    }

    fun recommendFor(models: List<AiModel>, device: DeviceProfile): List<Recommendation> {
        return models.map { classify(it, device) }
    }

    /** Suggests a sensible default model id for first-time users based on device tier. */
    fun suggestedDefaultModelId(device: DeviceProfile): String {
        return when (device.performanceTier()) {
            DeviceTier.LOW -> "smollm2-360m-instruct-q4"
            DeviceTier.MEDIUM -> "smollm2-1.7b-instruct-q4"
            DeviceTier.HIGH -> "qwen2.5-3b-instruct-q4"
        }
    }
}
