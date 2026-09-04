package com.example.mesh

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ApkBeamerManager(private val context: Context) {

    /**
     * Extracts and packages Nomad's own APK file for zero-state Bluetooth or Wi-Fi beaming.
     */
    fun prepareNomadApkFile(): File? {
        return try {
            val sourceApkPath = context.applicationInfo.sourceDir
            val sourceFile = File(sourceApkPath)
            if (!sourceFile.exists()) return null

            // Target distribution directory in app's cache or files
            val exportDir = File(context.cacheDir, "apk_beamer")
            if (!exportDir.exists()) exportDir.mkdirs()

            val targetApk = File(exportDir, "Nomad-Survival-Node.apk")

            // Copy APK binary if not already cached or updated
            if (!targetApk.exists() || targetApk.length() != sourceFile.length()) {
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(targetApk).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            targetApk
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Gets a shareable content Uri using the registered FileProvider.
     */
    fun getApkContentUri(apkFile: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, apkFile)
    }

    /**
     * Creates an Intent to send the APK file directly via Bluetooth.
     */
    fun createBluetoothBeamIntent(apkUri: Uri): Intent {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            putExtra(Intent.EXTRA_SUBJECT, "Nomad Offline Survival Node APK")
            putExtra(Intent.EXTRA_TEXT, "Nomad mesh survival node installer. Install without app store to join ghost sync.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Prioritize Bluetooth component if available
            setPackage("com.android.bluetooth")
        }

        // Check if Bluetooth share receiver is available, otherwise fallback to system chooser
        val activities = context.packageManager.queryIntentActivities(intent, 0)
        return if (activities.isNotEmpty()) {
            intent
        } else {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Intent.createChooser(fallback, "Beam Nomad APK via Bluetooth / Nearby")
        }
    }

    fun getApkSizeDisplay(file: File?): String {
        if (file == null || !file.exists()) return "14.2 MB"
        val bytes = file.length()
        val mb = bytes.toDouble() / (1024 * 1024)
        return "%.1f MB".format(mb)
    }
}
