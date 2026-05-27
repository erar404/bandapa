package com.bandapa.feature.band.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bandapa.feature.band.domain.BandUiState
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBandScreen(
    onNavigateBack: () -> Unit,
    onBandCreated: () -> Unit,
    viewModel: BandViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name        by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genres      by remember { mutableStateOf("") }
    var dateFormed  by remember { mutableStateOf("") }
    var label       by remember { mutableStateOf("") }

    val isLoading = uiState is BandUiState.Loading

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is BandUiState.BandCreated -> onBandCreated()
            is BandUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.clearError()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Band",
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
            Spacer(Modifier.height(8.dp))

            BandTextField(
                value         = name,
                onValueChange = { name = it },
                label         = "Band Name *",
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(12.dp))

            BandTextField(
                value         = description,
                onValueChange = { description = it },
                label         = "Description",
                minLines      = 3,
                maxLines      = 6,
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(12.dp))

            BandTextField(
                value         = genres,
                onValueChange = { genres = it },
                label         = "Genres (comma-separated)",
                placeholder   = "Rock, Jazz, Indie",
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(12.dp))

            BandTextField(
                value         = dateFormed,
                onValueChange = { dateFormed = it },
                label         = "Date Formed (YYYY-MM-DD)",
                placeholder   = "2020-01-15",
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(12.dp))

            BandTextField(
                value         = label,
                onValueChange = { label = it },
                label         = "Record Label",
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick  = { viewModel.createBand(name, description, genres, dateFormed, label) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !isLoading,
                shape    = MaterialTheme.shapes.small,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = ElectricPurple,
                    contentColor           = OnAccent,
                    disabledContainerColor = ElectricPurple.copy(alpha = 0.4f),
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
                    Text("Create Band", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BandTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    minLines: Int = 1,
    maxLines: Int = 1,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        placeholder   = if (placeholder.isNotEmpty()) {{ Text(placeholder) }} else null,
        minLines      = minLines,
        maxLines      = maxLines,
        enabled       = enabled,
        modifier      = Modifier.fillMaxWidth(),
        shape         = MaterialTheme.shapes.small,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = ElectricPurple,
            focusedLabelColor     = ElectricPurple,
            unfocusedBorderColor  = SurfaceVariant,
            unfocusedLabelColor   = OnSurface.copy(alpha = 0.5f),
            focusedTextColor      = OnSurface,
            unfocusedTextColor    = OnSurface,
            cursorColor           = ElectricPurple,
        ),
    )
}
