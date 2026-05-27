package com.bandapa.feature.band.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.band.domain.BandUiState
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricCyan
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.NeonGreen
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinBandScreen(
    initialCode: String = "",
    onNavigateBack: () -> Unit,
    onJoined: () -> Unit,
    viewModel: BandViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var inviteCode by remember { mutableStateOf(initialCode.uppercase()) }

    val isLoading = uiState is BandUiState.Loading

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is BandUiState.JoinedBand -> onJoined()
            is BandUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.clearError()
            }
            else -> Unit
        }
    }

    // Pre-populate from deep link
    LaunchedEffect(initialCode) {
        if (initialCode.isNotBlank()) viewModel.lookUpBand(initialCode)
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Join a Band",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = OnSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = OnSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text  = "Enter the invite code shared by your bandmate.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface.copy(alpha = 0.6f),
            )

            Spacer(Modifier.height(24.dp))

            // ── Code input ──────────────────────────────────
            OutlinedTextField(
                value         = inviteCode,
                onValueChange = { v ->
                    inviteCode = v.uppercase().take(6)
                    // Reset preview if user changes the code
                    if (uiState is BandUiState.BandFound) viewModel.resetState()
                },
                label       = { Text("Invite Code") },
                placeholder = { Text("A1B2C3") },
                singleLine  = true,
                enabled     = !isLoading,
                modifier    = Modifier.fillMaxWidth(),
                shape       = MaterialTheme.shapes.small,
                textStyle   = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight  = FontWeight.Bold,
                    textAlign   = TextAlign.Center,
                    letterSpacing = MaterialTheme.typography.headlineSmall.letterSpacing,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction      = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.lookUpBand(inviteCode) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = ElectricCyan,
                    focusedLabelColor    = ElectricCyan,
                    unfocusedBorderColor = SurfaceVariant,
                    unfocusedLabelColor  = OnSurface.copy(alpha = 0.5f),
                    focusedTextColor     = ElectricCyan,
                    unfocusedTextColor   = OnSurface,
                    cursorColor          = ElectricCyan,
                ),
            )

            Spacer(Modifier.height(16.dp))

            // ── Look Up button ───────────────────────────────
            if (uiState !is BandUiState.BandFound) {
                OutlinedButton(
                    onClick  = { viewModel.lookUpBand(inviteCode) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled  = !isLoading && inviteCode.length == 6,
                    shape    = MaterialTheme.shapes.small,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            color       = ElectricCyan,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Look Up", color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Band preview card ────────────────────────────
            val foundState = uiState as? BandUiState.BandFound
            if (foundState != null) {
                Spacer(Modifier.height(24.dp))
                BandPreviewCard(band = foundState.band)
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick  = { viewModel.confirmJoin() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled  = !isLoading,
                    shape    = MaterialTheme.shapes.small,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = NeonGreen,
                        contentColor           = OnAccent,
                        disabledContainerColor = NeonGreen.copy(alpha = 0.4f),
                        disabledContentColor   = OnAccent.copy(alpha = 0.6f),
                    ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = OnAccent,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Join ${foundState.band.name}", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BandPreviewCard(band: Band) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, shape = MaterialTheme.shapes.medium)
            .border(1.dp, ElectricCyan.copy(alpha = 0.3f), shape = MaterialTheme.shapes.medium)
            .padding(20.dp),
    ) {
        Column {
            Text(
                text       = band.name,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = OnSurface,
            )

            if (!band.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = band.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.7f),
                )
            }

            if (band.genres.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    band.genres.take(4).forEach { genre ->
                        Box(
                            modifier = Modifier
                                .background(SurfaceVariant, shape = MaterialTheme.shapes.extraSmall)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text  = genre,
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricPurple,
                            )
                        }
                    }
                }
            }

            if (!band.label.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = band.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}
