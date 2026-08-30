package com.ashmeet.hyperlauncher.screens.layouts.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.CardPosition
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsCard
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsScreenWrapper
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.PreferenceCategory
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsActionItem
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsSwitchItem
import net.kdt.pojavlaunch.plugins.NativePluginManager
import net.kdt.pojavlaunch.utils.RendererCompatUtil

@Composable
fun PluginSettingsScreen(
    onBack: () -> Unit
) {
    val discoveredLibs = remember { NativePluginManager.getDiscoveredLibraries() }
    val isPluginInstalled = discoveredLibs.isNotEmpty()

    SettingsScreenWrapper(
        title = "Plugins",
        onBack = onBack,
        addTopGap = true
    ) {
        if (!isPluginInstalled) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SettingsCard(position = CardPosition.SINGLE, useSurface = true) {
                    SettingsActionItem(
                        title = "No plugins detected",
                        summary = "Install the HyperPlugin bundle to enable additional features.",
                        icon = Icons.Default.Info,
                        onClick = {}
                    )
                }
            }
        } else {
            PreferenceCategory(title = "Discovered Libraries")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                discoveredLibs.forEachIndexed { index, lib ->
                    val position = when {
                        discoveredLibs.size == 1 -> CardPosition.SINGLE
                        index == 0 -> CardPosition.TOP
                        index == discoveredLibs.size - 1 -> CardPosition.BOTTOM
                        else -> CardPosition.MIDDLE
                    }

                    SettingsCard(position = position, useSurface = true) {
                        if (lib.optional) {
                            var enabled by remember(lib.name) { mutableStateOf(LauncherPreferences.isPluginLibraryEnabled(lib.name)) }
                            SettingsSwitchItem(
                                title = lib.name,
                                summary = "Version: ${lib.version} | Strategy: ${lib.strategy}",
                                icon = Icons.Default.Extension,
                                checked = enabled,
                                onCheckedChange = {
                                    enabled = it
                                    LauncherPreferences.setPluginLibraryEnabled(lib.name, it)
                                    // Refresh renderer list instantly if needed
                                    RendererCompatUtil.releaseRenderersCache()
                                }
                            )
                        } else {
                            SettingsActionItem(
                                title = lib.name,
                                summary = "Version: ${lib.version} | Strategy: ${lib.strategy} (Required)",
                                icon = Icons.Default.Extension,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
