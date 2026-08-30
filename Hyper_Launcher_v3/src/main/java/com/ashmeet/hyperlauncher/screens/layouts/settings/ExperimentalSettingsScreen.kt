package com.ashmeet.hyperlauncher.screens.layouts.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.core.content.edit
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.CardPosition
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsCard
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsScreenWrapper
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsSwitchItem
import net.ashmeet.hyperlauncher.R
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences

@Composable
fun ExperimentalSettingsScreen(
    onBack: () -> Unit,
    isFreedrenoAvailable: Boolean
) {
    val context = LocalContext.current
    var dumpShaders by remember { mutableStateOf(LauncherPreferences.PREF_DUMP_SHADERS) }
    var bigCoreAffinity by remember { mutableStateOf(LauncherPreferences.PREF_BIG_CORE_AFFINITY) }
    var freedrenoSysmem by remember { mutableStateOf(LauncherPreferences.PREF_FREEDRENO_SYSMEM) }
    var alsoftForceOpenSL by remember { mutableStateOf(LauncherPreferences.PREF_ALSOFT_FORCE_OPENSL) }

    SettingsScreenWrapper(
        title = stringResource(R.string.preference_experimental_title),
        onBack = onBack,
        addTopGap = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val items = mutableListOf<@Composable () -> Unit>()

            items.add {
                SettingsSwitchItem(
                    title = stringResource(R.string.preference_shader_dump_title),
                    summary = stringResource(R.string.preference_shader_dump_description),
                    icon = Icons.Default.Settings,
                    checked = dumpShaders,
                    warningTooltip = "Experimental: Shader dumping can significantly impact performance and fill up storage space.",
                    onCheckedChange = {
                        dumpShaders = it
                        LauncherPreferences.prefs.edit { putBoolean("dump_shaders", it) }
                        LauncherPreferences.loadPreferences(context)
                    }
                )
            }

            items.add {
                SettingsSwitchItem(
                    title = stringResource(R.string.preference_force_big_core_title),
                    summary = stringResource(R.string.preference_force_big_core_desc),
                    icon = Icons.Default.Settings,
                    checked = bigCoreAffinity,
                    warningTooltip = "Experimental: Forcing big core affinity may lead to increased heat and battery drain.",
                    onCheckedChange = {
                        bigCoreAffinity = it
                        LauncherPreferences.prefs.edit { putBoolean("bigCoreAffinity", it) }
                        LauncherPreferences.loadPreferences(context)
                    }
                )
            }

            if (isFreedrenoAvailable) {
                items.add {
                    SettingsSwitchItem(
                        title = stringResource(R.string.preference_sysmem_title),
                        summary = stringResource(R.string.preference_sysmem_summary),
                        icon = Icons.Default.Settings,
                        checked = freedrenoSysmem,
                        warningTooltip = "Experimental: Freedreno System Memory rendering can be unstable and cause crashes on certain drivers.",
                        onCheckedChange = {
                            freedrenoSysmem = it
                            LauncherPreferences.prefs.edit { putBoolean("freedrenoSysmem", it) }
                            LauncherPreferences.loadPreferences(context)
                        }
                    )
                }
            }

            items.add {
                SettingsSwitchItem(
                    title = stringResource(R.string.preference_alsoft_opensl_title),
                    summary = stringResource(R.string.preference_alsoft_opensl_summary),
                    icon = ImageVector.vectorResource(R.drawable.ic_px_dynamic),
                    checked = alsoftForceOpenSL,
                    warningTooltip = "Experimental: Forcing OpenSL may solve audio issues on some devices but could cause latency or crashes on others.",
                    onCheckedChange = {
                        alsoftForceOpenSL = it
                        LauncherPreferences.prefs.edit { putBoolean("alsoftForceOpenSL", it) }
                        LauncherPreferences.loadPreferences(context)
                    }
                )
            }

            items.forEachIndexed { index, content ->
                val position = when {
                    items.size == 1 -> CardPosition.SINGLE
                    index == 0 -> CardPosition.TOP
                    index == items.size - 1 -> CardPosition.BOTTOM
                    else -> CardPosition.MIDDLE
                }
                SettingsCard(position = position, useSurface = true) {
                    content()
                }
            }
        }
    }
}
