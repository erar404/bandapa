package com.bandapa.feature.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bandapa.feature.announcements.domain.Announcement
import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.calendar.domain.Event
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricCyan
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.NeonGreen
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.OnSurfaceVariant
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier       = modifier,
        containerColor = Background,
    ) { padding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier   = Modifier.fillMaxWidth().padding(padding),
                color      = ElectricPurple,
                trackColor = SurfaceVariant,
            )
            return@Scaffold
        }

        var contentVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { contentVisible = true }

        AnimatedVisibility(
            visible = contentVisible,
            enter   = fadeIn(animationSpec = spring(stiffness = 200f)) +
                      slideInVertically(animationSpec = spring(stiffness = 200f)) { it / 6 },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 40.dp),
            ) {
                // ─── Greeting ─────────────────────────────────────────────────────
                val displayName = uiState.profile.name?.ifBlank { null }
                    ?: uiState.profile.email?.substringBefore("@")
                    ?: "there"

                Text(
                    text       = "${greeting()},",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = OnSurface,
                )
                Text(
                    text       = displayName,
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = ElectricPurple,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = todayLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                )

                // ─── Announcements ────────────────────────────────────────────────
                if (uiState.announcements.isNotEmpty()) {
                    Spacer(Modifier.height(32.dp))
                    SectionLabel(
                        icon  = Icons.Default.Campaign,
                        title = "Announcements",
                        tint  = ElectricCyan,
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.announcements.forEachIndexed { index, announcement ->
                            var visible by remember(announcement.id) { mutableStateOf(false) }
                            LaunchedEffect(announcement.id) {
                                delay(index.toLong() * 60L)
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter   = fadeIn() + slideInVertically { it / 4 },
                            ) {
                                AnnouncementCard(announcement)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ─── Today's Events ───────────────────────────────────────────────
                SectionLabel(
                    icon  = Icons.Default.CalendarToday,
                    title = "Today's Events",
                    tint  = ElectricPurple,
                )
                Spacer(Modifier.height(12.dp))

                if (uiState.todayEvents.isEmpty()) {
                    EmptyHint("Nothing scheduled for today.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.todayEvents.forEachIndexed { index, event ->
                            var visible by remember(event.id) { mutableStateOf(false) }
                            LaunchedEffect(event.id) {
                                delay(index.toLong() * 70L)
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter   = fadeIn() + slideInVertically { it / 4 },
                            ) {
                                TodayEventCard(event)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ─── My Bands ─────────────────────────────────────────────────────
                SectionLabel(
                    icon  = Icons.Default.MusicNote,
                    title = "My Bands",
                    tint  = ElectricCyan,
                )
                Spacer(Modifier.height(12.dp))

                if (uiState.myBands.isEmpty()) {
                    EmptyHint("You're not in any bands yet.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.myBands.forEachIndexed { index, band ->
                            var visible by remember(band.id) { mutableStateOf(false) }
                            LaunchedEffect(band.id) {
                                delay(index.toLong() * 60L)
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter   = fadeIn() + slideInVertically { it / 4 },
                            ) {
                                BandChip(band)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Section label ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: androidx.compose.ui.graphics.Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier         = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = OnSurface,
        )
    }
}

// ─── Today event card ─────────────────────────────────────────────────────────

@Composable
private fun TodayEventCard(event: Event) {
    val accent = if (event.bandId != null) ElectricCyan else NeonGreen
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = event.title,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = if (event.isAllDay) "All day"
                        else "${event.startTime.formatTime()} – ${event.endTime.formatTime()}",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
            )
        }
        event.location?.let { loc ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint               = OnSurface.copy(alpha = 0.3f),
                    modifier           = Modifier.size(11.dp),
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text     = loc,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = OnSurface.copy(alpha = 0.35f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(76.dp),
                )
            }
        }
    }
}

// ─── Announcement card ────────────────────────────────────────────────────────

@Composable
private fun AnnouncementCard(announcement: Announcement) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface),
    ) {
        if (!announcement.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model              = announcement.imageUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            )
        }
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (announcement.body.isBlank()) 22.dp else 44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ElectricCyan),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = announcement.title,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = OnSurface,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                )
                if (announcement.body.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = announcement.body,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = OnSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ─── Band chip ────────────────────────────────────────────────────────────────

@Composable
private fun BandChip(band: Band) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(ElectricCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = band.name.firstOrNull()?.uppercaseChar()?.toString() ?: "B",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = ElectricCyan,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = band.name,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            if (band.genres.isNotEmpty()) {
                Text(
                    text     = band.genres.take(3).joinToString(" · "),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ─── Empty hint ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = OnSurface.copy(alpha = 0.35f))
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun greeting(): String {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else      -> "Good night"
    }
}

private fun todayLabel(): String {
    val dt    = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dt.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val dow   = dt.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$dow, $month ${dt.dayOfMonth} ${dt.year}"
}

private fun String.formatTime(): String =
    if (length >= 16) substring(11, 16) else this
