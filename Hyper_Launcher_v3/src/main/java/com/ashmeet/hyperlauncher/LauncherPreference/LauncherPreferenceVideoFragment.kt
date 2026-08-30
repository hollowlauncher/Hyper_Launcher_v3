package com.ashmeet.hyperlauncher.LauncherPreference

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.ashmeet.hyperlauncher.screens.layouts.settings.VideoSettingsScreen
import com.ashmeet.hyperlauncher.theme.PojavTheme
import net.kdt.pojavlaunch.plugins.LibraryPlugin
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences

class LauncherPreferenceVideoFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val isAngleAvailable = LibraryPlugin.discoverPlugin(requireContext(), LibraryPlugin.ID_ANGLE_PLUGIN) != null
        return ComposeView(requireContext()).apply {
            setContent {
                PojavTheme {
                    VideoSettingsScreen(
                        onBack = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                        isAngleAvailable = isAngleAvailable
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LauncherPreferences.DEFAULT_PREF?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        LauncherPreferences.DEFAULT_PREF?.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        LauncherPreferences.loadPreferences(context)
    }
}
