package com.bandapa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.bandapa.feature.band.ui.BandsScreen
import com.bandapa.feature.calendar.ui.CalendarScreen
import com.bandapa.feature.conflicts.ui.ConflictsScreen
import com.bandapa.feature.profile.ui.ProfileScreen
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.Surface

enum class MainTab(val label: String, val icon: ImageVector) {
    CALENDAR ("Calendar",  Icons.Default.CalendarMonth),
    BANDS    ("Bands",     Icons.Default.Groups),
    CONFLICTS("Conflicts", Icons.Default.Warning),
    PROFILE  ("Profile",   Icons.Default.Person),
}

@Composable
fun MainScreen(
    onNavigateToBandDetail: (bandId: String) -> Unit = {},
    onNavigateToCreateBand: () -> Unit = {},
    onNavigateToJoinBand:   () -> Unit = {},
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.CALENDAR) }

    Scaffold(
        containerColor = Background,
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
        when (selectedTab) {
            MainTab.CALENDAR  -> CalendarScreen(modifier = Modifier.padding(padding))
            MainTab.BANDS     -> BandsScreen(
                modifier               = Modifier.padding(padding),
                onNavigateToBandDetail = onNavigateToBandDetail,
                onNavigateToCreateBand = onNavigateToCreateBand,
                onNavigateToJoinBand   = onNavigateToJoinBand,
            )
            MainTab.CONFLICTS -> ConflictsScreen(modifier = Modifier.padding(padding))
            MainTab.PROFILE   -> ProfileScreen(modifier = Modifier.padding(padding))
        }
    }
}
