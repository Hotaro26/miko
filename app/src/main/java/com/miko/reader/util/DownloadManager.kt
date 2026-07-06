package com.miko.reader.util

import android.content.Context
import com.miko.reader.api.MangaDexApi
import com.miko.reader.model.DownloadedChapter
import com.miko.reader.model.MikoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object DownloadManager {
    private val client = OkHttpClient()

    suspend fun downloadChapter(
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
        withContext(Dispatchers.IO) {
            try {
                // 1. Get image URLs
                val res = api.getAtHomeServer(chapterId)
                val base = res.baseUrl
                val hash = res.chapter.hash
                val images = res.chapter.data.map { "$base/data/$hash/$it" }
                
                val totalPages = images.size
                if (totalPages == 0) return@withContext

                // 2. Prepare folder
                val folder = File(context.filesDir, "downloads/$mangaId/$chapterId")
                if (!folder.exists()) folder.mkdirs()

                // 3. Initial DB entry
                var downloadedChapter = DownloadedChapter(
                    chapterId = chapterId,
                    mangaId = mangaId,
                    mangaTitle = mangaTitle,
                    mangaCoverUrl = mangaCoverUrl,
                    chapterTitle = chapterTitle,
                    chapterNum = chapterNum,
                    totalPages = totalPages,
                    downloadedPages = 0,
                    isDownloadComplete = false,
                    folderPath = folder.absolutePath
                )
                db.downloadDao().insert(downloadedChapter)

                // 4. Download each image
                for ((index, url) in images.withIndex()) {
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val file = File(folder, "${index}.jpg")
                        FileOutputStream(file).use { output ->
                            response.body?.byteStream()?.copyTo(output)
                        }
                        
                        // Update DB
                        downloadedChapter = downloadedChapter.copy(downloadedPages = index + 1)
                        db.downloadDao().insert(downloadedChapter)
                    }
                }
                
                // 5. Mark complete
                db.downloadDao().insert(downloadedChapter.copy(isDownloadComplete = true))
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
