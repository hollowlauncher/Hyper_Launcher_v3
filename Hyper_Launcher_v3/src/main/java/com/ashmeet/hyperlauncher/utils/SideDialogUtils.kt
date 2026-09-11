package com.ashmeet.hyperlauncher.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ashmeet.hyperlauncher.fragments.dialog.SideDialogView

/**
 * Manages the active side dialog in the Compose UI tree.
 */
object SideDialogUtils {
    var activeDialog by mutableStateOf<SideDialogView?>(null)
        @JvmStatic get
        @JvmStatic set

    /**
     * Set the active dialog to be displayed.
     */
    @JvmStatic
    fun show(dialog: SideDialogView, fromRight: Boolean) {
        activeDialog = dialog
        dialog.appear(fromRight)
    }

    /**
     * Clear the active dialog.
     */
    @JvmStatic
    fun dismiss() {
        activeDialog?.disappear(true)
        activeDialog = null
    }
}