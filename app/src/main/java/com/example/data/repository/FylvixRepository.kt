package com.example.data.repository

import com.example.data.local.FavoriteEntity
import com.example.data.local.FylvixDao
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.UserRatingEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.models.WatchHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FylvixRepository(private val dao: FylvixDao) {

    fun observeWatchlistIds(profileId: String): Flow<Set<String>> =
        dao.observeWatchlist(profileId).map { list -> list.map { it.contentId }.toSet() }

    suspend fun toggleWatchlist(profileId: String, contentId: String, currentlyInWatchlist: Boolean) {
        if (currentlyInWatchlist) {
            dao.removeWatchlist(profileId, contentId)
        } else {
            dao.insertWatchlist(
                WatchlistEntity(
                    key = "${profileId}_${contentId}",
                    profileId = profileId,
                    contentId = contentId
                )
            )
        }
    }

    fun observeFavoriteIds(profileId: String): Flow<Set<String>> =
        dao.observeFavorites(profileId).map { list -> list.map { it.contentId }.toSet() }

    suspend fun toggleFavorite(profileId: String, contentId: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            dao.removeFavorite(profileId, contentId)
        } else {
            dao.insertFavorite(
                FavoriteEntity(
                    key = "${profileId}_${contentId}",
                    profileId = profileId,
                    contentId = contentId
                )
            )
        }
    }

    fun observeWatchHistory(profileId: String): Flow<List<WatchHistoryItem>> =
        dao.observeWatchHistory(profileId).map { entities ->
            entities.map { e ->
                WatchHistoryItem(
                    id = e.id,
                    userId = e.userId,
                    profileId = e.profileId,
                    contentId = e.contentId,
                    episodeId = e.episodeId,
                    episodeTitle = e.episodeTitle,
                    seasonNumber = e.seasonNumber,
                    episodeNumber = e.episodeNumber,
                    playbackPositionSec = e.playbackPositionSec,
                    durationSec = e.durationSec,
                    percentageWatched = e.percentageWatched,
                    isCompleted = e.isCompleted,
                    lastWatchedTimestamp = e.lastWatchedTimestamp
                )
            }
        }

    suspend fun recordPlaybackProgress(
        userId: String,
        profileId: String,
        contentId: String,
        episodeId: String?,
        episodeTitle: String?,
        seasonNumber: Int?,
        episodeNumber: Int?,
        positionSec: Long,
        durationSec: Long
    ) {
        val safeDuration = durationSec.coerceAtLeast(1L)
        val pct = ((positionSec.toFloat() / safeDuration.toFloat()) * 100f).coerceIn(0f, 100f)
        val completed = pct >= 92f
        val entryId = "${profileId}_${contentId}_${episodeId ?: "main"}"
        dao.upsertWatchHistory(
            WatchHistoryEntity(
                id = entryId,
                userId = userId,
                profileId = profileId,
                contentId = contentId,
                episodeId = episodeId,
                episodeTitle = episodeTitle,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                playbackPositionSec = positionSec,
                durationSec = safeDuration,
                percentageWatched = pct,
                isCompleted = completed,
                lastWatchedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeWatchHistoryEntry(id: String) = dao.deleteWatchHistoryItem(id)

    suspend fun clearWatchHistory(profileId: String) = dao.clearWatchHistory(profileId)

    fun observeUserRatings(profileId: String): Flow<Map<String, Double>> =
        dao.observeRatings(profileId).map { list -> list.associate { it.contentId to it.score } }

    suspend fun rateContent(profileId: String, contentId: String, score: Double) {
        dao.upsertRating(
            UserRatingEntity(
                key = "${profileId}_${contentId}",
                profileId = profileId,
                contentId = contentId,
                score = score
            )
        )
    }

    fun observeSearchHistory(): Flow<List<String>> =
        dao.observeSearchHistory().map { list -> list.map { it.query } }

    suspend fun saveSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            dao.insertSearchQuery(SearchHistoryEntity(query = trimmed))
        }
    }

    suspend fun removeSearchQuery(query: String) = dao.deleteSearchQuery(query)

    suspend fun clearSearchHistory() = dao.clearSearchHistory()
}
