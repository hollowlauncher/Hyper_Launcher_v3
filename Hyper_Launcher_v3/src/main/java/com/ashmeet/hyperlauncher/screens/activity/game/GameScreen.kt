package com.ashmeet.hyperlauncher.screens.activity.game

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.utils.LoggerProxy
import net.kdt.pojavlaunch.Logger

@Composable
fun GameBasemainScreen(
    instanceName: String,
    content: @Composable BoxScope.() -> Unit
) {
    var isSplashVisible by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = Logger.eventLogListener { text ->
            if (text != null && (
                text.contains("[main/INFO]: OpenAL initialized") || 
                text.contains("[main/INFO]: Sound engine started") ||
                text.contains("Backend library:") ||
                text.contains("Setting user:") ||
                text.contains("Created: ")
            )) {
                isSplashVisible = false
            }
        }
        LoggerProxy.addListener(listener)
        onDispose {
            LoggerProxy.removeListener(listener)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AnimatedVisibility(
            visible = isSplashVisible,
            exit = fadeOut(animationSpec = tween(800))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        content()
    }
}
