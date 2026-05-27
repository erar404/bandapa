package com.bandapa.feature.venues.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bandapa.feature.venues.domain.Venue
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricCyan
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenuesScreen(
    onNavigateBack: () -> Unit,
    viewModel: VenueViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Venues",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = OnSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                actions = {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp).padding(end = 4.dp),
                            color       = ElectricPurple,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(
                                imageVector        = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint               = OnSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddSheet = true },
                containerColor = ElectricPurple,
                contentColor   = OnAccent,
            ) { Icon(Icons.Default.Add, contentDescription = "Add venue") }
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = ElectricPurple) }
            }
            uiState.venues.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector        = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint               = ElectricCyan,
                            modifier           = Modifier.size(52.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No venues yet",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = OnSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Add venues to attach them to events.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier              = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement   = Arrangement.spacedBy(10.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(uiState.venues, key = { it.id }) { venue ->
                        VenueRow(
                            venue    = venue,
                            onDelete = { viewModel.deleteVenue(venue.id) },
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState       = sheetState,
            containerColor   = Surface,
        ) {
            AddVenueSheet(
                isSaving  = uiState.isSaving,
                onSave    = { name, address, city ->
                    viewModel.addVenue(name, address, city)
                    showAddSheet = false
                },
                onDismiss = { showAddSheet = false },
            )
        }
    }
}

@Composable
private fun VenueRow(venue: Venue, onDelete: () -> Unit) {
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
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ElectricCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = venue.name,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
            )
            val subtitle = listOfNotNull(venue.address, venue.city).joinToString(", ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface.copy(alpha = 0.5f),
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = "Delete venue",
                tint               = OnSurface.copy(alpha = 0.4f),
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun AddVenueSheet(
    isSaving: Boolean,
    onSave: (name: String, address: String, city: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name    by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city    by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            "Add Venue",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = OnSurface,
        )
        Spacer(Modifier.height(20.dp))

        VenueTextField(value = name,    onValueChange = { name    = it }, label = "Name *",             enabled = !isSaving)
        Spacer(Modifier.height(12.dp))
        VenueTextField(value = address, onValueChange = { address = it }, label = "Address (optional)",  enabled = !isSaving)
        Spacer(Modifier.height(12.dp))
        VenueTextField(value = city,    onValueChange = { city    = it }, label = "City (optional)",     enabled = !isSaving)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = { onSave(name, address, city) },
            enabled  = name.isNotBlank() && !isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = MaterialTheme.shapes.small,
            colors   = ButtonDefaults.buttonColors(
                containerColor         = ElectricPurple,
                contentColor           = OnAccent,
                disabledContainerColor = ElectricPurple.copy(alpha = 0.4f),
                disabledContentColor   = OnAccent.copy(alpha = 0.6f),
            ),
        ) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = OnAccent, strokeWidth = 2.dp)
            else Text("Save Venue", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VenueTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        enabled       = enabled,
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(10.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = ElectricPurple,
            focusedLabelColor    = ElectricPurple,
            unfocusedBorderColor = SurfaceVariant,
            focusedTextColor     = OnSurface,
            unfocusedTextColor   = OnSurface,
            cursorColor          = ElectricPurple,
        ),
    )
}
