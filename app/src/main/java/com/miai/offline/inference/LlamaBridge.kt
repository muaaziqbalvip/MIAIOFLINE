package com.miai.offline.inference

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * Kotlin-side bridge to the native llama.cpp engine (see app/src/main/cpp/).
 * This is the layer that gives "fast full fast response" — actual token
 * generation happens in C++ for speed; this class just exposes it to Kotlin
 * as a Flow so the chat UI can stream tokens in as they're generated.
 *
 * NOTE: This skeleton declares the JNI contract. To make it fully functional,
 * llama.cpp must be added as a git submodule / CMake dependency in
 * app/src/main/cpp/CMakeLists.txt (see README "Next Steps" — this is the
 * single biggest remaining integration step).
 */
object LlamaBridge {

    init {
        System.loadLibrary("miai_inference")
    }

    /** Loads a GGUF model from disk into memory. Returns a context handle, or -1 on failure. */
    external fun loadModel(modelPath: String, nThreads: Int, contextSize: Int): Long

    /** Frees a loaded model context. Always call when switching models or closing chat. */
    external fun unloadModel(contextHandle: Long)

    /**
     * Streams generated tokens one at a time via the [onToken] callback as they're produced,
     * so the UI can show text appearing live ("palak jhapakte fast response").
     * Returns once generation finishes (EOS token or maxTokens reached).
     */
    external fun generateStreaming(
        contextHandle: Long,
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        onToken: (String) -> Unit
    )

    external fun cancelGeneration(contextHandle: Long)

    /** High-level Kotlin Flow wrapper around the native streaming callback. */
    fun generateFlow(
        contextHandle: Long,
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f
    ): Flow<String> = callbackFlow {
        generateStreaming(contextHandle, prompt, maxTokens, temperature) { token ->
            trySend(token)
        }
        close()
    }.flowOn(Dispatchers.Default)
}
