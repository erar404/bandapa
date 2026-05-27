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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bandapa.feature.band.domain.Band
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.NeonGreen
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BandsScreen(
    modifier: Modifier = Modifier,
    onNavigateToBandDetail: (bandId: String) -> Unit,
    onNavigateToCreateBand: () -> Unit,
    onNavigateToJoinBand: () -> Unit,
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
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Bands",
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

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricPurple)
                }
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
private fun BandCard(band: Band, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ElectricPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.MusicNote,
                contentDescription = null,
                tint               = ElectricPurple,
                modifier           = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
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
                    color    = OnSurface.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector        = Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = OnSurface.copy(alpha = 0.4f),
            modifier           = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun EmptyBandsState(onCreateBand: () -> Unit, onJoinBand: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = Icons.Default.Groups,
                contentDescription = null,
                tint               = ElectricPurple,
                modifier           = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No bands yet",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Create your first band or join one with an invite code.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCreateBand,
                    colors  = ButtonDefaults.buttonColors(containerColor = ElectricPurple, contentColor = OnAccent),
                    shape   = MaterialTheme.shapes.small,
                ) { Text("Create Band") }
                OutlinedButton(
                    onClick = onJoinBand,
                    shape   = MaterialTheme.shapes.small,
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                ) { Text("Join Band") }
            }
        }
    }
}
