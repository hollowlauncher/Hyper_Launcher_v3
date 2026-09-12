package com.ashmeet.hyperlauncher.components.gamepad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.components.DefaultSwitch
import com.ashmeet.hyperlauncher.components.HyperSpinner
import net.ashmeet.hyperlauncher.R

@Composable
fun ControllerMappingItem(
    iconRes: Int,
    keyCodeLabel: String,
    currentKeys: List<String>, // Up to 4 keys
    isToggle: Boolean,
    onKeySelected: (Int, String) -> Unit,
    onToggleChanged: (Boolean) -> Unit,
    keyOptions: List<String>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Default View
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(55.dp)
                )

                Text(
                    text = keyCodeLabel,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Image(
                    painter = painterResource(id = R.drawable.spinner_arrow),
                    contentDescription = stringResource(id = R.string.controller_remapper_expand_entry),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isExpanded) 0f else 180f)
                )
            }
        }

        // Expanded View
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Key Spinners Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HyperSpinner(
                        options = keyOptions,
                        selectedOption = currentKeys.getOrElse(0) { "" },
                        onOptionSelected = { onKeySelected(0, it) },
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "+", color = MaterialTheme.colorScheme.onSurface)
                    HyperSpinner(
                        options = keyOptions,
                        selectedOption = currentKeys.getOrElse(1) { "" },
                        onOptionSelected = { onKeySelected(1, it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Key Spinners Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HyperSpinner(
                        options = keyOptions,
                        selectedOption = currentKeys.getOrElse(2) { "" },
                        onOptionSelected = { onKeySelected(2, it) },
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "+", color = MaterialTheme.colorScheme.onSurface)
                    HyperSpinner(
                        options = keyOptions,
                        selectedOption = currentKeys.getOrElse(3) { "" },
                        onOptionSelected = { onKeySelected(3, it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Toggle Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.customctrl_toggle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    DefaultSwitch(
                        checked = isToggle,
                        onCheckedChange = onToggleChanged
                    )
                }
            }
        }
    }
}
