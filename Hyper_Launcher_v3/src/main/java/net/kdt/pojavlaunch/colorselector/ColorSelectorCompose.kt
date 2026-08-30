package net.kdt.pojavlaunch.colorselector

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.colorpicker.rememberColorPickerController
import com.ashmeet.hyperlauncher.colorpicker.components.VerticalAlphaBarPicker
import com.ashmeet.hyperlauncher.colorpicker.components.ColorSquarePicker
import com.ashmeet.hyperlauncher.colorpicker.components.VerticalHueBarPicker
import com.ashmeet.hyperlauncher.colorpicker.components.TransparentChecker

@Composable
fun ColorSelectorContent(
    initialColor: Int,
    alphaEnabled: Boolean,
    onColorChanged: (Int) -> Unit,
    onClose: () -> Unit
) {
    val controller = rememberColorPickerController(initialColor = Color(initialColor))
    val currentColor by controller.color

    LaunchedEffect(currentColor) {
        onColorChanged(currentColor.toArgb())
    }

    BackHandler {
        onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Main Row: Square Picker and Sliders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSquarePicker(
                controller = controller,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
            )

            // Vertical Hue Picker
            VerticalHueBarPicker(
                controller = controller,
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
            )

            if (alphaEnabled) {
                // Vertical Alpha Picker
                VerticalAlphaBarPicker(
                    controller = controller,
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        // Bottom Row: Preview and HEX
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                TransparentChecker(modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(currentColor)
                )
            }

            var hexText by remember(currentColor) { 
                mutableStateOf(String.format("%08X", currentColor.toArgb())) 
            }

            OutlinedTextField(
                value = hexText,
                onValueChange = { hex ->
                    hexText = hex
                    try {
                        if (hex.length == 8) {
                            val colorInt = android.graphics.Color.parseColor("#$hex")
                            controller.setColor(Color(colorInt))
                        }
                    } catch (e: Exception) {
                        // Ignore invalid hex
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text("HEX") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}
