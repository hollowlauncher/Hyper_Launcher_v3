package com.ashmeet.hyperlauncher.screens.layouts.auth.methods

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.components.MineButton
import com.ashmeet.hyperlauncher.theme.PojavTheme
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import java.io.File
import java.io.FileOutputStream

@Composable
fun LocalLoginScreen(
    onLoginClick: (String, String?, String?) -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var selectedSkinPath by remember { mutableStateOf<String?>(null) }
    var selectedCapePath by remember { mutableStateOf<String?>(null) }

    val skinPickerLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentWithExtension("image/png")
    ) { uri ->
        uri?.let {
            val skinFile = File(context.cacheDir, "skin_import_temp_${System.currentTimeMillis()}.png")
            try {
                context.contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(skinFile).use { output ->
                        input.copyTo(output)
                    }
                }
                selectedSkinPath = skinFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val capePickerLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentWithExtension("image/png")
    ) { uri ->
        uri?.let {
            val capeFile = File(context.cacheDir, "cape_import_temp_${System.currentTimeMillis()}.png")
            try {
                context.contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(capeFile).use { output ->
                        input.copyTo(output)
                    }
                }
                selectedCapePath = capeFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(id = R.string.login_online_username_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Username") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Person, contentDescription = null)
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { skinPickerLauncher.launch(null) },
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f),
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (selectedSkinPath != null) Color(0xFF4CAF50).copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        contentColor = if (selectedSkinPath != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    if (selectedSkinPath != null) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = if (selectedSkinPath != null) "Skin selected" else "Change skin",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                FilledTonalButton(
                    onClick = { capePickerLauncher.launch(null) },
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f),
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (selectedCapePath != null) Color(0xFF4CAF50).copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        contentColor = if (selectedCapePath != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    if (selectedCapePath != null) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = if (selectedCapePath != null) "Cape selected" else "Change cape",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            MineButton(
                onClick = { onLoginClick(username, selectedSkinPath, selectedCapePath) },
                text = stringResource(id = R.string.login_online_login_label),
                modifier = Modifier.fillMaxWidth(),
                isUppercase = false
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=400dp,dpi=420",
)
@Composable
fun LocalLoginScreenPreview() {
    PojavTheme {
        LocalLoginScreen(onLoginClick = { _, _, _ -> })
    }
}
