package com.miai.offline.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.miai.offline.R

/**
 * Keeps large model downloads (can be multiple GB) running even if the
 * user backgrounds the app, with a persistent progress notification.
 */
class ModelDownloadService : Service() {

    companion object {
        const val CHANNEL_ID = "model_download_channel"
        const val NOTIFICATION_ID = 1001

        const val EXTRA_MODEL_NAME = "model_name"
        const val EXTRA_PROGRESS = "progress"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val modelName = intent?.getStringExtra(EXTRA_MODEL_NAME) ?: "Model"
        val progress = intent?.getIntExtra(EXTRA_PROGRESS, 0) ?: 0

        val notification = buildNotification(modelName, progress)
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    fun updateProgress(modelName: String, progress: Int) {
        val notification = buildNotification(modelName, progress)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(modelName: String, progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading $modelName")
            .setContentText("$progress% complete")
            .setProgress(100, progress, false)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Model Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress while AI models download"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
