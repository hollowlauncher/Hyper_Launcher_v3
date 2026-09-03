package com.ashmeet.hyperlauncher.helper

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.ashmeet.hyperlauncher.components.SideNavigationRail
import com.ashmeet.hyperlauncher.screens.activity.game.ExitScreen
import com.ashmeet.hyperlauncher.screens.activity.game.GameBasemainScreen
import com.ashmeet.hyperlauncher.screens.activity.game.LoggerView
import com.ashmeet.hyperlauncher.screens.activity.game.controls.ControlsEditorScreen
import com.ashmeet.hyperlauncher.screens.activity.game.controls.GameControlsScreen
import com.ashmeet.hyperlauncher.screens.activity.game.controls.ImportControlScreen
import com.ashmeet.hyperlauncher.screens.activity.launcher.PojavLauncherScreen
import com.ashmeet.hyperlauncher.theme.PojavTheme
import kotlinx.coroutines.launch
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.customcontrols.ControlLayout

object LauncherComposeHelper {
    private var settingsIconRes: Int by mutableIntStateOf(R.drawable.ic_px_sliders)
    private var mIsFileManagerVisible by mutableStateOf(true)

    interface OnFragmentViewCreatedListener {
        fun onCreated(view: FrameLayout)
    }

    interface DrawerController {
        fun open()
        fun close()
        fun toggle()
        fun isOpen(): Boolean
    }

    private fun ensureViewTreeOwners(view: ComposeView) {
        if (view.findViewTreeLifecycleOwner() == null) {
            val activity = view.context as? FragmentActivity
            if (activity != null) {
                view.setViewTreeLifecycleOwner(activity)
                view.setViewTreeViewModelStoreOwner(activity)
                view.setViewTreeSavedStateRegistryOwner(activity)
            }
        }
        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    @JvmStatic
    fun setSettingsIcon(iconRes: Int) {
        settingsIconRes = iconRes
    }

    @JvmStatic
    fun setFileManagerVisible(visible: Boolean) {
        mIsFileManagerVisible = visible
    }

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
        ensureViewTreeOwners(composeView)
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

    @JvmStatic
    fun setContent(
        activity: FragmentActivity,
        onSettingsClick: Runnable,
        onContentInstallerClick: Runnable,
        onInstanceDirectoryClick: Runnable,
        onFragmentViewCreated: OnFragmentViewCreatedListener
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

    @JvmStatic
    fun setBaseMainContent(
        composeView: ComposeView,
        isInEditor: Boolean,
        controlLayout: ControlLayout,
        loggerView: LoggerView,
        instanceName: String,
        onDrawerControllerCreated: (DrawerController) -> Unit,
        onAction: (Int) -> Unit
    ) {
        ensureViewTreeOwners(composeView)
        composeView.setContent {
            PojavTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                onDrawerControllerCreated(object : DrawerController {
                    override fun open() { scope.launch { drawerState.open() } }
                    override fun close() { scope.launch { drawerState.close() } }
                    override fun toggle() {
                        scope.launch {
                            if (drawerState.isOpen) drawerState.close()
                            else drawerState.open()
                        }
                    }
                    override fun isOpen(): Boolean = drawerState.isOpen
                })

                GameBasemainScreen {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    SideNavigationRail(
                                        isEditor = isInEditor,
                                        onAction = { action ->
                                            onAction(action)
                                            scope.launch { drawerState.close() }
                                        },
                                        isExport = isInEditor
                                    )
                                }
                            },
                            gesturesEnabled = false
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (isInEditor) {
                                        ControlsEditorScreen(
                                            controlLayout = controlLayout,
                                            drawerState = drawerState
                                        )
                                    } else {
                                        GameControlsScreen(
                                            drawerState = drawerState,
                                            controlLayout = controlLayout,
                                            loggerView = loggerView
                                        )
                                    }

                                    if (drawerState.targetValue != DrawerValue.Closed) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.01f))
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    scope.launch { drawerState.close() }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    fun setControlsEditorContent(
        composeView: ComposeView,
        controlLayout: ControlLayout,
        onAction: (Int) -> Unit
    ) {
        ensureViewTreeOwners(composeView)
        composeView.setContent {
            PojavTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                SideNavigationRail(
                                    isEditor = true,
                                    onAction = { action ->
                                        onAction(action)
                                        scope.launch { drawerState.close() }
                                    },
                                    isExport = true
                                )
                            }
                        },
                        gesturesEnabled = false
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                ControlsEditorScreen(
                                    controlLayout = controlLayout,
                                    drawerState = drawerState
                                )

                                if (drawerState.targetValue != DrawerValue.Closed) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.01f))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                scope.launch { drawerState.close() }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    fun setImportControlContent(
        composeView: ComposeView,
        initialFileName: String,
        onImport: (String) -> Unit
    ) {
        ensureViewTreeOwners(composeView)
        composeView.setContent {
            ImportControlScreen(
                initialFileName = initialFileName,
                onImport = onImport
            )
        }
    }
}
