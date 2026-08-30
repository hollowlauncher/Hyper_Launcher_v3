package com.ashmeet.hyperlauncher.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ashmeet.hyperlauncher.theme.PojavTheme
import com.ashmeet.hyperlauncher.utils.drawable.rememberDrawablePainter
import fr.spse.gamepad_remapper.RemapperManager
import fr.spse.gamepad_remapper.RemapperView
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.customcontrols.gamepad.Gamepad
import net.kdt.pojavlaunch.customcontrols.gamepad.GamepadMapperAdapter
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SingleChoiceDialog

class GamepadMapperFragment : Fragment(), View.OnKeyListener, View.OnGenericMotionListener {
    companion object {
        const val TAG = "GamepadMapperFragment"
    }

    private val remapperViewBuilder = RemapperView.Builder(null)
        .remapA(true)
        .remapB(true)
        .remapX(true)
        .remapY(true)
        .remapLeftJoystick(true)
        .remapRightJoystick(true)
        .remapStart(true)
        .remapSelect(true)
        .remapLeftShoulder(true)
        .remapRightShoulder(true)
        .remapLeftTrigger(true)
        .remapRightTrigger(true)
        .remapDpad(true)

    private val exitHandler = Handler(Looper.getMainLooper())
    private val exitRunnable = Runnable {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private var inputManager: RemapperManager? = null
    private var mapperAdapter: GamepadMapperAdapter? = null
    private var gamepad: Gamepad? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PojavTheme {
                    GamepadMapperContent(
                        onAdapterCreated = { mapperAdapter = it },
                        onInputManagerCreated = { inputManager = it },
                        onKeyListenerSet = { this@GamepadMapperFragment.view?.setOnKeyListener(this@GamepadMapperFragment) },
                        onGenericMotionListenerSet = { this@GamepadMapperFragment.view?.setOnGenericMotionListener(this@GamepadMapperFragment) }
                    )
                }
            }
        }
    }

    @Composable
    fun GamepadMapperContent(
        onAdapterCreated: (GamepadMapperAdapter) -> Unit,
        onInputManagerCreated: (RemapperManager) -> Unit,
        onKeyListenerSet: () -> Unit,
        onGenericMotionListenerSet: () -> Unit
    ) {
        var grabState by remember { mutableIntStateOf(0) }
        val grabOptions = listOf(
            stringResource(R.string.customctrl_visibility_in_menus),
            stringResource(R.string.customctrl_visibility_ingame)
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.controller_remapper_exit_part1),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Image(
                            painter = rememberDrawablePainter(
                                ContextCompat.getDrawable(LocalContext.current, R.drawable.button_select)
                            ),
                            contentDescription = stringResource(R.string.controller_button_select),
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = stringResource(R.string.controller_remapper_exit_part2),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.controller_remapper_operating_mode),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                        var showGrabDialog by remember { mutableStateOf(false) }
                        Text(
                            text = grabOptions[grabState],
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { showGrabDialog = true }
                                .padding(8.dp)
                        )

                        if (showGrabDialog) {
                            SingleChoiceDialog(
                                title = stringResource(R.string.controller_remapper_operating_mode),
                                options = grabOptions,
                                optionValues = listOf("0", "1"),
                                selectedValue = grabState.toString(),
                                onValueChange = {
                                    val newState = it.toInt()
                                    grabState = newState
                                    mapperAdapter?.setGrabState(newState == 1)
                                },
                                onDismiss = { showGrabDialog = false }
                            )
                        }
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        RecyclerView(ctx).apply {
                            layoutManager = LinearLayoutManager(ctx)
                            val adapter = GamepadMapperAdapter(ctx)
                            this.adapter = adapter
                            onAdapterCreated(adapter)
                            onInputManagerCreated(RemapperManager(ctx, remapperViewBuilder))

                            isFocusable = true
                            isFocusableInTouchMode = true
                            requestFocus()
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    update = {
                        onKeyListenerSet()
                        onGenericMotionListenerSet()
                    }
                )
            }
        }
    }

    private fun createGamepad(inputDevice: InputDevice) {
        gamepad = object : Gamepad(inputDevice, mapperAdapter, null) {
            override fun handleGamepadInput(keycode: Int, value: Float) {
                if (keycode == KeyEvent.KEYCODE_BUTTON_SELECT) {
                    handleExitButton(value > 0.5)
                }
                super.handleGamepadInput(keycode, value)
            }
        }
    }

    private fun handleExitButton(isPressed: Boolean) {
        if (isPressed) exitHandler.postDelayed(exitRunnable, 400)
        else exitHandler.removeCallbacks(exitRunnable)
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (!Gamepad.isGamepadEvent(event)) return false
        if (gamepad == null) createGamepad(event.device)
        inputManager?.handleKeyEventInput(requireContext(), event, gamepad)
        return true
    }

    override fun onGenericMotion(v: View, event: MotionEvent): Boolean {
        if (!Gamepad.isGamepadEvent(event)) return false
        if (gamepad == null) createGamepad(event.device)
        inputManager?.handleMotionEventInput(requireContext(), event, gamepad)
        return true
    }
}
