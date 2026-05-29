package com.bandapa.feature.band.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bandapa.feature.band.domain.BandUiState
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.OnSurfaceVariant
import com.bandapa.ui.theme.SurfaceVariant
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

private val GENRE_LIST = listOf(
    "Rock",        "Pop",
    "Jazz",        "Hip-Hop",
    "R&B",         "Electronic",
    "Classical",   "Country",
    "Reggae",      "Blues",
    "Metal",       "Indie",
    "Folk",        "Latin",
    "Punk",        "Alternative",
    "Funk",        "Gospel",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateBandScreen(
    onNavigateBack: () -> Unit,
    onBandCreated: () -> Unit,
    viewModel: BandViewModel = koinViewModel(),
) {
    val uiState        by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name           by remember { mutableStateOf("") }
    var description    by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf(setOf<String>()) }
    var otherChecked   by remember { mutableStateOf(false) }
    var otherText      by remember { mutableStateOf("") }
    var dateMillis     by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var label          by remember { mutableStateOf("") }
    var spotifyUrl     by remember { mutableStateOf("") }
    var imageBytes     by remember { mutableStateOf<ByteArray?>(null) }

    val isLoading = uiState is BandUiState.Loading

    val dateFormedStr = dateMillis?.let {
        Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date.toString()
    } ?: ""

    val genres = buildList {
        addAll(selectedGenres.sorted())
        if (otherChecked) {
            otherText.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { add(it) }
        }
    }

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

    // ── Date picker dialog ────────────────────────────────────────────────────
    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateMillis = dpState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = ElectricPurple, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = OnSurface.copy(alpha = 0.6f))
                }
            },
        ) {
            DatePicker(state = dpState)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Band photo ────────────────────────────────────────────────────
            BandImagePicker(
                selectedBytes   = imageBytes,
                onImageSelected = { imageBytes = it },
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = if (imageBytes != null) "Tap to change photo" else "Tap to add band photo",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // ── Band name ─────────────────────────────────────────────────────
            BandTextField(
                value         = name,
                onValueChange = { name = it },
                label         = "Band Name *",
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(12.dp))

            // ── Description ───────────────────────────────────────────────────
            BandTextField(
                value         = description,
                onValueChange = { description = it },
                label         = "Description",
                minLines      = 3,
                maxLines      = 6,
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = OnSurface.copy(alpha = 0.08f))
            Spacer(Modifier.height(16.dp))

            // ── Genres ────────────────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Genres",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = OnSurface,
                    modifier   = Modifier.weight(1f),
                )
                if (genres.isNotEmpty()) {
                    Text(
                        "${genres.size} selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricPurple,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            FlowRow(
                modifier              = Modifier.fillMaxWidth(),
                maxItemsInEachRow     = 2,
                horizontalArrangement = Arrangement.Start,
            ) {
                GENRE_LIST.forEach { genre ->
                    val checked = genre in selectedGenres
                    Row(
                        modifier          = Modifier
                            .weight(1f)
                            .clickable(enabled = !isLoading) {
                                selectedGenres =
                                    if (checked) selectedGenres - genre
                                    else selectedGenres + genre
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked         = checked,
                            onCheckedChange = { on ->
                                selectedGenres =
                                    if (on) selectedGenres + genre else selectedGenres - genre
                            },
                            enabled = !isLoading,
                            colors  = CheckboxDefaults.colors(
                                checkedColor   = ElectricPurple,
                                checkmarkColor = OnAccent,
                                uncheckedColor = OnSurface.copy(alpha = 0.35f),
                            ),
                        )
                        Text(
                            text  = genre,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (checked) OnSurface else OnSurfaceVariant,
                        )
                    }
                }

                // Other / specify
                val otherRow = @Composable {
                    Row(
                        modifier          = Modifier
                            .weight(1f)
                            .clickable(enabled = !isLoading) { otherChecked = !otherChecked }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked         = otherChecked,
                            onCheckedChange = { otherChecked = it },
                            enabled         = !isLoading,
                            colors          = CheckboxDefaults.colors(
                                checkedColor   = ElectricPurple,
                                checkmarkColor = OnAccent,
                                uncheckedColor = OnSurface.copy(alpha = 0.35f),
                            ),
                        )
                        Text(
                            text  = "Other",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (otherChecked) OnSurface else OnSurfaceVariant,
                        )
                    }
                }
                otherRow()
            }

            if (otherChecked) {
                Spacer(Modifier.height(8.dp))
                BandTextField(
                    value         = otherText,
                    onValueChange = { otherText = it },
                    label         = "Specify genres",
                    placeholder   = "e.g. Afrobeats, Bossa Nova",
                    enabled       = !isLoading,
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = OnSurface.copy(alpha = 0.08f))
            Spacer(Modifier.height(16.dp))

            // ── Date formed ───────────────────────────────────────────────────
            Text(
                "Date Formed",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
                modifier   = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Box {
                BandTextField(
                    value         = dateFormedStr,
                    onValueChange = {},
                    label         = "Date formed",
                    placeholder   = "Tap to select",
                    enabled       = false,
                    trailingIcon  = Icons.Default.CalendarMonth,
                )
                Box(Modifier.matchParentSize().clickable(enabled = !isLoading) { showDatePicker = true })
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = OnSurface.copy(alpha = 0.08f))
            Spacer(Modifier.height(16.dp))

            // ── Record label ──────────────────────────────────────────────────
            Text(
                "More Details",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
                modifier   = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            BandTextField(
                value         = label,
                onValueChange = { label = it },
                label         = "Record Label",
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(12.dp))

            // ── Spotify link ──────────────────────────────────────────────────
            BandTextField(
                value         = spotifyUrl,
                onValueChange = { spotifyUrl = it },
                label         = "Spotify Artist Link",
                placeholder   = "https://open.spotify.com/artist/...",
                enabled       = !isLoading,
                trailingIcon  = Icons.Default.Link,
            )

            Spacer(Modifier.height(32.dp))

            // ── Submit ────────────────────────────────────────────────────────
            Button(
                onClick  = {
                    viewModel.createBand(
                        name        = name,
                        description = description,
                        genres      = genres,
                        dateFormed  = dateFormedStr,
                        label       = label,
                        spotifyUrl  = spotifyUrl,
                        imageBytes  = imageBytes,
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !isLoading && name.isNotBlank(),
                shape    = MaterialTheme.shapes.small,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = ElectricPurple,
                    contentColor           = OnAccent,
                    disabledContainerColor = ElectricPurple.copy(alpha = 0.4f),
                    disabledContentColor   = OnAccent.copy(alpha = 0.6f),
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnAccent, strokeWidth = 2.dp)
                } else {
                    Text("Create Band", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(40.dp))
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
    isError: Boolean = false,
    trailingIcon: ImageVector? = null,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        placeholder   = if (placeholder.isNotEmpty()) {{ Text(placeholder) }} else null,
        minLines      = minLines,
        maxLines      = maxLines,
        enabled       = enabled,
        isError       = isError,
        modifier      = Modifier.fillMaxWidth(),
        shape         = MaterialTheme.shapes.small,
        trailingIcon  = trailingIcon?.let { icon ->
            { Icon(icon, contentDescription = null, tint = OnSurface.copy(alpha = 0.5f)) }
        },
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor        = ElectricPurple,
            focusedLabelColor         = ElectricPurple,
            unfocusedBorderColor      = SurfaceVariant,
            unfocusedLabelColor       = OnSurface.copy(alpha = 0.45f),
            focusedTextColor          = OnSurface,
            unfocusedTextColor        = OnSurface,
            disabledTextColor         = OnSurface,
            disabledBorderColor       = SurfaceVariant,
            disabledLabelColor        = OnSurface.copy(alpha = 0.45f),
            disabledTrailingIconColor = OnSurface.copy(alpha = 0.5f),
            cursorColor               = ElectricPurple,
            focusedContainerColor     = Color.Transparent,
            unfocusedContainerColor   = Color.Transparent,
            disabledContainerColor    = Color.Transparent,
        ),
    )
}
