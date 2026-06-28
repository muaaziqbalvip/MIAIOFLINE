package com.miai.offline.data.network

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Minimal HuggingFace Hub API client.
 * Used to fetch real file sizes (MB/GB) for GGUF files before download,
 * matching the requirement: "ye san show kre ga download fast krwaye".
 */
interface HuggingFaceApi {

    @GET("api/models/{repoId}")
    suspend fun getModelInfo(@Path("repoId", encoded = true) repoId: String): HfModelInfoResponse
}

data class HfModelInfoResponse(
    val id: String,
    val siblings: List<HfFileSibling>?
)

data class HfFileSibling(
    val rfilename: String
)

/**
 * HuggingFace doesn't always return file size in the model-info endpoint reliably for
 * all repo types, so the app also supports a HEAD request fallback (see HfSizeFetcher)
 * to read the Content-Length header directly from the resolve/main download URL.
 */
object HuggingFaceConstants {
    const val BASE_URL = "https://huggingface.co/"

    fun resolveDownloadUrl(repo: String, fileName: String): String {
        return "$BASE_URL$repo/resolve/main/$fileName"
    }
}
