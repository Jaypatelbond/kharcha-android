package com.kharcha.tracker.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AdFreeOfferDialog(
    onDismiss: () -> Unit,
    onWatchAd: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Ads for 24 Hours?") },
        text = { Text("Watch a short video to support the app and enjoy an ad-free experience for the next 24 hours!") },
        confirmButton = {
            Button(onClick = onWatchAd) {
                Text("Watch Video")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No Thanks")
            }
        }
    )
}
