package com.bandapa.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.bandapa.feature.auth.ui.LoginScreen
import com.bandapa.feature.auth.ui.SignUpScreen
import com.bandapa.feature.band.ui.BandDetailScreen
import com.bandapa.feature.band.ui.CreateBandScreen
import com.bandapa.feature.band.ui.JoinBandScreen
import com.bandapa.ui.MainScreen
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricPurple
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import org.koin.compose.koinInject

@Composable
fun BandapaNavHost() {
    val supabase       = koinInject<SupabaseClient>()
    val sessionStatus by supabase.auth.sessionStatus.collectAsState()

    if (sessionStatus is SessionStatus.Initializing) {
        SplashScreen()
        return
    }

    val navController = rememberNavController()
    val startDest = if (sessionStatus is SessionStatus.Authenticated)
        Route.Dashboard.path else Route.Login.path

    LaunchedEffect(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated    -> navController.navigate(Route.Dashboard.path) {
                popUpTo(0) { inclusive = true }
            }
            is SessionStatus.NotAuthenticated -> navController.navigate(Route.Login.path) {
                popUpTo(0) { inclusive = true }
            }
            else -> Unit
        }
    }

    NavHost(navController = navController, startDestination = startDest) {
        composable(Route.Login.path) {
            LoginScreen(onNavigateToSignUp = { navController.navigate(Route.SignUp.path) })
        }
        composable(Route.SignUp.path) {
            SignUpScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Route.Dashboard.path) {
            MainScreen(
                onNavigateToBandDetail = { bandId ->
                    navController.navigate("${Route.BandDetail.path}/$bandId")
                },
                onNavigateToCreateBand = { navController.navigate(Route.CreateBand.path) },
                onNavigateToJoinBand   = { navController.navigate(Route.JoinBand.path) },
            )
        }
        composable(Route.Venues.path) { PlaceholderScreen("Venues") }

        composable(Route.CreateBand.path) {
            CreateBandScreen(
                onNavigateBack = { navController.popBackStack() },
                onBandCreated  = {
                    navController.navigate(Route.Dashboard.path) {
                        popUpTo(Route.CreateBand.path) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route     = "${Route.JoinBand.path}?code={code}",
            arguments = listOf(navArgument("code") {
                type = NavType.StringType; defaultValue = ""; nullable = false
            }),
            deepLinks = listOf(navDeepLink { uriPattern = "https://bandapa.app/invite/{code}" }),
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            JoinBandScreen(
                initialCode    = code,
                onNavigateBack = { navController.popBackStack() },
                onJoined       = {
                    navController.navigate(Route.Dashboard.path) {
                        popUpTo(Route.JoinBand.path) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route     = "${Route.BandDetail.path}/{bandId}",
            arguments = listOf(navArgument("bandId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val bandId = backStackEntry.arguments?.getString("bandId") ?: return@composable
            BandDetailScreen(
                bandId          = bandId,
                onNavigateBack  = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier         = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = "bandapa",
            style      = MaterialTheme.typography.displayLarge,
            color      = ElectricPurple,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier         = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center,
    ) {
        Text(name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}
