package com.ashmeet.hyperlauncher.plugins.natives

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ashmeet.hyperlauncher.plugins.interfaces.HyperPlugin
import com.ashmeet.hyperlauncher.plugins.manager.HyperPluginManager

class DhCompatPlugin : HyperPlugin {
    override fun prepare(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String) {
        val zstdLib = HyperPluginManager.findNative(activity, mcVersion, "libzstd-jni_dh-1.5.7-6.so", "libzstd-jni.so")
        if (zstdLib != null) {
            val zstdLibName = zstdLib.name.removePrefix("lib").removeSuffix(".so")
            val zstdPath = zstdLib.parent
            javaArgList.add("-Dzstd.libname=$zstdLibName")
            javaArgList.add("-Dzstd.libpath=$zstdPath")
            javaArgList.add("-Ddhzstd.libname=$zstdLibName")
            javaArgList.add("-Ddhzstd.libpath=$zstdPath")
            Log.i("DhCompatPlugin", "Applied Zstd/DhCompat natives: $zstdLibName")
        }
    }
}
