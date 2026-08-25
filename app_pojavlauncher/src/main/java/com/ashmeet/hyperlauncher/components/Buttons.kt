package com.ashmeet.hyperlauncher.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    shape: Shape = CircleShape,
    isUppercase: Boolean = false
) {
    val isCustomTheme = remember { LauncherPreferences.PREF_CUSTOM_THEME }
    val primaryColor = MaterialTheme.colorScheme.primary
    val isLightMode = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val contentColor = if (isCustomTheme) {
        if (isLightMode) {
            Color(
                red = primaryColor.red * 0.3f,
                green = primaryColor.green * 0.3f,
                blue = primaryColor.blue * 0.3f,
                alpha = 1f
            )
        } else {
            if (primaryColor.luminance() > 0.5f) {
                Color(
                    red = primaryColor.red * 0.3f,
                    green = primaryColor.green * 0.3f,
                    blue = primaryColor.blue * 0.3f,
                    alpha = 1f
                )
            } else {
                Color(
                    red = primaryColor.red * 0.2f + 0.8f,
                    green = primaryColor.green * 0.2f + 0.8f,
                    blue = primaryColor.blue * 0.2f + 0.8f,
                    alpha = 1f
                )
            }
        }
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
    ) {
        Text(
            text = if (isUppercase) text.uppercase() else text,
            fontWeight = if (isCustomTheme) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SidebarRailButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    isExpanded: Boolean = false
) {
    val isCustomTheme = remember { LauncherPreferences.PREF_CUSTOM_THEME }
    val isLightMode = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val darkenedPrimary = Color(
        red = primaryColor.red * 0.3f,
        green = primaryColor.green * 0.3f,
        blue = primaryColor.blue * 0.3f,
        alpha = 1f
    )
    val lightenedPrimary = Color(
        red = primaryColor.red * 0.2f + 0.8f,
        green = primaryColor.green * 0.2f + 0.8f,
        blue = primaryColor.blue * 0.2f + 0.8f,
        alpha = 1f
    )
    
    val finalContentColor = if (isCustomTheme) {
        if (isLightMode) {
            if (containerColor == Color.Transparent) darkenedPrimary else contentColor
        } else {
            if (containerColor == Color.Transparent) lightenedPrimary else contentColor
        }
    } else {
        contentColor
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .then(if (isExpanded) Modifier.fillMaxWidth() else Modifier.size(56.dp))
            .wrapContentHeight()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = finalContentColor,
                modifier = Modifier.size(28.dp)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(200)) + expandHorizontally(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkHorizontally(animationSpec = tween(300))
            ) {
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = finalContentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
