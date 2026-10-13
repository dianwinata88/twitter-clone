package com.twitterclone.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * X Dark sign-in screen. The demo account (`demo`/`demo`) is prefilled so the
 * fake backend works out of the box. On success the caller navigates to the
 * feed, clearing the auth back stack.
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var username by rememberSaveable { mutableStateOf("demo") }
    var password by rememberSaveable { mutableStateOf("demo") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) onLoggedIn()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))
        AuthWordmark()
        Spacer(Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.auth_login_title),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(32.dp))

        AuthTextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.clearError()
            },
            labelRes = R.string.auth_username,
            keyboardType = KeyboardType.Ascii,
        )
        Spacer(Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            labelRes = R.string.auth_password,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
        )

        AuthErrorText(
            message = uiState.error?.ifBlank { stringResource(R.string.auth_error_generic) },
        )

        Spacer(Modifier.height(24.dp))
        AuthPrimaryButton(
            labelRes = R.string.auth_sign_in,
            onClick = { viewModel.login(username, password) },
            enabled = username.isNotBlank() && password.isNotBlank(),
            loading = uiState.loading,
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.auth_demo_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(48.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.auth_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = stringResource(R.string.auth_sign_up),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
