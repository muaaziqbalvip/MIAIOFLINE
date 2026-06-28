package com.miai.offline.ui

object Routes {
    const val HOME = "home"
    const val MODELS = "models"
    const val CHAT = "chat/{modelId}"

    fun chatRoute(modelId: String) = "chat/$modelId"
}
