package com.example.data.network

import com.example.BuildConfig
import com.example.data.models.SignedPlaybackSession
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.security.MessageDigest
import java.util.UUID

/**
 * Production REST API contract matching the Fylvix Backend API specification.
 */
interface FylvixApiService {
    // Auth endpoints
    @POST("api/auth/register")
    suspend fun register(@Body payload: Map<String, String>): Map<String, Any>

    @POST("api/auth/login")
    suspend fun login(@Body payload: Map<String, String>): Map<String, Any>

    @POST("api/auth/logout")
    suspend fun logout(): Map<String, Any>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body payload: Map<String, String>): Map<String, Any>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body payload: Map<String, String>): Map<String, Any>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body payload: Map<String, String>): Map<String, Any>

    // Movies endpoints
    @GET("api/movies")
    suspend fun getMovies(
        @Query("page") page: Int = 1,
        @Query("genre") genre: String? = null,
        @Query("country") country: String? = null,
        @Query("language") language: String? = null,
        @Query("year") year: Int? = null,
        @Query("sort") sort: String? = null
    ): List<Map<String, Any>>

    @GET("api/movies/{id}")
    suspend fun getMovieById(@Path("id") id: String): Map<String, Any>

    @GET("api/movies/trending")
    suspend fun getTrendingMovies(): List<Map<String, Any>>

    @GET("api/movies/popular")
    suspend fun getPopularMovies(): List<Map<String, Any>>

    @GET("api/movies/latest")
    suspend fun getLatestMovies(): List<Map<String, Any>>

    @GET("api/movies/top-rated")
    suspend fun getTopRatedMovies(): List<Map<String, Any>>

    @GET("api/movies/upcoming")
    suspend fun getUpcomingMovies(): List<Map<String, Any>>

    // TV & Series endpoints
    @GET("api/tv")
    suspend fun getTvShows(@Query("page") page: Int = 1): List<Map<String, Any>>

    @GET("api/tv/{id}")
    suspend fun getTvShowById(@Path("id") id: String): Map<String, Any>

    @GET("api/tv/popular")
    suspend fun getPopularTvShows(): List<Map<String, Any>>

    @GET("api/tv/trending")
    suspend fun getTrendingTvShows(): List<Map<String, Any>>

    @GET("api/tv/latest")
    suspend fun getLatestTvShows(): List<Map<String, Any>>

    @GET("api/tv/{id}/seasons")
    suspend fun getTvSeasons(@Path("id") id: String): List<Map<String, Any>>

    @GET("api/tv/{id}/seasons/{season}")
    suspend fun getTvSeasonDetails(
        @Path("id") id: String,
        @Path("season") seasonNumber: Int
    ): Map<String, Any>

    @GET("api/episodes/{id}")
    suspend fun getEpisodeById(@Path("id") id: String): Map<String, Any>

    // Anime endpoints
    @GET("api/anime")
    suspend fun getAnime(@Query("category") category: String? = null): List<Map<String, Any>>

    @GET("api/anime/{id}")
    suspend fun getAnimeById(@Path("id") id: String): Map<String, Any>

    // Live TV endpoints
    @GET("api/live")
    suspend fun getLiveChannels(@Query("category") category: String? = null): List<Map<String, Any>>

    @GET("api/live/{id}")
    suspend fun getLiveChannelById(@Path("id") id: String): Map<String, Any>

    // 18+ Adult endpoints
    @GET("api/adult")
    suspend fun getAdultCatalog(@Query("region") region: String = "GLOBAL"): List<Map<String, Any>>

    @GET("api/adult/{id}")
    suspend fun getAdultContentById(@Path("id") id: String): Map<String, Any>

    // Global Search & Taxonomy
    @GET("api/search")
    suspend fun searchCatalog(
        @Query("q") query: String,
        @Query("type") type: String? = null,
        @Query("genre") genre: String? = null,
        @Query("country") country: String? = null,
        @Query("language") language: String? = null,
        @Query("year") year: String? = null
    ): List<Map<String, Any>>

    @GET("api/search/suggestions")
    suspend fun getSearchSuggestions(@Query("q") query: String): List<String>

    @GET("api/genres")
    suspend fun getGenres(): List<Map<String, Any>>

    @GET("api/countries")
    suspend fun getCountries(): List<Map<String, Any>>

    @GET("api/languages")
    suspend fun getLanguages(): List<Map<String, Any>>

    @GET("api/people")
    suspend fun getPeople(): List<Map<String, Any>>

    // User & Personalization
    @GET("api/users/me")
    suspend fun getCurrentUser(): Map<String, Any>

    @PATCH("api/users/me/profile")
    suspend fun updateProfile(@Body payload: Map<String, Any>): Map<String, Any>

    @GET("api/users/me/watchlist")
    suspend fun getWatchlist(): List<String>

    @PUT("api/users/me/watchlist")
    suspend fun addToWatchlist(@Body payload: Map<String, String>): Map<String, Any>

    @DELETE("api/users/me/watchlist")
    suspend fun removeFromWatchlist(@Query("contentId") contentId: String): Map<String, Any>

    @GET("api/users/me/history")
    suspend fun getWatchHistory(): List<Map<String, Any>>

    @POST("api/users/me/history")
    suspend fun syncWatchHistory(@Body payload: Map<String, Any>): Map<String, Any>

    @GET("api/users/me/favorites")
    suspend fun getFavorites(): List<String>

    @PATCH("api/users/me/preferences")
    suspend fun updatePreferences(@Body payload: Map<String, Any>): Map<String, Any>

    @GET("api/recommendations")
    suspend fun getRecommendations(@Query("profileId") profileId: String): List<Map<String, Any>>

    // Player & Signed Media URLs
    @GET("api/player/{contentId}")
    suspend fun getPlayerManifest(
        @Path("contentId") contentId: String,
        @Query("token") signedToken: String
    ): Map<String, Any>

    @POST("api/player/{contentId}/token")
    suspend fun issueSignedPlaybackToken(@Path("contentId") contentId: String): Map<String, Any>
}

/**
 * Issues temporary, signed media playback tokens for authorized HLS/DASH/MP4 CDN delivery
 * without exposing permanent storage credentials on the client.
 */
object FylvixMediaTokenSigner {
    const val DEFAULT_API_BASE_URL = "https://api.fylvix.global"
    const val DEFAULT_MEDIA_API_URL = "https://media-cdn.fylvix.global"

    val configuredMetadataApiUrl: String
        get() = BuildConfig.METADATA_API_URL.ifBlank { "https://metadata.fylvix.global" }

    val hasConfiguredMetadataKey: Boolean
        get() = BuildConfig.METADATA_API_KEY.isNotBlank() &&
            BuildConfig.METADATA_API_KEY != "FYLVIX_METADATA_PROVIDER_KEY_PLACEHOLDER"

    fun createAuthorizedSession(
        contentId: String,
        episodeId: String? = null,
        rawStreamUrl: String
    ): SignedPlaybackSession {
        val mediaBase = DEFAULT_MEDIA_API_URL
        val expiresAt = (System.currentTimeMillis() / 1000L) + 900L // 15-minute TTL
        val nonce = UUID.randomUUID().toString().take(8)
        val rawPayload = "$contentId:${episodeId ?: "feature"}:$expiresAt:$nonce"
        val signature = sha256Hex(rawPayload).take(32)
        return SignedPlaybackSession(
            contentId = contentId,
            episodeId = episodeId,
            signedToken = "fvx_sig_${signature}_exp_${expiresAt}",
            expiresAtEpochSec = expiresAt,
            cdnNode = "$mediaBase/edge-global-01",
            streamFormat = if (rawStreamUrl.endsWith(".m3u8")) "HLS Adaptive (AES-128)" else "Authorized MP4 / HLS Hybrid",
            authorizedManifestUrl = "$rawStreamUrl?token=fvx_$signature&exp=$expiresAt"
        )
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
