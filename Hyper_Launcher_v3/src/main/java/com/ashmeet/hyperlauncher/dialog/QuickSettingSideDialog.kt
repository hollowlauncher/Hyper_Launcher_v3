package com.ashmeet.hyperlauncher.dialog

import com.ashmeet.hyperlauncher.utils.translatedText

import android.content.Context
import android.content.SharedPreferences
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.CardPosition
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsCard
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsSliderItem
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsSwitchItem
import com.ashmeet.hyperlauncher.theme.PojavTheme
import com.kdt.SideDialogView
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.Tools

/**
 * Side dialog for quick settings that you can change in game
 * The implementation has to take action on some preference changes
 */
abstract class QuickSettingSideDialog(context: Context, parent: ViewGroup) :
    SideDialogView(context, parent, R.layout.dialog_compose) {

    private var mEditor: SharedPreferences.Editor? = null

    private var mOriginalGyroEnabled = false
    private var mOriginalGyroXEnabled = false
    private var mOriginalGyroYEnabled = false
    private var mOriginalGestureDisabled = false

    private var mOriginalGyroSensitivity = 0f
    private var mOriginalMouseSpeed = 0f
    private var mOriginalResolution = 0f
    private var mOriginalGestureDelay = 0

    init {
        setTitle(R.string.quick_setting_title)
        setupCancelButton()
    }

    override fun onInflate() {
        mEditor = LauncherPreferences.prefs.edit()

        mOriginalGyroEnabled = LauncherPreferences.PREF_ENABLE_GYRO
        mOriginalGyroXEnabled = LauncherPreferences.PREF_GYRO_INVERT_X
        mOriginalGyroYEnabled = LauncherPreferences.PREF_GYRO_INVERT_Y
        mOriginalGestureDisabled = LauncherPreferences.PREF_DISABLE_GESTURES

        mOriginalGyroSensitivity = LauncherPreferences.PREF_GYRO_SENSITIVITY
        mOriginalMouseSpeed = LauncherPreferences.PREF_MOUSESPEED
        mOriginalGestureDelay = LauncherPreferences.PREF_LONGPRESS_TRIGGER
        mOriginalResolution = LauncherPreferences.PREF_SCALE_FACTOR

        val composeView = mDialogContent.findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            PojavTheme {
                Surface(
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    QuickSettingContent(
                        onResolutionChanged = { onResolutionChanged() },
                        onGyroStateChanged = { onGyroStateChanged() }
                    ) { key, value ->
                        when (value) {
                            is Boolean -> mEditor?.putBoolean(key, value)
                            is Int -> mEditor?.putInt(key, value)
                            is Float -> mEditor?.putFloat(key, value)
                        }
                    }
                }
            }
        }
    }

    private fun setupCancelButton() {
        setStartButtonListener(android.R.string.cancel) { cancel() }
        setEndButtonListener(android.R.string.ok) {
            mEditor?.apply()
            disappear(true)
        }
    }

    /** Resets all settings to their original values */
    fun cancel() {
        if (isDisplaying) {
            LauncherPreferences.PREF_ENABLE_GYRO = mOriginalGyroEnabled
            LauncherPreferences.PREF_GYRO_INVERT_X = mOriginalGyroXEnabled
            LauncherPreferences.PREF_GYRO_INVERT_Y = mOriginalGyroYEnabled
            LauncherPreferences.PREF_DISABLE_GESTURES = mOriginalGestureDisabled
            LauncherPreferences.PREF_GYRO_SENSITIVITY = mOriginalGyroSensitivity
            LauncherPreferences.PREF_MOUSESPEED = mOriginalMouseSpeed
            LauncherPreferences.PREF_LONGPRESS_TRIGGER = mOriginalGestureDelay
            LauncherPreferences.PREF_SCALE_FACTOR = mOriginalResolution
            onGyroStateChanged()
            onResolutionChanged()
        }
        disappear(true)
    }

    /** Called when the resolution is changed. Use [LauncherPreferences.PREF_SCALE_FACTOR] */
    abstract fun onResolutionChanged()

    /** Called when the gyro state is changed.
     * Use [LauncherPreferences.PREF_ENABLE_GYRO]
     * Use [LauncherPreferences.PREF_GYRO_INVERT_X]
     * Use [LauncherPreferences.PREF_GYRO_INVERT_Y]
     */
    abstract fun onGyroStateChanged()
}

@Composable
private fun QuickSettingContent(
    onResolutionChanged: () -> Unit,
    onGyroStateChanged: () -> Unit,
    onPreferenceChanged: (String, Any) -> Unit
) {
    val context = LocalContext.current
    var enableGyro by remember { mutableStateOf(LauncherPreferences.PREF_ENABLE_GYRO) }
    var gyroInvertX by remember { mutableStateOf(LauncherPreferences.PREF_GYRO_INVERT_X) }
    var gyroInvertY by remember { mutableStateOf(LauncherPreferences.PREF_GYRO_INVERT_Y) }
    var gyroSensitivity by remember { mutableFloatStateOf(LauncherPreferences.PREF_GYRO_SENSITIVITY * 100f) }

    var mouseSpeed by remember { mutableFloatStateOf(LauncherPreferences.PREF_MOUSESPEED * 100f) }

    var disableGestures by remember { mutableStateOf(LauncherPreferences.PREF_DISABLE_GESTURES) }
    var gestureDelay by remember { mutableFloatStateOf(LauncherPreferences.PREF_LONGPRESS_TRIGGER.toFloat()) }

    var resolutionScaler by remember { mutableFloatStateOf(LauncherPreferences.PREF_SCALE_FACTOR * 100f) }

    val isGyroAvailable = remember { Tools.deviceSupportsGyro(context) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        SettingsSliderItem(
            title = translatedText(stringResource(R.string.mcl_setting_title_resolution_scaler)),
            value = resolutionScaler,
            valueRange = 25f..100f,
            valueSuffix = "%",
            onValueChange = {
                resolutionScaler = it
                LauncherPreferences.PREF_SCALE_FACTOR = it / 100f
                onPreferenceChanged("resolutionRatio", it.toInt())
                onResolutionChanged()
            }
        )

        if (isGyroAvailable) {
            SettingsCard(position = CardPosition.TOP, useSurface = true) {
                SettingsSwitchItem(
                    title = translatedText(stringResource(R.string.preference_enable_gyro_title)),
                    checked = enableGyro,
                    onCheckedChange = {
                        enableGyro = it
                        LauncherPreferences.PREF_ENABLE_GYRO = it
                        onPreferenceChanged("enableGyro", it)
                        onGyroStateChanged()
                    }
                )
            }

            if (enableGyro) {
                SettingsCard(position = CardPosition.MIDDLE, useSurface = true) {
                    SettingsSwitchItem(
                        title = translatedText(stringResource(R.string.preference_gyro_invert_x_axis)),
                        checked = gyroInvertX,
                        onCheckedChange = {
                            gyroInvertX = it
                            LauncherPreferences.PREF_GYRO_INVERT_X = it
                            onPreferenceChanged("gyroInvertX", it)
                            onGyroStateChanged()
                        }
                    )
                }
                SettingsCard(position = CardPosition.BOTTOM, useSurface = true) {
                    SettingsSwitchItem(
                        title = translatedText(stringResource(R.string.preference_gyro_invert_y_axis)),
                        checked = gyroInvertY,
                        onCheckedChange = {
                            gyroInvertY = it
                            LauncherPreferences.PREF_GYRO_INVERT_Y = it
                            onPreferenceChanged("gyroInvertY", it)
                            onGyroStateChanged()
                        }
                    )
                }
                SettingsSliderItem(
                    title = translatedText(stringResource(R.string.preference_gyro_sensitivity_title)),
                    value = gyroSensitivity,
                    valueRange = 25f..300f,
                    valueSuffix = "%",
                    onValueChange = {
                        gyroSensitivity = it
                        LauncherPreferences.PREF_GYRO_SENSITIVITY = it / 100f
                        onPreferenceChanged("gyroSensitivity", it.toInt())
                        onGyroStateChanged()
                    }
                )
            }
        }

        SettingsSliderItem(
            title = translatedText(stringResource(R.string.mcl_setting_title_mousespeed)),
            value = mouseSpeed,
            valueRange = 25f..300f,
            valueSuffix = "%",
            onValueChange = {
                mouseSpeed = it
                LauncherPreferences.PREF_MOUSESPEED = it / 100f
                onPreferenceChanged("mousespeed", it.toInt())
            }
        )

        SettingsCard(position = CardPosition.SINGLE, useSurface = true) {
            SettingsSwitchItem(
                title = translatedText(stringResource(R.string.mcl_disable_gestures)),
                checked = disableGestures,
                onCheckedChange = {
                    disableGestures = it
                    LauncherPreferences.PREF_DISABLE_GESTURES = it
                    onPreferenceChanged("disableGestures", it)
                }
            )
        }

        if (!disableGestures) {
            SettingsSliderItem(
                title = translatedText(stringResource(R.string.mcl_setting_title_longpresstrigger)),
                value = gestureDelay,
                valueRange = 100f..1000f,
                valueSuffix = " ms",
                onValueChange = {
                    gestureDelay = it
                    LauncherPreferences.PREF_LONGPRESS_TRIGGER = it.toInt()
                    onPreferenceChanged("timeLongPressTrigger", it.toInt())
                }
            )
        }
    }
}
