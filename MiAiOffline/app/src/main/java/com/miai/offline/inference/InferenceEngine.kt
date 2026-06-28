package com.miai.offline.inference

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ResponseMode {
    FAST,           // Normal quick response
    DEEP_THINKING   // Step-by-step reasoning before final answer (slower, more detailed)
}

/**
 * Wraps the raw LlamaBridge with prompt templating, so callers just pass
 * user text + mode and get clean streamed output. This is where the
 * "Fast Deep Thinking section" requirement is implemented: Deep Thinking mode
 * injects a reasoning instruction before the user prompt.
 */
class InferenceEngine {

    private var activeContextHandle: Long = -1
    private var loadedModelPath: String? = null

    fun loadModel(modelPath: String, threadCount: Int = 4, contextSize: Int = 2048): Boolean {
        if (loadedModelPath == modelPath && activeContextHandle != -1L) return true

        unloadCurrentModel()
        val handle = LlamaBridge.loadModel(modelPath, threadCount, contextSize)
        return if (handle != -1L) {
            activeContextHandle = handle
            loadedModelPath = modelPath
            true
        } else {
            false
        }
    }

    fun unloadCurrentModel() {
        if (activeContextHandle != -1L) {
            LlamaBridge.unloadModel(activeContextHandle)
            activeContextHandle = -1
            loadedModelPath = null
        }
    }

    fun chat(
        userMessage: String,
        mode: ResponseMode,
        languageHint: String? = null  // "ur", "en", "hi", "ar" - helps model respond in same language
    ): Flow<String> {
        val prompt = buildPrompt(userMessage, mode, languageHint)
        val maxTokens = if (mode == ResponseMode.DEEP_THINKING) 1024 else 384
        val temperature = if (mode == ResponseMode.DEEP_THINKING) 0.3f else 0.7f

        return LlamaBridge.generateFlow(
            contextHandle = activeContextHandle,
            prompt = prompt,
            maxTokens = maxTokens,
            temperature = temperature
        )
    }

    private fun buildPrompt(userMessage: String, mode: ResponseMode, languageHint: String?): String {
        val languageInstruction = when (languageHint) {
            "ur" -> "Respond in Urdu (اردو) unless the user writes in another language."
            "hi" -> "Respond in Hindi unless the user writes in another language."
            "ar" -> "Respond in Arabic unless the user writes in another language."
            else -> "Respond in the same language the user writes in."
        }

        val systemInstruction = when (mode) {
            ResponseMode.DEEP_THINKING ->
                "You are MI AI, an offline assistant. $languageInstruction " +
                "Think through the problem step by step before giving your final answer. " +
                "Show brief reasoning, then clearly state the final answer."
            ResponseMode.FAST ->
                "You are MI AI, an offline assistant. $languageInstruction " +
                "Give a direct, concise, helpful answer."
        }

        return "<|system|>\n$systemInstruction\n<|user|>\n$userMessage\n<|assistant|>\n"
    }

    fun isModelLoaded(): Boolean = activeContextHandle != -1L
}
