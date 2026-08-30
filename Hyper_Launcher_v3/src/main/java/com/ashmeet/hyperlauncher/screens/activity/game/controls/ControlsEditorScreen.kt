package com.ashmeet.hyperlauncher.screens.activity.game.controls

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.ashmeet.hyperlauncher.components.ActionRow
import com.ashmeet.hyperlauncher.dialog.EditControlSideDialog
import kotlinx.coroutines.launch
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.customcontrols.ControlData
import net.kdt.pojavlaunch.customcontrols.ControlLayout
import net.kdt.pojavlaunch.customcontrols.buttons.ControlDrawer
import net.kdt.pojavlaunch.customcontrols.buttons.ControlInterface
import net.kdt.pojavlaunch.customcontrols.handleview.DrawerPullButton

@Composable
fun ControlsEditorScreen(
    controlLayout: ControlLayout,
    onDrawerButtonTap: (() -> Unit)? = null,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {
    val scope = rememberCoroutineScope()
    val context: Context = LocalContext.current

    var followedButton by remember { mutableStateOf<ControlInterface?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val editDialog = remember(controlLayout) {
        EditControlSideDialog(context, controlLayout)
    }

    LaunchedEffect(controlLayout) {
        controlLayout.setOnControlEditListener(object : ControlLayout.OnControlEditListener {
            override fun onEditControl(button: ControlInterface) {
                followedButton = button
                editDialog.setCurrentlyEditedButton(button)
                editDialog.adaptPanelPosition()
            }

            override fun onDisappearLayer(): Boolean {
                followedButton = null
                return editDialog.disappearLayer()
            }
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { controlLayout },
            modifier = Modifier.fillMaxSize()
        )

        if (onDrawerButtonTap != null) {
            AndroidView(
                factory = { ctx ->
                    // Use a match_parent container so the button can move freely
                    FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        val button = DrawerPullButton(ctx).apply {
                            setOnClickListener { onDrawerButtonTap() }
                        }
                        addView(button)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        ActionRow(
            followedButton = followedButton,
            onDelete = {
                showDeleteConfirm = true
            },
            onClone = {
                followedButton?.cloneButton()
                controlLayout.removeEditWindow()
                followedButton = null
            },
            onAddSub = {
                if (followedButton is ControlDrawer) {
                    controlLayout.addSubButton(followedButton as ControlDrawer, ControlData())
                }
            }
        )

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(text = stringResource(R.string.global_delete)) },
                text = { Text(text = "Are you sure you want to delete this button?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            followedButton?.removeButton()
                            followedButton = null
                            showDeleteConfirm = false
                        }
                    ) {
                        Text(text = stringResource(R.string.global_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                }
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
