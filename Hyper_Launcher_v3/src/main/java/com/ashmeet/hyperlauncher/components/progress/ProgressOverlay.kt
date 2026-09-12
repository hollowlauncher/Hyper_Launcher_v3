package com.ashmeet.hyperlauncher.components.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A full-screen overlay with a circular progress indicator.
 */
@Composable
fun ProgressOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable(enabled = true, onClick = {}), // Consume clicks
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
