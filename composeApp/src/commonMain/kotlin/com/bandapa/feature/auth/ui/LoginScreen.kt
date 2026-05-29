package com.bandapa.feature.auth.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bandapa.feature.auth.domain.AuthUiState
import com.bandapa.feature.auth.ui.components.AuthTextField
import com.bandapa.feature.auth.ui.components.SocialButton
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.NeonGreen
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    var identifier      by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = uiState is AuthUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
            viewModel.clearError()
        }
    }

    val btnInteraction = remember { MutableInteractionSource() }
    val btnPressed     by btnInteraction.collectIsPressedAsState()
    val btnScale       by animateFloatAsState(
        targetValue   = if (btnPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 600f),
        label         = "loginBtnScale",
    )

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(72.dp))

            // ── Brand block ──────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ElectricPurple),
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text       = "bandapa",
                        style      = MaterialTheme.typography.displaySmall,
                        color      = OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = "your band's calendar, unified",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricPurple,
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
            Spacer(Modifier.height(28.dp))

            // ── Username or Email ─────────────────────────────────────────────────
            AuthTextField(
                value         = identifier,
                onValueChange = { identifier = it },
                label         = "Username or Email",
                keyboardType  = KeyboardType.Text,
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(14.dp))

            // ── Password ─────────────────────────────────────────────────────────
            AuthTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = "Password",
                keyboardType         = KeyboardType.Password,
                imeAction            = ImeAction.Done,
                onImeAction          = { viewModel.loginWithEmail(identifier, password) },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                enabled              = !isLoading,
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text  = if (passwordVisible) "Hide" else "Show",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricPurple,
                        )
                    }
                },
            )

            Spacer(Modifier.height(6.dp))
            TextButton(
                onClick  = { /* TODO: forgot password */ },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    "Forgot password?",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurface.copy(alpha = 0.4f),
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Log In button ─────────────────────────────────────────────────────
            Button(
                onClick           = { viewModel.loginWithEmail(identifier, password) },
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .scale(btnScale),
                enabled           = !isLoading && identifier.isNotBlank() && password.isNotBlank(),
                interactionSource = btnInteraction,
                shape             = MaterialTheme.shapes.small,
                colors            = ButtonDefaults.buttonColors(
                    containerColor         = ElectricPurple,
                    contentColor           = OnAccent,
                    disabledContainerColor = ElectricPurple.copy(alpha = 0.35f),
                    disabledContentColor   = OnAccent.copy(alpha = 0.5f),
                ),
            ) {
                Text(
                    text       = if (isLoading) "Logging in..." else "Log In",
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Divider ───────────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariant)
                Text(
                    text  = "  or continue with  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurface.copy(alpha = 0.3f),
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariant)
            }

            Spacer(Modifier.height(14.dp))

            // ── Social buttons ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SocialButton(label = "Google", modifier = Modifier.weight(1f), enabled = !isLoading, onClick = {})
                SocialButton(label = "Apple",  modifier = Modifier.weight(1f), enabled = !isLoading, onClick = {})
            }

            Spacer(Modifier.height(40.dp))

            // ── Sign up link ──────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    "New here?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.5f),
                )
                TextButton(onClick = onNavigateToSignUp) {
                    Text(
                        "Create account",
                        fontWeight = FontWeight.Bold,
                        color      = NeonGreen,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
