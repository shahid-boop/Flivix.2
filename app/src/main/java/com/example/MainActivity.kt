package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.MainNavDestination
import com.example.ui.components.FylvixAppBar
import com.example.ui.components.FylvixBottomNavigation
import com.example.ui.components.FylvixSidebar
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.CatalogGridScreen
import com.example.ui.screens.GlobalSearchScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MediaDetailsScreen
import com.example.ui.screens.MyListAndFavoritesScreen
import com.example.ui.screens.ProfileAndAuthScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StreamingPlayerOverlay
import com.example.ui.screens.TaxonomyDirectoryScreen
import com.example.ui.screens.WatchHistoryScreen
import com.example.ui.theme.FylvixCrimson
import com.example.ui.theme.FylvixGlassBorder
import com.example.ui.theme.FylvixGold
import com.example.ui.theme.FylvixObsidian
import com.example.ui.theme.FylvixSurfaceCard
import com.example.ui.theme.FylvixSurfaceDark
import com.example.ui.theme.FylvixSurfaceElevated
import com.example.ui.theme.FylvixTextSecondary
import com.example.ui.theme.FylvixTheme
import com.example.ui.viewmodel.FylvixViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fylvixViewModel: FylvixViewModel = viewModel()
            val settings by fylvixViewModel.settings.collectAsStateWithLifecycle()

            FylvixTheme(themeMode = settings.themeMode) {
                FylvixPlatformApp(viewModel = fylvixViewModel)
            }
        }
    }
}

@Composable
fun FylvixPlatformApp(viewModel: FylvixViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val visibleCatalog by viewModel.visibleCatalog.collectAsStateWithLifecycle()
    val fullCatalog by viewModel.catalog.collectAsStateWithLifecycle()
    val liveChannels by viewModel.liveChannels.collectAsStateWithLifecycle()
    val selectedMediaId by viewModel.selectedMediaId.collectAsStateWithLifecycle()
    val selectedSeasonNumber by viewModel.selectedSeasonNumber.collectAsStateWithLifecycle()
    val activePlayer by viewModel.activePlayer.collectAsStateWithLifecycle()
    val userAccount by viewModel.userAccount.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val rawSearchInput by viewModel.rawSearchInput.collectAsStateWithLifecycle()
    val visibleGridLimit by viewModel.visibleGridLimit.collectAsStateWithLifecycle()
    val watchlistIds by viewModel.watchlistIds.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val userRatings by viewModel.userRatings.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val toastBanner by viewModel.toastBanner.collectAsStateWithLifecycle()

    val adultEnabled = settings.backendAdultCategoryEnabled &&
        activeProfile.allowAdultContent &&
        !activeProfile.isKidsProfile

    val recommendations = remember(visibleCatalog, watchHistory, favoriteIds, watchlistIds, activeProfile) {
        viewModel.getRecommendationsForActiveProfile(visibleCatalog)
    }

    var showNotificationsDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = viewModel.canHandleBack()) {
        viewModel.handleBack()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FylvixObsidian)
    ) {
        val screenWidthDp = maxWidth
        val isCompactScreen = screenWidthDp < 700.dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = isCompactScreen && activePlayer == null,
            drawerContent = {
                if (isCompactScreen) {
                    ModalDrawerSheet(
                        drawerContainerColor = FylvixSurfaceDark
                    ) {
                        FylvixSidebar(
                            currentDestination = currentDestination,
                            adultSectionEnabled = adultEnabled,
                            onSelectDestination = { dest ->
                                viewModel.navigateTo(dest)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    if (activePlayer == null || activePlayer?.isPipMode == true) {
                        FylvixAppBar(
                            currentDestination = currentDestination,
                            activeProfile = activeProfile,
                            unreadNotifications = notifications.count { !it.isRead },
                            adultSectionEnabled = adultEnabled,
                            isCompactScreen = isCompactScreen,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onSelectDestination = { viewModel.navigateTo(it) },
                            onOpenNotificationsDialog = { showNotificationsDialog = true }
                        )
                    }
                },
                bottomBar = {
                    if (isCompactScreen && (activePlayer == null || activePlayer?.isPipMode == true)) {
                        FylvixBottomNavigation(
                            currentDestination = currentDestination,
                            onSelectDestination = { viewModel.navigateTo(it) }
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Permanent Sidebar on Tablet / Desktop / Large Screens
                    if (!isCompactScreen && (activePlayer == null || activePlayer?.isPipMode == true)) {
                        FylvixSidebar(
                            currentDestination = currentDestination,
                            adultSectionEnabled = adultEnabled,
                            onSelectDestination = { viewModel.navigateTo(it) }
                        )
                    }

                    // Main Content Viewport
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        val selectedMedia = remember(selectedMediaId, fullCatalog) {
                            fullCatalog.find { it.id == selectedMediaId }
                        }

                        if (selectedMedia != null) {
                            MediaDetailsScreen(
                                item = selectedMedia,
                                catalog = visibleCatalog,
                                selectedSeasonNumber = selectedSeasonNumber,
                                isInWatchlist = watchlistIds.contains(selectedMedia.id),
                                isFavorite = favoriteIds.contains(selectedMedia.id),
                                userRating = userRatings[selectedMedia.id],
                                watchHistory = watchHistory,
                                watchlistIds = watchlistIds,
                                onBack = { viewModel.closeMediaDetails() },
                                onSelectSeason = { viewModel.selectSeason(it) },
                                onWatchNow = { media, ep, pos -> viewModel.startPlayback(media, ep, pos) },
                                onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onRateContent = { score -> viewModel.rateTitle(selectedMedia.id, score) },
                                onSubmitReport = { reason, details ->
                                    viewModel.submitContentReport(selectedMedia, reason, details)
                                },
                                onOpenOtherMedia = { viewModel.openMediaDetails(it.id) }
                            )
                        } else {
                            when (currentDestination) {
                                MainNavDestination.HOME -> {
                                    HomeScreen(
                                        catalog = visibleCatalog,
                                        watchHistory = watchHistory,
                                        watchlistIds = watchlistIds,
                                        recommendations = recommendations,
                                        settings = settings,
                                        activeProfile = activeProfile,
                                        onOpenDetails = { viewModel.openMediaDetails(it.id) },
                                        onWatchNow = { media, ep, pos -> viewModel.startPlayback(media, ep, pos) },
                                        onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                                        onRemoveHistory = { viewModel.removeHistoryEntry(it) },
                                        onNavigateToSection = { dest, genre, country, tab ->
                                            viewModel.navigateToFilteredCatalog(
                                                destination = dest,
                                                genre = genre,
                                                country = country,
                                                categoryTab = tab
                                            )
                                        }
                                    )
                                }

                                MainNavDestination.MOVIES,
                                MainNavDestination.TV_SHOWS,
                                MainNavDestination.SERIES,
                                MainNavDestination.ANIME,
                                MainNavDestination.ADULT -> {
                                    CatalogGridScreen(
                                        destination = currentDestination,
                                        catalog = visibleCatalog,
                                        filterState = filterState,
                                        visibleLimit = visibleGridLimit,
                                        watchlistIds = watchlistIds,
                                        settings = settings,
                                        screenWidthDp = screenWidthDp,
                                        onUpdateFilter = { viewModel.updateFilter(it) },
                                        onResetFilters = { viewModel.resetFilters() },
                                        onLoadMore = { viewModel.loadMoreItems() },
                                        onOpenDetails = { viewModel.openMediaDetails(it.id) },
                                        onPlayMedia = { viewModel.startPlayback(it) },
                                        onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                                        onToggleAdultBackendSetting = { enabled ->
                                            viewModel.updateSettings { it.copy(backendAdultCategoryEnabled = enabled) }
                                        },
                                        onOpenAdminPanel = { viewModel.navigateTo(MainNavDestination.ADMIN) }
                                    )
                                }

                                MainNavDestination.LIVE_TV -> {
                                    LiveTvScreen(
                                        channels = liveChannels,
                                        onWatchLiveChannel = { viewModel.startLiveChannelPlayback(it) }
                                    )
                                }

                                MainNavDestination.GENRES,
                                MainNavDestination.COUNTRIES,
                                MainNavDestination.LANGUAGES -> {
                                    TaxonomyDirectoryScreen(
                                        mode = currentDestination,
                                        catalog = visibleCatalog,
                                        onSelectTaxonomyItem = { selectedValue ->
                                            when (currentDestination) {
                                                MainNavDestination.GENRES -> viewModel.navigateToFilteredCatalog(
                                                    destination = MainNavDestination.MOVIES,
                                                    genre = selectedValue
                                                )
                                                MainNavDestination.COUNTRIES -> viewModel.navigateToFilteredCatalog(
                                                    destination = MainNavDestination.MOVIES,
                                                    country = selectedValue
                                                )
                                                else -> viewModel.navigateToFilteredCatalog(
                                                    destination = MainNavDestination.MOVIES,
                                                    language = selectedValue
                                                )
                                            }
                                        }
                                    )
                                }

                                MainNavDestination.SEARCH -> {
                                    GlobalSearchScreen(
                                        catalog = visibleCatalog,
                                        rawQuery = rawSearchInput,
                                        filterState = filterState,
                                        searchHistory = searchHistory,
                                        watchlistIds = watchlistIds,
                                        settings = settings,
                                        screenWidthDp = screenWidthDp,
                                        onQueryChange = { viewModel.updateSearchInput(it) },
                                        onCommitQuery = { viewModel.commitSearchToHistory(it) },
                                        onRemoveHistoryQuery = { viewModel.removeSearchHistoryQuery(it) },
                                        onClearSearchHistory = { viewModel.clearSearchHistory() },
                                        onUpdateFilter = { viewModel.updateFilter(it) },
                                        onResetFilters = { viewModel.resetFilters() },
                                        onOpenDetails = { viewModel.openMediaDetails(it.id) },
                                        onPlayMedia = { media, ep -> viewModel.startPlayback(media, ep) },
                                        onToggleWatchlist = { viewModel.toggleWatchlist(it) }
                                    )
                                }

                                MainNavDestination.MY_LIST -> {
                                    MyListAndFavoritesScreen(
                                        catalog = fullCatalog,
                                        watchlistIds = watchlistIds,
                                        favoriteIds = favoriteIds,
                                        watchHistory = watchHistory,
                                        screenWidthDp = screenWidthDp,
                                        onOpenDetails = { viewModel.openMediaDetails(it.id) },
                                        onPlayMedia = { viewModel.startPlayback(it) },
                                        onToggleWatchlist = { viewModel.toggleWatchlist(it) },
                                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                                    )
                                }

                                MainNavDestination.HISTORY -> {
                                    WatchHistoryScreen(
                                        catalog = fullCatalog,
                                        watchHistory = watchHistory,
                                        onResumeItem = { media, ep, pos ->
                                            viewModel.startPlayback(media, ep, pos)
                                        },
                                        onOpenDetails = { viewModel.openMediaDetails(it.id) },
                                        onRemoveEntry = { viewModel.removeHistoryEntry(it) },
                                        onClearAll = { viewModel.clearAllWatchHistory() }
                                    )
                                }

                                MainNavDestination.PROFILE -> {
                                    ProfileAndAuthScreen(
                                        userAccount = userAccount,
                                        profiles = profiles,
                                        activeProfile = activeProfile,
                                        watchlistCount = watchlistIds.size,
                                        favoritesCount = favoriteIds.size,
                                        historyCount = watchHistory.size,
                                        onSwitchProfile = { viewModel.switchProfile(it) },
                                        onCreateProfile = { name, handle, emoji, kids, adult, lang ->
                                            viewModel.createProfile(name, handle, emoji, kids, adult, lang)
                                        },
                                        onToggleProfileAdultAccess = { viewModel.toggleActiveProfileAdultAccess(it) },
                                        onLogin = { email, user, admin -> viewModel.loginUser(email, user, admin) },
                                        onLogout = { viewModel.logoutUser() },
                                        onSendResetPassword = { viewModel.sendPasswordResetEmail(it) }
                                    )
                                }

                                MainNavDestination.SETTINGS -> {
                                    SettingsScreen(
                                        settings = settings,
                                        onUpdateSettings = { viewModel.updateSettings(it) }
                                    )
                                }

                                MainNavDestination.ADMIN -> {
                                    AdminPanelScreen(
                                        catalog = fullCatalog,
                                        liveChannels = liveChannels,
                                        analytics = analytics,
                                        settings = settings,
                                        providers = providers,
                                        reports = reports,
                                        auditLogs = auditLogs,
                                        onAddOrUpdateMedia = viewModel::adminAddOrUpdateMedia,
                                        onTogglePublishMedia = { viewModel.adminTogglePublishMedia(it) },
                                        onToggleFeaturedMedia = { viewModel.adminToggleFeaturedMedia(it) },
                                        onAddEpisodeToShow = viewModel::adminAddEpisodeToShow,
                                        onAddLiveChannel = viewModel::adminAddLiveChannel,
                                        onToggleLiveChannel = { viewModel.adminToggleLiveChannelStatus(it) },
                                        onUpdateSettings = { viewModel.updateSettings(it) },
                                        onTriggerProviderSync = { viewModel.adminTriggerProviderSync(it) },
                                        onResolveReport = { viewModel.adminResolveReport(it) }
                                    )
                                }
                            }
                        }

                        // Fullscreen or PiP Streaming Player Overlay
                        activePlayer?.let { playerState ->
                            StreamingPlayerOverlay(
                                playerState = playerState,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onSeekTo = { viewModel.seekTo(it) },
                                onSeekRelative = { viewModel.seekRelative(it) },
                                onSkipIntro = { viewModel.skipIntro() },
                                onSetVolume = { viewModel.setPlayerVolume(it) },
                                onToggleMute = { viewModel.toggleMute() },
                                onToggleFullscreen = { viewModel.toggleFullscreen() },
                                onTogglePip = { viewModel.togglePipMode() },
                                onSelectQuality = { viewModel.setPlayerQuality(it) },
                                onSelectSubtitle = { viewModel.setPlayerSubtitle(it) },
                                onSelectAudio = { viewModel.setPlayerAudioTrack(it) },
                                onSelectSpeed = { viewModel.setPlaybackSpeed(it) },
                                onPlayNextEpisode = { viewModel.playAdjacentEpisode(next = true) },
                                onPlayPrevEpisode = { viewModel.playAdjacentEpisode(next = false) },
                                onSelectSpecificEpisode = { ep ->
                                    playerState.mediaItem?.let { media ->
                                        viewModel.startPlayback(media, ep, 0L)
                                    }
                                },
                                onRetryNetwork = { viewModel.simulateNetworkRecovery() },
                                onSimulateDrop = { viewModel.triggerSimulatedNetworkDrop() },
                                onClosePlayer = { viewModel.closePlayer() }
                            )
                        }

                        // Floating Feedback Banner
                        androidx.compose.animation.AnimatedVisibility(
                            visible = toastBanner != null,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                        ) {
                            Surface(
                                color = FylvixSurfaceElevated,
                                shape = RoundedCornerShape(24.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGold),
                                shadowElevation = 10.dp
                            ) {
                                Text(
                                    text = toastBanner.orEmpty(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Notifications Center Modal Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = FylvixGold)
                    Text("Fylvix Notifications")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    notifications.take(6).forEach { notif ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (notif.isRead) FylvixSurfaceCard else FylvixSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (notif.isRead) FylvixGlassBorder else FylvixCrimson
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    notif.contentId?.let { id ->
                                        showNotificationsDialog = false
                                        viewModel.openMediaDetails(id)
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = notif.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FylvixGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = notif.timestampLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FylvixTextSecondary
                                    )
                                }
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FylvixTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAllNotificationsRead()
                        showNotificationsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson)
                ) {
                    Text("Mark All Read")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
