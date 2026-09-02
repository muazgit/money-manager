package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun getUpdateUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_CUSTOM_UPDATE_URL, "")?.trim() ?: ""
        return if (saved.isNotEmpty()) saved else DEFAULT_UPDATE_CONFIG_URL
    }

    /**
     * Normalizes user-input GitHub URLs to raw.githubusercontent.com format if needed.
     * E.g. https://github.com/muazgit/money-manager/blob/main/version.json
     *   -> https://raw.githubusercontent.com/muazgit/money-manager/main/version.json
     * Or https://github.com/muazgit/money-manager
     *   -> https://raw.githubusercontent.com/muazgit/money-manager/main/version.json
     */
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

        // Return current version status (no update available or offline)
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

    fun openDownloadOrBrowser(context: Context, downloadUrl: String) {
        if (downloadUrl.isBlank()) {
            Toast.makeText(context, "No download link provided", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (downloadUrl.endsWith(".apk", ignoreCase = true)) {
                // Download using Android DownloadManager
                val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                    setTitle("Taka Manager Update")
                    setDescription("Downloading latest update APK...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        "taka-manager-update.apk"
                    )
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                }

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                dm?.enqueue(request)
                Toast.makeText(context, "Downloading update in background...", Toast.LENGTH_LONG).show()
            } else {
                // Open in web browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // Fallback to generic browser intent
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (err: Exception) {
                Toast.makeText(context, "Unable to open download URL: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
