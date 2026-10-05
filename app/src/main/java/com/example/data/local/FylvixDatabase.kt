package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "watchlist_entries")
data class WatchlistEntity(
    @PrimaryKey val key: String, // "${profileId}_${contentId}"
    val profileId: String,
    val contentId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_entries")
data class FavoriteEntity(
    @PrimaryKey val key: String, // "${profileId}_${contentId}"
    val profileId: String,
    val contentId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history_entries")
data class WatchHistoryEntity(
    @PrimaryKey val id: String, // "${profileId}_${contentId}_${episodeId ?: "main"}"
    val userId: String,
    val profileId: String,
    val contentId: String,
    val episodeId: String?,
    val episodeTitle: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val playbackPositionSec: Long,
    val durationSec: Long,
    val percentageWatched: Float,
    val isCompleted: Boolean,
    val lastWatchedTimestamp: Long
)

@Entity(tableName = "user_ratings")
data class UserRatingEntity(
    @PrimaryKey val key: String, // "${profileId}_${contentId}"
    val profileId: String,
    val contentId: String,
    val score: Double,
    val ratedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@Dao
interface FylvixDao {
    @Query("SELECT * FROM watchlist_entries WHERE profileId = :profileId ORDER BY addedAt DESC")
    fun observeWatchlist(profileId: String): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist_entries WHERE profileId = :profileId AND contentId = :contentId")
    suspend fun removeWatchlist(profileId: String, contentId: String)

    @Query("SELECT * FROM favorite_entries WHERE profileId = :profileId ORDER BY addedAt DESC")
    fun observeFavorites(profileId: String): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorite_entries WHERE profileId = :profileId AND contentId = :contentId")
    suspend fun removeFavorite(profileId: String, contentId: String)

    @Query("SELECT * FROM watch_history_entries WHERE profileId = :profileId ORDER BY lastWatchedTimestamp DESC")
    fun observeWatchHistory(profileId: String): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(entity: WatchHistoryEntity)

    @Query("DELETE FROM watch_history_entries WHERE id = :id")
    suspend fun deleteWatchHistoryItem(id: String)

    @Query("DELETE FROM watch_history_entries WHERE profileId = :profileId")
    suspend fun clearWatchHistory(profileId: String)

    @Query("SELECT * FROM user_ratings WHERE profileId = :profileId")
    fun observeRatings(profileId: String): Flow<List<UserRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRating(entity: UserRatingEntity)

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 12")
    fun observeSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearchQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}

@Database(
    entities = [
        WatchlistEntity::class,
        FavoriteEntity::class,
        WatchHistoryEntity::class,
        UserRatingEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FylvixDatabase : RoomDatabase() {
    abstract fun fylvixDao(): FylvixDao

    companion object {
        @Volatile
        private var INSTANCE: FylvixDatabase? = null

        fun getInstance(context: Context): FylvixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FylvixDatabase::class.java,
                    "fylvix_platform.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
