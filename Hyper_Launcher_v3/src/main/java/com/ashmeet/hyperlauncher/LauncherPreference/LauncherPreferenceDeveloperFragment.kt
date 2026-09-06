package com.ashmeet.hyperlauncher.LauncherPreference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.ashmeet.hyperlauncher.screens.layouts.settings.DeveloperSettingsScreen
import com.ashmeet.hyperlauncher.theme.PojavTheme
import net.kdt.pojavlaunch.Tools

class LauncherPreferenceDeveloperFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PojavTheme {
                    DeveloperSettingsScreen(
                        onBack = { Tools.removeCurrentFragment(requireActivity()) },
                        onNavigateToPlugins = { Tools.swapFragment(requireActivity(), LauncherPreferencePluginFragment::class.java, null, null) }
                    )
                }
            }
        }
    }
}
