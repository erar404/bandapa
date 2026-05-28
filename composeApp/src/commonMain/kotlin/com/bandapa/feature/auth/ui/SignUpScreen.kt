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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bandapa.feature.auth.domain.AuthUiState
import com.bandapa.feature.auth.ui.components.AuthTextField
import com.bandapa.ui.theme.Background
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.Surface
import com.bandapa.ui.theme.SurfaceVariant
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

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

    val btnInteraction = remember { MutableInteractionSource() }
    val btnPressed     by btnInteraction.collectIsPressedAsState()
    val btnScale       by animateFloatAsState(
        targetValue   = if (btnPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 600f),
        label         = "signupBtnScale",
    )

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
    ) { innerPadding ->

        // ── Email confirmation pending ───────────────────────────────────────────
        if (uiState is AuthUiState.EmailConfirmationPending) {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier         = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ElectricPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Default.Email,
                        contentDescription = null,
                        tint               = ElectricPurple,
                        modifier           = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text       = "Check your inbox",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text      = "We sent a confirmation link to\n$email\nClick it to activate your account.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = OnSurface.copy(alpha = 0.6f),
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

        // ── Registration form ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Back button ──────────────────────────────────────────────────────
            IconButton(
                onClick  = onNavigateBack,
                modifier = Modifier.padding(start = 0.dp),
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = OnSurface.copy(alpha = 0.7f),
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Brand block ──────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ElectricPurple),
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text       = "Create your account",
                        style      = MaterialTheme.typography.headlineLarge,
                        color      = OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = "Join the lineup.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricPurple,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = SurfaceVariant, thickness = 1.dp)
            Spacer(Modifier.height(24.dp))

            // ── Section: Account ─────────────────────────────────────────────────
            Text(
                text          = "ACCOUNT",
                style         = MaterialTheme.typography.labelSmall,
                color         = OnSurface.copy(alpha = 0.4f),
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.height(10.dp))

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

            // ── Section: Profile ─────────────────────────────────────────────────
            Text(
                text          = "PROFILE",
                style         = MaterialTheme.typography.labelSmall,
                color         = OnSurface.copy(alpha = 0.4f),
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.height(10.dp))

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
                    viewModel.signUp(email, password, confirmPassword, username, firstName, lastName, contactNumber)
                },
                enabled = !isLoading,
            )

            Spacer(Modifier.height(32.dp))

            // ── Create Account button ────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.signUp(email, password, confirmPassword, username, firstName, lastName, contactNumber)
                },
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .scale(btnScale),
                enabled           = !isLoading,
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
                    text       = if (isLoading) "Creating account..." else "Create Account",
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Login link ───────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.5f),
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
