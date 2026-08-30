package net.kdt.pojavlaunch.colorselector

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.ashmeet.hyperlauncher.theme.PojavTheme
import com.kdt.SideDialogView
import net.ashmeet.hyperlauncher.R

class ColorSelector(context: Context, parent: ViewGroup, private var colorSelectionListener: ColorSelectionListener?) :
    SideDialogView(context, parent, R.layout.dialog_compose) {

    private var selectedColor: Int = Color.RED
    private var initialColor: Int = Color.RED
    private var alphaEnabled: Boolean = true

    init {
        setupButtons()
    }

    override fun onInflate() {
        setupButtons()
        updateComposeContent()
    }

    private fun setupButtons() {
        setStartButtonListener(android.R.string.cancel) {
            colorSelectionListener?.onColorSelected(initialColor)
            disappear(true)
        }
        setEndButtonListener(android.R.string.ok) {
            disappear(true)
        }
    }

    private fun updateComposeContent() {
        val composeView = mDialogContent?.findViewById<ComposeView>(R.id.compose_view) ?: return
        composeView.setContent {
            PojavTheme {
                ColorSelectorContent(
                    initialColor = selectedColor,
                    alphaEnabled = alphaEnabled,
                    onColorChanged = { color ->
                        selectedColor = color
                        colorSelectionListener?.onColorSelected(color)
                    },
                    onClose = { disappear(true) }
                )
            }
        }
    }

    fun show(fromRight: Boolean, previousColor: Int = Color.RED) {
        initialColor = previousColor
        selectedColor = previousColor
        appear(fromRight)
        updateComposeContent()
    }

    fun setAlphaEnabled(enabled: Boolean) {
        alphaEnabled = enabled
        updateComposeContent()
    }

    fun setColorSelectionListener(listener: ColorSelectionListener?) {
        colorSelectionListener = listener
    }

    companion object {
        @JvmStatic
        fun setAlpha(color: Int, alpha: Int): Int {
            return (color and 0x00FFFFFF) or (alpha shl 24)
        }
    }
}
