package com.example.data.models

import com.example.R

enum class MainNavDestination(val route: String, val label: String) {
    HOME("/", "Home"),
    MOVIES("/movies", "Movies"),
    TV_SHOWS("/tv", "TV Shows"),
    SERIES("/series", "Series"),
    ANIME("/anime", "Anime"),
    LIVE_TV("/live", "Live TV"),
    ADULT("/adult", "18+"),
    GENRES("/genres", "Genres"),
    COUNTRIES("/countries", "Countries"),
    LANGUAGES("/languages", "Languages"),
    SEARCH("/search", "Search"),
    MY_LIST("/my-list", "My List"),
    HISTORY("/history", "History"),
    PROFILE("/profile", "Profile"),
    SETTINGS("/settings", "Settings"),
    ADMIN("/admin", "Admin Panel")
}

enum class ContentKind(val displayName: String) {
    MOVIE("Movie"),
    TV_SHOW("TV Show"),
    SERIES("Series"),
    ANIME_SERIES("Anime Series"),
    ANIME_MOVIE("Anime Movie"),
    DOCUMENTARY("Documentary"),
    KIDS("Kids & Family"),
    ADULT_MOVIE("18+ Movie"),
    ADULT_SERIES("18+ Series")
}

data class CastMember(
    val id: String,
    val name: String,
    val characterName: String,
    val roleType: String = "Actor",
    val country: String = "International",
    val avatarColorHex: Long = 0xFFE50938
)

data class CrewMember(
    val id: String,
    val name: String,
    val job: String, // Director, Writer, Composer, Cinematographer
    val department: String
)

data class AnimeCharacter(
    val id: String,
    val name: String,
    val japaneseVoiceActor: String,
    val englishVoiceActor: String,
    val role: String = "Main"
)

data class SubtitleTrack(
    val id: String,
    val languageCode: String,
    val label: String,
    val vttUrl: String = "/cdn/subtitles/en.vtt"
)

data class AudioTrackItem(
    val id: String,
    val languageCode: String,
    val label: String,
    val channels: String = "Dolby 5.1"
)

data class VideoQualitySource(
    val quality: String, // "Auto (HLS)", "4K UHD", "1080p FHD", "720p HD", "480p SD"
    val bitrateKbps: Int,
    val codec: String = "HEVC / H.264",
    val manifestUrl: String
)

data class EpisodeItem(
    val id: String,
    val showId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val description: String,
    val airDate: String,
    val runtimeMinutes: Int,
    val rating: Double,
    val accentColorHex: Long = 0xFFE50938,
    val streamUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
)

data class SeasonItem(
    val id: String,
    val showId: String,
    val seasonNumber: Int,
    val title: String,
    val overview: String,
    val airDate: String,
    val episodes: List<EpisodeItem>
) {
    val episodeCount: Int get() = episodes.size
}

data class MediaItem(
    val id: String,
    val title: String,
    val originalTitle: String,
    val alternativeTitles: List<String> = emptyList(),
    val tagline: String = "",
    val overview: String,
    val kind: ContentKind,
    val releaseYear: Int,
    val releaseDate: String,
    val runtimeMinutes: Int,
    val rating: Double,
    val voteCount: Int,
    val genres: List<String>,
    val country: String,
    val countryCode: String,
    val language: String,
    val languageCode: String,
    val regionalHub: String, // Hollywood, Bollywood, Pakistani, Turkish, Korean, Japanese, Arabic, European, African, Asian Cinema, International
    val certification: String, // G, PG, PG-13, R, TV-14, TV-MA, 18+
    val classification: String, // Standard, Teen, Mature, 18+
    val qualityBadge: String = "4K UHD",
    val status: String = "Released", // Now Playing, Upcoming, Airing Today, On Air, Completed, Ongoing
    val director: String,
    val writers: List<String>,
    val productionCompanies: List<String> = listOf("Fylvix Studios Global", "Aethelgard Pictures"),
    val cast: List<CastMember> = emptyList(),
    val crew: List<CrewMember> = emptyList(),
    val characters: List<AnimeCharacter> = emptyList(),
    val seasons: List<SeasonItem> = emptyList(),
    val isDubbed: Boolean = true,
    val isSubbed: Boolean = true,
    val subtitles: List<SubtitleTrack> = listOf(
        SubtitleTrack("sub_en", "en", "English [CC]"),
        SubtitleTrack("sub_es", "es", "Español"),
        SubtitleTrack("sub_ar", "ar", "العربية"),
        SubtitleTrack("sub_ur", "ur", "اردو"),
        SubtitleTrack("sub_hi", "hi", "हिन्दी"),
        SubtitleTrack("sub_tr", "tr", "Türkçe"),
        SubtitleTrack("sub_ko", "ko", "한국어"),
        SubtitleTrack("sub_ja", "ja", "日本語"),
        SubtitleTrack("sub_fr", "fr", "Français")
    ),
    val audioTracks: List<AudioTrackItem> = listOf(
        AudioTrackItem("aud_orig", languageCode, "$language (Original) • Dolby Atmos"),
        AudioTrackItem("aud_en", "en", "English Dub • 5.1 Surround"),
        AudioTrackItem("aud_es", "es", "Español • Stereo"),
        AudioTrackItem("aud_hi", "hi", "Hindi / Urdu • 5.1")
    ),
    val videoQualities: List<VideoQualitySource> = listOf(
        VideoQualitySource("Auto (Adaptive HLS)", 14500, "ABR HLS", "https://media-cdn.fylvix.global/hls/$id/master.m3u8"),
        VideoQualitySource("4K UHD", 24000, "HEVC HDR10", "https://media-cdn.fylvix.global/hls/$id/2160p.m3u8"),
        VideoQualitySource("1080p FHD", 8500, "H.264 High", "https://media-cdn.fylvix.global/hls/$id/1080p.m3u8"),
        VideoQualitySource("720p HD", 4200, "H.264 Main", "https://media-cdn.fylvix.global/hls/$id/720p.m3u8"),
        VideoQualitySource("480p Data Saver", 1800, "H.264 Base", "https://media-cdn.fylvix.global/hls/$id/480p.m3u8")
    ),
    val trailerUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
    val authorizedStreamUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    val heroDrawableRes: Int = R.drawable.img_hero_cyber_odyssey,
    val gradientColors: List<Long> = listOf(0xFF1E1B4B, 0xFFE50938),
    val isFeatured: Boolean = false,
    val isTrendingToday: Boolean = false,
    val isTrendingWeek: Boolean = false,
    val isPopular: Boolean = false,
    val isTopRated: Boolean = false,
    val isUpcoming: Boolean = false,
    val isAdult18Plus: Boolean = false,
    val isPublished: Boolean = true,
    val showOnHomepage: Boolean = true,
    val showInSearch: Boolean = true,
    val allowedRegions: List<String> = listOf("GLOBAL")
)

data class ChannelProgram(
    val id: String,
    val title: String,
    val timeSlot: String,
    val category: String,
    val description: String,
    val isCurrent: Boolean = false
)

data class LiveTvChannel(
    val id: String,
    val name: String,
    val code: String,
    val category: String, // News, Sports, Entertainment, Movies, Music, Kids, Documentary, International
    val country: String,
    val countryCode: String,
    val language: String,
    val badgeColorHex: Long,
    val currentProgram: String,
    val nextProgram: String,
    val viewersCount: String,
    val schedule: List<ChannelProgram>,
    val authorizedStreamUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
    val isLive: Boolean = true,
    val isPublished: Boolean = true
)

data class WatchHistoryItem(
    val id: String,
    val userId: String,
    val profileId: String,
    val contentId: String,
    val episodeId: String? = null,
    val episodeTitle: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val playbackPositionSec: Long,
    val durationSec: Long,
    val percentageWatched: Float,
    val isCompleted: Boolean,
    val lastWatchedTimestamp: Long
)

data class UserProfile(
    val id: String,
    val userId: String,
    val name: String,
    val usernameHandle: String,
    val avatarColorHex: Long,
    val avatarEmoji: String,
    val isKidsProfile: Boolean = false,
    val allowAdultContent: Boolean = true,
    val preferredLanguage: String = "English"
)

data class UserAccount(
    val id: String,
    val email: String,
    val username: String,
    val role: String = "admin", // "user", "admin"
    val emailVerified: Boolean = true,
    val isLoggedIn: Boolean = true
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val category: String, // New Movie, New Episode, New Series, Recommended, System
    val timestampLabel: String,
    val contentId: String? = null,
    val isRead: Boolean = false
)

data class ContentReport(
    val id: String,
    val contentId: String,
    val contentTitle: String,
    val reason: String,
    val details: String,
    val submittedBy: String,
    val status: String = "Open",
    val createdAt: String = "Just now"
)

data class SignedPlaybackSession(
    val contentId: String,
    val episodeId: String?,
    val signedToken: String,
    val expiresAtEpochSec: Long,
    val cdnNode: String,
    val streamFormat: String,
    val authorizedManifestUrl: String
)

data class MetadataProviderConfig(
    val id: String,
    val name: String,
    val endpointUrl: String,
    val adapterType: String, // "TMDB-Compatible Adapter", "Fylvix Global Sync v2", "AniList/Jikan Bridge", "EPG XMLTV Adapter"
    val isEnabled: Boolean,
    val lastSyncedAt: String,
    val syncedRecordsCount: Int
)

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val actor: String,
    val action: String,
    val target: String,
    val severity: String = "INFO"
)

data class PlatformSettings(
    val themeMode: String = "Cinematic Dark", // Cinematic Dark, Midnight Gold, Studio Light
    val appLanguage: String = "English",
    val autoplayNextEpisode: Boolean = true,
    val autoplayPreviews: Boolean = true,
    val defaultQuality: String = "Auto (Adaptive HLS)",
    val preferredSubtitleLanguage: String = "English [CC]",
    val preferredAudioLanguage: String = "Original Audio",
    val notificationsNewMovies: Boolean = true,
    val notificationsNewEpisodes: Boolean = true,
    val notificationsRecommendations: Boolean = true,
    // Backend / Admin Configurable 18+ Policy Controls (No repetitive age popup on every visit)
    val backendAdultCategoryEnabled: Boolean = true,
    val adultAllowedInSearch: Boolean = true,
    val adultAllowedOnHomepage: Boolean = true,
    val activeRegionCode: String = "GLOBAL"
)

data class FilterState(
    val query: String = "",
    val categoryTab: String = "All",
    val genre: String = "All",
    val country: String = "All",
    val language: String = "All",
    val year: String = "All",
    val minRating: Double = 0.0,
    val maxRuntime: Int = 300,
    val certification: String = "All",
    val quality: String = "All",
    val sortBy: String = "Popularity", // Popularity, Latest, Rating, Title A-Z, Runtime
    val audioType: String = "All" // All, Subbed, Dubbed
)
