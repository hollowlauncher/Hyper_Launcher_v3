package com.ashmeet.hyperlauncher.helper

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import com.ashmeet.hyperlauncher.screens.activity.launcher.PojavLauncherScreen
import com.ashmeet.hyperlauncher.theme.PojavTheme

object LauncherMainComposeHelper {
    private var settingsIconRes: Int by mutableIntStateOf(net.ashmeet.hyperlauncher.R.drawable.ic_px_sliders)
    private var mIsFileManagerVisible: Boolean by mutableStateOf(true)

    @JvmStatic
    fun setSettingsIcon(iconRes: Int) {
        settingsIconRes = iconRes
    }

    @JvmStatic
    fun setFileManagerVisible(visible: Boolean) {
        mIsFileManagerVisible = visible
    }

    @JvmStatic
    fun setContent(
        activity: FragmentActivity,
        onSettingsClick: Runnable,
        onContentInstallerClick: Runnable,
        onInstanceDirectoryClick: Runnable,
        onFragmentViewCreated: LauncherComposeHelper.OnFragmentViewCreatedListener
    ) {
        val composeView = ComposeView(activity).apply {
            setContent {
                PojavTheme {
                    PojavLauncherScreen(
                        settingsIconRes = settingsIconRes,
                        isFileManagerVisible = mIsFileManagerVisible,
                        onSettingsClick = { onSettingsClick.run() },
                        onContentInstallerClick = { onContentInstallerClick.run() },
                        onInstanceDirectoryClick = { onInstanceDirectoryClick.run() },
                        onFragmentViewCreated = { onFragmentViewCreated.onCreated(it) }
                    )
                }
            }
        }
        activity.setContentView(composeView)
    }
}
