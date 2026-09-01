package com.ashmeet.hyperlauncher.screens.layouts.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.CardPosition
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsCard
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsScreenWrapper
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.PreferenceCategory
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsActionItem
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsSwitchItem
import com.ashmeet.hyperlauncher.utils.translatedText
import net.ashmeet.hyperlauncher.R
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences

@Composable
fun MainSettingsScreen(
    onBack: () -> Unit,
    onNavigateToVideo: () -> Unit,
    onNavigateToControl: () -> Unit,
    onNavigateToJava: () -> Unit,
    onNavigateToMisc: () -> Unit,
    onNavigateToExperimental: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    showNotificationRequest: Boolean,
    onNotificationRequestClick: () -> Unit
) {
    var forceEnglish by remember { mutableStateOf(LauncherPreferences.PREF_FORCE_ENGLISH) }
    val context = LocalContext.current

    SettingsScreenWrapper(
        title = translatedText(stringResource(R.string.mcl_options)),
        onBack = null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SettingsCard(position = CardPosition.TOP, useSurface = true) {
                SettingsActionItem(
                    title = translatedText(stringResource(R.string.preference_video_title)),
                    summary = translatedText(stringResource(R.string.preference_video_description)),
                    icon = Icons.Default.Image,
                    onClick = onNavigateToVideo
                )
            }

            SettingsCard(position = CardPosition.MIDDLE, useSurface = true) {
                SettingsActionItem(
                    title = translatedText(stringResource(R.string.preference_control_title)),
                    summary = translatedText(stringResource(R.string.preference_control_description)),
                    icon = Icons.Default.VideogameAsset,
                    onClick = onNavigateToControl
                )
            }

            SettingsCard(position = CardPosition.MIDDLE, useSurface = true) {
                SettingsActionItem(
                    title = translatedText(stringResource(R.string.preference_java_title)),
                    summary = translatedText(stringResource(R.string.preference_java_description)),
                    icon = Icons.Default.Coffee,
                    onClick = onNavigateToJava
                )
            }

            SettingsCard(position = CardPosition.MIDDLE, useSurface = true) {
                SettingsActionItem(
                    title = translatedText(stringResource(R.string.preference_misc_title)),
                    summary = translatedText(stringResource(R.string.preference_misc_description)),
                    icon = Icons.Default.SettingsApplications,
                    onClick = onNavigateToMisc
                )
            }

            SettingsCard(position = CardPosition.MIDDLE, useSurface = true) {
                SettingsActionItem(
                    title = translatedText(stringResource(R.string.preference_experimental_title)),
                    summary = translatedText(stringResource(R.string.preference_experimental_description)),
                    icon = Icons.Default.Science,
                    onClick = onNavigateToExperimental
                )
            }

            SettingsCard(position = CardPosition.MIDDLE, useSurface = true) {
                SettingsActionItem(
                    title = translatedText("Plugins"),
                    summary = translatedText("Manage installed native plugins"),
                    icon = Icons.Default.Extension,
                    onClick = onNavigateToPlugins
                )
            }

            SettingsCard(position = CardPosition.BOTTOM, useSurface = true) {
                SettingsActionItem(
                    title = translatedText(stringResource(R.string.preference_appearance_title)),
                    summary = translatedText(stringResource(R.string.preference_appearance_description)),
                    icon = Icons.Default.Palette,
                    onClick = onNavigateToAppearance
                )
            }
        }

        PreferenceCategory(title = translatedText(stringResource(R.string.preference_category_miscellaneous)))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SettingsCard(position = if (showNotificationRequest) CardPosition.TOP else CardPosition.SINGLE, useSurface = true) {
                SettingsSwitchItem(
                    title = translatedText(stringResource(R.string.preference_force_english_title)),
                    summary = translatedText(stringResource(R.string.preference_force_english_description)),
                    icon = Icons.Default.Translate,
                    checked = forceEnglish,
                    onCheckedChange = {
                        forceEnglish = it
                        LauncherPreferences.prefs.edit { putBoolean("force_english", it) }
                        LauncherPreferences.loadPreferences(context)
                    }
                )
            }

            if (showNotificationRequest) {
                SettingsCard(position = CardPosition.BOTTOM, useSurface = true) {
                    SettingsActionItem(
                        title = translatedText(stringResource(R.string.preference_ask_for_notification_title)),
                        summary = translatedText(stringResource(R.string.preference_ask_for_notification_description)),
                        icon = Icons.Default.Notifications,
                        onClick = onNotificationRequestClick
                    )
                }
            }
        }
    }
}
