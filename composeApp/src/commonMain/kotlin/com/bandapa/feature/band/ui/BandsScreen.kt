package com.bandapa.feature.band.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bandapa.feature.band.domain.Band
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricCyan
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.NeonGreen
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.OnSurfaceVariant
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BandsScreen(
    modifier: Modifier = Modifier,
    onNavigateToBandDetail: (bandId: String) -> Unit,
    onNavigateToCreateBand: () -> Unit,
    onNavigateToJoinBand: () -> Unit,
    onNavigateToConflicts: () -> Unit,
    viewModel: BandsListViewModel = koinViewModel(),
) {
    val bands        by viewModel.bands.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        modifier       = modifier,
        containerColor = Background,
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick        = { menuExpanded = true },
                    containerColor = ElectricPurple,
                    contentColor   = OnAccent,
                ) { Icon(Icons.Default.Add, contentDescription = "Add band") }
                DropdownMenu(
                    expanded         = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor   = SurfaceVariant,
                ) {
                    DropdownMenuItem(
                        text    = { Text("Create Band", color = OnSurface) },
                        onClick = { menuExpanded = false; onNavigateToCreateBand() },
                    )
                    DropdownMenuItem(
                        text    = { Text("Join Band", color = OnSurface) },
                        onClick = { menuExpanded = false; onNavigateToJoinBand() },
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Header ────────────────────────────────────────────────────────────
            Text(
                "Bands",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = OnSurface,
            )

            Spacer(Modifier.height(12.dp))
            ConflictsShortcutRow(onClick = onNavigateToConflicts)

            // Refresh indicator
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier   = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    color      = ElectricPurple,
                    trackColor = SurfaceVariant,
                )
            } else {
                Spacer(Modifier.height(12.dp))
            }

            when {
                isLoading -> LinearProgressIndicator(
                    modifier   = Modifier.fillMaxWidth(),
                    color      = ElectricPurple,
                    trackColor = SurfaceVariant,
                )
                bands.isEmpty() -> EmptyBandsState(
                    onCreateBand = onNavigateToCreateBand,
                    onJoinBand   = onNavigateToJoinBand,
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(bands, key = { it.id }) { band ->
                        BandCard(band = band, onClick = { onNavigateToBandDetail(band.id) })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ConflictsShortcutRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ElectricPurple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = ElectricPurple,
                modifier           = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Conflicts",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
            )
            Text(
                "Check schedule overlaps",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
            )
        }
        Icon(
            imageVector        = Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = OnSurface.copy(alpha = 0.35f),
            modifier           = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun BandCard(band: Band, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ElectricCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            if (!band.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model              = band.imageUrl,
                    contentDescription = band.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                )
            } else {
                Text(
                    text       = band.name.firstOrNull()?.uppercaseChar()?.toString() ?: "B",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = ElectricCyan,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                band.name,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            if (band.genres.isNotEmpty()) {
                Text(
                    band.genres.joinToString(" · "),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector        = Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = OnSurface.copy(alpha = 0.35f),
            modifier           = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun EmptyBandsState(onCreateBand: () -> Unit, onJoinBand: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 32.dp),
        ) {
            Box(
                modifier         = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ElectricPurple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Default.Groups,
                    contentDescription = null,
                    tint               = ElectricPurple,
                    modifier           = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "No bands yet",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = OnSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Create your first band or join one with an invite code.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = OnSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onCreateBand,
                    colors  = ButtonDefaults.buttonColors(containerColor = ElectricPurple, contentColor = OnAccent),
                    shape   = MaterialTheme.shapes.small,
                ) {
                    Text("Create Band", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onJoinBand,
                    shape   = MaterialTheme.shapes.small,
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                ) {
                    Text("Join Band", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
