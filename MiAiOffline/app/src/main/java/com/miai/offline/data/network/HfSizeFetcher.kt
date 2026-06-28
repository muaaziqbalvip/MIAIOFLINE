package com.miai.offline.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Sends a HEAD request to the HuggingFace resolve URL to read the exact
 * Content-Length, so the UI can show "2.1 GB" etc BEFORE the user
 * commits to downloading — required behaviour: "ye models kitne mb gb
 * ka he ye san show kre ga".
 */
class HfSizeFetcher(private val client: OkHttpClient) {

    suspend fun fetchSizeBytes(downloadUrl: String): Long? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .head()
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.header("Content-Length")?.toLongOrNull()
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
