package com.bandapa.feature.conflicts.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bandapa.feature.calendar.domain.Event
import com.bandapa.feature.conflicts.domain.ConflictDetail
import com.bandapa.feature.conflicts.domain.ConflictsUiState
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricCyan
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.NeonGreen
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConflictsScreen(
    modifier: Modifier = Modifier,
    viewModel: ConflictsViewModel = koinViewModel(),
) {
    val uiState      by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is ConflictsUiState.Error) {
            snackbarHostState.showSnackbar((uiState as ConflictsUiState.Error).message)
        }
    }

    Scaffold(
        modifier       = modifier,
        containerColor = Background,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Conflicts",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = OnSurface,
                    modifier   = Modifier.weight(1f),
                )
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = ElectricPurple,
                        strokeWidth = 2.dp,
                    )
                } else {
                    IconButton(onClick = viewModel::refresh, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector        = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint               = OnSurface.copy(alpha = 0.4f),
                            modifier           = Modifier.size(18.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            when (val state = uiState) {
                is ConflictsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricPurple)
                    }
                }
                is ConflictsUiState.Loaded -> {
                    if (state.items.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.items, key = { it.conflict.id }) { detail ->
                                ConflictCard(
                                    detail    = detail,
                                    onVote    = { eventId -> viewModel.vote(detail.conflict.id, eventId) },
                                    onDismiss = { viewModel.dismiss(detail.conflict.id) },
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
                is ConflictsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = OnSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = Icons.Default.CheckCircle,
                contentDescription = null,
                tint               = NeonGreen,
                modifier           = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No active conflicts",
                style      = MaterialTheme.typography.titleMedium,
                color      = OnSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Your schedule is clear.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface.copy(alpha = 0.5f),
            )
        }
    }
}

// ─── Conflict card ────────────────────────────────────────────────────────────

@Composable
private fun ConflictCard(
    detail: ConflictDetail,
    onVote: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val votesForA  = detail.votes.count { it.votedFor == detail.eventA.id }
    val votesForB  = detail.votes.count { it.votedFor == detail.eventB.id }
    val totalVotes = detail.votes.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = ElectricPurple,
                modifier           = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Scheduling Conflict",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = ElectricPurple,
            )
        }

        Spacer(Modifier.height(12.dp))
        EventRow(event = detail.eventA)
        Spacer(Modifier.height(8.dp))
        Text(
            "vs",
            style    = MaterialTheme.typography.labelSmall,
            color    = OnSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = 8.dp),
        )
        Spacer(Modifier.height(8.dp))
        EventRow(event = detail.eventB)

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = SurfaceVariant)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VoteButton(
                label    = detail.eventA.title,
                votes    = votesForA,
                isMyVote = detail.myVote == detail.eventA.id,
                modifier = Modifier.weight(1f),
                onClick  = { onVote(detail.eventA.id) },
            )
            VoteButton(
                label    = detail.eventB.title,
                votes    = votesForB,
                isMyVote = detail.myVote == detail.eventB.id,
                modifier = Modifier.weight(1f),
                onClick  = { onVote(detail.eventB.id) },
            )
        }

        if (totalVotes > 0) {
            Spacer(Modifier.height(8.dp))
            VoteBars(votesForA = votesForA, votesForB = votesForB, totalVotes = totalVotes)
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick  = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape    = MaterialTheme.shapes.small,
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface.copy(alpha = 0.6f)),
        ) {
            Text("Dismiss", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun EventRow(event: Event) {
    val accent = if (event.bandId != null) ElectricCyan else NeonGreen
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                event.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Text(
                buildEventSubtitle(event),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun VoteButton(
    label: String,
    votes: Int,
    isMyVote: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick  = onClick,
        modifier = modifier.height(44.dp),
        shape    = MaterialTheme.shapes.small,
        colors   = ButtonDefaults.buttonColors(
            containerColor         = if (isMyVote) ElectricPurple else SurfaceVariant,
            contentColor           = if (isMyVote) OnAccent else OnSurface,
            disabledContainerColor = SurfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Text(
            label,
            style    = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VoteBars(votesForA: Int, votesForB: Int, totalVotes: Int) {
    val fracA = votesForA.toFloat() / totalVotes
    val fracB = votesForB.toFloat() / totalVotes

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(fracA.coerceAtLeast(0.01f))
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ElectricPurple),
        )
        Box(
            modifier = Modifier
                .weight(fracB.coerceAtLeast(0.01f))
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SurfaceVariant),
        )
    }
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$votesForA vote${if (votesForA != 1) "s" else ""}", style = MaterialTheme.typography.labelSmall, color = OnSurface.copy(alpha = 0.5f))
        Text("$votesForB vote${if (votesForB != 1) "s" else ""}", style = MaterialTheme.typography.labelSmall, color = OnSurface.copy(alpha = 0.5f))
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun buildEventSubtitle(event: Event): String {
    val date = event.startTime.take(10)
    return if (event.isAllDay) "$date · All day"
    else "$date · ${event.startTime.formatTime()} – ${event.endTime.formatTime()}"
}

private fun String.formatTime(): String =
    if (length >= 16) substring(11, 16) else this
