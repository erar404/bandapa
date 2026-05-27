package com.bandapa.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bandapa.feature.band.domain.Band
import com.bandapa.feature.calendar.domain.CalendarUiState
import com.bandapa.feature.calendar.domain.Event
import com.bandapa.feature.venues.domain.Venue
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricCyan
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.NeonGreen
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val displayedMonth  by viewModel.displayedMonth.collectAsState()
    val selectedDay     by viewModel.selectedDay.collectAsState()
    val eventsForMonth  by viewModel.eventsForMonth.collectAsState()
    val myBands         by viewModel.myBands.collectAsState()
    val venues          by viewModel.venues.collectAsState()
    val uiState         by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is CalendarUiState.EventCreated -> {
                showAddSheet = false
                viewModel.resetState()
            }
            is CalendarUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.clearError()
            }
            else -> Unit
        }
    }

    Scaffold(
        modifier       = modifier,
        containerColor = Background,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddSheet = true },
                containerColor = ElectricPurple,
                contentColor   = OnAccent,
            ) { Icon(Icons.Default.Add, contentDescription = "Add event") }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            MonthHeader(
                month  = displayedMonth,
                onPrev = viewModel::prevMonth,
                onNext = viewModel::nextMonth,
            )
            Spacer(Modifier.height(8.dp))
            MonthGrid(
                displayedMonth = displayedMonth,
                selectedDay    = selectedDay,
                eventsForMonth = eventsForMonth,
                onDayClick     = viewModel::selectDay,
            )
            Spacer(Modifier.height(16.dp))
            selectedDay?.let { day ->
                val dayEvents = eventsForMonth.filter { it.startTime.startsWith(day.toString()) }
                DayEventList(
                    day      = day,
                    events   = dayEvents,
                    onDelete = viewModel::deleteEvent,
                    onTap    = { selectedEvent = it },
                )
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState       = sheetState,
            containerColor   = Surface,
        ) {
            AddEventSheet(
                initialDate = selectedDay,
                bands       = myBands,
                venues      = venues,
                isLoading   = uiState is CalendarUiState.Loading,
                onSave      = { title, date, start, end, bandId, venueId, location, isAllDay ->
                    viewModel.createEvent(title, date, start, end, bandId, venueId, location, isAllDay)
                },
                onDismiss   = { showAddSheet = false },
            )
        }
    }

    selectedEvent?.let { event ->
        ModalBottomSheet(
            onDismissRequest = { selectedEvent = null },
            sheetState       = detailSheetState,
            containerColor   = Surface,
        ) {
            EventDetailSheet(
                event     = event,
                bands     = myBands,
                venues    = venues,
                onDelete  = {
                    viewModel.deleteEvent(event.id)
                    selectedEvent = null
                },
                onDismiss = { selectedEvent = null },
            )
        }
    }
}

// ─── Month header ─────────────────────────────────────────────────────────────

@Composable
private fun MonthHeader(
    month: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = OnSurface)
        }
        Text(
            text       = "${month.month.displayName()} ${month.year}",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = OnSurface,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = OnSurface)
        }
    }
}

// ─── Month grid ───────────────────────────────────────────────────────────────

private val DAY_LABELS = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

@Composable
private fun MonthGrid(
    displayedMonth: LocalDate,
    selectedDay: LocalDate?,
    eventsForMonth: List<Event>,
    onDayClick: (LocalDate) -> Unit,
) {
    val year  = displayedMonth.year
    val month = displayedMonth.monthNumber

    val firstOfMonth = LocalDate(year, month, 1)
    val daysInMonth  = daysInMonth(year, month)
    val startOffset  = (firstOfMonth.dayOfWeek.ordinal + 1) % 7

    val eventDays = eventsForMonth.mapNotNull { ev ->
        runCatching { LocalDate.parse(ev.startTime.take(10)) }.getOrNull()
    }.filter { it.year == year && it.monthNumber == month }
        .map { it.dayOfMonth }.toSet()

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_LABELS.forEach { label ->
                Text(
                    text       = label,
                    modifier   = Modifier.weight(1f),
                    textAlign  = TextAlign.Center,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = OnSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        val totalCells = startOffset + daysInMonth
        val rows       = (totalCells + 6) / 7

        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNum    = cellIndex - startOffset + 1
                    val inMonth   = dayNum in 1..daysInMonth
                    val date      = if (inMonth) LocalDate(year, month, dayNum) else null

                    DayCell(
                        modifier   = Modifier.weight(1f),
                        day        = if (inMonth) dayNum else null,
                        isSelected = date != null && date == selectedDay,
                        hasEvents  = inMonth && dayNum in eventDays,
                        onClick    = { date?.let { onDayClick(it) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier,
    day: Int?,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier         = modifier.aspectRatio(1f).clickable(enabled = day != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (day != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier         = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) ElectricPurple else androidx.compose.ui.graphics.Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = day.toString(),
                        style      = MaterialTheme.typography.bodySmall,
                        color      = if (isSelected) OnAccent else OnSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
                if (hasEvents) {
                    Spacer(Modifier.height(2.dp))
                    Box(Modifier.size(4.dp).clip(CircleShape).background(ElectricCyan))
                }
            }
        }
    }
}

// ─── Day event list ───────────────────────────────────────────────────────────

@Composable
private fun DayEventList(
    day: LocalDate,
    events: List<Event>,
    onDelete: (String) -> Unit,
    onTap: (Event) -> Unit,
) {
    Text(
        text       = day.toDayLabel(),
        style      = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color      = OnSurface,
    )
    Spacer(Modifier.height(8.dp))

    if (events.isEmpty()) {
        Text(
            text  = "No events",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurface.copy(alpha = 0.4f),
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            events.forEach { event ->
                EventCard(
                    event    = event,
                    onDelete = { onDelete(event.id) },
                    onTap    = { onTap(event) },
                )
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, onDelete: () -> Unit, onTap: () -> Unit) {
    val accentColor = if (event.bandId != null) ElectricCyan else NeonGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .clickable(onClick = onTap)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = event.title,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = OnSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            if (!event.isAllDay) {
                Text(
                    text  = "${event.startTime.formatTime()} – ${event.endTime.formatTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface.copy(alpha = 0.6f),
                )
            } else {
                Text("All day", style = MaterialTheme.typography.bodySmall, color = OnSurface.copy(alpha = 0.6f))
            }
            event.location?.let { loc ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint               = OnSurface.copy(alpha = 0.4f),
                        modifier           = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text     = loc,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = OnSurface.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = "Delete",
                tint               = OnSurface.copy(alpha = 0.4f),
                modifier           = Modifier.size(18.dp),
            )
        }
    }
}

// ─── Add Event bottom sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventSheet(
    initialDate: LocalDate?,
    bands: List<com.bandapa.feature.band.domain.Band>,
    venues: List<Venue>,
    isLoading: Boolean,
    onSave: (title: String, date: LocalDate, startHhmm: String, endHhmm: String, bandId: String?, venueId: String?, location: String, isAllDay: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var title    by remember { mutableStateOf("") }
    var startT   by remember { mutableStateOf("09:00") }
    var endT     by remember { mutableStateOf("10:00") }
    var location by remember { mutableStateOf("") }
    var isAllDay by remember { mutableStateOf(false) }
    var selectedBandId  by remember { mutableStateOf<String?>(null) }
    var selectedVenueId by remember { mutableStateOf<String?>(null) }
    var bandMenuExpanded  by remember { mutableStateOf(false) }
    var venueMenuExpanded by remember { mutableStateOf(false) }

    val date = initialDate ?: LocalDate(2025, 1, 1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            "Add Event",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = OnSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            date.toDayLabel(),
            style = MaterialTheme.typography.bodyMedium,
            color = ElectricPurple,
        )
        Spacer(Modifier.height(16.dp))

        CalTextField(value = title, onValueChange = { title = it }, label = "Title *", enabled = !isLoading)
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked         = isAllDay,
                onCheckedChange = { isAllDay = it },
                colors          = CheckboxDefaults.colors(checkedColor = ElectricPurple),
            )
            Text("All day", color = OnSurface, style = MaterialTheme.typography.bodyMedium)
        }

        if (!isAllDay) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalTextField(value = startT, onValueChange = { startT = it }, label = "Start (HH:mm)", modifier = Modifier.weight(1f), enabled = !isLoading)
                CalTextField(value = endT,   onValueChange = { endT   = it }, label = "End (HH:mm)",   modifier = Modifier.weight(1f), enabled = !isLoading)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ─── Band selector ────────────────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded         = bandMenuExpanded,
            onExpandedChange = { bandMenuExpanded = !bandMenuExpanded },
        ) {
            OutlinedTextField(
                value         = if (selectedBandId == null) "Personal" else bands.firstOrNull { it.id == selectedBandId }?.name ?: "Personal",
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Band") },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(bandMenuExpanded) },
                modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape         = MaterialTheme.shapes.small,
                colors        = calTextFieldColors(),
            )
            ExposedDropdownMenu(
                expanded         = bandMenuExpanded,
                onDismissRequest = { bandMenuExpanded = false },
                containerColor   = SurfaceVariant,
            ) {
                DropdownMenuItem(
                    text    = { Text("Personal", color = OnSurface) },
                    onClick = { selectedBandId = null; bandMenuExpanded = false },
                )
                bands.forEach { band ->
                    DropdownMenuItem(
                        text    = { Text(band.name, color = OnSurface) },
                        onClick = { selectedBandId = band.id; bandMenuExpanded = false },
                    )
                }
            }
        }

        // ─── Venue selector ───────────────────────────────────────────────────
        if (venues.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded         = venueMenuExpanded,
                onExpandedChange = { venueMenuExpanded = !venueMenuExpanded },
            ) {
                OutlinedTextField(
                    value         = venues.firstOrNull { it.id == selectedVenueId }?.name ?: "No venue",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Venue") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(venueMenuExpanded) },
                    modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape         = MaterialTheme.shapes.small,
                    colors        = calTextFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded         = venueMenuExpanded,
                    onDismissRequest = { venueMenuExpanded = false },
                    containerColor   = SurfaceVariant,
                ) {
                    DropdownMenuItem(
                        text    = { Text("No venue", color = OnSurface) },
                        onClick = { selectedVenueId = null; venueMenuExpanded = false },
                    )
                    venues.forEach { venue ->
                        val label = buildString {
                            append(venue.name)
                            venue.city?.let { append(" · $it") }
                        }
                        DropdownMenuItem(
                            text    = { Text(label, color = OnSurface) },
                            onClick = { selectedVenueId = venue.id; venueMenuExpanded = false },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        CalTextField(value = location, onValueChange = { location = it }, label = "Location (optional)", enabled = !isLoading)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = { onSave(title, date, startT, endT, selectedBandId, selectedVenueId, location, isAllDay) },
            enabled  = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = MaterialTheme.shapes.small,
            colors   = ButtonDefaults.buttonColors(
                containerColor         = ElectricPurple,
                contentColor           = OnAccent,
                disabledContainerColor = ElectricPurple.copy(alpha = 0.4f),
                disabledContentColor   = OnAccent.copy(alpha = 0.6f),
            ),
        ) {
            Text("Save Event", fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Event detail sheet ───────────────────────────────────────────────────────

@Composable
private fun EventDetailSheet(
    event: Event,
    bands: List<Band>,
    venues: List<Venue>,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val bandName  = bands.firstOrNull { it.id == event.bandId }?.name
    val venueName = venues.firstOrNull { it.id == event.venueId }?.let { v ->
        buildString {
            append(v.name)
            v.city?.let { append(", $it") }
        }
    }
    val accentColor = if (event.bandId != null) ElectricCyan else NeonGreen

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        // Accent bar + title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text       = event.title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = OnSurface,
            )
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = SurfaceVariant)
        Spacer(Modifier.height(16.dp))

        // Date / time
        DetailRow(
            icon  = Icons.Default.ChevronRight, // repurposed as a clock-like icon
            label = if (event.isAllDay) {
                "${event.startTime.take(10)} · All day"
            } else {
                "${event.startTime.take(10)} · ${event.startTime.formatTime()} – ${event.endTime.formatTime()}"
            },
        )

        // Band
        bandName?.let {
            Spacer(Modifier.height(10.dp))
            DetailRow(icon = Icons.Default.MusicNote, label = it)
        }

        // Venue
        venueName?.let {
            Spacer(Modifier.height(10.dp))
            DetailRow(icon = Icons.Default.LocationOn, label = it)
        }

        // Freetext location (if different from venue)
        event.location?.let { loc ->
            if (venueName == null || loc.trim() != venueName.trim()) {
                Spacer(Modifier.height(10.dp))
                DetailRow(icon = Icons.Default.LocationOn, label = loc, tint = OnSurface.copy(alpha = 0.5f))
            }
        }

        // Description
        event.description?.let { desc ->
            Spacer(Modifier.height(10.dp))
            Text(
                text  = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface.copy(alpha = 0.7f),
            )
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick  = onDelete,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape    = MaterialTheme.shapes.small,
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFFF6B6B)),
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Delete Event", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color = OnSurface.copy(alpha = 0.7f),
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = tint)
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun CalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        enabled       = enabled,
        modifier      = modifier,
        shape         = MaterialTheme.shapes.small,
        colors        = calTextFieldColors(),
    )
}

@Composable
private fun calTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = ElectricPurple,
    focusedLabelColor    = ElectricPurple,
    unfocusedBorderColor = SurfaceVariant,
    unfocusedLabelColor  = OnSurface.copy(alpha = 0.5f),
    focusedTextColor     = OnSurface,
    unfocusedTextColor   = OnSurface,
    cursorColor          = ElectricPurple,
)

private fun Month.displayName(): String = when (this) {
    Month.JANUARY   -> "January"
    Month.FEBRUARY  -> "February"
    Month.MARCH     -> "March"
    Month.APRIL     -> "April"
    Month.MAY       -> "May"
    Month.JUNE      -> "June"
    Month.JULY      -> "July"
    Month.AUGUST    -> "August"
    Month.SEPTEMBER -> "September"
    Month.OCTOBER   -> "October"
    Month.NOVEMBER  -> "November"
    Month.DECEMBER  -> "December"
}

private fun LocalDate.toDayLabel(): String {
    val dow = when (dayOfWeek) {
        DayOfWeek.MONDAY    -> "Monday"
        DayOfWeek.TUESDAY   -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY  -> "Thursday"
        DayOfWeek.FRIDAY    -> "Friday"
        DayOfWeek.SATURDAY  -> "Saturday"
        DayOfWeek.SUNDAY    -> "Sunday"
    }
    return "$dow, ${month.displayName()} $dayOfMonth"
}

private fun String.formatTime(): String =
    if (length >= 16) substring(11, 16) else this

private fun daysInMonth(year: Int, month: Int): Int {
    val next = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
    return (next.toEpochDays() - LocalDate(year, month, 1).toEpochDays()).toInt()
}
