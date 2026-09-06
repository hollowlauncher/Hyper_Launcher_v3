package com.ashmeet.hyperlauncher.screens.layouts.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.ashmeet.hyperlauncher.BuildConfig
import androidx.core.content.edit
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.CardPosition
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsCard
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsScreenWrapper
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.PreferenceCategory
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsActionItem
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsSwitchItem
import com.ashmeet.hyperlauncher.utils.translatedText

@Composable
fun DeveloperSettingsScreen(
    onBack: () -> Unit,
    onNavigateToPlugins: () -> Unit
) {
    var ditchHyperPlugin by remember { mutableStateOf(LauncherPreferences.PREF_DITCH_HYPER_PLUGIN) }

    SettingsScreenWrapper(
        title = translatedText("Developer options"),
        onBack = onBack,
        addTopGap = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Warning header
            SettingsCard(position = CardPosition.SINGLE, useSurface = true) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = translatedText("Warning: These settings are for developers only. Changing them may break the application or cause data loss."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            PreferenceCategory(title = translatedText("Development"))

            SettingsCard(position = CardPosition.TOP, useSurface = true) {
                SettingsActionItem(
                    title = translatedText("Plugins"),
                    summary = translatedText("Manage installed native plugins"),
                    icon = Icons.Default.Extension,
                    onClick = onNavigateToPlugins
                )
            }

            val packageName = BuildConfig.APPLICATION_ID
            val isDebugPackage = packageName.contains("debug", ignoreCase = true)
            if (isDebugPackage) {
                SettingsCard(position = CardPosition.MIDDLE, useSurface = true) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = translatedText("Warning: This is a debug package. Package name: $packageName"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            SettingsCard(position = CardPosition.BOTTOM, useSurface = true) {
                SettingsSwitchItem(
                    title = translatedText("Ditch Hyper Plugin"),
                    summary = translatedText("Disable all Hyper Plugin features completely. This may affect performance and stability."),
                    icon = Icons.Default.NoEncryption,
                    checked = ditchHyperPlugin,
                    onCheckedChange = {
                        ditchHyperPlugin = it
                        LauncherPreferences.PREF_DITCH_HYPER_PLUGIN = it
                        LauncherPreferences.prefs.edit { putBoolean("ditch_hyper_plugin", it) }
                    }
                )
            }
        }
    }
}
