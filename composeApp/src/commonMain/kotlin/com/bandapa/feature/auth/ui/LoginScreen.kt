package com.bandapa.feature.auth.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = uiState is AuthUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        containerColor  = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(72.dp))

            // ── Wordmark ────────────────────────────────────
            Text(
                text       = "bandapa",
                style      = MaterialTheme.typography.displayLarge,
                color      = ElectricPurple,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text  = "your band's calendar, unified",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface.copy(alpha = 0.55f),
            )

            Spacer(Modifier.height(56.dp))

            // ── Email ────────────────────────────────────────
            AuthTextField(
                value         = email,
                onValueChange = { email = it },
                label         = "Email",
                keyboardType  = KeyboardType.Email,
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(12.dp))

            // ── Password ─────────────────────────────────────
            AuthTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = "Password",
                keyboardType         = KeyboardType.Password,
                imeAction            = ImeAction.Done,
                onImeAction          = { viewModel.loginWithEmail(email, password) },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                enabled              = !isLoading,
                trailingIcon = {
                    TextButton(
                        onClick = { passwordVisible = !passwordVisible },
                    ) {
                        Text(
                            text  = if (passwordVisible) "Hide" else "Show",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricPurple,
                        )
                    }
                },
            )

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick  = { /* TODO: forgot password */ },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    "Forgot password?",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurface.copy(alpha = 0.5f),
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Log In button ────────────────────────────────
            Button(
                onClick  = { viewModel.loginWithEmail(email, password) },
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
                    Text("Log In", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Divider ──────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariant)
                Text(
                    text  = "  or continue with  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurface.copy(alpha = 0.35f),
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            // ── Social buttons ───────────────────────────────
            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SocialButton(
                    label    = "Google",
                    modifier = Modifier.weight(1f),
                    enabled  = !isLoading,
                    onClick  = { /* TODO: Google OAuth — Phase 2 */ },
                )
                SocialButton(
                    label    = "Apple",
                    modifier = Modifier.weight(1f),
                    enabled  = !isLoading,
                    onClick  = { /* TODO: Apple OAuth — Phase 2 */ },
                )
            }

            Spacer(Modifier.height(48.dp))

            // ── Sign up link ─────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    "New here?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.55f),
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
