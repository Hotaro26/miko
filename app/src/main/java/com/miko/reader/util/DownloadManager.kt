package com.miko.reader.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.miko.reader.R
import com.miko.reader.api.MangaDexApi
import com.miko.reader.model.DownloadedChapter
import com.miko.reader.model.MikoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DownloadJob(
    val context: Context,
    val api: MangaDexApi,
    val db: MikoDatabase,
    val mangaId: String,
    val mangaTitle: String,
    val mangaCoverUrl: String?,
    val chapterId: String,
    val chapterTitle: String,
    val chapterNum: String
)

object DownloadManager {
    private val client = OkHttpClient()
    private val downloadQueue = Channel<DownloadJob>(Channel.UNLIMITED)
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()
    
    private const val CHANNEL_ID = "miko_downloads"
    private var notificationIdCounter = 100

    init {
        scope.launch {
            for (job in downloadQueue) {
                processJob(job)
            }
        }
    }

    private fun addLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _logs.value = _logs.value + "[$time] $message"
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun downloadChapter(
        context: Context,
        api: MangaDexApi,
        db: MikoDatabase,
        mangaId: String,
        mangaTitle: String,
        mangaCoverUrl: String?,
        chapterId: String,
        chapterTitle: String,
        chapterNum: String
    ) {
        createNotificationChannel(context)
        val job = DownloadJob(context, api, db, mangaId, mangaTitle, mangaCoverUrl, chapterId, chapterTitle, chapterNum)
        addLog("QUEUED: $mangaTitle - Ch $chapterNum")
        downloadQueue.trySend(job)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Downloads"
            val descriptionText = "Manga chapter download progress"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private suspend fun processJob(job: DownloadJob) {
        val notifId = notificationIdCounter++
        val notifBuilder = NotificationCompat.Builder(job.context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading ${job.mangaTitle}")
            .setContentText("Chapter ${job.chapterNum} (0/0)")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, true)

        val notificationManager = NotificationManagerCompat.from(job.context)
        
        addLog("STARTING: ${job.mangaTitle} - Ch ${job.chapterNum}")

        try {
            // Check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        job.context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManager.notify(notifId, notifBuilder.build())
                }
            } else {
                notificationManager.notify(notifId, notifBuilder.build())
            }

            // 1. Get image URLs
            val res = job.api.getAtHomeServer(job.chapterId)
            val base = res.baseUrl
            val hash = res.chapter.hash
            val images = res.chapter.data.map { "$base/data/$hash/$it" }
            
            val totalPages = images.size
            if (totalPages == 0) {
                addLog("ERROR: No pages found for ${job.mangaTitle} - Ch ${job.chapterNum}")
                notificationManager.cancel(notifId)
                return
            }

            // 2. Prepare folder
            val folder = File(job.context.filesDir, "downloads/${job.mangaId}/${job.chapterId}")
            if (!folder.exists()) folder.mkdirs()

            // 3. Initial DB entry
            var downloadedChapter = DownloadedChapter(
                chapterId = job.chapterId,
                mangaId = job.mangaId,
                mangaTitle = job.mangaTitle,
                mangaCoverUrl = job.mangaCoverUrl,
                chapterTitle = job.chapterTitle,
                chapterNum = job.chapterNum,
                totalPages = totalPages,
                downloadedPages = 0,
                isDownloadComplete = false,
                folderPath = folder.absolutePath
            )
            job.db.downloadDao().insert(downloadedChapter)

            addLog("FETCHING: ${images.size} pages for ${job.mangaTitle}")

            // 4. Download each image
            for ((index, url) in images.withIndex()) {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val file = File(folder, "${index}.jpg")
                    FileOutputStream(file).use { output ->
                        response.body?.byteStream()?.copyTo(output)
                    }
                    
                    // Update DB and Notification
                    downloadedChapter = downloadedChapter.copy(downloadedPages = index + 1)
                    job.db.downloadDao().insert(downloadedChapter)
                    
                    notifBuilder.setProgress(totalPages, index + 1, false)
                        .setContentText("Chapter ${job.chapterNum} (${index + 1}/$totalPages)")
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (androidx.core.content.ContextCompat.checkSelfPermission(job.context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            notificationManager.notify(notifId, notifBuilder.build())
                        }
                    } else {
                        notificationManager.notify(notifId, notifBuilder.build())
                    }
                } else {
                    addLog("FAILED PAGE ${index + 1} for ${job.mangaTitle}")
                }
            }
            
            // 5. Mark complete
            job.db.downloadDao().insert(downloadedChapter.copy(isDownloadComplete = true))
            addLog("COMPLETED: ${job.mangaTitle} - Ch ${job.chapterNum}")
            
            notifBuilder.setContentText("Chapter ${job.chapterNum} complete")
                .setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(job.context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(notifId, notifBuilder.build())
                }
            } else {
                notificationManager.notify(notifId, notifBuilder.build())
            }

        } catch (e: Exception) {
            e.printStackTrace()
            addLog("ERROR: ${e.message} for ${job.mangaTitle}")
            notifBuilder.setContentText("Download failed")
                .setProgress(0, 0, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(job.context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(notifId, notifBuilder.build())
                }
            } else {
                notificationManager.notify(notifId, notifBuilder.build())
            }
        }
    }
}
