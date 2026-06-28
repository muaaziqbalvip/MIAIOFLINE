// jni_bridge.cpp
//
// JNI implementation for com.miai.offline.inference.LlamaBridge.
//
// STATUS: Skeleton only. The functions below compile and run, but
// loadModel/generateStreaming currently return placeholder/echo behaviour
// rather than real LLM inference, because llama.cpp itself is not yet
// vendored into this project (see CMakeLists.txt comment block).
//
// To wire up real inference once llama.cpp is added as a submodule:
//   1. #include "llama.h"
//   2. In loadModel: call llama_model_load_from_file + llama_new_context_with_model,
//      store the llama_context* in the handle map below.
//   3. In generateStreaming: run the standard llama.cpp sampling loop,
//      calling env->CallVoidMethod on the onToken callback for each
//      decoded token piece.
//   4. In unloadModel: call llama_free + llama_model_free.
//
// This file intentionally keeps a clean Java<->C++ callback pattern in
// place so that swap-in is mechanical once llama.h is available.

#include <jni.h>
#include <string>
#include <unordered_map>
#include <mutex>
#include <android/log.h>

#define LOG_TAG "MiAiInference"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Placeholder "context" — once llama.cpp is integrated this becomes
// a real llama_context* stored per handle.
struct InferenceContext {
    std::string modelPath;
    int nThreads;
    int contextSize;
    bool cancelRequested = false;
};

static std::mutex g_contextsMutex;
static std::unordered_map<jlong, InferenceContext*> g_contexts;
static jlong g_nextHandle = 1;

extern "C" JNIEXPORT jlong JNICALL
Java_com_miai_offline_inference_LlamaBridge_loadModel(
        JNIEnv *env, jobject /* this */,
        jstring modelPath, jint nThreads, jint contextSize) {

    const char *pathChars = env->GetStringUTFChars(modelPath, nullptr);
    std::string path(pathChars);
    env->ReleaseStringUTFChars(modelPath, pathChars);

    LOGI("loadModel called for: %s (threads=%d, ctx=%d)", path.c_str(), nThreads, contextSize);

    // TODO (llama.cpp integration): replace this stub with:
    //   llama_model* model = llama_model_load_from_file(path.c_str(), model_params);
    //   llama_context* ctx = llama_new_context_with_model(model, ctx_params);
    // and store ctx instead of this placeholder struct.

    auto *context = new InferenceContext{path, nThreads, contextSize, false};

    std::lock_guard<std::mutex> lock(g_contextsMutex);
    jlong handle = g_nextHandle++;
    g_contexts[handle] = context;

    return handle;
}

extern "C" JNIEXPORT void JNICALL
Java_com_miai_offline_inference_LlamaBridge_unloadModel(
        JNIEnv *env, jobject /* this */, jlong contextHandle) {

    std::lock_guard<std::mutex> lock(g_contextsMutex);
    auto it = g_contexts.find(contextHandle);
    if (it != g_contexts.end()) {
        // TODO (llama.cpp integration): llama_free(ctx); llama_model_free(model);
        delete it->second;
        g_contexts.erase(it);
        LOGI("unloadModel: freed handle %lld", (long long) contextHandle);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_miai_offline_inference_LlamaBridge_cancelGeneration(
        JNIEnv *env, jobject /* this */, jlong contextHandle) {

    std::lock_guard<std::mutex> lock(g_contextsMutex);
    auto it = g_contexts.find(contextHandle);
    if (it != g_contexts.end()) {
        it->second->cancelRequested = true;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_miai_offline_inference_LlamaBridge_generateStreaming(
        JNIEnv *env, jobject /* this */,
        jlong contextHandle, jstring prompt, jint maxTokens, jfloat temperature,
        jobject onTokenCallback) {

    const char *promptChars = env->GetStringUTFChars(prompt, nullptr);
    std::string promptStr(promptChars);
    env->ReleaseStringUTFChars(prompt, promptChars);

    LOGI("generateStreaming: prompt length=%zu maxTokens=%d temp=%.2f",
         promptStr.size(), maxTokens, temperature);

    // Resolve the Kotlin lambda's invoke() method so we can call it per-token.
    jclass callbackClass = env->GetObjectClass(onTokenCallback);
    jmethodID invokeMethod = env->GetMethodID(
            callbackClass, "invoke", "(Ljava/lang/Object;)Ljava/lang/Object;");

    // TODO (llama.cpp integration): replace this placeholder loop with the
    // real llama.cpp sampling loop: tokenize promptStr, then repeatedly call
    // llama_decode + sample next token + llama_token_to_piece, emitting each
    // piece via the callback below, until EOS or maxTokens reached or
    // cancelRequested is set.

    std::string placeholderMsg =
        "[MI AI skeleton] llama.cpp native engine not yet linked — "
        "see CMakeLists.txt and jni_bridge.cpp TODOs to enable real offline inference.";

    for (char c : placeholderMsg) {
        std::string piece(1, c);
        jstring jPiece = env->NewStringUTF(piece.c_str());
        env->CallObjectMethod(onTokenCallback, invokeMethod, jPiece);
        env->DeleteLocalRef(jPiece);
    }
}
