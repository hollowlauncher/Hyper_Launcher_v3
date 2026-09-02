package com.ashmeet.hyperlauncher.screens.activity.game.controls

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.ashmeet.hyperlauncher.screens.activity.game.LoggerView
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.customcontrols.ControlLayout

@Composable
fun GameControlsScreen(
    controlLayout: ControlLayout,
    loggerView: LoggerView,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { controlLayout },
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(
            factory = { loggerView },
            modifier = Modifier.fillMaxSize()
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
