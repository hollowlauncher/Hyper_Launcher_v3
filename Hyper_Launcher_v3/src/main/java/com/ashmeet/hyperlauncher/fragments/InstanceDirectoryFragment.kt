package com.ashmeet.hyperlauncher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.ashmeet.hyperlauncher.screens.layouts.instances.InstanceDirectoryScreen
import com.ashmeet.hyperlauncher.theme.PojavTheme
import net.kdt.pojavlaunch.Tools

class InstanceDirectoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PojavTheme {
                    InstanceDirectoryScreen(
                        onBack = { Tools.removeCurrentFragment(requireActivity()) }
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "InstanceDirectoryFragment"
    }
}
