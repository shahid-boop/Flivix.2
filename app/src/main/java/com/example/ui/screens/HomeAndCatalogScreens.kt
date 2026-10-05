package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.models.ContentKind
import com.example.data.models.EpisodeItem
import com.example.data.models.FilterState
import com.example.data.models.LiveTvChannel
import com.example.data.models.MainNavDestination
import com.example.data.models.MediaItem
import com.example.data.models.PlatformSettings
import com.example.data.models.UserProfile
import com.example.data.models.WatchHistoryItem
import com.example.data.seed.FylvixSeedCatalog
import com.example.ui.components.CategoryRow
import com.example.ui.components.ChannelCard
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.EmptyState
import com.example.ui.components.FilterPanel
import com.example.ui.components.HeroBanner
import com.example.ui.components.MovieCard
import com.example.ui.theme.FylvixCrimson
import com.example.ui.theme.FylvixCyan
import com.example.ui.theme.FylvixGlassBorder
import com.example.ui.theme.FylvixGold
import com.example.ui.theme.FylvixObsidian
import com.example.ui.theme.FylvixSurfaceCard
import com.example.ui.theme.FylvixSurfaceElevated
import com.example.ui.theme.FylvixTextPrimary
import com.example.ui.theme.FylvixTextSecondary

@Composable
fun HomeScreen(
    catalog: List<MediaItem>,
    watchHistory: List<WatchHistoryItem>,
    watchlistIds: Set<String>,
    recommendations: List<MediaItem>,
    settings: PlatformSettings,
    activeProfile: UserProfile,
    onOpenDetails: (MediaItem) -> Unit,
    onWatchNow: (MediaItem, EpisodeItem?, Long?) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onNavigateToSection: (MainNavDestination, String, String, String) -> Unit
) {
    val nonAdultCatalog = remember(catalog) { catalog.filter { !it.isAdult18Plus } }
    val featuredItems = remember(nonAdultCatalog) {
        nonAdultCatalog.filter { it.isFeatured }.ifEmpty { nonAdultCatalog.take(4) }
    }
    val latestEpisodes = remember(nonAdultCatalog) {
        nonAdultCatalog
            .filter { it.seasons.isNotEmpty() }
            .flatMap { show ->
                show.seasons.lastOrNull()?.episodes?.map { ep -> show to ep } ?: emptyList()
            }
            .take(10)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Large Hero Carousel
        item {
            HeroBanner(
                featuredItems = featuredItems,
                watchlistIds = watchlistIds,
                onWatchNow = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist,
                onOpenDetails = onOpenDetails
            )
        }

        // 2. Continue Watching Row
        if (watchHistory.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Continue Watching for ${activeProfile.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = FylvixTextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(watchHistory, key = { it.id }) { hist ->
                            val media = catalog.find { it.id == hist.contentId }
                            if (media != null) {
                                val ep = media.seasons.flatMap { it.episodes }.find { it.id == hist.episodeId }
                                ContinueWatchingCard(
                                    historyItem = hist,
                                    mediaItem = media,
                                    onResume = { onWatchNow(media, ep, hist.playbackPositionSec) },
                                    onOpenDetails = { onOpenDetails(media) },
                                    onRemove = { onRemoveHistory(hist.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Featured Content
        item {
            CategoryRow(
                title = "Featured Content",
                subtitle = "Curated Fylvix worldwide premieres in 4K HDR",
                items = featuredItems,
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist,
                onSeeAllClick = { onNavigateToSection(MainNavDestination.MOVIES, "All", "All", "All") }
            )
        }

        // 4. Trending Today
        item {
            CategoryRow(
                title = "Trending Today",
                subtitle = "Top streamed titles across 190+ countries in the last 24 hours",
                items = nonAdultCatalog.filter { it.isTrendingToday },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist,
                onSeeAllClick = { onNavigateToSection(MainNavDestination.MOVIES, "All", "All", "Trending") }
            )
        }

        // 5. Trending This Week
        item {
            CategoryRow(
                title = "Trending This Week",
                items = nonAdultCatalog.filter { it.isTrendingWeek || it.isPopular },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }

        // 6. Recommended For You
        item {
            CategoryRow(
                title = "Recommended For You",
                subtitle = "Personalized by your Watch History, My List, Favorites & Language",
                items = recommendations.filter { !it.isAdult18Plus },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }

        // 7. Popular Movies
        item {
            CategoryRow(
                title = "Popular Movies",
                items = nonAdultCatalog.filter { it.kind == ContentKind.MOVIE && it.isPopular },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist,
                onSeeAllClick = { onNavigateToSection(MainNavDestination.MOVIES, "All", "All", "Popular") }
            )
        }

        // 8. Popular TV Shows
        item {
            CategoryRow(
                title = "Popular TV Shows",
                items = nonAdultCatalog.filter { it.kind == ContentKind.TV_SHOW || it.kind == ContentKind.SERIES },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist,
                onSeeAllClick = { onNavigateToSection(MainNavDestination.TV_SHOWS, "All", "All", "Popular") }
            )
        }

        // 9. Latest Movies
        item {
            CategoryRow(
                title = "Latest Movies",
                items = nonAdultCatalog.filter { it.kind == ContentKind.MOVIE && it.releaseYear >= 2026 },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }

        // 10. Latest Series
        item {
            CategoryRow(
                title = "Latest Series",
                items = nonAdultCatalog.filter { (it.kind == ContentKind.SERIES || it.kind == ContentKind.TV_SHOW) && it.releaseYear >= 2026 },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist,
                onSeeAllClick = { onNavigateToSection(MainNavDestination.SERIES, "All", "All", "Latest") }
            )
        }

        // 11. Latest Episodes Strip
        if (latestEpisodes.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Latest Episodes",
                        style = MaterialTheme.typography.titleLarge,
                        color = FylvixTextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(latestEpisodes, key = { it.second.id }) { (show, ep) ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                                modifier = Modifier
                                    .width(240.dp)
                                    .border(1.dp, FylvixGlassBorder, RoundedCornerShape(14.dp))
                                    .clickable { onWatchNow(show, ep, 0L) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "S${ep.seasonNumber} • EP ${ep.episodeNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FylvixCrimson,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${ep.runtimeMinutes}m",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FylvixGold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = show.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ep.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FylvixTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 12. Top Rated & Upcoming
        item {
            CategoryRow(
                title = "Top Rated Masterpieces",
                items = nonAdultCatalog.filter { it.isTopRated || it.rating >= 8.9 },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }
        item {
            CategoryRow(
                title = "Upcoming Releases",
                items = nonAdultCatalog.filter { it.isUpcoming || it.status == "Upcoming" },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }

        // 13. Anime Rows (Popular Anime & Latest Anime)
        item {
            CategoryRow(
                title = "Popular Anime",
                subtitle = "Simulcast Subbed & Dubbed series and theatrical anime films",
                items = nonAdultCatalog.filter { it.kind == ContentKind.ANIME_SERIES || it.kind == ContentKind.ANIME_MOVIE },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist,
                onSeeAllClick = { onNavigateToSection(MainNavDestination.ANIME, "All", "All", "Popular") }
            )
        }
        item {
            CategoryRow(
                title = "Latest Anime",
                items = nonAdultCatalog.filter { (it.kind == ContentKind.ANIME_SERIES || it.kind == ContentKind.ANIME_MOVIE) && it.releaseYear >= 2026 },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }

        // 14. Worldwide Regional Cinema Hubs
        val regionalHubs = listOf(
            "Hollywood" to "Blockbuster American cinema & original series",
            "Bollywood" to "Indian blockbusters, thrillers & musical dramas",
            "Pakistani" to "Acclaimed Pakistani dramas, telefilms & cinema",
            "Turkish" to "Epic Turkish dizi series & Bosphorus thrillers",
            "Korean" to "K-Dramas, Seoul thrillers & Korean cinema",
            "Japanese" to "Japanese live-action cinema & anime",
            "Asian Cinema" to "Pan-Asian martial arts, historical epics & neon action",
            "Arabic" to "Egyptian, Gulf & Levantine cinema and series",
            "European" to "French, Spanish, German & British productions",
            "African" to "Nollywood, South African & Pan-African stories",
            "International" to "Borderless festival winners & global co-productions"
        )
        regionalHubs.forEach { (hub, desc) ->
            item {
                val hubItems = nonAdultCatalog.filter {
                    it.regionalHub.equals(hub, ignoreCase = true) ||
                        (hub == "Asian Cinema" && it.country in listOf("Japan", "South Korea", "Thailand", "India", "Pakistan")) ||
                        (hub == "International" && it.country !in listOf("United States"))
                }
                CategoryRow(
                    title = hub,
                    subtitle = desc,
                    items = hubItems,
                    watchlistIds = watchlistIds,
                    onItemClick = onOpenDetails,
                    onPlayClick = { onWatchNow(it, null, null) },
                    onToggleWatchlist = onToggleWatchlist
                )
            }
        }

        // 15. Documentaries & Kids & Family
        item {
            CategoryRow(
                title = "Documentaries",
                items = nonAdultCatalog.filter { it.kind == ContentKind.DOCUMENTARY || "Documentary" in it.genres },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }
        item {
            CategoryRow(
                title = "Kids & Family",
                items = nonAdultCatalog.filter { it.kind == ContentKind.KIDS || "Kids & Family" in it.genres || "Animation" in it.genres },
                watchlistIds = watchlistIds,
                onItemClick = onOpenDetails,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }

        // 16. Core Genre Rows (Action, Comedy, Drama, Horror, Romance, Sci-Fi, Thriller, Animation)
        listOf("Action", "Comedy", "Drama", "Horror", "Romance", "Sci-Fi", "Thriller", "Animation").forEach { genreName ->
            item {
                CategoryRow(
                    title = genreName,
                    items = nonAdultCatalog.filter { genreName in it.genres },
                    watchlistIds = watchlistIds,
                    onItemClick = onOpenDetails,
                    onPlayClick = { onWatchNow(it, null, null) },
                    onToggleWatchlist = onToggleWatchlist,
                    onSeeAllClick = { onNavigateToSection(MainNavDestination.MOVIES, genreName, "All", "All") }
                )
            }
        }

        // 17. Configurable 18+ / Adult Row (Controlled by Backend/Admin & Profile Settings without repetitive popup)
        val adultItems = catalog.filter { it.isAdult18Plus && it.showOnHomepage }
        if (settings.backendAdultCategoryEnabled && settings.adultAllowedOnHomepage && activeProfile.allowAdultContent && !activeProfile.isKidsProfile && adultItems.isNotEmpty()) {
            item {
                CategoryRow(
                    title = "18+ / Adult Late-Night Cinema",
                    subtitle = "Backend-classified 18+ uncut cinema & mature series",
                    items = adultItems,
                    watchlistIds = watchlistIds,
                    onItemClick = onOpenDetails,
                    onPlayClick = { onWatchNow(it, null, null) },
                    onToggleWatchlist = onToggleWatchlist,
                    onSeeAllClick = { onNavigateToSection(MainNavDestination.ADULT, "All", "All", "All") }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogGridScreen(
    destination: MainNavDestination,
    catalog: List<MediaItem>,
    filterState: FilterState,
    visibleLimit: Int,
    watchlistIds: Set<String>,
    settings: PlatformSettings,
    screenWidthDp: Dp,
    onUpdateFilter: ((FilterState) -> FilterState) -> Unit,
    onResetFilters: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenDetails: (MediaItem) -> Unit,
    onPlayMedia: (MediaItem) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onToggleAdultBackendSetting: (Boolean) -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    val categoryTabs = remember(destination) {
        when (destination) {
            MainNavDestination.MOVIES -> listOf("All", "Trending", "Popular", "Latest", "Top Rated", "Upcoming", "Now Playing")
            MainNavDestination.TV_SHOWS, MainNavDestination.SERIES -> listOf("All", "Popular", "Trending", "Latest", "Top Rated", "Airing Today", "On Air", "Completed")
            MainNavDestination.ANIME -> listOf("All", "Popular", "Trending", "Latest", "Ongoing", "Completed", "Anime Movies", "Anime Series", "Dubbed", "Subbed")
            MainNavDestination.ADULT -> listOf("All", "Adult Movies", "Adult Series")
            else -> listOf("All", "Trending", "Popular", "Latest", "Top Rated")
        }
    }

    val baseItems = remember(destination, catalog) {
        when (destination) {
            MainNavDestination.MOVIES -> catalog.filter {
                !it.isAdult18Plus && (it.kind == ContentKind.MOVIE || it.kind == ContentKind.DOCUMENTARY || it.kind == ContentKind.KIDS)
            }
            MainNavDestination.TV_SHOWS -> catalog.filter {
                !it.isAdult18Plus && (it.kind == ContentKind.TV_SHOW || it.kind == ContentKind.SERIES || it.kind == ContentKind.DOCUMENTARY)
            }
            MainNavDestination.SERIES -> catalog.filter {
                !it.isAdult18Plus && (it.kind == ContentKind.SERIES || it.kind == ContentKind.TV_SHOW)
            }
            MainNavDestination.ANIME -> catalog.filter {
                !it.isAdult18Plus && (it.kind == ContentKind.ANIME_SERIES || it.kind == ContentKind.ANIME_MOVIE)
            }
            MainNavDestination.ADULT -> catalog.filter { it.isAdult18Plus }
            else -> catalog.filter { !it.isAdult18Plus }
        }
    }

    val filteredItems = remember(baseItems, filterState) {
        baseItems.filter { item ->
            val matchesTab = when (filterState.categoryTab) {
                "All" -> true
                "Trending" -> item.isTrendingToday || item.isTrendingWeek
                "Popular" -> item.isPopular
                "Latest" -> item.releaseYear >= 2026
                "Top Rated" -> item.isTopRated || item.rating >= 8.8
                "Upcoming" -> item.isUpcoming || item.status == "Upcoming"
                "Now Playing" -> item.status == "Now Playing"
                "Airing Today" -> item.status == "Airing Today"
                "On Air" -> item.status == "On Air" || item.status == "Airing Today"
                "Completed" -> item.status == "Completed"
                "Ongoing" -> item.status == "Ongoing"
                "Anime Movies" -> item.kind == ContentKind.ANIME_MOVIE
                "Anime Series" -> item.kind == ContentKind.ANIME_SERIES
                "Dubbed" -> item.isDubbed
                "Subbed" -> item.isSubbed
                "Adult Movies" -> item.kind == ContentKind.ADULT_MOVIE
                "Adult Series" -> item.kind == ContentKind.ADULT_SERIES
                else -> true
            }
            val matchesGenre = filterState.genre == "All" || item.genres.any { it.equals(filterState.genre, ignoreCase = true) }
            val matchesCountry = filterState.country == "All" || item.country.equals(filterState.country, ignoreCase = true)
            val matchesLang = filterState.language == "All" || item.language.equals(filterState.language, ignoreCase = true)
            val matchesYear = filterState.year == "All" || item.releaseYear.toString() == filterState.year
            val matchesCert = filterState.certification == "All" || item.certification.equals(filterState.certification, ignoreCase = true)
            val matchesQuery = filterState.query.isBlank() ||
                item.title.contains(filterState.query, ignoreCase = true) ||
                item.originalTitle.contains(filterState.query, ignoreCase = true) ||
                item.director.contains(filterState.query, ignoreCase = true) ||
                item.country.contains(filterState.query, ignoreCase = true) ||
                item.genres.any { it.contains(filterState.query, ignoreCase = true) }

            matchesTab && matchesGenre && matchesCountry && matchesLang && matchesYear && matchesCert && matchesQuery
        }.let { list ->
            when (filterState.sortBy) {
                "Rating" -> list.sortedByDescending { it.rating }
                "Latest" -> list.sortedByDescending { it.releaseYear }
                "Title A-Z" -> list.sortedBy { it.title }
                "Runtime" -> list.sortedByDescending { it.runtimeMinutes }
                else -> list.sortedByDescending { it.voteCount }
            }
        }
    }

    val columns = when {
        screenWidthDp >= 1200.dp -> 6
        screenWidthDp >= 900.dp -> 5
        screenWidthDp >= 600.dp -> 3
        else -> 2
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("catalog_grid_screen_${destination.name.lowercase()}"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Section Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.headlineMedium,
                            color = FylvixTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredItems.size} authorized titles available • Adaptive 4K HLS",
                            style = MaterialTheme.typography.bodySmall,
                            color = FylvixTextSecondary
                        )
                    }
                    if (destination == MainNavDestination.ADULT) {
                        Surface(
                            color = FylvixCrimson.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FylvixCrimson)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = FylvixCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "18+ Classification Active",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Configurable 18+ Policy Governance Banner (No annoying popup on every visit!)
                if (destination == MainNavDestination.ADULT) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FylvixGlassBorder, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Backend-Controlled 18+ Classification & Regional Policy",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = FylvixGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Managed dynamically via Fylvix Admin & Profile classification rules without intrusive per-visit popups. Region: ${settings.activeRegionCode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FylvixTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = onOpenAdminPanel,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Policy Admin", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Inline Search Bar inside Catalog
                OutlinedTextField(
                    value = filterState.query,
                    onValueChange = { q -> onUpdateFilter { it.copy(query = q) } },
                    placeholder = { Text("Filter ${destination.label} by title, director, country, genre…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_inline_search")
                )
            }
        }

        // Filter & Sort Panel
        item {
            FilterPanel(
                filterState = filterState,
                categoryTabs = categoryTabs,
                onUpdateFilter = onUpdateFilter,
                onResetFilters = onResetFilters
            )
        }

        // Responsive Grid of Media Cards
        if (filteredItems.isEmpty()) {
            item {
                EmptyState(
                    title = "No ${destination.label} Match Your Filters",
                    subtitle = "Try clearing your genre, country, language, or year filters.",
                    onAction = onResetFilters
                )
            }
        } else {
            val visibleSlice = filteredItems.take(visibleLimit)
            val rows = visibleSlice.chunked(columns)
            items(rows) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        MovieCard(
                            item = item,
                            isInWatchlist = watchlistIds.contains(item.id),
                            onClick = { onOpenDetails(item) },
                            onPlayClick = { onPlayMedia(item) },
                            onToggleWatchlist = { onToggleWatchlist(item.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Pagination / Load More
            if (filteredItems.size > visibleLimit) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onLoadMore,
                            colors = ButtonDefaults.buttonColors(containerColor = FylvixSurfaceElevated),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("load_more_catalog_button")
                        ) {
                            Text(
                                text = "Load More Titles (${visibleSlice.size} of ${filteredItems.size})",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaxonomyDirectoryScreen(
    mode: MainNavDestination, // GENRES, COUNTRIES, LANGUAGES
    catalog: List<MediaItem>,
    onSelectTaxonomyItem: (String) -> Unit
) {
    val entries = remember(mode, catalog) {
        when (mode) {
            MainNavDestination.GENRES -> FylvixSeedCatalog.allGenres.map { g ->
                val count = catalog.count { item -> item.genres.any { it.equals(g, ignoreCase = true) } }
                Triple(g, "$count titles • Movies, Series & Anime", Icons.Default.Explore)
            }
            MainNavDestination.COUNTRIES -> FylvixSeedCatalog.allCountries.map { c ->
                val count = catalog.count { item -> item.country.equals(c, ignoreCase = true) }
                Triple(c, "$count regional productions streaming", Icons.Default.Public)
            }
            else -> FylvixSeedCatalog.allLanguages.map { l ->
                val count = catalog.count { item -> item.language.equals(l, ignoreCase = true) }
                Triple(l, "$count original audio & subtitle tracks", Icons.Default.Language)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Explore Worldwide by ${mode.label}",
                style = MaterialTheme.typography.headlineMedium,
                color = FylvixTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select any ${mode.label.lowercase().removeSuffix("s")} below to filter Fylvix movies, series, and anime.",
                style = MaterialTheme.typography.bodyMedium,
                color = FylvixTextSecondary
            )
        }

        items(entries.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { (title, subtitle, iconVec) ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, FylvixGlassBorder, RoundedCornerShape(16.dp))
                            .clickable { onSelectTaxonomyItem(title) }
                            .testTag("taxonomy_card_${title.lowercase().replace(" ", "_")}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            FylvixCrimson.copy(alpha = 0.16f),
                                            FylvixSurfaceCard
                                        )
                                    )
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = null,
                                tint = FylvixGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = FylvixTextSecondary
                            )
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun LiveTvScreen(
    channels: List<LiveTvChannel>,
    onWatchLiveChannel: (LiveTvChannel) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All Channels") }
    var selectedCountry by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf(
        "All Channels", "News", "Sports", "Entertainment",
        "Movies", "Music", "Kids", "Documentary", "International"
    )
    val countries = remember(channels) {
        listOf("All") + channels.map { it.country }.distinct()
    }

    val filteredChannels = remember(channels, selectedCategory, selectedCountry, searchQuery) {
        channels.filter { ch ->
            ch.isPublished &&
                (selectedCategory == "All Channels" || ch.category.equals(selectedCategory, ignoreCase = true)) &&
                (selectedCountry == "All" || ch.country.equals(selectedCountry, ignoreCase = true)) &&
                (searchQuery.isBlank() ||
                    ch.name.contains(searchQuery, ignoreCase = true) ||
                    ch.currentProgram.contains(searchQuery, ignoreCase = true) ||
                    ch.country.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("live_tv_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = FylvixCrimson,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Fylvix Live TV & Global Broadcasts",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FylvixTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Authorized 24/7 live channels with real-time EPG schedules",
                        style = MaterialTheme.typography.bodySmall,
                        color = FylvixTextSecondary
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search live channels, programs, or countries…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_channel_search_input")
            )
        }

        // Category Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FylvixCrimson,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Country Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Country:",
                    style = MaterialTheme.typography.labelMedium,
                    color = FylvixGold
                )
                countries.forEach { country ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedCountry == country) FylvixCyan.copy(alpha = 0.25f) else FylvixSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGlassBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedCountry = country }
                    ) {
                        Text(
                            text = country,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedCountry == country) FylvixCyan else FylvixTextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        items(filteredChannels, key = { it.id }) { channel ->
            ChannelCard(
                channel = channel,
                onWatchLive = { onWatchLiveChannel(channel) }
            )
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}
