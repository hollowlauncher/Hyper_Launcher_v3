package com.ashmeet.hyperlauncher.plugins.natives

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ashmeet.hyperlauncher.plugins.interfaces.HyperPlugin
import java.io.File

class HpmPlugin : HyperPlugin {
    override fun prepare(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String) {
        val allPlugins = LibraryPlugin.discoverAllPlugins(activity)
        for (plugin in allPlugins) {
            val metaData = plugin.getMetaData()
            if (metaData.getString(LibraryPlugin.METADATA_POJAV_PLUGIN_TYPE) == "native-bundle") {
                val libsString = metaData.getString(LibraryPlugin.METADATA_POJAV_PLUGIN_LIBS) ?: continue
                val libs = libsString.split(",")
                for (libEntry in libs) {
                    val parts = libEntry.split(":")
                    if (parts.isEmpty()) continue
                    val libName = parts[0].trim()

                    when (libName) {
                        "discord-rpc" -> {
                            val rpcLib = File(plugin.libraryPath, "libdiscord-rpc.so")
                            if (rpcLib.exists()) {
                                javaArgList.add("-Ddiscord.rpc.library.path=${rpcLib.absolutePath}")
                                Log.i("HpmPlugin", "Applied Discord RPC from HPM plugin")
                            }
                        }
                        "mobileglues" -> {
                            val gluesLib = File(plugin.libraryPath, "libmobileglues.so")
                            if (gluesLib.exists()) {
                                javaArgList.add("-Dmobileglues.library.path=${gluesLib.absolutePath}")
                                Log.i("HpmPlugin", "Applied MobileGlues from HPM plugin")
                            }
                        }
                    }
                }
            }
        }
    }
}
