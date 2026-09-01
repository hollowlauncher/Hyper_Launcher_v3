package com.ashmeet.hyperlauncher.fragments

import com.ashmeet.hyperlauncher.utils.translatedText

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.DialogFragment
import com.ashmeet.hyperlauncher.theme.PojavTheme
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.instances.InstanceIconProvider
import net.kdt.pojavlaunch.instances.Instances
import java.io.IOException

class DeleteConfirmDialogFragment : DialogFragment() {
    private val mInstance = Instances.loadSelectedInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PojavTheme {
                    if (mInstance == null) {
                        dismiss()
                    } else {
                        AlertDialog(
                            onDismissRequest = { dismiss() },
                            title = { Text(text = translatedText(stringResource(R.string.instance_delete))) },
                            text = { Text(text = translatedText(stringResource(R.string.instance_delete_confirmation))) },
                            confirmButton = {
                                TextButton(onClick = {
                                    InstanceIconProvider.dropIcon(mInstance)
                                    Tools.removeCurrentFragment(requireActivity())
                                    try {
                                        Instances.removeInstance(mInstance)
                                    } catch (e: IOException) {
                                        Tools.showErrorRemote(e)
                                    }
                                    dismiss()
                                }) {
                                    Text(text = translatedText(stringResource(R.string.global_delete)))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { dismiss() }) {
                                    Text(text = translatedText(stringResource(R.string.global_no)))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "delete_dialog_confirm"
    }
}
