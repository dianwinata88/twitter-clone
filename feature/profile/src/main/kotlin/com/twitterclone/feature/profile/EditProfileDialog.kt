package com.twitterclone.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Edit-profile dialog: display name + bio, saved via
 * `ProfileViewModel.updateProfile`. Field state is hoisted locally; the
 * caller dismisses it on `ProfileEvent.ProfileSaved`.
 */
@Composable
fun EditProfileDialog(
    initialDisplayName: String,
    initialBio: String,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String) -> Unit,
) {
    var displayName by rememberSaveable { mutableStateOf(initialDisplayName) }
    var bio by rememberSaveable { mutableStateOf(initialBio) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(stringResource(R.string.edit_profile_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(stringResource(R.string.edit_profile_name)) },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(stringResource(R.string.edit_profile_bio)) },
                    minLines = 3,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(displayName, bio) },
                enabled = displayName.isNotBlank() && !saving,
            ) {
                if (saving) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.edit_profile_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text(stringResource(R.string.edit_profile_cancel))
            }
        },
    )
}
