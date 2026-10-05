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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import com.example.data.models.AuditLogEntry
import com.example.data.models.ContentKind
import com.example.data.models.ContentReport
import com.example.data.models.LiveTvChannel
import com.example.data.models.MediaItem
import com.example.data.models.MetadataProviderConfig
import com.example.data.models.PlatformSettings
import com.example.data.seed.FylvixSeedCatalog
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
import com.example.ui.viewmodel.AnalyticsMetrics

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPanelScreen(
    catalog: List<MediaItem>,
    liveChannels: List<LiveTvChannel>,
    analytics: AnalyticsMetrics,
    settings: PlatformSettings,
    providers: List<MetadataProviderConfig>,
    reports: List<ContentReport>,
    auditLogs: List<AuditLogEntry>,
    onAddOrUpdateMedia: (
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
    ) -> Unit,
    onTogglePublishMedia: (String) -> Unit,
    onToggleFeaturedMedia: (String) -> Unit,
    onAddEpisodeToShow: (String, Int, String, String, Int) -> Unit,
    onAddLiveChannel: (String, String, String, String, String, String, String, String) -> Unit,
    onToggleLiveChannel: (String) -> Unit,
    onUpdateSettings: ((PlatformSettings) -> PlatformSettings) -> Unit,
    onTriggerProviderSync: (String) -> Unit,
    onResolveReport: (String) -> Unit
) {
    var activeModule by remember { mutableStateOf("Dashboard") }
    val adminModules = listOf(
        "Dashboard",
        "Add Movie / Content",
        "Catalog & Banners",
        "Series & Episodes",
        "18+ Content & Policy",
        "Live TV Manager",
        "API Providers",
        "Reports & Audit Logs"
    )

    // Form State for Adding Movie / Series / Anime / 18+
    var formTitle by remember { mutableStateOf("") }
    var formOriginalTitle by remember { mutableStateOf("") }
    var formOverview by remember { mutableStateOf("") }
    var formKind by remember { mutableStateOf(ContentKind.MOVIE) }
    var formYear by remember { mutableStateOf("2026") }
    var formRuntime by remember { mutableStateOf("132") }
    var formCountry by remember { mutableStateOf("United States") }
    var formLanguage by remember { mutableStateOf("English") }
    var formHub by remember { mutableStateOf("Hollywood") }
    var formGenre by remember { mutableStateOf("Sci-Fi") }
    var formRating by remember { mutableStateOf("8.9") }
    var formCert by remember { mutableStateOf("PG-13") }
    var formDirector by remember { mutableStateOf("Elena Vance") }
    var formWriter by remember { mutableStateOf("Marcus Sterling") }
    var formQuality by remember { mutableStateOf("4K UHD") }
    var formFeatured by remember { mutableStateOf(true) }
    var formTrending by remember { mutableStateOf(true) }
    var formAdult by remember { mutableStateOf(false) }
    var formPublished by remember { mutableStateOf(true) }

    // Series Episode Form State
    val seriesItems = remember(catalog) { catalog.filter { it.seasons.isNotEmpty() } }
    var selectedShowId by remember(seriesItems) {
        mutableStateOf(seriesItems.firstOrNull()?.id ?: "tv_sovereign_grid")
    }
    var epSeasonNum by remember { mutableIntStateOf(1) }
    var epTitle by remember { mutableStateOf("") }
    var epDesc by remember { mutableStateOf("") }
    var epRuntime by remember { mutableStateOf("48") }

    // Live TV Form State
    var chName by remember { mutableStateOf("") }
    var chCode by remember { mutableStateOf("FXN") }
    var chCategory by remember { mutableStateOf("News") }
    var chCountry by remember { mutableStateOf("United Kingdom") }
    var chLanguage by remember { mutableStateOf("English") }
    var chCurrentProg by remember { mutableStateOf("") }
    var chNextProg by remember { mutableStateOf("") }
    var chStreamUrl by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_panel_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Admin Header & Module Selector
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = FylvixGold,
                    modifier = Modifier.size(30.dp)
                )
                Column {
                    Text(
                        text = "Fylvix Global Admin & Studio Console",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FylvixTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage Movies, Series, Seasons, Episodes, Anime, 18+ Policy, Live TV, Providers & Analytics",
                        style = MaterialTheme.typography.bodySmall,
                        color = FylvixTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                adminModules.forEach { mod ->
                    FilterChip(
                        selected = activeModule == mod,
                        onClick = { activeModule = mod },
                        label = { Text(mod) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FylvixCrimson,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("admin_tab_${mod.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        // MODULE 1: DASHBOARD & ANALYTICS
        if (activeModule == "Dashboard") {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminKpiCard(
                            title = "Total Catalog",
                            value = "${catalog.size} Titles",
                            subtitle = "${catalog.count { it.isPublished }} Published • ${catalog.count { it.isAdult18Plus }} 18+",
                            color = FylvixCrimson,
                            modifier = Modifier.weight(1f)
                        )
                        AdminKpiCard(
                            title = "Live TV Channels",
                            value = "${liveChannels.size} Live",
                            subtitle = "Authorized 24/7 Streams",
                            color = FylvixCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminKpiCard(
                            title = "Page Views",
                            value = "%,d".format(analytics.pageViews),
                            subtitle = "%,d Searches".format(analytics.totalSearches),
                            color = FylvixGold,
                            modifier = Modifier.weight(1f)
                        )
                        AdminKpiCard(
                            title = "Play Starts",
                            value = "%,d".format(analytics.playStarts),
                            subtitle = "${analytics.avgCompletionPercent}% Avg Completion",
                            color = FylvixEmerald,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FylvixGlassBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Top Streamed Global Titles & Retention", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("30-Day Viewer Retention Rate: ${analytics.userRetentionRate}% • Privacy-Safe Aggregate Telemetry", style = MaterialTheme.typography.labelSmall, color = FylvixEmerald)
                            catalog.sortedByDescending { it.voteCount }.take(5).forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${item.title} (${item.country})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "★ ${item.rating} • ${item.voteCount} streams",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FylvixGold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // MODULE 2: ADMIN MOVIE / CONTENT FORM
        if (activeModule == "Add Movie / Content") {
            item {
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
                            text = "Publish New Movie, Series, Anime or 18+ Title",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text("Content Kind", style = MaterialTheme.typography.labelMedium, color = FylvixGold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ContentKind.entries.forEach { k ->
                                FilterChip(
                                    selected = formKind == k,
                                    onClick = {
                                        formKind = k
                                        formAdult = (k == ContentKind.ADULT_MOVIE || k == ContentKind.ADULT_SERIES)
                                    },
                                    label = { Text(k.displayName) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = formTitle,
                            onValueChange = { formTitle = it },
                            label = { Text("Title *") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_movie_title_input")
                        )

                        OutlinedTextField(
                            value = formOriginalTitle,
                            onValueChange = { formOriginalTitle = it },
                            label = { Text("Original Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = formOverview,
                            onValueChange = { formOverview = it },
                            label = { Text("Synopsis / Overview") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formYear,
                                onValueChange = { formYear = it },
                                label = { Text("Release Year") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formRuntime,
                                onValueChange = { formRuntime = it },
                                label = { Text("Runtime (min)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formRating,
                                onValueChange = { formRating = it },
                                label = { Text("Rating (1-10)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("Regional Hub", style = MaterialTheme.typography.labelMedium, color = FylvixGold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FylvixSeedCatalog.regionalHubs.forEach { hub ->
                                FilterChip(
                                    selected = formHub == hub,
                                    onClick = { formHub = hub },
                                    label = { Text(hub) }
                                )
                            }
                        }

                        Text("Primary Genre", style = MaterialTheme.typography.labelMedium, color = FylvixGold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FylvixSeedCatalog.allGenres.forEach { g ->
                                FilterChip(
                                    selected = formGenre == g,
                                    onClick = { formGenre = g },
                                    label = { Text(g) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formCountry,
                                onValueChange = { formCountry = it },
                                label = { Text("Country") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formLanguage,
                                onValueChange = { formLanguage = it },
                                label = { Text("Language") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formDirector,
                                onValueChange = { formDirector = it },
                                label = { Text("Director") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formWriter,
                                onValueChange = { formWriter = it },
                                label = { Text("Writer") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Feature on Hero Banner", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = formFeatured, onCheckedChange = { formFeatured = it })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mark as 18+ Adult Classification", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = formAdult, onCheckedChange = { formAdult = it })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Publish Immediately", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = formPublished, onCheckedChange = { formPublished = it })
                        }

                        Button(
                            onClick = {
                                onAddOrUpdateMedia(
                                    formTitle,
                                    formOriginalTitle,
                                    formOverview,
                                    formKind,
                                    formYear.toIntOrNull() ?: 2026,
                                    formRuntime.toIntOrNull() ?: 120,
                                    formCountry,
                                    formLanguage,
                                    formHub,
                                    listOf(formGenre),
                                    formRating.toDoubleOrNull() ?: 8.5,
                                    if (formAdult) "18+" else formCert,
                                    formDirector,
                                    formWriter,
                                    formQuality,
                                    formFeatured,
                                    formTrending,
                                    formAdult,
                                    formPublished
                                )
                                formTitle = ""
                                formOriginalTitle = ""
                                formOverview = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_publish_media_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Publish to Fylvix Catalog")
                        }
                    }
                }
            }
        }

        // MODULE 3: CATALOG & BANNERS PUBLISH/UNPUBLISH MANAGER
        if (activeModule == "Catalog & Banners") {
            items(catalog, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
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
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${item.kind.displayName} • ${item.country} (${item.language}) • ${item.certification}",
                                style = MaterialTheme.typography.labelSmall,
                                color = FylvixGold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = item.isFeatured,
                                onClick = { onToggleFeaturedMedia(item.id) },
                                label = { Text("Hero") }
                            )
                            FilterChip(
                                selected = item.isPublished,
                                onClick = { onTogglePublishMedia(item.id) },
                                label = { Text(if (item.isPublished) "Published" else "Draft") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FylvixEmerald.copy(alpha = 0.25f),
                                    selectedLabelColor = FylvixEmerald
                                )
                            )
                        }
                    }
                }
            }
        }

        // MODULE 4: SERIES & EPISODES HIERARCHY MANAGER
        if (activeModule == "Series & Episodes") {
            item {
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
                            text = "Add Season / Episode to Series or Anime",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Select Series / Anime", style = MaterialTheme.typography.labelMedium, color = FylvixGold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            seriesItems.forEach { show ->
                                FilterChip(
                                    selected = selectedShowId == show.id,
                                    onClick = { selectedShowId = show.id },
                                    label = { Text(show.title, maxLines = 1) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 4).forEach { sNum ->
                                FilterChip(
                                    selected = epSeasonNum == sNum,
                                    onClick = { epSeasonNum = sNum },
                                    label = { Text("Season $sNum") }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = epTitle,
                            onValueChange = { epTitle = it },
                            label = { Text("Episode Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = epDesc,
                            onValueChange = { epDesc = it },
                            label = { Text("Episode Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = epRuntime,
                            onValueChange = { epRuntime = it },
                            label = { Text("Runtime (Minutes)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                onAddEpisodeToShow(
                                    selectedShowId,
                                    epSeasonNum,
                                    epTitle,
                                    epDesc,
                                    epRuntime.toIntOrNull() ?: 48
                                )
                                epTitle = ""
                                epDesc = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publish Episode to Season $epSeasonNum")
                        }
                    }
                }
            }
        }

        // MODULE 5: 18+ CONTENT & POLICY CONTROLS
        if (activeModule == "18+ Content & Policy") {
            item {
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
                            text = "Backend 18+ Classification & Regional Policy",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Control 18+ availability, regional policy, search visibility, and homepage visibility from the backend without requiring a per-visit popup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FylvixTextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Backend 18+ Category Enabled", color = Color.White)
                            Switch(
                                checked = settings.backendAdultCategoryEnabled,
                                onCheckedChange = { v -> onUpdateSettings { it.copy(backendAdultCategoryEnabled = v) } }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("18+ Search Visibility", color = Color.White)
                            Switch(
                                checked = settings.adultAllowedInSearch,
                                onCheckedChange = { v -> onUpdateSettings { it.copy(adultAllowedInSearch = v) } }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("18+ Homepage Row Visibility", color = Color.White)
                            Switch(
                                checked = settings.adultAllowedOnHomepage,
                                onCheckedChange = { v -> onUpdateSettings { it.copy(adultAllowedOnHomepage = v) } }
                            )
                        }

                        Text("Active Regional Policy Scope", style = MaterialTheme.typography.labelMedium, color = FylvixGold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("GLOBAL", "NA-EU", "APAC", "MENA-RESTRICTED").forEach { reg ->
                                FilterChip(
                                    selected = settings.activeRegionCode == reg,
                                    onClick = {
                                        onUpdateSettings {
                                            it.copy(
                                                activeRegionCode = reg,
                                                backendAdultCategoryEnabled = reg != "MENA-RESTRICTED"
                                            )
                                        }
                                    },
                                    label = { Text(reg) }
                                )
                            }
                        }
                    }
                }
            }

            items(catalog.filter { it.isAdult18Plus }, key = { it.id }) { adultItem ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FylvixCrimson.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(adultItem.title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("18+ • ${adultItem.country} • ${adultItem.language}", style = MaterialTheme.typography.labelSmall, color = FylvixGold)
                        }
                        FilterChip(
                            selected = adultItem.isPublished,
                            onClick = { onTogglePublishMedia(adultItem.id) },
                            label = { Text(if (adultItem.isPublished) "Published" else "Unpublished") }
                        )
                    }
                }
            }
        }

        // MODULE 6: LIVE TV MANAGER
        if (activeModule == "Live TV Manager") {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FylvixGlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Add Authorized Live TV Channel", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = chName,
                                onValueChange = { chName = it },
                                label = { Text("Channel Name") },
                                singleLine = true,
                                modifier = Modifier.weight(2f)
                            )
                            OutlinedTextField(
                                value = chCode,
                                onValueChange = { chCode = it },
                                label = { Text("Code") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = chCategory,
                                onValueChange = { chCategory = it },
                                label = { Text("Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = chCountry,
                                onValueChange = { chCountry = it },
                                label = { Text("Country") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = chCurrentProg,
                            onValueChange = { chCurrentProg = it },
                            label = { Text("Current EPG Program") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = chNextProg,
                            onValueChange = { chNextProg = it },
                            label = { Text("Next EPG Program") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = chStreamUrl,
                            onValueChange = { chStreamUrl = it },
                            label = { Text("Authorized HLS / Stream URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onAddLiveChannel(chName, chCode, chCategory, chCountry, chLanguage, chCurrentProg, chNextProg, chStreamUrl)
                                chName = ""
                                chCurrentProg = ""
                                chNextProg = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LiveTv, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publish Authorized Live Channel")
                        }
                    }
                }
            }

            items(liveChannels, key = { it.id }) { ch ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ch.name, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${ch.category} • ${ch.country} • NOW: ${ch.currentProgram}", style = MaterialTheme.typography.labelSmall, color = FylvixGold)
                        }
                        FilterChip(
                            selected = ch.isPublished,
                            onClick = { onToggleLiveChannel(ch.id) },
                            label = { Text(if (ch.isPublished) "Live" else "Paused") }
                        )
                    }
                }
            }
        }

        // MODULE 7: API PROVIDERS & METADATA SYNCHRONIZATION ADAPTERS
        if (activeModule == "API Providers") {
            items(providers, key = { it.id }) { prov ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FylvixGlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prov.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(prov.adapterType, style = MaterialTheme.typography.labelSmall, color = FylvixCyan)
                            }
                            Surface(
                                color = FylvixEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "CONNECTED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FylvixEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text("Endpoint: ${prov.endpointUrl}", style = MaterialTheme.typography.bodySmall, color = FylvixTextSecondary)
                        Text(
                            text = "Normalized PostgreSQL Records: %,d • Last Sync: %s".format(prov.syncedRecordsCount, prov.lastSyncedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = FylvixGold
                        )
                        Button(
                            onClick = { onTriggerProviderSync(prov.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = FylvixSurfaceElevated),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = FylvixCyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Metadata Adapter Now", color = Color.White)
                        }
                    }
                }
            }
        }

        // MODULE 8: REPORTS & SYSTEM AUDIT LOGS
        if (activeModule == "Reports & Audit Logs") {
            item {
                Text("User Moderation Reports", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }
            items(reports, key = { it.id }) { rep ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${rep.contentTitle} • ${rep.reason}", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(rep.details, style = MaterialTheme.typography.bodySmall, color = FylvixTextSecondary)
                            Text("By ${rep.submittedBy} • ${rep.createdAt} • Status: ${rep.status}", style = MaterialTheme.typography.labelSmall, color = FylvixGold)
                        }
                        if (rep.status != "Resolved") {
                            OutlinedButton(onClick = { onResolveReport(rep.id) }) {
                                Text("Resolve", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Security & System Audit Logs", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }
            items(auditLogs, key = { it.id }) { log ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("[${log.severity}] ${log.actor}", style = MaterialTheme.typography.labelMedium, color = FylvixCyan, fontWeight = FontWeight.Bold)
                            Text(log.timestamp, style = MaterialTheme.typography.labelSmall, color = FylvixTextMuted)
                        }
                        Text(log.action, style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text("Target: ${log.target}", style = MaterialTheme.typography.labelSmall, color = FylvixGold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = FylvixTextSecondary)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = FylvixTextPrimary)
        }
    }
}
