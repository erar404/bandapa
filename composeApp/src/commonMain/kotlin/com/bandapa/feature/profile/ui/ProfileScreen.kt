package com.bandapa.feature.profile.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.venues.ui.VenuesScreen
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
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    var showVenues by remember { mutableStateOf(false) }

    if (showVenues) {
        VenuesScreen(onNavigateBack = { showVenues = false })
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var nameField by remember(uiState.profile.name) {
        mutableStateOf(uiState.profile.name ?: "")
    }

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            snackbarHostState.showSnackbar("Name saved!")
            viewModel.clearSavedSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier       = modifier,
        containerColor = Background,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ElectricPurple)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 40.dp),
        ) {
            // ─── Avatar ────────────────────────────────────────────────────────
            val initials = (uiState.profile.name ?: uiState.profile.email ?: "?")
                .trim().split(" ")
                .take(2)
                .joinToString("") { it.firstOrNull()?.uppercaseChar()?.toString() ?: "" }
                .ifEmpty { "?" }

            Box(
                modifier         = Modifier.size(80.dp).clip(CircleShape).background(ElectricPurple),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = initials, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnAccent)
            }

            Spacer(Modifier.height(20.dp))

            // ─── Name field ────────────────────────────────────────────────────
            Text(
                text       = "Display Name",
                style      = MaterialTheme.typography.labelMedium,
                color      = OnSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value         = nameField,
                    onValueChange = { nameField = it },
                    placeholder   = { Text("Your name", color = OnSurface.copy(alpha = 0.4f)) },
                    singleLine    = true,
                    enabled       = !uiState.isSaving,
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = profileTextFieldColors(),
                )
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp), color = ElectricPurple, strokeWidth = 2.dp)
                } else {
                    IconButton(
                        onClick = { viewModel.saveName(nameField) },
                        enabled = nameField.isNotBlank() && nameField != (uiState.profile.name ?: ""),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save name",
                            tint = if (nameField.isNotBlank() && nameField != (uiState.profile.name ?: ""))
                                NeonGreen else OnSurface.copy(alpha = 0.3f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Email ─────────────────────────────────────────────────────────
            Text(
                text       = "Email",
                style      = MaterialTheme.typography.labelMedium,
                color      = OnSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text  = uiState.profile.email ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.8f),
                )
            }

            Spacer(Modifier.height(28.dp))

            // ─── My Bands ──────────────────────────────────────────────────────
            Text(
                text       = "My Bands",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = OnSurface,
            )
            Spacer(Modifier.height(10.dp))

            if (uiState.bands.isEmpty()) {
                Text(
                    text  = "You're not in any bands yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.4f),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.bands.forEach { band -> BandRow(band) }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ─── Settings / actions ────────────────────────────────────────────
            Text(
                text       = "Settings",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = OnSurface,
            )
            Spacer(Modifier.height(10.dp))

            SettingsRow(
                icon    = Icons.Default.LocationOn,
                iconTint = ElectricCyan,
                label   = "Manage Venues",
                onClick = { showVenues = true },
            )

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(40.dp))

            // ─── Sign Out ──────────────────────────────────────────────────────
            Button(
                onClick  = viewModel::signOut,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = com.bandapa.ui.theme.ErrorContainer,
                    contentColor   = com.bandapa.ui.theme.ErrorRed,
                ),
            ) {
                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ─── Sub-components ───────────────────────────────────────────────────────────

@Composable
private fun BandRow(band: Band) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier.size(36.dp).clip(CircleShape).background(ElectricPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = band.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = OnSurface)
            band.genres.takeIf { it.isNotEmpty() }?.let { genres ->
                Text(
                    text  = genres.take(3).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = OnSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = ElectricPurple,
    focusedLabelColor       = ElectricPurple,
    unfocusedBorderColor    = SurfaceVariant,
    unfocusedLabelColor     = OnSurface.copy(alpha = 0.45f),
    focusedTextColor        = OnSurface,
    unfocusedTextColor      = OnSurface,
    cursorColor             = ElectricPurple,
    focusedContainerColor   = androidx.compose.ui.graphics.Color.Transparent,
    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
    disabledContainerColor  = androidx.compose.ui.graphics.Color.Transparent,
)
