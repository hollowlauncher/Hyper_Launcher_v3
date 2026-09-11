package com.ashmeet.hyperlauncher.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.TestStorageActivity
import net.kdt.pojavlaunch.instances.DisplayInstance

object ShortcutUtils {
    const val EXTRA_INSTANCE_NAME = "instance_name"

    fun createShortcut(context: Context, instance: DisplayInstance) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(context, "Shortcuts not supported on this launcher", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(context, TestStorageActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_INSTANCE_NAME, instance.mInstanceRoot.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val shortcut = ShortcutInfoCompat.Builder(context, instance.mInstanceRoot.name)
            .setShortLabel(instance.name ?: "Game")
            .setLongLabel("Launch ${instance.name ?: "Game"}")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        Toast.makeText(context, "Adding shortcut for ${instance.name}", Toast.LENGTH_SHORT).show()
    }
}
