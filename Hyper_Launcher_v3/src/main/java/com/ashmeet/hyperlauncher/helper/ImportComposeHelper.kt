package com.ashmeet.hyperlauncher.helper

import androidx.compose.ui.platform.ComposeView
import com.ashmeet.hyperlauncher.screens.activity.game.controls.ImportControlScreen

object ImportComposeHelper {
    @JvmStatic
    fun setImportControlContent(
        composeView: ComposeView,
        initialFileName: String,
        onImport: (String) -> Unit
    ) {
        LauncherComposeHelper.ensureViewTreeOwners(composeView)
        composeView.setContent {
            ImportControlScreen(
                initialFileName = initialFileName,
                onImport = onImport
            )
        }
    }
}
