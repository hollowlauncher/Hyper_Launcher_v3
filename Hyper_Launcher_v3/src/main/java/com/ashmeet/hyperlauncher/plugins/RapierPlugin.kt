package com.ashmeet.hyperlauncher.plugins

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ashmeet.hyperlauncher.plugins.manager.HyperPlugin
import com.ashmeet.hyperlauncher.plugins.manager.HyperPluginManager

class RapierPlugin : HyperPlugin {
    override fun prepare(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String) {
        val rapierLib = HyperPluginManager.findNative(activity, mcVersion, "libsable_rapier.so")
        if (rapierLib != null) {
            javaArgList.add("-Dmiolibpatcher.sablerapier_path=${rapierLib.absolutePath}")
            Log.i("RapierPlugin", "Found Rapier native at: ${rapierLib.absolutePath}")
        }
    }
}
