package com.ashmeet.hyperlauncher.fragments.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.ashmeet.hyperlauncher.components.dialogs.SideDialog

/**
 * Base class for side dialogs, rewritten in pure Compose.
 * This class now acts as a state holder and content provider for side dialogs.
 */
abstract class SideDialogView {

    var isDisplaying by mutableStateOf(false)
    var isAtRight by mutableStateOf(false)

    protected var mTitleRes: Int = 0
    protected var mStartButtonRes: Int = 0
    protected var mEndButtonRes: Int = 0

    protected var mStartButtonListener: (() -> Unit)? = null
    protected var mEndButtonListener: (() -> Unit)? = null

    fun setTitle(textId: Int) {
        mTitleRes = textId
    }

    fun setStartButtonListener(textId: Int, listener: (() -> Unit)?) {
        mStartButtonRes = textId
        mStartButtonListener = listener
    }

    fun setEndButtonListener(textId: Int, listener: (() -> Unit)?) {
        mEndButtonRes = textId
        mEndButtonListener = listener
    }

    /**
     * Show the dialog.
     * @param fromRight whether to slide in from the right side.
     */
    open fun appear(fromRight: Boolean) {
        isAtRight = fromRight
        isDisplaying = true
        onAppear()
    }

    /**
     * Hide the dialog.
     * @param destroy whether to perform cleanup (kept for compatibility).
     */
    open fun disappear(destroy: Boolean) {
        isDisplaying = false
        onDisappear()
        if (destroy) onDestroy()
    }

    /**
     * The Composable content of the dialog.
     * This should be called within a Composable tree.
     */
    @Composable
    fun Content() {
        SideDialog(
            visible = isDisplaying,
            onDismissRequest = { disappear(false) },
            title = if (mTitleRes != 0) stringResource(mTitleRes) else null,
            fromRight = isAtRight,
            startText = if (mStartButtonRes != 0) stringResource(mStartButtonRes) else null,
            onStartClick = mStartButtonListener,
            endText = if (mEndButtonRes != 0) stringResource(mEndButtonRes) else null,
            onEndClick = mEndButtonListener
        ) {
            DialogContent()
        }
    }

    @Composable
    abstract fun DialogContent()

    protected open fun onAppear() {}
    protected open fun onDisappear() {}
    protected open fun onDestroy() {}
}
