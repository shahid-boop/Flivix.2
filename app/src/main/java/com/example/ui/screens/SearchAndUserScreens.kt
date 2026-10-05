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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.models.ContentKind
import com.example.data.models.EpisodeItem
import com.example.data.models.FilterState
import com.example.data.models.MediaItem
import com.example.data.models.PlatformSettings
import com.example.data.models.UserAccount
import com.example.data.models.UserProfile
import com.example.data.models.WatchHistoryItem
import com.example.data.seed.FylvixSeedCatalog
import com.example.ui.components.EmptyState
import com.example.ui.components.FilterPanel
import com.example.ui.components.MovieCard
import com.example.ui.components.PersonCard
import com.example.ui.components.ProfileAvatar
import com.example.ui.theme.FylvixCrimson
import com.example.ui.theme.FylvixCyan
import com.example.ui.theme.FylvixEmerald
import com.example.ui.theme.FylvixGlassBorder
import com.example.ui.theme.FylvixGold
import com.example.ui.theme.FylvixSurfaceCard
import com.example.ui.theme.FylvixSurfaceElevated
import com.example.ui.theme.FylvixTextMuted
import com.example.ui.theme.FylvixTextPrimary
import com.example.ui.theme.FylvixTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalSearchScreen(
    catalog: List<MediaItem>,
    rawQuery: String,
    filterState: FilterState,
    searchHistory: List<String>,
    watchlistIds: Set<String>,
    settings: PlatformSettings,
    screenWidthDp: Dp,
    onQueryChange: (String) -> Unit,
    onCommitQuery: (String) -> Unit,
    onRemoveHistoryQuery: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    onUpdateFilter: ((FilterState) -> FilterState) -> Unit,
    onResetFilters: () -> Unit,
    onOpenDetails: (MediaItem) -> Unit,
    onPlayMedia: (MediaItem, EpisodeItem?) -> Unit,
    onToggleWatchlist: (String) -> Unit
) {
    val searchableCatalog = remember(catalog, settings) {
        catalog.filter {
            if (it.isAdult18Plus) settings.backendAdultCategoryEnabled && settings.adultAllowedInSearch && it.showInSearch
            else it.showInSearch
        }
    }

    // Instant Autocomplete Suggestions
    val suggestions = remember(rawQuery, searchableCatalog) {
        if (rawQuery.trim().isEmpty()) emptyList()
        else {
            val q = rawQuery.trim().lowercase()
            val titleMatches = searchableCatalog.filter {
                it.title.lowercase().contains(q) || it.originalTitle.lowercase().contains(q)
            }.map { it.title }
            val peopleMatches = searchableCatalog.flatMap { it.cast.map { c -> c.name } + it.director }
                .distinct()
                .filter { it.lowercase().contains(q) }
            val taxonomyMatches = (FylvixSeedCatalog.allGenres + FylvixSeedCatalog.allCountries + FylvixSeedCatalog.allLanguages)
                .filter { it.lowercase().contains(q) }
            (titleMatches + peopleMatches + taxonomyMatches).distinct().take(6)
        }
    }

    val matchedItems = remember(searchableCatalog, filterState) {
        val q = filterState.query.trim().lowercase()
        searchableCatalog.filter { item ->
            val typeMatch = when (filterState.categoryTab) {
                "All" -> true
                "Movies" -> item.kind == ContentKind.MOVIE || item.kind == ContentKind.DOCUMENTARY || item.kind == ContentKind.KIDS
                "Series & TV" -> item.kind == ContentKind.SERIES || item.kind == ContentKind.TV_SHOW
                "Anime" -> item.kind == ContentKind.ANIME_SERIES || item.kind == ContentKind.ANIME_MOVIE
                "18+" -> item.isAdult18Plus
                else -> true
            }
            val genreMatch = filterState.genre == "All" || item.genres.any { it.equals(filterState.genre, ignoreCase = true) }
            val countryMatch = filterState.country == "All" || item.country.equals(filterState.country, ignoreCase = true)
            val langMatch = filterState.language == "All" || item.language.equals(filterState.language, ignoreCase = true)
            val yearMatch = filterState.year == "All" || item.releaseYear.toString() == filterState.year

            val queryMatch = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.originalTitle.lowercase().contains(q) ||
                item.director.lowercase().contains(q) ||
                item.writers.any { it.lowercase().contains(q) } ||
                item.cast.any { it.name.lowercase().contains(q) || it.characterName.lowercase().contains(q) } ||
                item.genres.any { it.lowercase().contains(q) } ||
                item.country.lowercase().contains(q) ||
                item.language.lowercase().contains(q) ||
                item.releaseYear.toString().contains(q) ||
                item.seasons.any { s -> s.episodes.any { ep -> ep.title.lowercase().contains(q) } }

            typeMatch && genreMatch && countryMatch && langMatch && yearMatch && queryMatch
        }
    }

    val matchedPeople = remember(searchableCatalog, filterState.query) {
        val q = filterState.query.trim().lowercase()
        if (q.isEmpty()) emptyList()
        else searchableCatalog
            .flatMap { it.cast }
            .distinctBy { it.name }
            .filter { it.name.lowercase().contains(q) || it.characterName.lowercase().contains(q) }
            .take(8)
    }

    val matchedEpisodes = remember(searchableCatalog, filterState.query) {
        val q = filterState.query.trim().lowercase()
        if (q.isEmpty()) emptyList()
        else searchableCatalog.flatMap { show ->
            show.seasons.flatMap { s -> s.episodes.map { ep -> show to ep } }
        }.filter { (show, ep) ->
            ep.title.lowercase().contains(q) || show.title.lowercase().contains(q)
        }.take(6)
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
            .testTag("global_search_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Search Input Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Global Discovery & Search",
                    style = MaterialTheme.typography.headlineMedium,
                    color = FylvixTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = rawQuery,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text("Search by title, actor, director, genre, country, language, or year…")
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (rawQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input")
                )

                // Instant Suggestions Strip
                if (suggestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Suggestions:", style = MaterialTheme.typography.labelSmall, color = FylvixGold)
                        suggestions.forEach { sug ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = FylvixSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGlassBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        onQueryChange(sug)
                                        onCommitQuery(sug)
                                    }
                            ) {
                                Text(
                                    text = sug,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Recent Search History
                if (searchHistory.isNotEmpty() && rawQuery.isBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.labelLarge,
                                color = FylvixTextSecondary
                            )
                            TextButton(onClick = onClearSearchHistory) {
                                Text("Clear All", style = MaterialTheme.typography.labelSmall, color = FylvixCrimson)
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            searchHistory.forEach { histQuery ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FylvixSurfaceCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGlassBorder),
                                    modifier = Modifier.clickable { onQueryChange(histQuery) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp), tint = FylvixGold)
                                        Text(histQuery, style = MaterialTheme.typography.labelMedium, color = Color.White)
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Delete Query",
                                            tint = FylvixTextMuted,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { onRemoveHistoryQuery(histQuery) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Panel
        item {
            FilterPanel(
                filterState = filterState,
                categoryTabs = listOf("All", "Movies", "Series & TV", "Anime", "18+"),
                onUpdateFilter = onUpdateFilter,
                onResetFilters = onResetFilters
            )
        }

        // Matched People (Actors & Directors)
        if (matchedPeople.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Matching Cast & Creators",
                        style = MaterialTheme.typography.titleMedium,
                        color = FylvixGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(matchedPeople, key = { it.id }) { person ->
                            PersonCard(
                                person = person,
                                onClick = { onQueryChange(person.name) }
                            )
                        }
                    }
                }
            }
        }

        // Matched Episodes
        if (matchedEpisodes.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Matching Episodes",
                        style = MaterialTheme.typography.titleMedium,
                        color = FylvixCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(matchedEpisodes, key = { it.second.id }) { (show, ep) ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                                modifier = Modifier
                                    .width(230.dp)
                                    .border(1.dp, FylvixGlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { onPlayMedia(show, ep) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "${show.title} • S${ep.seasonNumber}:E${ep.episodeNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FylvixGold
                                    )
                                    Text(
                                        text = ep.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
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

        // Matched Movies, Series, Anime, 18+
        if (matchedItems.isEmpty()) {
            item {
                EmptyState(
                    title = "No Direct Matches for \"${filterState.query}\"",
                    subtitle = "Explore related worldwide titles below or reset your search filters.",
                    onAction = onResetFilters
                )
            }
        } else {
            val rows = matchedItems.chunked(columns)
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
                            onClick = {
                                onCommitQuery(rawQuery)
                                onOpenDetails(item)
                            },
                            onPlayClick = {
                                onCommitQuery(rawQuery)
                                onPlayMedia(item, null)
                            },
                            onToggleWatchlist = { onToggleWatchlist(item.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun MyListAndFavoritesScreen(
    catalog: List<MediaItem>,
    watchlistIds: Set<String>,
    favoriteIds: Set<String>,
    watchHistory: List<WatchHistoryItem>,
    screenWidthDp: Dp,
    onOpenDetails: (MediaItem) -> Unit,
    onPlayMedia: (MediaItem) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf("My List") }
    var filterKind by remember { mutableStateOf("All") }

    val savedItems = remember(catalog, watchlistIds, favoriteIds, activeTab, filterKind) {
        val idSet = if (activeTab == "My List") watchlistIds else favoriteIds
        catalog.filter { it.id in idSet }.filter { item ->
            when (filterKind) {
                "Movies" -> item.kind == ContentKind.MOVIE || item.kind == ContentKind.ADULT_MOVIE
                "Series" -> item.kind == ContentKind.SERIES || item.kind == ContentKind.TV_SHOW || item.kind == ContentKind.ADULT_SERIES
                "Anime" -> item.kind == ContentKind.ANIME_SERIES || item.kind == ContentKind.ANIME_MOVIE
                else -> true
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
            .testTag("my_list_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "My List & Saved Favorites",
                style = MaterialTheme.typography.headlineMedium,
                color = FylvixTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("My List", "Favorites").forEach { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = {
                            val count = if (tab == "My List") watchlistIds.size else favoriteIds.size
                            Text("$tab ($count)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FylvixCrimson,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Movies", "Series", "Anime").forEach { kind ->
                    FilterChip(
                        selected = filterKind == kind,
                        onClick = { filterKind = kind },
                        label = { Text(kind) }
                    )
                }
            }
        }

        if (savedItems.isEmpty()) {
            item {
                EmptyState(
                    title = "Your $activeTab Is Empty",
                    subtitle = "Tap '+ My List' or the Heart icon on any movie, series, or anime to save it here.",
                    actionLabel = "Show All Types",
                    onAction = { filterKind = "All" }
                )
            }
        } else {
            items(savedItems.chunked(columns)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        MovieCard(
                            item = item,
                            isInWatchlist = watchlistIds.contains(item.id),
                            onClick = { onOpenDetails(item) },
                            onPlayClick = { onPlayMedia(item) },
                            onToggleWatchlist = {
                                if (activeTab == "My List") onToggleWatchlist(item.id)
                                else onToggleFavorite(item.id)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun WatchHistoryScreen(
    catalog: List<MediaItem>,
    watchHistory: List<WatchHistoryItem>,
    onResumeItem: (MediaItem, EpisodeItem?, Long) -> Unit,
    onOpenDetails: (MediaItem) -> Unit,
    onRemoveEntry: (String) -> Unit,
    onClearAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("watch_history_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Watch History & Resume Sync",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FylvixTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Synced playback position, duration & completion status",
                        style = MaterialTheme.typography.bodySmall,
                        color = FylvixTextSecondary
                    )
                }
                if (watchHistory.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearAll,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("clear_watch_history_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        if (watchHistory.isEmpty()) {
            item {
                EmptyState(
                    title = "No Watch History Yet",
                    subtitle = "Movies, series episodes, and anime you watch will automatically track progress here.",
                    actionLabel = "Refresh",
                    onAction = {}
                )
            }
        } else {
            items(watchHistory, key = { it.id }) { hist ->
                val item = catalog.find { it.id == hist.contentId }
                if (item != null) {
                    val ep = item.seasons.flatMap { it.episodes }.find { it.id == hist.episodeId }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FylvixGlassBorder, RoundedCornerShape(16.dp))
                            .clickable { onResumeItem(item, ep, hist.playbackPositionSec) }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (ep != null) {
                                        Text(
                                            text = "Season ${ep.seasonNumber} • Episode ${ep.episodeNumber}: ${ep.title}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = FylvixGold
                                        )
                                    }
                                    Text(
                                        text = "Profile: ${hist.profileId} • Position: ${hist.playbackPositionSec / 60}m / ${hist.durationSec / 60}m (${hist.percentageWatched.toInt()}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FylvixTextSecondary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (hist.isCompleted) {
                                        Surface(
                                            color = FylvixEmerald.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "COMPLETED",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FylvixEmerald,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { onResumeItem(item, ep, hist.playbackPositionSec) },
                                        colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Resume", style = MaterialTheme.typography.labelSmall)
                                    }
                                    IconButton(onClick = { onRemoveEntry(hist.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = FylvixTextMuted)
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { (hist.percentageWatched / 100f).coerceIn(0.04f, 1f) },
                                color = FylvixCrimson,
                                trackColor = FylvixGlassBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun ProfileAndAuthScreen(
    userAccount: UserAccount,
    profiles: List<UserProfile>,
    activeProfile: UserProfile,
    watchlistCount: Int,
    favoritesCount: Int,
    historyCount: Int,
    onSwitchProfile: (UserProfile) -> Unit,
    onCreateProfile: (String, String, String, Boolean, Boolean, String) -> Unit,
    onToggleProfileAdultAccess: (Boolean) -> Unit,
    onLogin: (String, String, Boolean) -> Unit,
    onLogout: () -> Unit,
    onSendResetPassword: (String) -> Unit
) {
    var authTab by remember { mutableStateOf("Login") }
    var emailInput by remember { mutableStateOf(userAccount.email) }
    var usernameInput by remember { mutableStateOf(userAccount.username) }
    var passwordInput by remember { mutableStateOf("••••••••••••") }
    var showCreateProfileDialog by remember { mutableStateOf(false) }

    var newProfileName by remember { mutableStateOf("") }
    var newProfileHandle by remember { mutableStateOf("") }
    var newProfileEmoji by remember { mutableStateOf("🍿") }
    var newProfileKids by remember { mutableStateOf(false) }
    var newProfileAdult by remember { mutableStateOf(true) }
    var newProfileLang by remember { mutableStateOf("English") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_and_auth_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Multi-Profile Switcher Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FylvixGlassBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Who's Watching Fylvix?",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Multi-Profile Architecture • Isolated Watchlist, History & Classification",
                                style = MaterialTheme.typography.bodySmall,
                                color = FylvixTextSecondary
                            )
                        }
                        OutlinedButton(
                            onClick = { showCreateProfileDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_profile_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Profile", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(profiles, key = { it.id }) { prof ->
                            val isSelected = prof.id == activeProfile.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) FylvixCrimson.copy(alpha = 0.16f) else FylvixSurfaceElevated)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) FylvixCrimson else FylvixGlassBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onSwitchProfile(prof) }
                                    .padding(14.dp)
                                    .testTag("switch_profile_card_${prof.id}")
                            ) {
                                ProfileAvatar(profile = prof, sizeDp = 52, onClick = { onSwitchProfile(prof) })
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(prof.name, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(prof.usernameHandle, style = MaterialTheme.typography.labelSmall, color = FylvixGold)
                                Text(
                                    text = if (prof.isKidsProfile) "Kids Safe" else if (prof.allowAdultContent) "All + 18+" else "Standard",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FylvixCyan
                                )
                            }
                        }
                    }

                    // Active Profile Quick Stats & Adult Classification Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FylvixSurfaceElevated)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active: ${activeProfile.name} (${activeProfile.preferredLanguage})",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$watchlistCount in My List • $favoritesCount Favorites • $historyCount Watched",
                                style = MaterialTheme.typography.labelSmall,
                                color = FylvixGold
                            )
                        }
                        if (!activeProfile.isKidsProfile) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("18+ Access", style = MaterialTheme.typography.labelSmall, color = FylvixTextSecondary)
                                Switch(
                                    checked = activeProfile.allowAdultContent,
                                    onCheckedChange = onToggleProfileAdultAccess
                                )
                            }
                        }
                    }
                }
            }
        }

        // Account & Authentication System Card (Login / Register / Forgot Password / Reset / Logout)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FylvixGlassBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = FylvixCrimson)
                            Column {
                                Text(
                                    text = if (userAccount.isLoggedIn) "Authenticated Account: ${userAccount.username}" else "Sign In to Fylvix Global",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${userAccount.email} • Role: ${userAccount.role.uppercase()} • Email Verified",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FylvixEmerald
                                )
                            }
                        }
                        if (userAccount.isLoggedIn) {
                            OutlinedButton(
                                onClick = onLogout,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("logout_button")
                            ) {
                                Text("Logout", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Login", "Register", "Forgot / Reset Password").forEach { tab ->
                            FilterChip(
                                selected = authTab == tab,
                                onClick = { authTab = tab },
                                label = { Text(tab) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FylvixCrimson,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (authTab == "Register" || authTab == "Login") {
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password (Argon2id Hashed on Backend)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { onLogin(emailInput, usernameInput, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_auth_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (authTab == "Register") "Create Fylvix Account & Verify Email" else "Sign In with JWT Session")
                        }
                    } else {
                        Button(
                            onClick = { onSendResetPassword(emailInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = FylvixGold, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Password Reset Token")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showCreateProfileDialog) {
        AlertDialog(
            onDismissRequest = { showCreateProfileDialog = false },
            title = { Text("Create New Viewer Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newProfileHandle,
                        onValueChange = { newProfileHandle = it },
                        label = { Text("Username Handle (e.g. @cinema)") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("🍿", "🎬", "🌏", "🚀", "🐉", "👑").forEach { em ->
                            FilterChip(
                                selected = newProfileEmoji == em,
                                onClick = { newProfileEmoji = em },
                                label = { Text(em) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kids-Safe Profile (G/PG Only)")
                        Switch(checked = newProfileKids, onCheckedChange = { newProfileKids = it })
                    }
                    if (!newProfileKids) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow 18+ Classification")
                            Switch(checked = newProfileAdult, onCheckedChange = { newProfileAdult = it })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateProfile(
                            newProfileName,
                            newProfileHandle,
                            newProfileEmoji,
                            newProfileKids,
                            newProfileAdult,
                            newProfileLang
                        )
                        showCreateProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson)
                ) {
                    Text("Create Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsScreen(
    settings: PlatformSettings,
    onUpdateSettings: ((PlatformSettings) -> PlatformSettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Platform, Playback & Classification Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = FylvixTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // 1. Appearance & Language
        item {
            SettingsCardGroup(title = "Theme & Localization") {
                Text("Visual Theme", style = MaterialTheme.typography.labelLarge, color = FylvixGold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cinematic Dark", "Midnight Gold", "Studio Light").forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onUpdateSettings { it.copy(themeMode = mode) } },
                            label = { Text(mode) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FylvixCrimson,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text("Interface Language", style = MaterialTheme.typography.labelLarge, color = FylvixGold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FylvixSeedCatalog.allLanguages.take(8).forEach { lang ->
                        FilterChip(
                            selected = settings.appLanguage == lang,
                            onClick = { onUpdateSettings { it.copy(appLanguage = lang) } },
                            label = { Text(lang) }
                        )
                    }
                }
            }
        }

        // 2. Streaming & Playback Settings
        item {
            SettingsCardGroup(title = "Streaming & Player Preferences") {
                Text("Default Streaming Quality", style = MaterialTheme.typography.labelLarge, color = FylvixGold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Auto (Adaptive HLS)", "4K UHD", "1080p FHD", "720p HD", "480p Data Saver").forEach { q ->
                        FilterChip(
                            selected = settings.defaultQuality == q,
                            onClick = { onUpdateSettings { it.copy(defaultQuality = q) } },
                            label = { Text(q) }
                        )
                    }
                }

                Text("Default Subtitle Language", style = MaterialTheme.typography.labelLarge, color = FylvixGold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("English [CC]", "Urdu", "Hindi", "Turkish", "Korean", "Japanese", "Arabic", "Off").forEach { sub ->
                        FilterChip(
                            selected = settings.preferredSubtitleLanguage == sub,
                            onClick = { onUpdateSettings { it.copy(preferredSubtitleLanguage = sub) } },
                            label = { Text(sub) }
                        )
                    }
                }

                SettingsToggleRow(
                    label = "Autoplay Next Episode",
                    subtitle = "Automatically start the next episode in Series & Anime",
                    checked = settings.autoplayNextEpisode,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(autoplayNextEpisode = v) } }
                )
                SettingsToggleRow(
                    label = "Autoplay Hero Previews",
                    subtitle = "Play muted widescreen previews on home carousel",
                    checked = settings.autoplayPreviews,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(autoplayPreviews = v) } }
                )
            }
        }

        // 3. Configurable 18+ / Adult Content Policy (Backend/Admin & Regional Controls)
        item {
            SettingsCardGroup(title = "18+ / Adult Content Classification Policy") {
                Text(
                    text = "Fylvix implements 18+ content as a configurable backend/profile classification rather than showing a repetitive age popup on every visit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FylvixTextSecondary
                )
                SettingsToggleRow(
                    label = "Enable 18+ Category Availability",
                    subtitle = "Controls whether the 18+ navigation tab and catalog are enabled",
                    checked = settings.backendAdultCategoryEnabled,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(backendAdultCategoryEnabled = v) } }
                )
                SettingsToggleRow(
                    label = "Include 18+ Titles in Global Search",
                    subtitle = "Allow adult-classified movies and series in search results",
                    checked = settings.adultAllowedInSearch,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(adultAllowedInSearch = v) } }
                )
                SettingsToggleRow(
                    label = "Show 18+ Row on Homepage",
                    subtitle = "Display the 18+ Late-Night Cinema row at the bottom of Home",
                    checked = settings.adultAllowedOnHomepage,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(adultAllowedOnHomepage = v) } }
                )
            }
        }

        // 4. Notification Preferences
        item {
            SettingsCardGroup(title = "Notification Preferences") {
                SettingsToggleRow(
                    label = "New Movie Premieres",
                    subtitle = "Notify when 4K theatrical releases arrive",
                    checked = settings.notificationsNewMovies,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(notificationsNewMovies = v) } }
                )
                SettingsToggleRow(
                    label = "New Series & Anime Episodes",
                    subtitle = "Simulcast alerts for shows in your My List",
                    checked = settings.notificationsNewEpisodes,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(notificationsNewEpisodes = v) } }
                )
                SettingsToggleRow(
                    label = "Personalized Recommendations",
                    subtitle = "Weekly curated picks based on your watch history",
                    checked = settings.notificationsRecommendations,
                    onCheckedChange = { v -> onUpdateSettings { it.copy(notificationsRecommendations = v) } }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun SettingsCardGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FylvixGlassBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FylvixTextSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
