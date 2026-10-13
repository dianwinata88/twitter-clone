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
 * X Dark registration screen. Fields validate inline via [AuthValidation];
 * the submit button stays disabled until every field passes. On success the
 * caller navigates to the feed, clearing the auth back stack.
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegistered: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var username by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var submittedOnce by rememberSaveable { mutableStateOf(false) }

    val usernameValid = AuthValidation.isUsernameValid(username)
    val displayNameValid = AuthValidation.isDisplayNameValid(displayName)
    val passwordValid = AuthValidation.isPasswordValid(password)
    val formValid = usernameValid && displayNameValid && passwordValid

    LaunchedEffect(uiState.success) {
        if (uiState.success) onRegistered()
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
            text = stringResource(R.string.auth_register_title),
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
            errorRes =
                if (submittedOnce && !usernameValid) {
                    R.string.auth_username_invalid
                } else {
                    null
                },
            keyboardType = KeyboardType.Ascii,
        )
        Spacer(Modifier.height(16.dp))
        AuthTextField(
            value = displayName,
            onValueChange = {
                displayName = it
                viewModel.clearError()
            },
            labelRes = R.string.auth_display_name,
            errorRes =
                if (submittedOnce && !displayNameValid) {
                    R.string.auth_display_name_invalid
                } else {
                    null
                },
        )
        Spacer(Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            labelRes = R.string.auth_password,
            errorRes =
                if (submittedOnce && !passwordValid) {
                    R.string.auth_password_invalid
                } else {
                    null
                },
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
            labelRes = R.string.auth_create_account,
            onClick = {
                submittedOnce = true
                if (formValid) {
                    viewModel.register(username, displayName, password)
                }
            },
            enabled = formValid,
            loading = uiState.loading,
        )

        Spacer(Modifier.height(48.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.auth_have_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = stringResource(R.string.auth_sign_in),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
