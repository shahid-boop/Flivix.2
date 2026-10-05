package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.models.EpisodeItem
import com.example.data.models.MediaItem
import com.example.data.models.WatchHistoryItem
import com.example.ui.components.CategoryRow
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.EpisodeCard
import com.example.ui.components.FavoriteButton
import com.example.ui.components.MyListButton
import com.example.ui.components.PersonCard
import com.example.ui.components.QualityBadge
import com.example.ui.components.RatingBadge
import com.example.ui.components.WatchButton
import com.example.ui.theme.FylvixCrimson
import com.example.ui.theme.FylvixCyan
import com.example.ui.theme.FylvixEmerald
import com.example.ui.theme.FylvixGlassBorder
import com.example.ui.theme.FylvixGold
import com.example.ui.theme.FylvixObsidian
import com.example.ui.theme.FylvixSurfaceCard
import com.example.ui.theme.FylvixSurfaceDark
import com.example.ui.theme.FylvixSurfaceElevated
import com.example.ui.theme.FylvixTextMuted
import com.example.ui.theme.FylvixTextPrimary
import com.example.ui.theme.FylvixTextSecondary
import com.example.ui.viewmodel.ActivePlayerState
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaDetailsScreen(
    item: MediaItem,
    catalog: List<MediaItem>,
    selectedSeasonNumber: Int,
    isInWatchlist: Boolean,
    isFavorite: Boolean,
    userRating: Double?,
    watchHistory: List<WatchHistoryItem>,
    watchlistIds: Set<String>,
    onBack: () -> Unit,
    onSelectSeason: (Int) -> Unit,
    onWatchNow: (MediaItem, EpisodeItem?, Long?) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRateContent: (Double) -> Unit,
    onSubmitReport: (String, String) -> Unit,
    onOpenOtherMedia: (MediaItem) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("Subtitle or Audio Sync") }
    var reportDetails by remember { mutableStateOf("") }

    val similarItems = remember(item, catalog) {
        catalog.filter {
            it.id != item.id &&
                !it.isAdult18Plus &&
                (it.genres.any { g -> g in item.genres } || it.country == item.country)
        }.take(10)
    }

    val recommendedItems = remember(item, catalog) {
        catalog.filter { it.id != item.id && !it.isAdult18Plus }
            .sortedByDescending { it.rating }
            .take(10)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FylvixObsidian)
            .testTag("media_details_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Full Backdrop & Poster Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp)
            ) {
                Image(
                    painter = painterResource(id = item.heroDrawableRes),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.65f),
                                    FylvixObsidian
                                )
                            )
                        )
                )

                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(14.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, FylvixGlassBorder, CircleShape)
                        .testTag("details_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Poster + Primary Title Metadata
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Poster Thumbnail
                    Box(
                        modifier = Modifier
                            .width(112.dp)
                            .height(164.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, FylvixGlassBorder, RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            painter = painterResource(id = item.heroDrawableRes),
                            contentDescription = "${item.title} Poster",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QualityBadge(item.qualityBadge)
                            ClassificationBadge(item.certification)
                            RatingBadge(item.rating)
                        }
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Original: ${item.originalTitle} • ${item.voteCount} votes",
                            style = MaterialTheme.typography.bodySmall,
                            color = FylvixGold
                        )
                        if (item.alternativeTitles.isNotEmpty()) {
                            Text(
                                text = "Also known as: ${item.alternativeTitles.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = FylvixTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${item.releaseDate} • ${item.runtimeMinutes} min • ${item.status} • ${item.country} (${item.language})",
                            style = MaterialTheme.typography.labelMedium,
                            color = FylvixTextPrimary
                        )
                    }
                }
            }
        }

        // 2. Primary Action Bar (Watch Now, Trailer, My List, Favorite, Share, Report)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WatchButton(
                        text = if (item.seasons.isNotEmpty()) "Watch S1:E1" else "Watch Movie",
                        onClick = { onWatchNow(item, null, null) },
                        modifier = Modifier.testTag("details_watch_now_button")
                    )

                    OutlinedButton(
                        onClick = { onWatchNow(item, null, 0L) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FylvixCyan)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = FylvixCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch Trailer", color = FylvixCyan, style = MaterialTheme.typography.labelLarge)
                    }

                    MyListButton(
                        isInList = isInWatchlist,
                        onClick = { onToggleWatchlist(item.id) },
                        modifier = Modifier.testTag("details_my_list_button")
                    )

                    FavoriteButton(
                        isFavorite = isFavorite,
                        onClick = { onToggleFavorite(item.id) },
                        modifier = Modifier.testTag("details_favorite_button")
                    )

                    IconButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Watch ${item.title} (${item.releaseYear}) on Fylvix Global Streaming: https://fylvix.global/watch/${item.id}"
                                )
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share ${item.title}"))
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FylvixSurfaceElevated)
                            .border(1.dp, FylvixGlassBorder, CircleShape)
                            .testTag("details_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }

                    IconButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FylvixSurfaceElevated)
                            .border(1.dp, FylvixGlassBorder, CircleShape)
                            .testTag("details_report_button")
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = "Report Content", tint = FylvixTextSecondary)
                    }
                }

                // Tagline & Synopsis
                if (item.tagline.isNotBlank()) {
                    Text(
                        text = "\"${item.tagline}\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = FylvixGold
                    )
                }
                Text(
                    text = item.overview,
                    style = MaterialTheme.typography.bodyLarge,
                    color = FylvixTextPrimary
                )

                // Genre Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.genres.forEach { genre ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = FylvixSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGlassBorder)
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelMedium,
                                color = FylvixTextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. TV Show / Series / Anime Season & Episode Selector Hierarchy
        if (item.seasons.isNotEmpty()) {
            val activeSeason = item.seasons.find { it.seasonNumber == selectedSeasonNumber } ?: item.seasons.first()
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Seasons & Episodes (${item.seasons.size} Seasons)",
                        style = MaterialTheme.typography.titleLarge,
                        color = FylvixTextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item.seasons.forEach { season ->
                            val selected = season.seasonNumber == activeSeason.seasonNumber
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectSeason(season.seasonNumber) },
                                label = { Text("Season ${season.seasonNumber} (${season.episodeCount} Eps)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FylvixCrimson,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("season_chip_${season.seasonNumber}")
                            )
                        }
                    }

                    Text(
                        text = "${activeSeason.title} • Aired ${activeSeason.airDate}",
                        style = MaterialTheme.typography.titleSmall,
                        color = FylvixGold
                    )
                    Text(
                        text = activeSeason.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = FylvixTextSecondary
                    )

                    activeSeason.episodes.forEach { ep ->
                        val epProgress = watchHistory.find { it.episodeId == ep.id }?.percentageWatched ?: 0f
                        EpisodeCard(
                            episode = ep,
                            progressPercent = epProgress,
                            onPlayEpisode = { onWatchNow(item, ep, null) }
                        )
                    }
                }
            }
        }

        // 4. Anime Characters & Voice Cast (When Anime)
        if (item.characters.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Anime Characters & Voice Cast (Sub / Dub)",
                        style = MaterialTheme.typography.titleLarge,
                        color = FylvixTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    item.characters.forEach { ch ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FylvixGlassBorder, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(ch.name, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(ch.role, style = MaterialTheme.typography.labelSmall, color = FylvixGold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("JP VA: ${ch.japaneseVoiceActor}", style = MaterialTheme.typography.labelSmall, color = FylvixCyan)
                                    Text("EN VA: ${ch.englishVoiceActor}", style = MaterialTheme.typography.labelSmall, color = FylvixTextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Cast, Director, Writers, Crew & Production Metadata
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Cast & Crew",
                    style = MaterialTheme.typography.titleLarge,
                    color = FylvixTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(item.cast, key = { it.id }) { person ->
                        PersonCard(person = person)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, FylvixGlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetadataSpecRow("Director", item.director)
                        MetadataSpecRow("Writers", item.writers.joinToString(", "))
                        MetadataSpecRow("Production", item.productionCompanies.joinToString(" • "))
                        MetadataSpecRow("Classification", "${item.certification} (${item.classification}) • ${item.regionalHub}")
                        MetadataSpecRow("Video Qualities", item.videoQualities.joinToString(", ") { it.quality })
                        MetadataSpecRow("Audio Tracks", item.audioTracks.joinToString(" | ") { it.label })
                        MetadataSpecRow("Subtitles", item.subtitles.joinToString(", ") { it.label })
                    }
                }

                // Interactive User Rating Bar
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Rate This Title",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (userRating != null) "Your rating: ★ $userRating / 10" else "Tap a score to personalize your recommendations",
                                style = MaterialTheme.typography.labelSmall,
                                color = FylvixGold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(7.0, 8.0, 9.0, 10.0).forEach { score ->
                                val active = userRating == score
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (active) FylvixGold else FylvixSurfaceCard,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onRateContent(score) }
                                ) {
                                    Text(
                                        text = "★ ${score.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Similar & Recommended Titles
        item {
            CategoryRow(
                title = "Similar Titles",
                items = similarItems,
                watchlistIds = watchlistIds,
                onItemClick = onOpenOtherMedia,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }
        item {
            CategoryRow(
                title = "Recommended Movies & Series",
                items = recommendedItems,
                watchlistIds = watchlistIds,
                onItemClick = onOpenOtherMedia,
                onPlayClick = { onWatchNow(it, null, null) },
                onToggleWatchlist = onToggleWatchlist
            )
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Issue • ${item.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select reason for moderation or technical review:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Subtitle or Audio Sync", "Playback Buffering", "Classification Check", "Metadata Correction").forEach { r ->
                            FilterChip(
                                selected = reportReason == r,
                                onClick = { reportReason = r },
                                label = { Text(r, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        label = { Text("Additional details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitReport(reportReason, reportDetails)
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MetadataSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            color = FylvixGold,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = FylvixTextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StreamingPlayerOverlay(
    playerState: ActivePlayerState,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onSkipIntro: () -> Unit,
    onSetVolume: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onTogglePip: () -> Unit,
    onSelectQuality: (String) -> Unit,
    onSelectSubtitle: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onPlayNextEpisode: () -> Unit,
    onPlayPrevEpisode: () -> Unit,
    onSelectSpecificEpisode: (EpisodeItem) -> Unit,
    onRetryNetwork: () -> Unit,
    onSimulateDrop: () -> Unit,
    onClosePlayer: () -> Unit
) {
    if (!playerState.isPipMode) {
        BackHandler { onClosePlayer() }
    }

    var showQualityMenu by remember { mutableStateOf(false) }
    var showSubtitleMenu by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showEpisodeDrawer by remember { mutableStateOf(false) }

    val titleText = playerState.liveChannel?.name ?: playerState.mediaItem?.title ?: "Fylvix Stream"
    val subtitleText = when {
        playerState.liveChannel != null -> "LIVE • ${playerState.liveChannel.currentProgram}"
        playerState.episode != null -> "S${playerState.episode.seasonNumber}:E${playerState.episode.episodeNumber} • ${playerState.episode.title}"
        playerState.mediaItem != null -> "${playerState.mediaItem.releaseYear} • ${playerState.mediaItem.qualityBadge} • Authorized Stream"
        else -> ""
    }

    // Picture-in-Picture Floating Mini Player Mode
    if (playerState.isPipMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FylvixSurfaceDark),
                modifier = Modifier
                    .width(270.dp)
                    .border(2.dp, FylvixCrimson, RoundedCornerShape(16.dp))
                    .testTag("pip_mini_player")
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(onClick = onTogglePip, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Fullscreen, contentDescription = "Expand", tint = FylvixCyan, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = onClosePlayer, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.labelSmall,
                        color = FylvixGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatDuration(playerState.currentPositionSec)} / ${formatDuration(playerState.durationSec)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FylvixTextSecondary
                        )
                        IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = FylvixCrimson
                            )
                        }
                    }
                }
            }
        }
        return
    }

    // Full Interactive Streaming Player Screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("streaming_video_player")
    ) {
        // Top Player Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FylvixObsidian.copy(alpha = 0.92f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onClosePlayer,
                    modifier = Modifier.testTag("player_close_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Player", tint = Color.White)
                }
                Column {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.labelSmall,
                        color = FylvixGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = FylvixEmerald.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FylvixEmerald)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = FylvixEmerald, modifier = Modifier.size(12.dp))
                        Text("Signed HLS", style = MaterialTheme.typography.labelSmall, color = FylvixEmerald)
                    }
                }

                IconButton(onClick = onTogglePip, modifier = Modifier.testTag("player_pip_button")) {
                    Icon(Icons.Default.PictureInPictureAlt, contentDescription = "Picture in Picture", tint = Color.White)
                }

                IconButton(onClick = onToggleFullscreen, modifier = Modifier.testTag("player_fullscreen_button")) {
                    Icon(
                        imageVector = if (playerState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = Color.White
                    )
                }
            }
        }

        // Main Video Viewport with Dynamic Cinema Frame & Live Captions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF04060A)),
            contentAlignment = Alignment.Center
        ) {
            val heroRes = playerState.mediaItem?.heroDrawableRes ?: com.example.R.drawable.img_hero_cyber_odyssey
            Image(
                painter = painterResource(id = heroRes),
                contentDescription = titleText,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.52f
            )

            // Animated Cinema Telemetry / Waveform Canvas Layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val phase = (playerState.currentPositionSec % 60).toFloat()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
                for (i in 0..24) {
                    val x = (w / 25f) * i
                    val waveH = (sin(phase + i * 0.6f) * 28f + 36f)
                    drawLine(
                        color = Color(0xFFE50938).copy(alpha = 0.28f),
                        start = Offset(x, h / 2f - waveH),
                        end = Offset(x, h / 2f + waveH),
                        strokeWidth = 4f
                    )
                }
            }

            // Network Error Recovery State
            if (playerState.hasNetworkError) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FylvixSurfaceDark.copy(alpha = 0.95f)),
                    modifier = Modifier
                        .padding(24.dp)
                        .border(1.dp, FylvixCrimson, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = FylvixCrimson, modifier = Modifier.size(36.dp))
                        Text("Stream Network Interruption Detected", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            "Switching to backup Fylvix Edge CDN node (${playerState.signedSession.cdnNode})",
                            style = MaterialTheme.typography.bodySmall,
                            color = FylvixTextSecondary
                        )
                        Button(
                            onClick = onRetryNetwork,
                            colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson),
                            modifier = Modifier.testTag("player_reconnect_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reconnect Authorized Stream")
                        }
                    }
                }
            } else if (playerState.isBuffering) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = FylvixCrimson)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Adapting bitrate to ${playerState.selectedQuality}…", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            } else {
                // Center Transport Controls (-10s, Play/Pause, +10s)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onSeekRelative(-10L) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .testTag("player_rewind_10s")
                    ) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = Color.White)
                    }

                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(FylvixCrimson)
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play or Pause",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSeekRelative(10L) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .testTag("player_forward_10s")
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = "Forward 10s", tint = Color.White)
                    }
                }
            }

            // Skip Intro Floating Button
            if (playerState.liveChannel == null && playerState.currentPositionSec < 120L) {
                Button(
                    onClick = onSkipIntro,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FylvixSurfaceElevated.copy(alpha = 0.9f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .border(1.dp, FylvixGold, RoundedCornerShape(10.dp))
                        .testTag("player_skip_intro_button")
                ) {
                    Text("Skip Intro", color = FylvixGold, fontWeight = FontWeight.Bold)
                }
            }

            // Live Subtitle Caption Overlay
            if (playerState.selectedSubtitle != "Off") {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        text = "[${playerState.selectedSubtitle}] \"Signal lock confirmed on orbital vector—initiating Fylvix protocol.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Controls & Stream Settings Deck
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(FylvixSurfaceDark)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Seek Slider & Timestamps
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (playerState.liveChannel != null) "LIVE" else formatDuration(playerState.currentPositionSec),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (playerState.liveChannel != null) FylvixCrimson else Color.White,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = playerState.currentPositionSec.toFloat(),
                    onValueChange = { onSeekTo(it.toLong()) },
                    valueRange = 0f..playerState.durationSec.coerceAtLeast(1L).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = FylvixCrimson,
                        activeTrackColor = FylvixCrimson,
                        inactiveTrackColor = FylvixGlassBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("player_seek_slider")
                )
                Text(
                    text = formatDuration(playerState.durationSec),
                    style = MaterialTheme.typography.labelMedium,
                    color = FylvixTextSecondary
                )
            }

            // Multi-Control Bar: Volume, Quality, Subtitles, Audio, Speed, Prev/Next Episode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute + Volume
                IconButton(onClick = onToggleMute, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = if (playerState.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Mute",
                        tint = Color.White
                    )
                }
                Slider(
                    value = if (playerState.isMuted) 0f else playerState.volume,
                    onValueChange = onSetVolume,
                    modifier = Modifier.width(88.dp)
                )

                // Quality Selector
                Box {
                    OutlinedButton(
                        onClick = { showQualityMenu = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("player_quality_button")
                    ) {
                        Icon(Icons.Default.HighQuality, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(playerState.selectedQuality, style = MaterialTheme.typography.labelSmall)
                    }
                    DropdownMenu(expanded = showQualityMenu, onDismissRequest = { showQualityMenu = false }) {
                        listOf("Auto (Adaptive HLS)", "4K UHD", "1080p FHD", "720p HD", "480p Data Saver").forEach { q ->
                            DropdownMenuItem(
                                text = { Text(q) },
                                onClick = {
                                    onSelectQuality(q)
                                    showQualityMenu = false
                                }
                            )
                        }
                    }
                }

                // Subtitle Selector
                Box {
                    OutlinedButton(
                        onClick = { showSubtitleMenu = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("player_subtitle_button")
                    ) {
                        Icon(Icons.Default.ClosedCaption, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CC: ${playerState.selectedSubtitle}", style = MaterialTheme.typography.labelSmall)
                    }
                    DropdownMenu(expanded = showSubtitleMenu, onDismissRequest = { showSubtitleMenu = false }) {
                        val subs = listOf("Off", "English [CC]", "Urdu", "Hindi", "Turkish", "Korean", "Japanese", "Arabic", "Spanish", "French")
                        subs.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    onSelectSubtitle(sub)
                                    showSubtitleMenu = false
                                }
                            )
                        }
                    }
                }

                // Audio Language Selector
                Box {
                    OutlinedButton(
                        onClick = { showAudioMenu = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("player_audio_button")
                    ) {
                        Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Audio: ${playerState.selectedAudioTrack.take(16)}", style = MaterialTheme.typography.labelSmall)
                    }
                    DropdownMenu(expanded = showAudioMenu, onDismissRequest = { showAudioMenu = false }) {
                        val tracks = playerState.mediaItem?.audioTracks?.map { it.label }
                            ?: listOf("Original Audio • Dolby Atmos", "English Dub • 5.1", "Urdu / Hindi • 5.1")
                        tracks.forEach { tr ->
                            DropdownMenuItem(
                                text = { Text(tr) },
                                onClick = {
                                    onSelectAudio(tr)
                                    showAudioMenu = false
                                }
                            )
                        }
                    }
                }

                // Playback Speed Selector
                Box {
                    OutlinedButton(
                        onClick = { showSpeedMenu = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("player_speed_button")
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${playerState.playbackSpeed}x", style = MaterialTheme.typography.labelSmall)
                    }
                    DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                            DropdownMenuItem(
                                text = { Text("${spd}x Speed") },
                                onClick = {
                                    onSelectSpeed(spd)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }

                // Episode Navigation (Previous, Next, Episode Selector)
                if (playerState.mediaItem != null && playerState.mediaItem.seasons.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onPlayPrevEpisode,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Prev Ep", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onPlayNextEpisode,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("player_next_episode_button")
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Next Ep", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { showEpisodeDrawer = !showEpisodeDrawer },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Episodes", style = MaterialTheme.typography.labelSmall, color = FylvixGold)
                    }
                }

                OutlinedButton(
                    onClick = onSimulateDrop,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Test Failover", style = MaterialTheme.typography.labelSmall, color = FylvixTextMuted)
                }
            }

            // Expandable Episode Selector inside Player
            AnimatedVisibility(visible = showEpisodeDrawer && playerState.mediaItem != null) {
                val allEps = playerState.mediaItem?.seasons?.flatMap { it.episodes }.orEmpty()
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    items(allEps, key = { it.id }) { ep ->
                        val isCurrent = playerState.episode?.id == ep.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) FylvixCrimson else FylvixSurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGlassBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectSpecificEpisode(ep) }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = "S${ep.seasonNumber}:E${ep.episodeNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FylvixGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ep.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Authorized CDN Token Footer
            Text(
                text = "Token: ${playerState.signedSession.signedToken} • Node: ${playerState.signedSession.cdnNode} • Format: ${playerState.signedSession.streamFormat}",
                style = MaterialTheme.typography.labelSmall,
                color = FylvixTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val hrs = totalSeconds / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return if (hrs > 0) {
        "%d:%02d:%02d".format(hrs, mins, secs)
    } else {
        "%02d:%02d".format(mins, secs)
    }
}
