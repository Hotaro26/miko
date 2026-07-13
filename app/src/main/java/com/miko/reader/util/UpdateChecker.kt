package com.miko.reader.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.miko.reader.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object UpdateChecker {
    private const val REPO = "Hotaro26/miko"
    private const val API_URL = "https://api.github.com/repos/$REPO/releases/latest"

    suspend fun checkUpdate(context: Context, showToastIfLatest: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val response = URL(API_URL).readText()
                val json = JSONObject(response)
                val latestVersion = json.getString("tag_name").replace("v", "")
                val currentVersion = BuildConfig.VERSION_NAME.replace("v", "")

                if (isNewerVersion(currentVersion, latestVersion)) {
                    val releaseUrl = json.getString("html_url")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "New update available: v$latestVersion", Toast.LENGTH_LONG).show()
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                } else {
                    if (showToastIfLatest) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "You are on the latest version", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (showToastIfLatest) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to check for updates", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun isNewerVersion(current: String, latest: String): Boolean {
        val cParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val lParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val length = maxOf(cParts.size, lParts.size)
        for (i in 0 until length) {
            val c = cParts.getOrElse(i) { 0 }
            val l = lParts.getOrElse(i) { 0 }
            if (c < l) return true
            if (c > l) return false
        }
        return false
    }
}
