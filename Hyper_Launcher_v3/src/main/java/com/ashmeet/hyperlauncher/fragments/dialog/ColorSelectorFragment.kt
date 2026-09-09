package com.ashmeet.hyperlauncher.fragments.dialog

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.ashmeet.hyperlauncher.components.ColorSelectorContent
import com.ashmeet.hyperlauncher.components.colorselector.ColorSelectionListener
import com.ashmeet.hyperlauncher.theme.PojavTheme
import com.kdt.SideDialogView
import net.ashmeet.hyperlauncher.R

class ColorSelectorFragment(context: Context, parent: ViewGroup, private var colorSelectionListener: ColorSelectionListener?) :
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
    }
}