package com.twitterclone.feature.profile.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.twitterclone.feature.profile.R

/**
 * X-style follow button: filled white "Follow" when not following, outlined
 * "Following" once followed. Shared by the profile header and user rows.
 */
@Composable
fun FollowButton(
    followedByMe: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (followedByMe) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
        ) {
            Text(stringResource(R.string.follow_button_following))
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
        ) {
            Text(stringResource(R.string.follow_button_follow))
        }
    }
}
