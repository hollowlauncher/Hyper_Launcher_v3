package com.ashmeet.hyperlauncher.screens.activity.launcher

import android.widget.FrameLayout
import com.ashmeet.hyperlauncher.utils.translatedText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.ashmeet.hyperlauncher.screens.layouts.compose.AccountSpinnerCompose
import com.ashmeet.hyperlauncher.screens.layouts.compose.ProgressLayoutCompose
import com.ashmeet.hyperlauncher.theme.PojavTheme
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener

@Composable
fun PojavLauncherScreen(
    settingsIconRes: Int,
    isFileManagerVisible: Boolean,
    onSettingsClick: () -> Unit,
    onContentInstallerClick: () -> Unit,
    onInstanceDirectoryClick: () -> Unit,
    onFragmentViewCreated: (FrameLayout) -> Unit
) {
    var taskCount by remember { mutableIntStateOf(ProgressKeeper.getTaskCount()) }
    DisposableEffect(Unit) {
        val listener = TaskCountListener { count ->
            taskCount = count
            false
        }
        ProgressKeeper.addTaskCountListener(listener)
        onDispose {
            ProgressKeeper.removeTaskCountListener(listener)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .zIndex(1f)
            ) {
                AccountSpinnerCompose(
                    modifier = Modifier.fillMaxSize(),
                    hideDivider = taskCount > 0
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                ) {
                    if (isFileManagerVisible) {
                        IconButton(
                            onClick = onContentInstallerClick,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = translatedText("Content Installer"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = onInstanceDirectoryClick,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Folder,
                                contentDescription = translatedText("Instance Directory"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = settingsIconRes),
                            contentDescription = translatedText("Settings"),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (taskCount > 0) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AndroidView(
                    factory = { context ->
                        FrameLayout(context).apply {
                            id = R.id.container_fragment
                            onFragmentViewCreated(this)
                        }
                    },
                    update = {},
                    modifier = Modifier.fillMaxSize()
                )
            }

            ProgressLayoutCompose()
        }
    }
}

@Preview(showBackground = true, name = "File Manager Visible")
@Composable
fun PojavLauncherScreenPreview() {
    PojavTheme {
        PojavLauncherScreen(
            settingsIconRes = R.drawable.ic_sharp_settings_24,
            isFileManagerVisible = true,
            onSettingsClick = {},
            onContentInstallerClick = {},
            onInstanceDirectoryClick = {},
            onFragmentViewCreated = {}
        )
    }
}

@Preview(showBackground = true, name = "File Manager Hidden")
@Composable
fun PojavLauncherScreenHiddenPreview() {
    PojavTheme {
        PojavLauncherScreen(
            settingsIconRes = R.drawable.ic_sharp_settings_24,
            isFileManagerVisible = false,
            onSettingsClick = {},
            onContentInstallerClick = {},
            onInstanceDirectoryClick = {},
            onFragmentViewCreated = {}
        )
    }
}