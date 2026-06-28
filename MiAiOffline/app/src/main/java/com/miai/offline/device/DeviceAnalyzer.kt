package com.miai.offline.device

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.miai.offline.data.model.DeviceProfile
import java.io.File

/**
 * Reads real device hardware info so the app can recommend models that will
 * actually run smoothly, and show the user how much storage is free
 * (internal vs SD card) before they pick where to save downloaded models.
 */
class DeviceAnalyzer(private val context: Context) {

    fun analyze(): DeviceProfile {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024 * 1024)).toInt()
        val availableRamMb = (memInfo.availMem / (1024 * 1024)).toInt()

        val internalStat = StatFs(Environment.getDataDirectory().path)
        val internalAvailableMb = (internalStat.availableBytes / (1024 * 1024))
        val internalTotalMb = (internalStat.totalBytes / (1024 * 1024))

        val sdCardInfo = detectSdCard()

        return DeviceProfile(
            totalRamMb = totalRamMb,
            availableRamMb = availableRamMb,
            totalStorageMb = internalTotalMb,
            availableInternalStorageMb = internalAvailableMb,
            availableSdCardStorageMb = sdCardInfo?.second,
            hasSdCard = sdCardInfo != null,
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
            cpuCoreCount = Runtime.getRuntime().availableProcessors(),
            supportsNeon = Build.SUPPORTED_ABIS.any { it.contains("arm64") || it.contains("armeabi-v7a") }
        )
    }

    /** Returns Pair(path, availableMb) for a real removable SD card, or null if none mounted. */
    private fun detectSdCard(): Pair<String, Long>? {
        val externalDirs = context.getExternalFilesDirs(null)
        for (dir in externalDirs) {
            if (dir == null) continue
            val isRemovable = Environment.isExternalStorageRemovable(dir)
            val isMounted = Environment.getExternalStorageState(dir) == Environment.MEDIA_MOUNTED
            // The first entry in getExternalFilesDirs is always primary (built-in) storage,
            // so we skip it — we only want a TRUE removable SD card.
            if (isRemovable && isMounted) {
                val stat = StatFs(dir.path)
                val availableMb = stat.availableBytes / (1024 * 1024)
                return Pair(dir.path, availableMb)
            }
        }
        return null
    }

    fun formatStorageSize(mb: Long): String {
        return if (mb >= 1024) {
            String.format("%.1f GB", mb / 1024.0)
        } else {
            "$mb MB"
        }
    }
}
