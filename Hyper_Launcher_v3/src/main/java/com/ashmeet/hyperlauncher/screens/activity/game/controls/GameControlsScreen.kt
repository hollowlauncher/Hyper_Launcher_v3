package com.ashmeet.hyperlauncher.screens.activity.game.controls

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ashmeet.hyperlauncher.screens.activity.game.LoggerView
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.customcontrols.ControlLayout
import net.kdt.pojavlaunch.customcontrols.handleview.DrawerPullButton
import net.kdt.pojavlaunch.game.GameView

@Composable
fun GameControlsScreen(
    controlLayout: ControlLayout,
    loggerView: LoggerView,
    gameView: GameView? = null,
    hostViews: Boolean = true,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
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
