package com.miai.offline.data.repository

import com.miai.offline.data.model.AiModel
import com.miai.offline.data.model.ModelCategory
import com.miai.offline.data.network.HuggingFaceConstants

/**
 * Curated starter catalog of GGUF models known to run well via llama.cpp on Android.
 * Sizes here are approximate placeholders — the real exact size is fetched live
 * via HfSizeFetcher (HEAD request) before showing to the user / before download,
 * so this list just needs correct repo + filename to point at the right file.
 *
 * IMPORTANT: This is a STARTER list, not the "hazaaron models" (thousands) requested.
 * To scale to thousands, wire HuggingFaceApi.getModelInfo() against curated GGUF
 * authors (TheBloke, bartowski, HuggingFaceTB, Qwen, unsloth, etc.) and cache the
 * resulting catalog — see README "Next Steps" for the plan.
 */
object ModelCatalog {

    val starterModels: List<AiModel> = listOf(
        AiModel(
            id = "smollm2-360m-instruct-q4",
            displayName = "SmolLM2 360M Instruct (Q4_K_M)",
            huggingFaceRepo = "HuggingFaceTB/SmolLM2-360M-Instruct-GGUF",
            fileName = "smollm2-360m-instruct-q4_k_m.gguf",
            downloadUrl = HuggingFaceConstants.resolveDownloadUrl(
                "HuggingFaceTB/SmolLM2-360M-Instruct-GGUF",
                "smollm2-360m-instruct-q4_k_m.gguf"
            ),
            sizeBytes = 250_000_000L,
            quantization = "Q4_K_M",
            paramCount = "360M",
            minRamMb = 1500,
            languages = listOf("en"),
            category = ModelCategory.GENERAL_CHAT,
            description = "Sabse halka model — purane / kam RAM wale phones ke liye."
        ),
        AiModel(
            id = "smollm2-1.7b-instruct-q4",
            displayName = "SmolLM2 1.7B Instruct (Q4_K_M)",
            huggingFaceRepo = "HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF",
            fileName = "smollm2-1.7b-instruct-q4_k_m.gguf",
            downloadUrl = HuggingFaceConstants.resolveDownloadUrl(
                "HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF",
                "smollm2-1.7b-instruct-q4_k_m.gguf"
            ),
            sizeBytes = 1_100_000_000L,
            quantization = "Q4_K_M",
            paramCount = "1.7B",
            minRamMb = 3000,
            languages = listOf("en"),
            category = ModelCategory.GENERAL_CHAT,
            description = "Balanced model — mid-range phones ke liye general chat."
        ),
        AiModel(
            id = "qwen2.5-3b-instruct-q4",
            displayName = "Qwen2.5 3B Instruct (Q4_K_M)",
            huggingFaceRepo = "Qwen/Qwen2.5-3B-Instruct-GGUF",
            fileName = "qwen2.5-3b-instruct-q4_k_m.gguf",
            downloadUrl = HuggingFaceConstants.resolveDownloadUrl(
                "Qwen/Qwen2.5-3B-Instruct-GGUF",
                "qwen2.5-3b-instruct-q4_k_m.gguf"
            ),
            sizeBytes = 1_900_000_000L,
            quantization = "Q4_K_M",
            paramCount = "3B",
            minRamMb = 4500,
            languages = listOf("en", "ar", "hi", "ur"),
            category = ModelCategory.URDU_MULTILINGUAL,
            description = "Strong multilingual support — Urdu/Arabic/Hindi quality behtar hai."
        ),
        AiModel(
            id = "qwen2.5-coder-1.5b-q4",
            displayName = "Qwen2.5 Coder 1.5B (Q4_K_M)",
            huggingFaceRepo = "Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF",
            fileName = "qwen2.5-coder-1.5b-instruct-q4_k_m.gguf",
            downloadUrl = HuggingFaceConstants.resolveDownloadUrl(
                "Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF",
                "qwen2.5-coder-1.5b-instruct-q4_k_m.gguf"
            ),
            sizeBytes = 1_000_000_000L,
            quantization = "Q4_K_M",
            paramCount = "1.5B",
            minRamMb = 3000,
            languages = listOf("en"),
            category = ModelCategory.CODING,
            description = "Coding tasks ke liye specialised — chota aur fast."
        ),
        AiModel(
            id = "deepseek-r1-distill-qwen-1.5b-q4",
            displayName = "DeepSeek-R1-Distill-Qwen 1.5B (Q4_K_M)",
            huggingFaceRepo = "unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF",
            fileName = "DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf",
            downloadUrl = HuggingFaceConstants.resolveDownloadUrl(
                "unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF",
                "DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf"
            ),
            sizeBytes = 1_100_000_000L,
            quantization = "Q4_K_M",
            paramCount = "1.5B",
            minRamMb = 3500,
            languages = listOf("en"),
            category = ModelCategory.REASONING_DEEP_THINK,
            description = "Deep Thinking mode — step-by-step reasoning ke liye (responses slow par detailed)."
        )
    )

    fun findById(id: String): AiModel? = starterModels.find { it.id == id }

    fun byCategory(category: ModelCategory): List<AiModel> =
        starterModels.filter { it.category == category }
}
