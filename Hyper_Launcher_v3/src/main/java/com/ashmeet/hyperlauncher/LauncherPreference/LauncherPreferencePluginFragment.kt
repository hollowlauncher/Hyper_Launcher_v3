package com.ashmeet.hyperlauncher.LauncherPreference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.ashmeet.hyperlauncher.screens.layouts.settings.PluginSettingsScreen
import com.ashmeet.hyperlauncher.theme.PojavTheme

class LauncherPreferencePluginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PojavTheme {
                    PluginSettingsScreen(
                        onBack = { requireActivity().onBackPressedDispatcher.onBackPressed() }
                    )
                }
            }
        }
    }
}
