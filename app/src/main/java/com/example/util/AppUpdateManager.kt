package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    data class Downloading(val progress: Float, val downloadedMb: String, val totalMb: String) : UpdateDownloadState()
    data class ReadyToInstall(val apkFile: File) : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}

data class AppUpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val currentVersionCode: Int = BuildConfig.VERSION_CODE,
    val currentVersionName: String = BuildConfig.VERSION_NAME,
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val isMandatory: Boolean = false
)

object AppUpdateManager {

    private const val PREFS_NAME = "taka_manager_update_prefs"
    private const val KEY_CUSTOM_UPDATE_URL = "custom_update_url"
    private const val KEY_IGNORED_VERSION = "ignored_version_code"

    // Default shared update metadata URL or GitHub raw configuration URL
    const val DEFAULT_UPDATE_CONFIG_URL = "https://raw.githubusercontent.com/muazgit/money-manager/main/version.json"
    private const val FALLBACK_UPDATE_CONFIG_URL = "https://raw.githubusercontent.com/abufiras985/taka-manager/main/version.json"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _downloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    val downloadState: StateFlow<UpdateDownloadState> = _downloadState.asStateFlow()

    fun resetDownloadState() {
        _downloadState.value = UpdateDownloadState.Idle
    }

    fun getUpdateUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_CUSTOM_UPDATE_URL, "")?.trim() ?: ""
        return if (saved.isNotEmpty()) saved else DEFAULT_UPDATE_CONFIG_URL
    }

    fun normalizeUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (url.isEmpty()) return DEFAULT_UPDATE_CONFIG_URL

        if (url.contains("github.com") && !url.contains("raw.githubusercontent.com")) {
            url = url.replace("https://github.com/", "https://raw.githubusercontent.com/")
                .replace("http://github.com/", "https://raw.githubusercontent.com/")
                .replace("/blob/", "/")
            if (!url.endsWith(".json")) {
                url = url.trimEnd('/') + "/main/version.json"
            }
        }
        return url
    }

    fun setUpdateUrl(context: Context, url: String) {
        val normalized = normalizeUrl(url)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_UPDATE_URL, normalized).apply()
    }

    suspend fun checkForUpdate(context: Context, isManual: Boolean = false): AppUpdateInfo = withContext(Dispatchers.IO) {
        val primaryUrl = getUpdateUrl(context)
        val urlsToTry = mutableListOf<String>()
        urlsToTry.add(primaryUrl)
        if (primaryUrl != DEFAULT_UPDATE_CONFIG_URL) {
            urlsToTry.add(DEFAULT_UPDATE_CONFIG_URL)
        }
        if (primaryUrl != FALLBACK_UPDATE_CONFIG_URL && DEFAULT_UPDATE_CONFIG_URL != FALLBACK_UPDATE_CONFIG_URL) {
            urlsToTry.add(FALLBACK_UPDATE_CONFIG_URL)
        }

        val currentCode = BuildConfig.VERSION_CODE
        val currentName = BuildConfig.VERSION_NAME

        var lastErrorMessage: String? = null

        for (url in urlsToTry) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "TakaManager-App/${currentName}")
                    .header("Cache-Control", "no-cache")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: ""
                        if (bodyString.isNotBlank()) {
                            val json = JSONObject(bodyString)
                            val latestCode = json.optInt("versionCode", json.optInt("latestVersionCode", currentCode))
                            val latestName = json.optString("versionName", json.optString("latestVersionName", currentName))
                            val releaseNotes = json.optString("releaseNotes", json.optString("changelog", "New improvements and fixes."))
                            val downloadUrl = json.optString("downloadUrl", json.optString("apkUrl", ""))
                            val isMandatory = json.optBoolean("mandatory", false)

                            val hasUpdate = latestCode > currentCode

                            return@withContext AppUpdateInfo(
                                hasUpdate = hasUpdate,
                                latestVersionCode = latestCode,
                                latestVersionName = latestName,
                                currentVersionCode = currentCode,
                                currentVersionName = currentName,
                                releaseNotes = releaseNotes,
                                downloadUrl = downloadUrl,
                                isMandatory = isMandatory
                            )
                        }
                    } else {
                        lastErrorMessage = "HTTP ${response.code}"
                    }
                }
            } catch (e: Exception) {
                lastErrorMessage = e.localizedMessage ?: "Connection failed"
            }
        }

        return@withContext AppUpdateInfo(
            hasUpdate = false,
            latestVersionCode = currentCode,
            latestVersionName = currentName,
            currentVersionCode = currentCode,
            currentVersionName = currentName,
            releaseNotes = if (lastErrorMessage != null) "Could not connect to update server ($lastErrorMessage)" else "You are already using the latest version.",
            downloadUrl = ""
        )
    }

    /**
     * Downloads the APK file directly with real-time progress and launches package installer upon completion.
     */
    suspend fun startDownloadAndAutoInstall(context: Context, downloadUrl: String) = withContext(Dispatchers.IO) {
        if (downloadUrl.isBlank()) {
            _downloadState.value = UpdateDownloadState.Error("No download link provided")
            return@withContext
        }

        // If URL doesn't look like an APK directly, open in browser as fallback
        if (!downloadUrl.endsWith(".apk", ignoreCase = true)) {
            withContext(Dispatchers.Main) {
                openBrowser(context, downloadUrl)
            }
            return@withContext
        }

        try {
            _downloadState.value = UpdateDownloadState.Downloading(0f, "0.0 MB", "...")

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "TakaManager-App/${BuildConfig.VERSION_NAME}")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _downloadState.value = UpdateDownloadState.Error("ডাউনলোড ব্যর্থ হয়েছে (HTTP ${response.code})")
                    return@use
                }

                val body = response.body
                if (body == null) {
                    _downloadState.value = UpdateDownloadState.Error("সার্ভার থেকে কোনো ফাইল পাওয়া যায়নি")
                    return@use
                }

                val contentLength = body.contentLength()
                val totalMbStr = if (contentLength > 0) String.format("%.1f MB", contentLength / (1024.0 * 1024.0)) else "Unknown size"

                // Destination update file in app cache
                val updateDir = File(context.cacheDir, "updates")
                if (!updateDir.exists()) {
                    updateDir.mkdirs()
                }
                val outputFile = File(updateDir, "taka_manager_update.apk")
                if (outputFile.exists()) {
                    outputFile.delete()
                }

                var totalBytesRead: Long = 0
                val buffer = ByteArray(8 * 1024)
                body.byteStream().use { input ->
                    FileOutputStream(outputFile).use { output ->
                        var bytesRead: Int
                        var lastProgressReport = 0f

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead

                            if (contentLength > 0) {
                                val progress = (totalBytesRead.toFloat() / contentLength).coerceIn(0f, 1f)
                                if (progress - lastProgressReport >= 0.02f || progress >= 1f) {
                                    lastProgressReport = progress
                                    val downloadedMbStr = String.format("%.1f MB", totalBytesRead / (1024.0 * 1024.0))
                                    _downloadState.value = UpdateDownloadState.Downloading(progress, downloadedMbStr, totalMbStr)
                                }
                            }
                        }
                        output.flush()
                    }
                }

                _downloadState.value = UpdateDownloadState.ReadyToInstall(outputFile)

                // Automatically prompt installation immediately on main thread!
                withContext(Dispatchers.Main) {
                    installApk(context, outputFile)
                }
            }
        } catch (e: Exception) {
            _downloadState.value = UpdateDownloadState.Error("ডাউনলোড ব্যর্থ হয়েছে: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Prompts the Android Package Installer with the downloaded APK.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "APK ফাইল পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                return
            }

            // On Android 8.0+ (Oreo+), verify if app can request package installations
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(
                        context,
                        "আপডেট সম্পূর্ণ করতে অনুমতি দিন",
                        Toast.LENGTH_LONG
                    ).show()
                    val manageIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(manageIntent)
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "ইনস্টল শুরু করা যায়নি: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun openDownloadOrBrowser(context: Context, downloadUrl: String) {
        if (downloadUrl.isBlank()) {
            Toast.makeText(context, "No download link provided", Toast.LENGTH_SHORT).show()
            return
        }
        openBrowser(context, downloadUrl)
    }

    fun openBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

