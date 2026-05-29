package com.bandapa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bandapa.feature.band.ui.BandsScreen
import com.bandapa.feature.calendar.ui.CalendarScreen
import com.bandapa.feature.conflicts.ui.ConflictsScreen
import com.bandapa.feature.home.ui.HomeScreen
import com.bandapa.feature.profile.ui.ProfileViewModel
import com.bandapa.feature.venues.ui.VenuesScreen
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.ErrorRed
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

enum class MainTab(val label: String, val icon: ImageVector) {
    HOME    ("Home",     Icons.Default.Home),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    BANDS   ("Bands",    Icons.Default.Groups),
    VENUES  ("Venues",   Icons.Default.LocationOn),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToBandDetail: (bandId: String) -> Unit = {},
    onNavigateToCreateBand: () -> Unit = {},
    onNavigateToJoinBand:   () -> Unit = {},
    profileViewModel: ProfileViewModel = koinViewModel(),
) {
    var selectedTab  by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var profileMenuExpanded by remember { mutableStateOf(false) }
    var showConflicts by remember { mutableStateOf(false) }

    val profileState by profileViewModel.uiState.collectAsState()

    if (showConflicts) {
        ConflictsScreen(onGoToBands = { showConflicts = false })
        return
    }

    val initials = run {
        val src = profileState.profile.name ?: profileState.profile.email ?: "?"
        src.trim().split(" ").take(2)
            .joinToString("") { it.firstOrNull()?.uppercaseChar()?.toString() ?: "" }
            .ifEmpty { "?" }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "bandapa",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = ElectricPurple,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
                actions = {
                    Box {
                        IconButton(onClick = { profileMenuExpanded = true }) {
                            Box(
                                modifier         = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElectricPurple),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text       = initials,
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = OnAccent,
                                )
                            }
                        }

                        DropdownMenu(
                            expanded         = profileMenuExpanded,
                            onDismissRequest = { profileMenuExpanded = false },
                            containerColor   = SurfaceVariant,
                        ) {
                            // Header: name + email
                            val name  = profileState.profile.name?.ifBlank { null }
                            val email = profileState.profile.email
                            if (name != null) {
                                DropdownMenuItem(
                                    text    = { Text(name, fontWeight = FontWeight.Bold, color = OnSurface) },
                                    onClick = {},
                                    enabled = false,
                                )
                            }
                            if (email != null) {
                                DropdownMenuItem(
                                    text    = { Text(email, style = MaterialTheme.typography.bodySmall, color = OnSurface.copy(alpha = 0.5f)) },
                                    onClick = {},
                                    enabled = false,
                                )
                            }

                            HorizontalDivider(color = OnSurface.copy(alpha = 0.1f))

                            DropdownMenuItem(
                                text    = { Text("Sign Out", color = ErrorRed) },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint     = ErrorRed,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                onClick = {
                                    profileMenuExpanded = false
                                    profileViewModel.signOut()
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Surface) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick  = { selectedTab = tab },
                        icon     = { Icon(tab.icon, contentDescription = tab.label) },
                        label    = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = ElectricPurple,
                            selectedTextColor   = ElectricPurple,
                            indicatorColor      = ElectricPurple.copy(alpha = 0.15f),
                            unselectedIconColor = OnSurface.copy(alpha = 0.5f),
                            unselectedTextColor = OnSurface.copy(alpha = 0.5f),
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (selectedTab) {
                MainTab.HOME      -> HomeScreen()
                MainTab.CALENDAR  -> CalendarScreen()
                MainTab.BANDS  -> BandsScreen(
                    onNavigateToBandDetail = onNavigateToBandDetail,
                    onNavigateToCreateBand = onNavigateToCreateBand,
                    onNavigateToJoinBand   = onNavigateToJoinBand,
                    onNavigateToConflicts  = { showConflicts = true },
                )
                MainTab.VENUES -> VenuesScreen()
            }
        }
    }
}
