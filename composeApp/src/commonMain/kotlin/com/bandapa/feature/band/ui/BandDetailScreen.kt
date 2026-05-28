package com.bandapa.feature.band.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.band.domain.BandDetailUiState
import com.bandapa.feature.band.domain.BandMemberWithProfile
import com.bandapa.feature.calendar.domain.Event
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricCyan
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.NeonGreen
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandDetailScreen(
    bandId: String,
    onNavigateBack: () -> Unit,
    viewModel: BandDetailViewModel = koinViewModel(parameters = { parametersOf(bandId) }),
) {
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }
    @Suppress("DEPRECATION")
    val clipboard          = LocalClipboardManager.current
    var showEditSheet      by remember { mutableStateOf(false) }
    val editSheetState     = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState) {
        if (uiState is BandDetailUiState.Error) {
            snackbarHostState.showSnackbar((uiState as BandDetailUiState.Error).message)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            val title = (uiState as? BandDetailUiState.Loaded)?.band?.name ?: ""
            val isOwner = (uiState as? BandDetailUiState.Loaded)?.isOwner == true
            TopAppBar(
                title = {
                    Text(
                        title,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = OnSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                actions = {
                    if (isOwner) {
                        IconButton(onClick = { showEditSheet = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit band", tint = ElectricPurple)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background),
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is BandDetailUiState.Loading -> LinearProgressIndicator(
                modifier   = Modifier.fillMaxWidth().padding(padding),
                color      = ElectricPurple,
                trackColor = SurfaceVariant,
            )

            is BandDetailUiState.Loaded -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(Modifier.height(8.dp))
                    BandInfoSection(state.band)
                    Spacer(Modifier.height(16.dp))
                    InviteCodeSection(inviteCode = state.band.inviteCode, onCopy = {
                        @Suppress("DEPRECATION")
                        clipboard.setText(AnnotatedString(state.band.inviteCode))
                    })
                    Spacer(Modifier.height(16.dp))
                    MembersSection(
                        members   = state.members,
                        isOwner   = state.isOwner,
                        ownerId   = state.band.ownerId,
                        onRemove  = viewModel::removeMember,
                    )
                    if (state.upcomingEvents.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        UpcomingEventsSection(state.upcomingEvents)
                    }
                    Spacer(Modifier.height(32.dp))
                }

                if (showEditSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showEditSheet = false },
                        sheetState       = editSheetState,
                        containerColor   = Surface,
                    ) {
                        EditBandSheet(
                            band      = state.band,
                            onSave    = { name, desc, genres, date, label, spotify ->
                                viewModel.updateBand(name, desc, genres, date, label, spotify)
                                showEditSheet = false
                            },
                            onDismiss = { showEditSheet = false },
                        )
                    }
                }
            }

            is BandDetailUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(state.message, color = OnSurface.copy(alpha = 0.5f))
            }

            else -> Unit
        }
    }
}

// ─── Band info ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BandInfoSection(band: Band) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(16.dp),
    ) {
        if (band.genres.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                band.genres.forEach { genre ->
                    AssistChip(
                        onClick = {},
                        label   = { Text(genre, style = MaterialTheme.typography.labelSmall) },
                        colors  = AssistChipDefaults.assistChipColors(
                            containerColor = ElectricPurple.copy(alpha = 0.15f),
                            labelColor     = ElectricPurple,
                        ),
                        border  = null,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (!band.description.isNullOrBlank()) {
            Text(band.description, style = MaterialTheme.typography.bodyMedium, color = OnSurface.copy(alpha = 0.8f))
            Spacer(Modifier.height(8.dp))
        }
        if (!band.label.isNullOrBlank()) {
            InfoRow("Label", band.label)
        }
        if (!band.dateFormed.isNullOrBlank()) {
            InfoRow("Since", band.dateFormed)
        }
        if (!band.spotifyUrl.isNullOrBlank()) {
            InfoRow("Spotify", band.spotifyUrl)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, color = OnSurface.copy(alpha = 0.5f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ─── Invite code ─────────────────────────────────────────────────────────────

@Composable
private fun InviteCodeSection(inviteCode: String, onCopy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(16.dp),
    ) {
        Text("Invite Code", style = MaterialTheme.typography.labelMedium, color = OnSurface.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                inviteCode,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = NeonGreen,
                modifier   = Modifier.weight(1f),
            )
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy invite code", tint = OnSurface.copy(alpha = 0.5f))
            }
        }
        Text(
            "Share this code with musicians to invite them.",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurface.copy(alpha = 0.4f),
        )
    }
}

// ─── Members ──────────────────────────────────────────────────────────────────

@Composable
private fun MembersSection(
    members: List<BandMemberWithProfile>,
    isOwner: Boolean,
    ownerId: String,
    onRemove: (memberId: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(16.dp),
    ) {
        Text(
            "Members (${members.size})",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = OnSurface.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(12.dp))
        members.forEachIndexed { i, mp ->
            if (i > 0) HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            MemberRow(
                mp       = mp,
                isOwner  = isOwner,
                isSelf   = mp.member.userId == ownerId,
                canRemove = isOwner && mp.member.role != "admin",
                onRemove = { onRemove(mp.member.id) },
            )
        }
    }
}

@Composable
private fun MemberRow(
    mp: BandMemberWithProfile,
    isOwner: Boolean,
    isSelf: Boolean,
    canRemove: Boolean,
    onRemove: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ElectricCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                mp.displayName.take(1).uppercase(),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = ElectricCyan,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(mp.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            mp.profile.email?.let { email ->
                Text(email, style = MaterialTheme.typography.bodySmall, color = OnSurface.copy(alpha = 0.45f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        RoleBadge(mp.member.role)
        if (canRemove) {
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.PersonRemove, contentDescription = "Remove", tint = OnSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val color = if (role == "admin") ElectricPurple else OnSurface.copy(alpha = 0.4f)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(role, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Upcoming events ──────────────────────────────────────────────────────────

@Composable
private fun UpcomingEventsSection(events: List<Event>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(16.dp),
    ) {
        Text("Upcoming Events", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = OnSurface.copy(alpha = 0.5f))
        Spacer(Modifier.height(10.dp))
        events.forEach { event ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(event.title, style = MaterialTheme.typography.bodyMedium, color = OnSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    event.startTime.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

// ─── Edit band bottom sheet ───────────────────────────────────────────────────

@Composable
private fun EditBandSheet(
    band: Band,
    onSave: (name: String, description: String, genres: String, dateFormed: String, label: String, spotifyUrl: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name       by remember { mutableStateOf(band.name) }
    var desc       by remember { mutableStateOf(band.description ?: "") }
    var genres     by remember { mutableStateOf(band.genres.joinToString(", ")) }
    var dateFormed by remember { mutableStateOf(band.dateFormed ?: "") }
    var label      by remember { mutableStateOf(band.label ?: "") }
    var spotify    by remember { mutableStateOf(band.spotifyUrl ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Edit Band", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = OnSurface)
        Spacer(Modifier.height(16.dp))

        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = ElectricPurple,
            focusedLabelColor    = ElectricPurple,
            unfocusedBorderColor = SurfaceVariant,
            unfocusedLabelColor  = OnSurface.copy(alpha = 0.5f),
            focusedTextColor     = OnSurface,
            unfocusedTextColor   = OnSurface,
            cursorColor          = ElectricPurple,
        )

        listOf(
            Triple(name,   { v: String -> name = v },       "Band Name *"),
            Triple(desc,   { v: String -> desc = v },       "Description"),
            Triple(genres, { v: String -> genres = v },     "Genres (comma-separated)"),
            Triple(dateFormed, { v: String -> dateFormed = v }, "Date Formed (YYYY-MM-DD)"),
            Triple(label,  { v: String -> label = v },      "Record Label"),
            Triple(spotify,{ v: String -> spotify = v },    "Spotify URL"),
        ).forEach { (value, onChange, fieldLabel) ->
            OutlinedTextField(
                value         = value,
                onValueChange = onChange,
                label         = { Text(fieldLabel) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = MaterialTheme.shapes.small,
                colors        = fieldColors,
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick  = { onSave(name, desc, genres, dateFormed, label, spotify) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = MaterialTheme.shapes.small,
            colors   = ButtonDefaults.buttonColors(containerColor = ElectricPurple, contentColor = OnAccent),
        ) {
            Text("Save Changes", fontWeight = FontWeight.Bold)
        }
    }
}
