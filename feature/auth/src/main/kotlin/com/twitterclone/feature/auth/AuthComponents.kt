package com.twitterclone.feature.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/** Centered X wordmark used at the top of the auth screens. */
@Composable
internal fun AuthWordmark(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.auth_wordmark),
        modifier = modifier,
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

/** Shared outlined field styling for the auth forms. */
@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    @StringRes errorRes: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(labelRes)) },
        isError = errorRes != null,
        supportingText = errorRes?.let { { Text(stringResource(it)) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation =
            if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
        trailingIcon =
            if (isPassword && onTogglePasswordVisibility != null) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector =
                                if (passwordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                            contentDescription =
                                stringResource(
                                    if (passwordVisible) {
                                        R.string.auth_hide_password
                                    } else {
                                        R.string.auth_show_password
                                    },
                                ),
                        )
                    }
                }
            } else {
                null
            },
    )
}

/** Full-width primary action button with a loading spinner swap. */
@Composable
internal fun AuthPrimaryButton(
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(52.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/** Inline error line shown under the form on a failed submit. */
@Composable
internal fun AuthErrorText(
    message: String?,
    modifier: Modifier = Modifier,
) {
    if (message != null) {
        Column(modifier = modifier.fillMaxWidth()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
