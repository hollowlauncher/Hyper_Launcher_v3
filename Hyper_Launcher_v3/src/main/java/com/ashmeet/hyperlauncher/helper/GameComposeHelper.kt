package com.ashmeet.hyperlauncher.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import com.ashmeet.hyperlauncher.components.SideNavigationRail
import com.ashmeet.hyperlauncher.screens.activity.game.GameBasemainScreen
import com.ashmeet.hyperlauncher.screens.activity.game.LoggerView
import com.ashmeet.hyperlauncher.screens.activity.game.controls.ControlsEditorScreen
import com.ashmeet.hyperlauncher.screens.activity.game.controls.GameControlsScreen
import com.ashmeet.hyperlauncher.theme.PojavTheme
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.customcontrols.ControlLayout
import net.kdt.pojavlaunch.game.GameView

object GameComposeHelper {

    @JvmStatic
    fun setBaseMainContent(
        composeView: ComposeView,
        isInEditor: Boolean,
        controlLayout: ControlLayout,
        loggerView: LoggerView,
        gameView: GameView?,
        hostViews: Boolean,
        onDrawerStateChanged: ((Boolean) -> Unit)?,
        onDrawerControllerCreated: (LauncherComposeHelper.DrawerController) -> Unit,
        onAction: (Int) -> Unit
    ) {
        LauncherComposeHelper.ensureViewTreeOwners(composeView)
        composeView.setContent {
            PojavTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                LaunchedEffect(drawerState.currentValue, drawerState.targetValue) {
                    val isVisible = drawerState.currentValue != DrawerValue.Closed || drawerState.targetValue != DrawerValue.Closed
                    onDrawerStateChanged?.invoke(isVisible)
                }

                onDrawerControllerCreated(object : LauncherComposeHelper.DrawerController {
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

                val mainUIContent = @Composable {
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
                                    // Persistent Legacy View Layer
                                    if (hostViews) {
                                        AndroidView(
                                            factory = {
                                                controlLayout.apply {
                                                    val parent = parent as? android.view.ViewGroup
                                                    parent?.removeView(this)
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        AndroidView(
                                            factory = {
                                                loggerView.apply {
                                                    val parent = parent as? android.view.ViewGroup
                                                    parent?.removeView(this)
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    if (isInEditor) {
                                        ControlsEditorScreen(
                                            controlLayout = controlLayout,
                                            drawerState = drawerState,
                                            hostViews = false // Already hosted above
                                        )
                                    } else {
                                        GameControlsScreen(
                                            drawerState = drawerState,
                                            controlLayout = controlLayout,
                                            loggerView = loggerView,
                                            gameView = gameView,
                                            hostViews = false // Already hosted above
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

                GameBasemainScreen(
                    drawerState = drawerState,
                    showLoading = hostViews
                ) {
                    mainUIContent()
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
        LauncherComposeHelper.ensureViewTreeOwners(composeView)
        composeView.setContent {
            PojavTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val editorContent = @Composable {
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
                                    AndroidView(
                                        factory = {
                                            controlLayout.apply {
                                                val parent = parent as? android.view.ViewGroup
                                                parent?.removeView(this)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    ControlsEditorScreen(
                                        controlLayout = controlLayout,
                                        drawerState = drawerState,
                                        hostViews = false
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

                GameBasemainScreen(
                    drawerState = drawerState,
                    showLoading = false
                ) {
                    editorContent()
                }
            }
        }
    }
}
