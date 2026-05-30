package com.bandapa.feature.venues.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bandapa.BuildKonfig
import com.bandapa.feature.venues.data.PlacePrediction
import com.bandapa.feature.venues.data.staticMapUrl
import com.bandapa.feature.venues.domain.Venue
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricCyan
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.OnSurfaceVariant
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceContainerHigh
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenuesScreen(
    onNavigateBack: (() -> Unit)? = null,
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
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
                        }
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
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = OnSurface.copy(alpha = 0.5f))
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
                LinearProgressIndicator(
                    modifier   = Modifier.fillMaxWidth().padding(padding),
                    color      = ElectricPurple,
                    trackColor = SurfaceVariant,
                )
            }
            uiState.venues.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, null, tint = ElectricCyan, modifier = Modifier.size(52.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No venues yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = OnSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Add venues to attach them to events.", style = MaterialTheme.typography.bodySmall, color = OnSurface.copy(alpha = 0.5f))
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(uiState.venues, key = { it.id }) { venue ->
                        VenueRow(venue = venue, onDelete = { viewModel.deleteVenue(venue.id) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                viewModel.resetAddForm()
            },
            sheetState     = sheetState,
            containerColor = Surface,
        ) {
            AddVenueSheet(
                uiState   = uiState,
                viewModel = viewModel,
                onDismiss = {
                    showAddSheet = false
                    viewModel.resetAddForm()
                },
            )
        }
    }
}

// ─── Venue row ────────────────────────────────────────────────────────────────

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
            modifier         = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(ElectricCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.LocationOn, null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(venue.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = OnSurface)
            val subtitle = listOfNotNull(venue.address, venue.city).joinToString(", ")
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurface.copy(alpha = 0.5f))
            }
            if (venue.venueType.isNotBlank() && venue.venueType != "others") {
                Text(
                    text  = venueTypeLabel(venue.venueType),
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricPurple.copy(alpha = 0.8f),
                )
            } else if (venue.venueType.isNotBlank()) {
                Text(
                    text  = venue.venueType,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricPurple.copy(alpha = 0.8f),
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = OnSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }
    }
}

private fun venueTypeLabel(type: String) = when (type) {
    "studio"        -> "Studio"
    "hangout_place" -> "Hangout Place"
    "bar_live_venue" -> "Bar / Live Venue"
    else            -> type.replaceFirstChar { it.uppercase() }
}

// ─── Add venue sheet ──────────────────────────────────────────────────────────

private val venueTypeOptions = listOf(
    "studio"         to "Studio",
    "hangout_place"  to "Hangout Place",
    "bar_live_venue" to "Bar / Live Venue",
    "others"         to "Others",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddVenueSheet(
    uiState: VenueUiState,
    viewModel: VenueViewModel,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    val apiKey = BuildKonfig.GOOGLE_MAPS_API_KEY

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Add Venue", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = OnSurface)
        Spacer(Modifier.height(20.dp))

        // ── Name ──────────────────────────────────────────────────────────────
        VenueTextField(value = name, onValueChange = { name = it }, label = "Name *", enabled = !uiState.isSaving)
        Spacer(Modifier.height(16.dp))

        // ── Venue type ────────────────────────────────────────────────────────
        Text(
            text       = "Venue Type",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = OnSurface.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            venueTypeOptions.forEach { (key, label) ->
                FilterChip(
                    selected = uiState.selectedVenueType == key,
                    onClick  = { viewModel.setVenueType(key) },
                    label    = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    enabled  = !uiState.isSaving,
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor     = ElectricPurple.copy(alpha = 0.18f),
                        selectedLabelColor         = ElectricPurple,
                        containerColor             = SurfaceVariant,
                        labelColor                 = OnSurface.copy(alpha = 0.7f),
                    ),
                    border   = FilterChipDefaults.filterChipBorder(
                        enabled          = !uiState.isSaving,
                        selected         = uiState.selectedVenueType == key,
                        selectedBorderColor = ElectricPurple,
                        borderColor      = SurfaceVariant,
                    ),
                )
            }
        }
        if (uiState.selectedVenueType == "others") {
            Spacer(Modifier.height(8.dp))
            VenueTextField(
                value         = uiState.customVenueType,
                onValueChange = viewModel::setCustomVenueType,
                label         = "Specify type",
                enabled       = !uiState.isSaving,
            )
        }
        Spacer(Modifier.height(12.dp))

        // ── Address with autocomplete ─────────────────────────────────────────
        OutlinedTextField(
            value         = uiState.addressQuery,
            onValueChange = { viewModel.onAddressQueryChanged(it) },
            label         = { Text("Address") },
            enabled       = !uiState.isSaving,
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(10.dp),
            leadingIcon   = {
                Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
            },
            trailingIcon  = {
                if (uiState.isGeocoding) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ElectricPurple, strokeWidth = 2.dp)
                }
            },
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = ElectricPurple,
                focusedLabelColor    = ElectricPurple,
                unfocusedBorderColor = SurfaceVariant,
                focusedTextColor     = OnSurface,
                unfocusedTextColor   = OnSurface,
                cursorColor          = ElectricPurple,
            ),
        )

        // Suggestions dropdown (shown directly below the field)
        if (uiState.suggestions.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerHigh),
            ) {
                uiState.suggestions.take(5).forEachIndexed { index, prediction ->
                    SuggestionRow(
                        prediction  = prediction,
                        onClick     = { viewModel.onPlaceSelected(prediction) },
                        showDivider = index < uiState.suggestions.size - 1,
                    )
                }
            }
        }

        // ── Lat / Lng (read-only, auto-filled) ────────────────────────────────
        uiState.geocoded?.let { loc ->
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CoordField("Latitude",  "%.6f".format(loc.lat), Modifier.weight(1f))
                CoordField("Longitude", "%.6f".format(loc.lng), Modifier.weight(1f))
            }
        }

        // ── Map preview ───────────────────────────────────────────────────────
        if (uiState.geocoded != null && apiKey.isNotBlank()) {
            val loc = uiState.geocoded
            Spacer(Modifier.height(12.dp))
            AsyncImage(
                model              = staticMapUrl(loc.lat, loc.lng, apiKey),
                contentDescription = "Map preview",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── City ──────────────────────────────────────────────────────────────
        VenueTextField(value = city, onValueChange = { city = it }, label = "City (optional)", enabled = !uiState.isSaving)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = { viewModel.addVenue(name, city); onDismiss() },
            enabled  = name.isNotBlank() && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = MaterialTheme.shapes.small,
            colors   = ButtonDefaults.buttonColors(
                containerColor         = ElectricPurple,
                contentColor           = OnAccent,
                disabledContainerColor = ElectricPurple.copy(alpha = 0.4f),
                disabledContentColor   = OnAccent.copy(alpha = 0.6f),
            ),
        ) {
            if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = OnAccent, strokeWidth = 2.dp)
            else Text("Save Venue", fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Suggestion row ───────────────────────────────────────────────────────────

@Composable
private fun SuggestionRow(prediction: PlacePrediction, onClick: () -> Unit, showDivider: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text     = prediction.description,
                style    = MaterialTheme.typography.bodyMedium,
                color    = OnSurface,
                maxLines = 2,
                fontSize = 13.sp,
            )
        }
        if (showDivider) {
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(SurfaceVariant))
        }
    }
}

// ─── Read-only coord field ────────────────────────────────────────────────────

@Composable
private fun CoordField(label: String, value: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value         = value,
        onValueChange = {},
        label         = { Text(label, fontSize = 11.sp) },
        readOnly      = true,
        singleLine    = true,
        modifier      = modifier,
        shape         = RoundedCornerShape(10.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = ElectricCyan,
            unfocusedBorderColor = SurfaceVariant,
            focusedTextColor     = ElectricCyan,
            unfocusedTextColor   = ElectricCyan,
            disabledTextColor    = ElectricCyan,
        ),
    )
}

// ─── Generic text field ───────────────────────────────────────────────────────

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
