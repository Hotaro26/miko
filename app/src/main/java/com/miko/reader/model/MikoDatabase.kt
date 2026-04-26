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
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

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

@Database(entities = [HistoryEntry::class, FavouriteManga::class], version = 2, exportSchema = false)
abstract class MikoDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favouriteDao(): FavouriteDao
}
