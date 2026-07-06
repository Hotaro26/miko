package com.miko.reader.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey val id: String, // Manga ID
    val title: String,
    val coverUrl: String?,
    val lastChapterId: String?,
    val lastChapterNum: String?,
    val lastPage: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history WHERE id = :id")
    suspend fun getHistoryById(id: String): HistoryEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntry)

    @Delete
    suspend fun delete(entry: HistoryEntry)
}

@Entity(tableName = "favourites")
data class FavouriteManga(
    @PrimaryKey val id: String,
    val title: String,
    val coverUrl: String?
)

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourites")
    fun getAllFavourites(): Flow<List<FavouriteManga>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE id = :id)")
    fun isFavourite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(manga: FavouriteManga)

    @Delete
    suspend fun delete(manga: FavouriteManga)
}

@Entity(tableName = "downloaded_chapters")
data class DownloadedChapter(
    @PrimaryKey val chapterId: String,
    val mangaId: String,
    val mangaTitle: String,
    val mangaCoverUrl: String?,
    val chapterTitle: String,
    val chapterNum: String,
    val totalPages: Int,
    val downloadedPages: Int,
    val isDownloadComplete: Boolean,
    val folderPath: String, // relative to context.filesDir
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloaded_chapters ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadedChapter>>

    @Query("SELECT * FROM downloaded_chapters WHERE mangaId = :mangaId ORDER BY timestamp DESC")
    fun getDownloadsForManga(mangaId: String): Flow<List<DownloadedChapter>>

    @Query("SELECT * FROM downloaded_chapters WHERE chapterId = :chapterId")
    suspend fun getDownloadByChapterId(chapterId: String): DownloadedChapter?
    
    @Query("SELECT * FROM downloaded_chapters WHERE chapterId = :chapterId")
    fun getDownloadByChapterIdFlow(chapterId: String): Flow<DownloadedChapter?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadedChapter)

    @Delete
    suspend fun delete(download: DownloadedChapter)
}

@Database(entities = [HistoryEntry::class, FavouriteManga::class, DownloadedChapter::class], version = 4, exportSchema = false)
abstract class MikoDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun downloadDao(): DownloadDao
}
