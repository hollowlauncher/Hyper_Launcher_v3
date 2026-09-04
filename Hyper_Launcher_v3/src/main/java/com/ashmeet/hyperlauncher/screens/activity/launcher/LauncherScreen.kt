package com.ashmeet.hyperlauncher.screens.activity.launcher

import android.graphics.BitmapFactory
import android.content.SharedPreferences
import android.widget.VideoView
import android.widget.FrameLayout
import android.view.ViewGroup
import android.view.Gravity
import android.media.MediaPlayer
import com.ashmeet.hyperlauncher.utils.translatedText
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences
import com.ashmeet.hyperlauncher.screens.layouts.compose.AccountSpinnerCompose
import com.ashmeet.hyperlauncher.screens.layouts.compose.ProgressLayoutCompose
import com.ashmeet.hyperlauncher.theme.PojavTheme
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener
import java.io.File

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
    var launcherBgPath by remember { mutableStateOf(LauncherPreferences.PREF_LAUNCHER_BACKGROUND_PATH) }
    var launcherBgType by remember { mutableStateOf(LauncherPreferences.PREF_LAUNCHER_BACKGROUND_TYPE) }
    var launcherBgOverlayEnabled by remember { mutableStateOf(LauncherPreferences.PREF_LAUNCHER_BACKGROUND_OVERLAY_ENABLED) }
    var launcherBgOverlayOpacity by remember { mutableFloatStateOf(LauncherPreferences.PREF_LAUNCHER_BACKGROUND_OVERLAY_OPACITY.toFloat()) }
    var launcherBgBlurEnabled by remember { mutableStateOf(LauncherPreferences.PREF_LAUNCHER_BACKGROUND_BLUR_ENABLED) }
    var launcherBgBlur by remember { mutableFloatStateOf(LauncherPreferences.PREF_LAUNCHER_BACKGROUND_BLUR.toFloat()) }
    var launcherVideoMuted by remember { mutableStateOf(LauncherPreferences.PREF_LAUNCHER_VIDEO_MUTED) }
    var launcherVideoVolume by remember { mutableFloatStateOf(LauncherPreferences.PREF_LAUNCHER_VIDEO_VOLUME.toFloat()) }
    var launcherVideoLoop by remember { mutableStateOf(LauncherPreferences.PREF_LAUNCHER_VIDEO_LOOP) }

    DisposableEffect(Unit) {
        val listener = TaskCountListener { count ->
            taskCount = count
            false
        }
        val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "launcher_background_path" -> launcherBgPath = LauncherPreferences.prefs.getString("launcher_background_path", null)
                "launcher_background_type" -> launcherBgType = LauncherPreferences.prefs.getString("launcher_background_type", "image") ?: "image"
                "launcher_background_overlay_enabled" -> launcherBgOverlayEnabled = LauncherPreferences.prefs.getBoolean("launcher_background_overlay_enabled", true)
                "launcher_background_overlay_opacity" -> launcherBgOverlayOpacity = LauncherPreferences.prefs.getInt("launcher_background_overlay_opacity", 50).toFloat()
                "launcher_background_blur_enabled" -> launcherBgBlurEnabled = LauncherPreferences.prefs.getBoolean("launcher_background_blur_enabled", false)
                "launcher_background_blur" -> launcherBgBlur = LauncherPreferences.prefs.getInt("launcher_background_blur", 0).toFloat()
                "launcher_video_muted" -> launcherVideoMuted = LauncherPreferences.prefs.getBoolean("launcher_video_muted", true)
                "launcher_video_volume" -> launcherVideoVolume = LauncherPreferences.prefs.getInt("launcher_video_volume", 50).toFloat()
                "launcher_video_loop" -> launcherVideoLoop = LauncherPreferences.prefs.getBoolean("launcher_video_loop", true)
            }
        }
        ProgressKeeper.addTaskCountListener(listener)
        LauncherPreferences.prefs.registerOnSharedPreferenceChangeListener(prefListener)
        onDispose {
            ProgressKeeper.removeTaskCountListener(listener)
            LauncherPreferences.prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        }
    }

    val backgroundBitmap = remember(launcherBgPath, launcherBgType) {
        if (launcherBgPath != null && launcherBgType == "image") {
            try {
                BitmapFactory.decodeFile(launcherBgPath)
            } catch (_: Exception) {
                null
            }
        } else null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (launcherBgPath != null) Color.Transparent else MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (launcherBgType == "image" && backgroundBitmap != null) {
                Image(
                    bitmap = backgroundBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (launcherBgBlurEnabled && launcherBgBlur > 0)
                                Modifier.blur(launcherBgBlur.dp)
                            else Modifier
                        ),
                    contentScale = ContentScale.Crop
                )
            } else if (launcherBgType == "video" && launcherBgPath != null) {
                AndroidView(
                    factory = { context ->
                        val root = FrameLayout(context)
                        val videoView = VideoView(context)
                        videoView.setOnPreparedListener { mp ->
                            mp.isLooping = launcherVideoLoop
                            if (launcherVideoMuted) {
                                mp.setVolume(0f, 0f)
                            } else {
                                val vol = launcherVideoVolume / 100f
                                mp.setVolume(vol, vol)
                            }

                            val videoWidth = mp.videoWidth.toFloat()
                            val videoHeight = mp.videoHeight.toFloat()
                            val viewWidth = root.width.toFloat()
                            val viewHeight = root.height.toFloat()

                            if (videoWidth > 0 && videoHeight > 0 && viewWidth > 0 && viewHeight > 0) {
                                val scale = Math.max(viewWidth / videoWidth, viewHeight / videoHeight)
                                videoView.layoutParams = FrameLayout.LayoutParams(
                                    (videoWidth * scale).toInt(),
                                    (videoHeight * scale).toInt(),
                                    Gravity.CENTER
                                )
                            }
                            mp.start()
                        }
                        videoView.tag = launcherBgPath
                        if (launcherBgPath != null) {
                            videoView.setVideoPath(launcherBgPath)
                        }
                        root.addView(videoView)
                        root
                    },
                    update = { root ->
                        val vv = root.getChildAt(0) as? VideoView
                        if (vv != null && vv.tag != launcherBgPath) {
                            vv.tag = launcherBgPath
                            if (launcherBgPath != null) {
                                vv.setVideoPath(launcherBgPath)
                            } else {
                                vv.stopPlayback()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (launcherBgPath != null && launcherBgOverlayEnabled) {
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val overlayColor = if (isDark) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f).compositeOver(Color.Black)
                } else {
                    MaterialTheme.colorScheme.primary
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(overlayColor.copy(alpha = launcherBgOverlayOpacity / 100f))
                )
            }

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
