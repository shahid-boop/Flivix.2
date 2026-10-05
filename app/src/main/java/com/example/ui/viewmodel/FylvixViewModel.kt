package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.FylvixDatabase
import com.example.data.models.AppNotification
import com.example.data.models.AuditLogEntry
import com.example.data.models.CastMember
import com.example.data.models.ChannelProgram
import com.example.data.models.ContentKind
import com.example.data.models.ContentReport
import com.example.data.models.CrewMember
import com.example.data.models.EpisodeItem
import com.example.data.models.FilterState
import com.example.data.models.LiveTvChannel
import com.example.data.models.MainNavDestination
import com.example.data.models.MediaItem
import com.example.data.models.MetadataProviderConfig
import com.example.data.models.PlatformSettings
import com.example.data.models.SeasonItem
import com.example.data.models.SignedPlaybackSession
import com.example.data.models.UserAccount
import com.example.data.models.UserProfile
import com.example.data.models.WatchHistoryItem
import com.example.data.network.FylvixMediaTokenSigner
import com.example.data.repository.FylvixRepository
import com.example.data.seed.FylvixSeedCatalog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivePlayerState(
    val mediaItem: MediaItem? = null,
    val episode: EpisodeItem? = null,
    val liveChannel: LiveTvChannel? = null,
    val signedSession: SignedPlaybackSession,
    val isPlaying: Boolean = true,
    val isBuffering: Boolean = false,
    val hasNetworkError: Boolean = false,
    val isFullscreen: Boolean = false,
    val isPipMode: Boolean = false,
    val currentPositionSec: Long = 0L,
    val durationSec: Long = 8520L,
    val volume: Float = 0.85f,
    val isMuted: Boolean = false,
    val selectedQuality: String = "Auto (Adaptive HLS)",
    val selectedSubtitle: String = "English [CC]",
    val selectedAudioTrack: String = "Original Audio • Dolby Atmos",
    val playbackSpeed: Float = 1.0f
)

data class AnalyticsMetrics(
    val pageViews: Int = 128450,
    val totalSearches: Int = 34910,
    val movieDetailViews: Int = 69200,
    val playStarts: Int = 51840,
    val avgCompletionPercent: Float = 78.4f,
    val userRetentionRate: Float = 91.2f
)

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FylvixViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FylvixDatabase.getInstance(application)
    private val repository = FylvixRepository(database.fylvixDao())

    // Navigation & Backstack
    private val _currentDestination = MutableStateFlow(MainNavDestination.HOME)
    val currentDestination: StateFlow<MainNavDestination> = _currentDestination.asStateFlow()

    private val navigationBackstack = ArrayDeque<MainNavDestination>()

    // Detail & Player Overlays
    private val _selectedMediaId = MutableStateFlow<String?>(null)
    val selectedMediaId: StateFlow<String?> = _selectedMediaId.asStateFlow()

    private val _selectedSeasonNumber = MutableStateFlow(1)
    val selectedSeasonNumber: StateFlow<Int> = _selectedSeasonNumber.asStateFlow()

    private val _activePlayer = MutableStateFlow<ActivePlayerState?>(null)
    val activePlayer: StateFlow<ActivePlayerState?> = _activePlayer.asStateFlow()

    private var playerTickerJob: Job? = null

    // Catalog & Channels State (Mutable by Admin Panel & Metadata Sync)
    private val _catalog = MutableStateFlow(FylvixSeedCatalog.initialCatalog)
    val catalog: StateFlow<List<MediaItem>> = _catalog.asStateFlow()

    private val _liveChannels = MutableStateFlow(FylvixSeedCatalog.initialLiveChannels)
    val liveChannels: StateFlow<List<LiveTvChannel>> = _liveChannels.asStateFlow()

    // User Account & Multi-Profile State
    private val _userAccount = MutableStateFlow(
        UserAccount(
            id = "usr_01",
            email = "sahil@fylvix.global",
            username = "SahilFylvix",
            role = "admin",
            emailVerified = true,
            isLoggedIn = true
        )
    )
    val userAccount: StateFlow<UserAccount> = _userAccount.asStateFlow()

    private val _profiles = MutableStateFlow(FylvixSeedCatalog.defaultProfiles)
    val profiles: StateFlow<List<UserProfile>> = _profiles.asStateFlow()

    private val _activeProfile = MutableStateFlow(FylvixSeedCatalog.defaultProfiles.first())
    val activeProfile: StateFlow<UserProfile> = _activeProfile.asStateFlow()

    // Platform Settings & Backend 18+ Policy
    private val _settings = MutableStateFlow(PlatformSettings())
    val settings: StateFlow<PlatformSettings> = _settings.asStateFlow()

    // Filter, Search & Pagination
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _rawSearchInput = MutableStateFlow("")
    val rawSearchInput: StateFlow<String> = _rawSearchInput.asStateFlow()

    private val _visibleGridLimit = MutableStateFlow(12)
    val visibleGridLimit: StateFlow<Int> = _visibleGridLimit.asStateFlow()

    // Notifications, Reports, Providers, Audit Logs & Analytics
    private val _notifications = MutableStateFlow(FylvixSeedCatalog.initialNotifications)
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _reports = MutableStateFlow(FylvixSeedCatalog.initialReports)
    val reports: StateFlow<List<ContentReport>> = _reports.asStateFlow()

    private val _providers = MutableStateFlow(FylvixSeedCatalog.initialProviders)
    val providers: StateFlow<List<MetadataProviderConfig>> = _providers.asStateFlow()

    private val _auditLogs = MutableStateFlow(FylvixSeedCatalog.initialAuditLogs)
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _analytics = MutableStateFlow(AnalyticsMetrics())
    val analytics: StateFlow<AnalyticsMetrics> = _analytics.asStateFlow()

    private val _toastBanner = MutableStateFlow<String?>(null)
    val toastBanner: StateFlow<String?> = _toastBanner.asStateFlow()

    // Reactive Room DB Streams per Active Profile
    val watchlistIds: StateFlow<Set<String>> = _activeProfile
        .flatMapLatest { profile -> repository.observeWatchlistIds(profile.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val favoriteIds: StateFlow<Set<String>> = _activeProfile
        .flatMapLatest { profile -> repository.observeFavoriteIds(profile.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val watchHistory: StateFlow<List<WatchHistoryItem>> = _activeProfile
        .flatMapLatest { profile -> repository.observeWatchHistory(profile.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userRatings: StateFlow<Map<String, Double>> = _activeProfile
        .flatMapLatest { profile -> repository.observeUserRatings(profile.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val searchHistory: StateFlow<List<String>> = repository.observeSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed initial watch history and watchlist so "Continue Watching" & "My List" work immediately on first launch
        viewModelScope.launch {
            val defaultProfileId = _activeProfile.value.id
            repository.toggleWatchlist(defaultProfileId, "mov_aethelgard", currentlyInWatchlist = false)
            repository.toggleWatchlist(defaultProfileId, "tv_sovereign_grid", currentlyInWatchlist = false)
            repository.toggleWatchlist(defaultProfileId, "ani_celestial_eclipse", currentlyInWatchlist = false)
            repository.toggleFavorite(defaultProfileId, "mov_crimson_lanterns", currentlyFavorite = false)
            repository.toggleFavorite(defaultProfileId, "mov_lahore_mirage", currentlyFavorite = false)

            repository.recordPlaybackProgress(
                userId = "usr_01",
                profileId = defaultProfileId,
                contentId = "mov_aethelgard",
                episodeId = null,
                episodeTitle = null,
                seasonNumber = null,
                episodeNumber = null,
                positionSec = 3840L,
                durationSec = 8520L
            )
            repository.recordPlaybackProgress(
                userId = "usr_01",
                profileId = defaultProfileId,
                contentId = "tv_sovereign_grid",
                episodeId = "tv_sovereign_grid_s1_e2",
                episodeTitle = "Chapter 2: Shadows Over the Strait",
                seasonNumber = 1,
                episodeNumber = 2,
                positionSec = 1920L,
                durationSec = 3120L
            )
            repository.recordPlaybackProgress(
                userId = "usr_01",
                profileId = defaultProfileId,
                contentId = "ani_celestial_eclipse",
                episodeId = "ani_celestial_eclipse_s1_e3",
                episodeTitle = "Chapter 3: Echoes of the Velvet Throne",
                seasonNumber = 1,
                episodeNumber = 3,
                positionSec = 940L,
                durationSec = 1440L
            )
        }

        // Debounced search listener (250ms)
        viewModelScope.launch {
            _rawSearchInput
                .debounce(250L)
                .collect { debounced ->
                    _filterState.update { it.copy(query = debounced) }
                    if (debounced.trim().length >= 3) {
                        _analytics.update { it.copy(totalSearches = it.totalSearches + 1) }
                    }
                }
        }
    }

    // Derived catalog respecting publish state, kids profile, and backend 18+ classification settings
    val visibleCatalog: StateFlow<List<MediaItem>> = combine(
        _catalog,
        _activeProfile,
        _settings
    ) { items, profile, prefs ->
        items.filter { item ->
            if (!item.isPublished) return@filter false
            if (profile.isKidsProfile && item.certification != "G" && item.certification != "PG" && item.kind != ContentKind.KIDS) {
                return@filter false
            }
            if (item.isAdult18Plus) {
                prefs.backendAdultCategoryEnabled && profile.allowAdultContent && !profile.isKidsProfile
            } else {
                true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FylvixSeedCatalog.initialCatalog)

    fun showToast(message: String) {
        _toastBanner.value = message
        viewModelScope.launch {
            delay(2800L)
            if (_toastBanner.value == message) {
                _toastBanner.value = null
            }
        }
    }

    // Navigation Actions
    fun navigateTo(destination: MainNavDestination, resetFilters: Boolean = true) {
        if (_activePlayer.value != null && !_activePlayer.value!!.isPipMode) {
            closePlayer()
        }
        _selectedMediaId.value = null
        if (resetFilters && destination != MainNavDestination.SEARCH) {
            _filterState.value = FilterState()
            _rawSearchInput.value = ""
            _visibleGridLimit.value = 12
        }
        if (_currentDestination.value != destination) {
            navigationBackstack.addLast(_currentDestination.value)
            if (navigationBackstack.size > 20) navigationBackstack.removeFirst()
            _currentDestination.value = destination
            _analytics.update { it.copy(pageViews = it.pageViews + 1) }
        }
    }

    fun navigateToFilteredCatalog(
        destination: MainNavDestination,
        genre: String = "All",
        country: String = "All",
        language: String = "All",
        year: String = "All",
        categoryTab: String = "All"
    ) {
        _selectedMediaId.value = null
        _filterState.value = FilterState(
            genre = genre,
            country = country,
            language = language,
            year = year,
            categoryTab = categoryTab
        )
        if (_currentDestination.value != destination) {
            navigationBackstack.addLast(_currentDestination.value)
            _currentDestination.value = destination
        }
    }

    fun openMediaDetails(mediaId: String) {
        _selectedMediaId.value = mediaId
        _selectedSeasonNumber.value = 1
        _analytics.update { it.copy(movieDetailViews = it.movieDetailViews + 1) }
    }

    fun closeMediaDetails() {
        _selectedMediaId.value = null
    }

    fun selectSeason(seasonNumber: Int) {
        _selectedSeasonNumber.value = seasonNumber
    }

    fun canHandleBack(): Boolean {
        return _activePlayer.value != null ||
            _selectedMediaId.value != null ||
            navigationBackstack.isNotEmpty() ||
            _currentDestination.value != MainNavDestination.HOME
    }

    fun handleBack() {
        when {
            _activePlayer.value != null -> closePlayer()
            _selectedMediaId.value != null -> _selectedMediaId.value = null
            navigationBackstack.isNotEmpty() -> {
                _currentDestination.value = navigationBackstack.removeLast()
            }
            _currentDestination.value != MainNavDestination.HOME -> {
                _currentDestination.value = MainNavDestination.HOME
            }
        }
    }

    // Search & Filter Mutations
    fun updateSearchInput(input: String) {
        _rawSearchInput.value = input
    }

    fun commitSearchToHistory(query: String) {
        viewModelScope.launch {
            repository.saveSearchQuery(query)
        }
    }

    fun removeSearchHistoryQuery(query: String) {
        viewModelScope.launch {
            repository.removeSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun updateFilter(transform: (FilterState) -> FilterState) {
        _filterState.update(transform)
        _visibleGridLimit.value = 12
    }

    fun resetFilters() {
        _rawSearchInput.value = ""
        _filterState.value = FilterState()
        _visibleGridLimit.value = 12
    }

    fun loadMoreItems() {
        _visibleGridLimit.update { it + 12 }
    }

    // Watchlist, Favorites & Ratings
    fun toggleWatchlist(contentId: String) {
        val profileId = _activeProfile.value.id
        val isCurrentlySaved = watchlistIds.value.contains(contentId)
        viewModelScope.launch {
            repository.toggleWatchlist(profileId, contentId, isCurrentlySaved)
            showToast(if (isCurrentlySaved) "Removed from My List" else "Added to My List")
        }
    }

    fun toggleFavorite(contentId: String) {
        val profileId = _activeProfile.value.id
        val isCurrentlyFav = favoriteIds.value.contains(contentId)
        viewModelScope.launch {
            repository.toggleFavorite(profileId, contentId, isCurrentlyFav)
            showToast(if (isCurrentlyFav) "Removed from Favorites" else "Saved to Favorites")
        }
    }

    fun rateTitle(contentId: String, score: Double) {
        val profileId = _activeProfile.value.id
        viewModelScope.launch {
            repository.rateContent(profileId, contentId, score)
            showToast("Rated ★ ${"%.1f".format(score)} / 10")
        }
    }

    fun removeHistoryEntry(entryId: String) {
        viewModelScope.launch {
            repository.removeWatchHistoryEntry(entryId)
            showToast("Removed from Watch History")
        }
    }

    fun clearAllWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory(_activeProfile.value.id)
            showToast("Cleared Watch History for ${_activeProfile.value.name}")
        }
    }

    // Player Lifecycle & Controls
    fun startPlayback(
        mediaItem: MediaItem,
        episode: EpisodeItem? = null,
        resumePositionSec: Long? = null
    ) {
        val targetEpisode = episode ?: mediaItem.seasons.firstOrNull()?.episodes?.firstOrNull()
        val durationSec = ((targetEpisode?.runtimeMinutes ?: mediaItem.runtimeMinutes).coerceAtLeast(20) * 60L)
        val existingHistory = watchHistory.value.firstOrNull {
            it.contentId == mediaItem.id && (targetEpisode == null || it.episodeId == targetEpisode.id)
        }
        val initialPos = resumePositionSec ?: existingHistory?.playbackPositionSec ?: 0L
        val signedSession = FylvixMediaTokenSigner.createAuthorizedSession(
            contentId = mediaItem.id,
            episodeId = targetEpisode?.id,
            rawStreamUrl = targetEpisode?.streamUrl ?: mediaItem.authorizedStreamUrl
        )
        _activePlayer.value = ActivePlayerState(
            mediaItem = mediaItem,
            episode = targetEpisode,
            liveChannel = null,
            signedSession = signedSession,
            isPlaying = true,
            currentPositionSec = initialPos.coerceIn(0L, (durationSec - 10L).coerceAtLeast(0L)),
            durationSec = durationSec,
            selectedQuality = _settings.value.defaultQuality,
            selectedSubtitle = _settings.value.preferredSubtitleLanguage,
            selectedAudioTrack = mediaItem.audioTracks.firstOrNull()?.label ?: "Original Audio"
        )
        _analytics.update { it.copy(playStarts = it.playStarts + 1) }
        startPlayerTicker()
    }

    fun startLiveChannelPlayback(channel: LiveTvChannel) {
        val signedSession = FylvixMediaTokenSigner.createAuthorizedSession(
            contentId = channel.id,
            episodeId = "live_stream",
            rawStreamUrl = channel.authorizedStreamUrl
        )
        _activePlayer.value = ActivePlayerState(
            mediaItem = null,
            episode = null,
            liveChannel = channel,
            signedSession = signedSession,
            isPlaying = true,
            currentPositionSec = 0L,
            durationSec = 3600L,
            selectedQuality = "Auto (Adaptive HLS)",
            selectedSubtitle = "English [CC]",
            selectedAudioTrack = "${channel.language} • Live Broadcast"
        )
        _analytics.update { it.copy(playStarts = it.playStarts + 1) }
        startPlayerTicker()
    }

    private fun startPlayerTicker() {
        playerTickerJob?.cancel()
        playerTickerJob = viewModelScope.launch {
            var tickCounter = 0
            while (true) {
                delay(1000L)
                val current = _activePlayer.value ?: break
                if (current.isPlaying && !current.isBuffering && !current.hasNetworkError) {
                    val step = current.playbackSpeed.toLong().coerceAtLeast(1L)
                    val nextPos = (current.currentPositionSec + step).coerceAtMost(current.durationSec)
                    _activePlayer.update { it?.copy(currentPositionSec = nextPos) }
                    tickCounter++
                    if (tickCounter % 5 == 0 && current.mediaItem != null) {
                        persistCurrentPlayerProgress()
                    }
                }
            }
        }
    }

    private fun persistCurrentPlayerProgress() {
        val state = _activePlayer.value ?: return
        val item = state.mediaItem ?: return
        viewModelScope.launch {
            repository.recordPlaybackProgress(
                userId = _userAccount.value.id,
                profileId = _activeProfile.value.id,
                contentId = item.id,
                episodeId = state.episode?.id,
                episodeTitle = state.episode?.title,
                seasonNumber = state.episode?.seasonNumber,
                episodeNumber = state.episode?.episodeNumber,
                positionSec = state.currentPositionSec,
                durationSec = state.durationSec
            )
        }
    }

    fun togglePlayPause() {
        _activePlayer.update { it?.copy(isPlaying = !it.isPlaying) }
        persistCurrentPlayerProgress()
    }

    fun seekTo(positionSec: Long) {
        _activePlayer.update { state ->
            state?.copy(currentPositionSec = positionSec.coerceIn(0L, state.durationSec))
        }
        persistCurrentPlayerProgress()
    }

    fun seekRelative(deltaSec: Long) {
        val current = _activePlayer.value ?: return
        seekTo(current.currentPositionSec + deltaSec)
    }

    fun skipIntro() {
        val current = _activePlayer.value ?: return
        val target = if (current.currentPositionSec < 90L) 90L else current.currentPositionSec + 85L
        seekTo(target)
        showToast("Skipped Intro")
    }

    fun setPlayerVolume(volume: Float) {
        _activePlayer.update {
            it?.copy(volume = volume.coerceIn(0f, 1f), isMuted = volume <= 0.01f)
        }
    }

    fun toggleMute() {
        _activePlayer.update { it?.copy(isMuted = !it.isMuted) }
    }

    fun toggleFullscreen() {
        _activePlayer.update { it?.copy(isFullscreen = !it.isFullscreen) }
    }

    fun togglePipMode() {
        _activePlayer.update { it?.copy(isPipMode = !it.isPipMode, isFullscreen = false) }
    }

    fun setPlayerQuality(quality: String) {
        _activePlayer.update { it?.copy(selectedQuality = quality, isBuffering = true) }
        viewModelScope.launch {
            delay(350L)
            _activePlayer.update { it?.copy(isBuffering = false) }
            showToast("Switched stream quality to $quality")
        }
    }

    fun setPlayerSubtitle(subtitle: String) {
        _activePlayer.update { it?.copy(selectedSubtitle = subtitle) }
        showToast("Subtitles: $subtitle")
    }

    fun setPlayerAudioTrack(audioTrack: String) {
        _activePlayer.update { it?.copy(selectedAudioTrack = audioTrack) }
        showToast("Audio Track: $audioTrack")
    }

    fun setPlaybackSpeed(speed: Float) {
        _activePlayer.update { it?.copy(playbackSpeed = speed) }
    }

    fun simulateNetworkRecovery() {
        _activePlayer.update { it?.copy(hasNetworkError = false, isBuffering = true) }
        viewModelScope.launch {
            delay(500L)
            _activePlayer.update { it?.copy(isBuffering = false, isPlaying = true) }
            showToast("CDN Edge Stream Reconnected")
        }
    }

    fun triggerSimulatedNetworkDrop() {
        _activePlayer.update { it?.copy(hasNetworkError = true, isPlaying = false) }
    }

    fun playAdjacentEpisode(next: Boolean) {
        val state = _activePlayer.value ?: return
        val item = state.mediaItem ?: return
        val currentEp = state.episode ?: return
        val allEpisodes = item.seasons.flatMap { it.episodes }
        val idx = allEpisodes.indexOfFirst { it.id == currentEp.id }
        if (idx == -1) return
        val targetIdx = if (next) idx + 1 else idx - 1
        val targetEp = allEpisodes.getOrNull(targetIdx)
        if (targetEp != null) {
            persistCurrentPlayerProgress()
            startPlayback(item, targetEp, resumePositionSec = 0L)
            showToast("Playing S${targetEp.seasonNumber}:E${targetEp.episodeNumber} • ${targetEp.title}")
        } else {
            showToast(if (next) "You've reached the latest episode" else "Already at Episode 1")
        }
    }

    fun closePlayer() {
        persistCurrentPlayerProgress()
        playerTickerJob?.cancel()
        _activePlayer.value = null
    }

    // Auth & Multi-Profile Operations
    fun loginUser(email: String, username: String, asAdmin: Boolean = true) {
        _userAccount.value = UserAccount(
            id = "usr_${username.lowercase().hashCode().and(0xffff)}",
            email = email.ifBlank { "user@fylvix.global" },
            username = username.ifBlank { "CinemaExplorer" },
            role = if (asAdmin) "admin" else "user",
            emailVerified = true,
            isLoggedIn = true
        )
        showToast("Signed in as ${_userAccount.value.username}")
    }

    fun logoutUser() {
        _userAccount.update { it.copy(isLoggedIn = false) }
        showToast("Signed out of Fylvix session")
    }

    fun sendPasswordResetEmail(email: String) {
        showToast("Password reset link dispatched to ${email.ifBlank { _userAccount.value.email }}")
    }

    fun switchProfile(profile: UserProfile) {
        _activeProfile.value = profile
        showToast("Switched profile to ${profile.name}")
    }

    fun createProfile(
        name: String,
        handle: String,
        emoji: String,
        isKids: Boolean,
        allowAdult: Boolean,
        preferredLang: String
    ) {
        val palette = listOf(0xFFE50938, 0xFFF5B841, 0xFF00D2FF, 0xFF10B981, 0xFF8B5CF6)
        val newProfile = UserProfile(
            id = "prof_${System.currentTimeMillis()}",
            userId = _userAccount.value.id,
            name = name.ifBlank { "New Viewer" },
            usernameHandle = if (handle.startsWith("@")) handle else "@${handle.ifBlank { "viewer" }}",
            avatarColorHex = palette[_profiles.value.size % palette.size],
            avatarEmoji = emoji.ifBlank { "🍿" },
            isKidsProfile = isKids,
            allowAdultContent = if (isKids) false else allowAdult,
            preferredLanguage = preferredLang
        )
        _profiles.update { it + newProfile }
        _activeProfile.value = newProfile
        showToast("Created & activated profile: ${newProfile.name}")
    }

    fun toggleActiveProfileAdultAccess(allowed: Boolean) {
        val updated = _activeProfile.value.copy(
            allowAdultContent = if (_activeProfile.value.isKidsProfile) false else allowed
        )
        _activeProfile.value = updated
        _profiles.update { list -> list.map { if (it.id == updated.id) updated else it } }
    }

    // Settings & Notifications
    fun updateSettings(transform: (PlatformSettings) -> PlatformSettings) {
        _settings.update(transform)
    }

    fun markAllNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    fun submitContentReport(contentItem: MediaItem, reason: String, details: String) {
        val newReport = ContentReport(
            id = "rep_${System.currentTimeMillis() % 10000}",
            contentId = contentItem.id,
            contentTitle = contentItem.title,
            reason = reason,
            details = details.ifBlank { "Submitted via Movie/Series Details modal." },
            submittedBy = _activeProfile.value.usernameHandle,
            status = "Open",
            createdAt = "Just now"
        )
        _reports.update { listOf(newReport) + it }
        appendAuditLog(_activeProfile.value.usernameHandle, "Submitted content report ($reason)", contentItem.id, "MODERATION")
        showToast("Report submitted to Fylvix Trust & Safety")
    }

    // Recommendation System
    fun getRecommendationsForActiveProfile(allVisible: List<MediaItem>): List<MediaItem> {
        val historyIds = watchHistory.value.map { it.contentId }.toSet()
        val favs = favoriteIds.value
        val watchlist = watchlistIds.value
        val seedItems = allVisible.filter { it.id in historyIds || it.id in favs || it.id in watchlist }
        val preferredGenres = seedItems.flatMap { it.genres }.groupingBy { it }.eachCount()
        val preferredCountries = seedItems.map { it.country }.toSet()
        val preferredLang = _activeProfile.value.preferredLanguage

        return allVisible
            .sortedByDescending { item ->
                val genreOverlap = item.genres.sumOf { preferredGenres[it] ?: 0 } * 2.5
                val countryBonus = if (item.country in preferredCountries) 2.0 else 0.0
                val langBonus = if (item.language.equals(preferredLang, ignoreCase = true)) 2.5 else 0.0
                val unwatchedBonus = if (item.id !in historyIds) 1.5 else 0.0
                item.rating + genreOverlap + countryBonus + langBonus + unwatchedBonus
            }
            .take(12)
    }

    // ================= ADMIN PANEL OPERATIONS =================
    fun adminAddOrUpdateMedia(
        title: String,
        originalTitle: String,
        overview: String,
        kind: ContentKind,
        releaseYear: Int,
        runtimeMinutes: Int,
        country: String,
        language: String,
        regionalHub: String,
        genres: List<String>,
        rating: Double,
        certification: String,
        director: String,
        writer: String,
        qualityBadge: String,
        isFeatured: Boolean,
        isTrending: Boolean,
        isAdult18Plus: Boolean,
        isPublished: Boolean
    ) {
        val newId = "custom_${System.currentTimeMillis() % 100000}"
        val isSeriesKind = kind == ContentKind.SERIES || kind == ContentKind.TV_SHOW ||
            kind == ContentKind.ANIME_SERIES || kind == ContentKind.ADULT_SERIES
        val generatedSeasons = if (isSeriesKind) {
            listOf(
                SeasonItem(
                    id = "${newId}_s1",
                    showId = newId,
                    seasonNumber = 1,
                    title = "Season 1 • Premiere Season",
                    overview = "Official Season 1 episodes for $title.",
                    airDate = "$releaseYear-01-15",
                    episodes = listOf(
                        EpisodeItem(
                            id = "${newId}_s1_e1",
                            showId = newId,
                            seasonNumber = 1,
                            episodeNumber = 1,
                            title = "Episode 1: Pilot Premiere",
                            description = "Opening chapter of $title.",
                            airDate = "$releaseYear-01-15",
                            runtimeMinutes = runtimeMinutes.coerceAtMost(60),
                            rating = rating
                        ),
                        EpisodeItem(
                            id = "${newId}_s1_e2",
                            showId = newId,
                            seasonNumber = 1,
                            episodeNumber = 2,
                            title = "Episode 2: The Escalation",
                            description = "Second episode of $title.",
                            airDate = "$releaseYear-01-22",
                            runtimeMinutes = runtimeMinutes.coerceAtMost(60),
                            rating = rating
                        )
                    )
                )
            )
        } else emptyList()

        val newItem = MediaItem(
            id = newId,
            title = title.ifBlank { "Untitled Fylvix Original" },
            originalTitle = originalTitle.ifBlank { title },
            tagline = "A Fylvix Worldwide Original Presentation",
            overview = overview.ifBlank { "Added via Fylvix Admin Studio with authorized adaptive streaming delivery." },
            kind = kind,
            releaseYear = releaseYear,
            releaseDate = "$releaseYear-06-01",
            runtimeMinutes = runtimeMinutes,
            rating = rating.coerceIn(1.0, 10.0),
            voteCount = 1200,
            genres = if (genres.isEmpty()) listOf("Drama") else genres,
            country = country,
            countryCode = country.take(2).uppercase(),
            language = language,
            languageCode = language.take(2).lowercase(),
            regionalHub = regionalHub,
            certification = if (isAdult18Plus) "18+" else certification,
            classification = if (isAdult18Plus) "18+" else "Standard",
            qualityBadge = qualityBadge,
            status = if (isSeriesKind) "On Air" else "Now Playing",
            director = director.ifBlank { "Fylvix Studio Director" },
            writers = listOf(writer.ifBlank { "Fylvix Screenplay Team" }),
            cast = listOf(
                CastMember("c_${newId}_1", "Lead Star", "Protagonist", "Actor", country, 0xFFE50938),
                CastMember("c_${newId}_2", "Supporting Star", "Co-Star", "Actor", country, 0xFFF5B841)
            ),
            crew = listOf(
                CrewMember("cr_${newId}_1", director.ifBlank { "Fylvix Director" }, "Director", "Directing")
            ),
            seasons = generatedSeasons,
            heroDrawableRes = when {
                kind == ContentKind.ANIME_SERIES || kind == ContentKind.ANIME_MOVIE -> R.drawable.img_hero_anime_eclipse
                isAdult18Plus -> R.drawable.img_hero_noir_heist
                else -> R.drawable.img_hero_cyber_odyssey
            },
            gradientColors = if (isAdult18Plus) listOf(0xFF4C0519, 0xFFE50938) else listOf(0xFF0F172A, 0xFFE50938),
            isFeatured = isFeatured,
            isTrendingToday = isTrending,
            isTrendingWeek = isTrending,
            isPopular = true,
            isAdult18Plus = isAdult18Plus,
            isPublished = isPublished
        )
        _catalog.update { listOf(newItem) + it }
        _notifications.update {
            listOf(
                AppNotification(
                    id = "notif_${System.currentTimeMillis()}",
                    title = "New ${kind.displayName}: ${newItem.title}",
                    message = "Published to Fylvix ${newItem.regionalHub} catalog in ${newItem.qualityBadge}.",
                    category = "New Movie",
                    timestampLabel = "Just now",
                    contentId = newItem.id
                )
            ) + it
        }
        appendAuditLog("admin@fylvix.global", "Created & published ${kind.displayName} '${newItem.title}'", newItem.id, "ADMIN")
        showToast("Published '${newItem.title}' to Fylvix Catalog")
    }

    fun adminTogglePublishMedia(mediaId: String) {
        var targetTitle = ""
        var newState = true
        _catalog.update { list ->
            list.map { item ->
                if (item.id == mediaId) {
                    targetTitle = item.title
                    newState = !item.isPublished
                    item.copy(isPublished = newState)
                } else item
            }
        }
        appendAuditLog("admin@fylvix.global", "Set published=$newState for '$targetTitle'", mediaId, "ADMIN")
        showToast("${if (newState) "Published" else "Unpublished"} $targetTitle")
    }

    fun adminToggleFeaturedMedia(mediaId: String) {
        _catalog.update { list ->
            list.map { item ->
                if (item.id == mediaId) item.copy(isFeatured = !item.isFeatured) else item
            }
        }
        showToast("Updated Hero Featured status")
    }

    fun adminAddEpisodeToShow(
        showId: String,
        seasonNumber: Int,
        episodeTitle: String,
        episodeDescription: String,
        runtimeMinutes: Int
    ) {
        _catalog.update { list ->
            list.map { show ->
                if (show.id != showId) return@map show
                val existingSeason = show.seasons.find { it.seasonNumber == seasonNumber }
                val updatedSeasons = if (existingSeason != null) {
                    show.seasons.map { s ->
                        if (s.seasonNumber == seasonNumber) {
                            val nextEpNum = s.episodes.size + 1
                            val newEp = EpisodeItem(
                                id = "${showId}_s${seasonNumber}_e$nextEpNum",
                                showId = showId,
                                seasonNumber = seasonNumber,
                                episodeNumber = nextEpNum,
                                title = episodeTitle.ifBlank { "Episode $nextEpNum" },
                                description = episodeDescription.ifBlank { "New episode added via Admin Series Manager." },
                                airDate = "2026-10-04",
                                runtimeMinutes = runtimeMinutes.coerceAtLeast(20),
                                rating = 8.9
                            )
                            s.copy(episodes = s.episodes + newEp)
                        } else s
                    }
                } else {
                    val newEp = EpisodeItem(
                        id = "${showId}_s${seasonNumber}_e1",
                        showId = showId,
                        seasonNumber = seasonNumber,
                        episodeNumber = 1,
                        title = episodeTitle.ifBlank { "Episode 1: Season Premiere" },
                        description = episodeDescription.ifBlank { "Season $seasonNumber premiere episode." },
                        airDate = "2026-10-04",
                        runtimeMinutes = runtimeMinutes.coerceAtLeast(20),
                        rating = 9.0
                    )
                    show.seasons + SeasonItem(
                        id = "${showId}_s$seasonNumber",
                        showId = showId,
                        seasonNumber = seasonNumber,
                        title = "Season $seasonNumber",
                        overview = "Season $seasonNumber of ${show.title}",
                        airDate = "2026-10-04",
                        episodes = listOf(newEp)
                    )
                }
                show.copy(seasons = updatedSeasons)
            }
        }
        appendAuditLog("admin@fylvix.global", "Added S$seasonNumber episode '$episodeTitle'", showId, "ADMIN")
        showToast("Added episode '$episodeTitle' to Season $seasonNumber")
    }

    fun adminAddLiveChannel(
        name: String,
        code: String,
        category: String,
        country: String,
        language: String,
        currentProgram: String,
        nextProgram: String,
        streamUrl: String
    ) {
        val newChannel = LiveTvChannel(
            id = "live_${System.currentTimeMillis() % 10000}",
            name = name.ifBlank { "Fylvix Live HD" },
            code = code.ifBlank { "FLV" }.take(4).uppercase(),
            category = category,
            country = country,
            countryCode = country.take(2).uppercase(),
            language = language,
            badgeColorHex = 0xFFE50938,
            currentProgram = currentProgram.ifBlank { "Live Global Broadcast" },
            nextProgram = nextProgram.ifBlank { "Upcoming Studio Feature" },
            viewersCount = "19.4K watching",
            schedule = listOf(
                ChannelProgram("cp_1", currentProgram.ifBlank { "Live Global Broadcast" }, "Now Live", category, "Authorized live stream.", true),
                ChannelProgram("cp_2", nextProgram.ifBlank { "Upcoming Studio Feature" }, "Next Hour", category, "Scheduled broadcast.", false)
            ),
            authorizedStreamUrl = streamUrl.ifBlank { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4" },
            isLive = true,
            isPublished = true
        )
        _liveChannels.update { listOf(newChannel) + it }
        appendAuditLog("admin@fylvix.global", "Added authorized Live TV channel '${newChannel.name}'", newChannel.id, "ADMIN")
        showToast("Added Live Channel: ${newChannel.name}")
    }

    fun adminToggleLiveChannelStatus(channelId: String) {
        _liveChannels.update { list ->
            list.map { ch ->
                if (ch.id == channelId) ch.copy(isPublished = !ch.isPublished) else ch
            }
        }
        showToast("Updated Live Channel publishing status")
    }

    fun adminResolveReport(reportId: String) {
        _reports.update { list ->
            list.map { rep -> if (rep.id == reportId) rep.copy(status = "Resolved") else rep }
        }
        appendAuditLog("admin@fylvix.global", "Resolved moderation report $reportId", reportId, "MODERATION")
        showToast("Marked report $reportId as Resolved")
    }

    fun adminTriggerProviderSync(providerId: String) {
        _providers.update { list ->
            list.map { prov ->
                if (prov.id == providerId) {
                    prov.copy(
                        lastSyncedAt = "Just now (Synced)",
                        syncedRecordsCount = prov.syncedRecordsCount + 42
                    )
                } else prov
            }
        }
        appendAuditLog("Metadata Sync Adapter", "Synchronized 42 worldwide records into PostgreSQL cache", providerId, "INFO")
        showToast("Metadata Provider Sync Completed (+42 normalized records)")
    }

    private fun appendAuditLog(actor: String, action: String, target: String, severity: String) {
        val entry = AuditLogEntry(
            id = "log_${System.currentTimeMillis() % 100000}",
            timestamp = "Just now",
            actor = actor,
            action = action,
            target = target,
            severity = severity
        )
        _auditLogs.update { listOf(entry) + it }
    }
}
