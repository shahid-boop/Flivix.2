package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CastMember
import com.example.data.models.ContentKind
import com.example.data.models.EpisodeItem
import com.example.data.models.FilterState
import com.example.data.models.LiveTvChannel
import com.example.data.models.MainNavDestination
import com.example.data.models.MediaItem
import com.example.data.models.UserProfile
import com.example.data.models.WatchHistoryItem
import com.example.data.seed.FylvixSeedCatalog
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
import kotlinx.coroutines.delay

fun destinationIcon(dest: MainNavDestination): ImageVector = when (dest) {
    MainNavDestination.HOME -> Icons.Default.Home
    MainNavDestination.MOVIES -> Icons.Default.Movie
    MainNavDestination.TV_SHOWS -> Icons.Default.Tv
    MainNavDestination.SERIES -> Icons.Default.Tv
    MainNavDestination.ANIME -> Icons.Default.Star
    MainNavDestination.LIVE_TV -> Icons.Default.LiveTv
    MainNavDestination.ADULT -> Icons.Default.VerifiedUser
    MainNavDestination.GENRES -> Icons.Default.Explore
    MainNavDestination.COUNTRIES -> Icons.Default.Public
    MainNavDestination.LANGUAGES -> Icons.Default.Language
    MainNavDestination.SEARCH -> Icons.Default.Search
    MainNavDestination.MY_LIST -> Icons.Default.Favorite
    MainNavDestination.HISTORY -> Icons.Default.History
    MainNavDestination.PROFILE -> Icons.Default.Person
    MainNavDestination.SETTINGS -> Icons.Default.Settings
    MainNavDestination.ADMIN -> Icons.Default.AdminPanelSettings
}

@Composable
fun FylvixAppBar(
    currentDestination: MainNavDestination,
    activeProfile: UserProfile,
    unreadNotifications: Int,
    adultSectionEnabled: Boolean,
    isCompactScreen: Boolean,
    onOpenDrawer: () -> Unit,
    onSelectDestination: (MainNavDestination) -> Unit,
    onOpenNotificationsDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FylvixObsidian,
                        FylvixObsidian.copy(alpha = 0.94f)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCompactScreen) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("open_navigation_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Navigation Menu",
                            tint = FylvixTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Fylvix Brand Mark
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectDestination(MainNavDestination.HOME) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .testTag("brand_logo_home"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(FylvixCrimson, FylvixGold)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "F",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FYLVIX",
                            style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 2.sp),
                            color = FylvixTextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "GLOBAL STREAMING",
                            style = MaterialTheme.typography.labelSmall,
                            color = FylvixGold
                        )
                    }
                }
            }

            // Action Icons: Search, Notifications, Admin, Active Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onSelectDestination(MainNavDestination.SEARCH) },
                    modifier = Modifier.testTag("top_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Global Search",
                        tint = if (currentDestination == MainNavDestination.SEARCH) FylvixCrimson else FylvixTextPrimary
                    )
                }

                IconButton(
                    onClick = onOpenNotificationsDialog,
                    modifier = Modifier.testTag("top_notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifications > 0) {
                                Badge(containerColor = FylvixCrimson) {
                                    Text(unreadNotifications.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = FylvixTextPrimary
                        )
                    }
                }

                IconButton(
                    onClick = { onSelectDestination(MainNavDestination.ADMIN) },
                    modifier = Modifier.testTag("top_admin_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Dashboard",
                        tint = if (currentDestination == MainNavDestination.ADMIN) FylvixGold else FylvixTextSecondary
                    )
                }

                ProfileAvatar(
                    profile = activeProfile,
                    sizeDp = 34,
                    onClick = { onSelectDestination(MainNavDestination.PROFILE) }
                )
            }
        }

        // Scrollable Quick Navigation Strip for all 16 Main Destinations
        val navItems = MainNavDestination.entries.filter {
            if (it == MainNavDestination.ADULT) adultSectionEnabled else true
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            navItems.forEach { dest ->
                val selected = dest == currentDestination
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) FylvixCrimson else FylvixSurfaceElevated.copy(alpha = 0.75f),
                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, FylvixGlassBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectDestination(dest) }
                        .testTag("nav_pill_${dest.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = destinationIcon(dest),
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (selected) Color.White else FylvixTextSecondary
                        )
                        Text(
                            text = dest.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else FylvixTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FylvixSidebar(
    currentDestination: MainNavDestination,
    adultSectionEnabled: Boolean,
    onSelectDestination: (MainNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = MainNavDestination.entries.filter {
        if (it == MainNavDestination.ADULT) adultSectionEnabled else true
    }
    Column(
        modifier = modifier
            .width(230.dp)
            .fillMaxHeight()
            .background(FylvixSurfaceDark)
            .border(1.dp, FylvixGlassBorder.copy(alpha = 0.4f))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "DISCOVER",
            style = MaterialTheme.typography.labelSmall,
            color = FylvixTextMuted,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
        destinations.forEach { dest ->
            val isSelected = dest == currentDestination
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) FylvixCrimson.copy(alpha = 0.18f) else Color.Transparent
                    )
                    .clickable { onSelectDestination(dest) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("sidebar_item_${dest.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = destinationIcon(dest),
                    contentDescription = dest.label,
                    tint = if (isSelected) FylvixCrimson else FylvixTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = dest.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) FylvixTextPrimary else FylvixTextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FylvixBottomNavigation(
    currentDestination: MainNavDestination,
    onSelectDestination: (MainNavDestination) -> Unit
) {
    val bottomTabs = listOf(
        MainNavDestination.HOME,
        MainNavDestination.MOVIES,
        MainNavDestination.TV_SHOWS,
        MainNavDestination.ANIME,
        MainNavDestination.LIVE_TV
    )
    NavigationBar(
        containerColor = FylvixSurfaceDark,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_navigation_bar")
    ) {
        bottomTabs.forEach { dest ->
            val selected = currentDestination == dest
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectDestination(dest) },
                icon = {
                    Icon(
                        imageVector = destinationIcon(dest),
                        contentDescription = dest.label
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = FylvixCrimson,
                    indicatorColor = FylvixCrimson,
                    unselectedIconColor = FylvixTextSecondary,
                    unselectedTextColor = FylvixTextSecondary
                ),
                modifier = Modifier.testTag("bottom_nav_${dest.name.lowercase()}")
            )
        }
    }
}

@Composable
fun HeroBanner(
    featuredItems: List<MediaItem>,
    watchlistIds: Set<String>,
    onWatchNow: (MediaItem) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onOpenDetails: (MediaItem) -> Unit
) {
    if (featuredItems.isEmpty()) return
    var currentIndex by remember { mutableIntStateOf(0) }
    val safeIndex = currentIndex % featuredItems.size
    val item = featuredItems[safeIndex]

    LaunchedEffect(featuredItems.size) {
        while (featuredItems.size > 1) {
            delay(6500L)
            currentIndex = (currentIndex + 1) % featuredItems.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(410.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, FylvixGlassBorder, RoundedCornerShape(22.dp))
            .testTag("hero_banner_carousel")
    ) {
        Image(
            painter = painterResource(id = item.heroDrawableRes),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Cinematic Multi-Stop Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.55f),
                            FylvixObsidian.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Badges Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = FylvixCrimson,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "FYLVIX PREMIERE • ${item.regionalHub.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                QualityBadge(item.qualityBadge)
                ClassificationBadge(item.certification)
                RatingBadge(item.rating)
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (item.tagline.isNotBlank()) {
                Text(
                    text = "\"${item.tagline}\"",
                    style = MaterialTheme.typography.titleSmall,
                    color = FylvixGold
                )
            }

            Text(
                text = item.overview,
                style = MaterialTheme.typography.bodyMedium,
                color = FylvixTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${item.releaseYear} • ${item.country} • ${item.language} • ${item.genres.joinToString(" / ")}",
                style = MaterialTheme.typography.labelMedium,
                color = FylvixTextPrimary.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Hero Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WatchButton(
                    text = "Watch Now",
                    onClick = { onWatchNow(item) },
                    modifier = Modifier.testTag("hero_watch_now_button")
                )
                MyListButton(
                    isInList = watchlistIds.contains(item.id),
                    onClick = { onToggleWatchlist(item.id) },
                    modifier = Modifier.testTag("hero_my_list_button")
                )
                OutlinedButton(
                    onClick = { onOpenDetails(item) },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGlassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("hero_details_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Details",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Details", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Carousel Indicator Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                featuredItems.forEachIndexed { idx, _ ->
                    val active = idx == safeIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (active) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (active) FylvixCrimson else FylvixTextMuted.copy(alpha = 0.5f))
                            .clickable { currentIndex = idx }
                    )
                }
            }
        }
    }
}

@Composable
fun RatingBadge(rating: Double) {
    Surface(
        color = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGold.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = FylvixGold,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "%.1f".format(rating),
                style = MaterialTheme.typography.labelSmall,
                color = FylvixGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QualityBadge(quality: String) {
    Surface(
        color = FylvixCyan.copy(alpha = 0.16f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FylvixCyan.copy(alpha = 0.5f))
    ) {
        Text(
            text = quality,
            style = MaterialTheme.typography.labelSmall,
            color = FylvixCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ClassificationBadge(classification: String) {
    val isAdult = classification == "18+" || classification == "TV-MA" || classification == "R"
    val badgeColor = if (isAdult) FylvixCrimson else FylvixEmerald
    Surface(
        color = badgeColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.65f))
    ) {
        Text(
            text = classification,
            style = MaterialTheme.typography.labelSmall,
            color = if (isAdult) Color(0xFFFF8FA3) else FylvixEmerald,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun WatchButton(
    text: String = "Watch Now",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FylvixCrimson,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun MyListButton(
    isInList: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isInList) FylvixGold else FylvixGlassBorder
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isInList) FylvixGold else Color.White
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isInList) Icons.Default.Check else Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isInList) "In My List" else "My List",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(CircleShape)
            .background(FylvixSurfaceElevated)
            .border(1.dp, FylvixGlassBorder, CircleShape)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) FylvixCrimson else Color.White
        )
    }
}

@Composable
fun MovieCard(
    item: MediaItem,
    isInWatchlist: Boolean,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onToggleWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c1 = Color(item.gradientColors.getOrElse(0) { 0xFF1E1B4B })
    val c2 = Color(item.gradientColors.getOrElse(1) { 0xFFE50938 })

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
        modifier = modifier
            .width(168.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, FylvixGlassBorder.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("media_card_${item.id}")
    ) {
        Column {
            // Poster Art Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp)
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
                                    c1.copy(alpha = 0.35f),
                                    c2.copy(alpha = 0.45f),
                                    FylvixSurfaceCard
                                )
                            )
                        )
                )

                // Top Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBadge(item.rating)
                    ClassificationBadge(item.certification)
                }

                // Bottom Overlay Info inside Poster
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    QualityBadge(item.qualityBadge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Metadata & Quick Actions Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${item.releaseYear} • ${item.countryCode} • ${item.language}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FylvixTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.genres.take(2).joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = FylvixGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FylvixCrimson)
                            .clickable { onPlayClick() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Play",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Toggle My List",
                            tint = if (isInWatchlist) FylvixGold else FylvixTextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    historyItem: WatchHistoryItem,
    mediaItem: MediaItem,
    onResume: () -> Unit,
    onOpenDetails: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
        modifier = Modifier
            .width(265.dp)
            .border(1.dp, FylvixGlassBorder, RoundedCornerShape(16.dp))
            .clickable { onResume() }
            .testTag("continue_watching_${mediaItem.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = mediaItem.heroDrawableRes),
                    contentDescription = mediaItem.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FylvixCrimson),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove from Continue Watching",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                LinearProgressIndicator(
                    progress = { (historyItem.percentageWatched / 100f).coerceIn(0.05f, 1f) },
                    color = FylvixCrimson,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(5.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetails() }
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = mediaItem.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                val subtitleText = if (historyItem.seasonNumber != null && historyItem.episodeNumber != null) {
                    "S${historyItem.seasonNumber}:E${historyItem.episodeNumber} • ${historyItem.episodeTitle ?: ""}"
                } else {
                    "${historyItem.playbackPositionSec / 60}m / ${historyItem.durationSec / 60}m (${historyItem.percentageWatched.toInt()}%)"
                }
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.labelSmall,
                    color = FylvixGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoryRow(
    title: String,
    subtitle: String? = null,
    items: List<MediaItem>,
    watchlistIds: Set<String>,
    onItemClick: (MediaItem) -> Unit,
    onPlayClick: (MediaItem) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onSeeAllClick: (() -> Unit)? = null
) {
    if (items.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = FylvixTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = FylvixTextSecondary
                    )
                }
            }
            if (onSeeAllClick != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSeeAllClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore All",
                        style = MaterialTheme.typography.labelLarge,
                        color = FylvixCrimson
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = FylvixCrimson,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { "${title}_${it.id}" }) { media ->
                MovieCard(
                    item = media,
                    isInWatchlist = watchlistIds.contains(media.id),
                    onClick = { onItemClick(media) },
                    onPlayClick = { onPlayClick(media) },
                    onToggleWatchlist = { onToggleWatchlist(media.id) }
                )
            }
        }
    }
}

@Composable
fun EpisodeCard(
    episode: EpisodeItem,
    progressPercent: Float = 0f,
    onPlayEpisode: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FylvixGlassBorder, RoundedCornerShape(14.dp))
            .clickable { onPlayEpisode() }
            .testTag("episode_card_${episode.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Episode Thumbnail Box
            Box(
                modifier = Modifier
                    .width(118.dp)
                    .height(74.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(episode.accentColorHex).copy(alpha = 0.7f),
                                FylvixObsidian
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Episode",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(4.dp)
                )
                Text(
                    text = "EP ${episode.episodeNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                )
                if (progressPercent > 0f) {
                    LinearProgressIndicator(
                        progress = { (progressPercent / 100f).coerceIn(0.05f, 1f) },
                        color = FylvixCrimson,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${episode.episodeNumber}. ${episode.title}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    RatingBadge(episode.rating)
                }
                Text(
                    text = episode.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = FylvixTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${episode.runtimeMinutes} min • Aired ${episode.airDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FylvixGold
                )
            }
        }
    }
}

@Composable
fun ChannelCard(
    channel: LiveTvChannel,
    onWatchLive: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FylvixSurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FylvixGlassBorder, RoundedCornerShape(16.dp))
            .clickable { onWatchLive() }
            .testTag("live_channel_card_${channel.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(channel.badgeColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = channel.code,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column {
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${channel.category} • ${channel.country} (${channel.language})",
                            style = MaterialTheme.typography.labelSmall,
                            color = FylvixTextSecondary
                        )
                    }
                }

                // Live Indicator Pill
                Surface(
                    color = FylvixCrimson,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // EPG Current & Next Program
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FylvixSurfaceElevated)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NOW: ${channel.currentProgram}",
                        style = MaterialTheme.typography.labelLarge,
                        color = FylvixGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = channel.viewersCount,
                        style = MaterialTheme.typography.labelSmall,
                        color = FylvixCyan
                    )
                }
                Text(
                    text = "NEXT: ${channel.nextProgram}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FylvixTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            WatchButton(
                text = "Watch Live Stream",
                onClick = onWatchLive,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PersonCard(
    person: CastMember,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = FylvixSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGlassBorder),
        modifier = Modifier
            .width(136.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(person.avatarColorHex).copy(alpha = 0.25f))
                    .border(2.dp, Color(person.avatarColorHex), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = person.name,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = person.characterName,
                style = MaterialTheme.typography.labelSmall,
                color = FylvixGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProfileAvatar(
    profile: UserProfile,
    sizeDp: Int = 40,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Color(profile.avatarColorHex).copy(alpha = 0.25f))
            .border(2.dp, Color(profile.avatarColorHex), CircleShape)
            .clickable { onClick() }
            .testTag("profile_avatar_${profile.id}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = profile.avatarEmoji,
            fontSize = (sizeDp * 0.45f).sp
        )
    }
}

@Composable
fun FilterPanel(
    filterState: FilterState,
    categoryTabs: List<String>,
    onUpdateFilter: ((FilterState) -> FilterState) -> Unit,
    onResetFilters: () -> Unit
) {
    var expandedFilters by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Primary Category Sub-Tabs + Filter & Sort Triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryTabs.forEach { tab ->
                    FilterChip(
                        selected = filterState.categoryTab == tab,
                        onClick = { onUpdateFilter { it.copy(categoryTab = tab) } },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FylvixCrimson,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { expandedFilters = !expandedFilters },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("toggle_filters_button")
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filters", style = MaterialTheme.typography.labelMedium)
                }

                Box {
                    OutlinedButton(
                        onClick = { showSortDropdown = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("sort_menu_button")
                    ) {
                        Text("Sort: ${filterState.sortBy}", style = MaterialTheme.typography.labelMedium)
                    }
                    DropdownMenu(
                        expanded = showSortDropdown,
                        onDismissRequest = { showSortDropdown = false }
                    ) {
                        listOf("Popularity", "Rating", "Latest", "Title A-Z", "Runtime").forEach { sortOpt ->
                            DropdownMenuItem(
                                text = { Text(sortOpt) },
                                onClick = {
                                    onUpdateFilter { it.copy(sortBy = sortOpt) }
                                    showSortDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Expandable Multi-Facet Filter Bar (Genre, Country, Language, Year, Quality, Certification)
        AnimatedVisibility(
            visible = expandedFilters,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FylvixSurfaceElevated)
                    .border(1.dp, FylvixGlassBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipStrip(
                    label = "Genre",
                    options = listOf("All") + FylvixSeedCatalog.allGenres,
                    selected = filterState.genre,
                    onSelect = { g -> onUpdateFilter { it.copy(genre = g) } }
                )
                FilterChipStrip(
                    label = "Country",
                    options = listOf("All") + FylvixSeedCatalog.allCountries,
                    selected = filterState.country,
                    onSelect = { c -> onUpdateFilter { it.copy(country = c) } }
                )
                FilterChipStrip(
                    label = "Language",
                    options = listOf("All") + FylvixSeedCatalog.allLanguages,
                    selected = filterState.language,
                    onSelect = { l -> onUpdateFilter { it.copy(language = l) } }
                )
                FilterChipStrip(
                    label = "Year",
                    options = listOf("All", "2027", "2026", "2025"),
                    selected = filterState.year,
                    onSelect = { y -> onUpdateFilter { it.copy(year = y) } }
                )
                FilterChipStrip(
                    label = "Certification",
                    options = listOf("All", "G", "PG", "PG-13", "TV-14", "R", "TV-MA", "18+"),
                    selected = filterState.certification,
                    onSelect = { cert -> onUpdateFilter { it.copy(certification = cert) } }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onResetFilters,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset All Filters", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipStrip(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            color = FylvixGold,
            modifier = Modifier.padding(end = 4.dp)
        )
        options.forEach { opt ->
            val isSelected = opt == selected
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) FylvixCrimson else FylvixSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, FylvixGlassBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(opt) }
            ) {
                Text(
                    text = opt,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else FylvixTextSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    actionLabel: String = "Reset Filters",
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Movie,
            contentDescription = null,
            tint = FylvixTextMuted,
            modifier = Modifier.size(54.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = FylvixTextPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = FylvixTextSecondary
        )
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = FylvixCrimson)
        ) {
            Text(actionLabel)
        }
    }
}
