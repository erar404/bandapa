package com.bandapa.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bandapa.feature.auth.domain.AuthUiState
import com.bandapa.feature.auth.ui.components.AuthTextField
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricCyan
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.NeonGreen
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username        by remember { mutableStateOf("") }
    var firstName       by remember { mutableStateOf("") }
    var lastName        by remember { mutableStateOf("") }
    var contactNumber   by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = uiState is AuthUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
    ) { innerPadding ->

        // ── Confirmation pending state ───────────────────────────
        if (uiState is AuthUiState.EmailConfirmationPending) {
            Column(
                modifier              = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp),
                verticalArrangement   = Arrangement.Center,
                horizontalAlignment   = Alignment.CenterHorizontally,
            ) {
                Text(
                    text      = "Check your inbox",
                    style     = MaterialTheme.typography.headlineMedium,
                    color     = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text      = "We sent a confirmation link to $email.\nClick it to activate your account, then log in.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = OnSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(40.dp))
                Button(
                    onClick  = {
                        viewModel.resetState()
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = MaterialTheme.shapes.small,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = ElectricPurple,
                        contentColor   = OnAccent,
                    ),
                ) {
                    Text("Back to Login", fontWeight = FontWeight.Bold)
                }
            }
            return@Scaffold
        }

        // ── Registration form ────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(24.dp))

            TextButton(
                onClick  = onNavigateBack,
                modifier = Modifier.padding(start = 0.dp),
            ) {
                Text(
                    "← Back",
                    color = OnSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text       = "Create your account",
                style      = MaterialTheme.typography.headlineLarge,
                color      = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text  = "Join the rhythm",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface.copy(alpha = 0.55f),
            )

            Spacer(Modifier.height(32.dp))

            // ── Email + password ─────────────────────────────
            AuthTextField(
                value         = email,
                onValueChange = { email = it },
                label         = "Email",
                keyboardType  = KeyboardType.Email,
                enabled       = !isLoading,
            )
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value                = password,
                onValueChange        = { password = it },
                label                = "Password",
                keyboardType         = KeyboardType.Password,
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
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value                = confirmPassword,
                onValueChange        = { confirmPassword = it },
                label                = "Confirm password",
                keyboardType         = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                enabled              = !isLoading,
            )

            Spacer(Modifier.height(24.dp))

            // ── Profile fields ───────────────────────────────
            AuthTextField(
                value         = username,
                onValueChange = { username = it },
                label         = "Username",
                enabled       = !isLoading,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuthTextField(
                    value         = firstName,
                    onValueChange = { firstName = it },
                    label         = "First name",
                    modifier      = Modifier.weight(1f),
                    enabled       = !isLoading,
                )
                AuthTextField(
                    value         = lastName,
                    onValueChange = { lastName = it },
                    label         = "Last name",
                    modifier      = Modifier.weight(1f),
                    enabled       = !isLoading,
                )
            }
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value         = contactNumber,
                onValueChange = { contactNumber = it },
                label         = "Contact number (optional)",
                keyboardType  = KeyboardType.Phone,
                imeAction     = ImeAction.Done,
                onImeAction   = {
                    viewModel.signUp(
                        email, password, confirmPassword,
                        username, firstName, lastName, contactNumber,
                    )
                },
                enabled = !isLoading,
            )

            Spacer(Modifier.height(32.dp))

            // ── Create Account button ────────────────────────
            Button(
                onClick = {
                    viewModel.signUp(
                        email, password, confirmPassword,
                        username, firstName, lastName, contactNumber,
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !isLoading,
                shape    = MaterialTheme.shapes.small,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = NeonGreen,
                    contentColor           = OnAccent,
                    disabledContainerColor = NeonGreen.copy(alpha = 0.4f),
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
                    Text("Create Account", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Login link ───────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.55f),
                )
                TextButton(onClick = onNavigateBack) {
                    Text(
                        "Log in",
                        fontWeight = FontWeight.Bold,
                        color      = ElectricPurple,
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
