package com.ashmeet.hyperlauncher.plugins.natives

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ashmeet.hyperlauncher.plugins.manager.HyperPlugin
import java.io.File

class DhCompatPlugin : HyperPlugin {
    override fun prepare(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String) {
        val nativeLibDir = activity.applicationInfo.nativeLibraryDir
        val zstdLib = File(nativeLibDir, "libzstd-jni_dh-1.5.7-6.so")
        if (zstdLib.exists()) {
            val zstdLibName = "zstd-jni_dh-1.5.7-6"
            val zstdPath = zstdLib.parent
            javaArgList.add("-Dzstd.libname=$zstdLibName")
            javaArgList.add("-Dzstd.libpath=$zstdPath")
            javaArgList.add("-Ddhzstd.libname=$zstdLibName")
            javaArgList.add("-Ddhzstd.libpath=$zstdPath")
            Log.i("DhCompatPlugin", "Applied Zstd/DhCompat natives")
        }
    }
}
