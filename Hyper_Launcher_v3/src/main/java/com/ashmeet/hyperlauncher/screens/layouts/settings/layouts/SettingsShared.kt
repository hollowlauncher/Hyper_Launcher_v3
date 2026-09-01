package com.ashmeet.hyperlauncher.screens.layouts.settings.layouts

import androidx.compose.foundation.layout.Arrangement
import com.ashmeet.hyperlauncher.utils.translatedText
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.theme.PojavTheme
import kotlin.math.max

@Composable
fun SettingsScreenWrapper(
    title: String,
    onBack: (() -> Unit)? = null,
    addTopGap: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val backButtonAlpha by remember {
        derivedStateOf {
            val alpha = 1f - (scrollState.value / 200f)
            max(0.2f, alpha)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (addTopGap) {
                    Spacer(modifier = Modifier.height(53.dp))
                }
                content()
            }

            if (onBack != null) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .graphicsLayer {
                            alpha = backButtonAlpha
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f * backButtonAlpha),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = translatedText("Back"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenWrapperPreview() {
    PojavTheme {
        SettingsScreenWrapper(
            title = translatedText("Preview Title"),
            onBack = {}
        ) {
            Text(
                text = translatedText("Content item 1"),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = translatedText("Content item 2"),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
