package com.ashmeet.hyperlauncher.natives.plugins

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ashmeet.hyperlauncher.natives.HyperPluginManager

class PhysicsPlugin : HyperPlugin {
    override fun prepare(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String) {
        val physxLib = HyperPluginManager.findNative(activity, mcVersion, "libPhysXJniBindings_64.so", "libphysx-jni.so")
        if (physxLib != null) {
            javaArgList.add("-Dmiolibpatcher.physx_redirect_path=${physxLib.absolutePath}")
            Log.i("PhysicsPlugin", "Found Official PhysX native at: ${physxLib.absolutePath}")
        }
    }
}
