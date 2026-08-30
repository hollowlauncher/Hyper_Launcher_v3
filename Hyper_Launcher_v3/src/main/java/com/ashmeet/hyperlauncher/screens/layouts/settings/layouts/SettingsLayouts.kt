package com.ashmeet.hyperlauncher.screens.layouts.settings.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.theme.PojavTheme

enum class CardPosition {
    TOP, MIDDLE, BOTTOM, SINGLE
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    position: CardPosition = CardPosition.SINGLE,
    outerShape: Dp = 20.dp,
    innerShape: Dp = 6.dp,
    useSurface: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val topRadius = if (position == CardPosition.TOP || position == CardPosition.SINGLE) outerShape else innerShape
    val bottomRadius = if (position == CardPosition.BOTTOM || position == CardPosition.SINGLE) outerShape else innerShape

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = topRadius,
            topEnd = topRadius,
            bottomStart = bottomRadius,
            bottomEnd = bottomRadius
        ),
        color = if (useSurface) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.background,
        content = {
            Column(content = content)
        }
    )
}

@Composable
fun TitleAndSummary(
    title: String,
    summary: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    summaryStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
    Column {
        Text(text = title, style = titleStyle)
        if (summary != null) {
            Text(
                text = summary,
                style = summaryStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun SettingsCardPreview() {
    PojavTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SettingsCard(position = CardPosition.TOP) {
                Text("Top Card (Background)", modifier = Modifier.padding(16.dp))
            }
            SettingsCard(position = CardPosition.MIDDLE) {
                Text("Middle Card (Surface)", modifier = Modifier.padding(16.dp))
            }
            SettingsCard(position = CardPosition.BOTTOM) {
                Text("Bottom Card", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
