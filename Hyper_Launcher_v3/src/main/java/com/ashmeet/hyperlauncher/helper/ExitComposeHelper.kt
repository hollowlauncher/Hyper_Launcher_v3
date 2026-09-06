package com.ashmeet.hyperlauncher.helper

import androidx.compose.ui.platform.ComposeView
import com.ashmeet.hyperlauncher.screens.activity.game.ExitScreen
import com.ashmeet.hyperlauncher.theme.PojavTheme

object ExitComposeHelper {
    @JvmStatic
    fun setExitContent(
        composeView: ComposeView,
        title: String,
        logs: String,
        onShareClick: () -> Unit,
        onCopyClick: () -> Unit,
        onRestartClick: () -> Unit,
        onOpenCrashReport: (String) -> Unit
    ) {
        LauncherComposeHelper.ensureViewTreeOwners(composeView)
        composeView.setContent {
            PojavTheme {
                ExitScreen(
                    title = title,
                    logs = logs,
                    onShareClick = onShareClick,
                    onCopyClick = onCopyClick,
                    onRestartClick = onRestartClick,
                    onOpenCrashReport = onOpenCrashReport
                )
            }
        }
    }
}
