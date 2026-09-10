package com.ashmeet.hyperlauncher.plugins.manager

import android.content.Context
import android.util.Log
import com.ashmeet.hyperlauncher.plugins.interfaces.NativePlugin
import com.ashmeet.hyperlauncher.plugins.natives.LibraryPlugin
import com.ashmeet.hyperlauncher.utils.LauncherPreferences
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.ComparableVersionString
import java.io.File
import java.util.HashMap

object NativePluginManager {
    private const val TAG = "NativePluginManager"
    private val sPlugins = mutableListOf<NativePlugin>()

    @JvmStatic
    fun registerPlugin(plugin: NativePlugin) {
        sPlugins.add(plugin)
    }

    @JvmStatic
    fun getPlugins(): List<NativePlugin> {
        return ArrayList(sPlugins)
    }

    @JvmStatic
    fun discoverAarPlugins(context: Context) {
        val destDir = File(Tools.DIR_CACHE, "hyper_plugin_libs")
        destDir.mkdirs()
        createPthreadShim(destDir)

        registerPlugin(object : NativePlugin {
            override fun getPaths(): Array<String> = arrayOf(context.applicationInfo.nativeLibraryDir, destDir.absolutePath)

            override fun getJVMEnv(): Map<String, String> {
                val env = HashMap<String, String>()
                env["HYPERPLUGIN_PATH"] = destDir.absolutePath
                return env
            }

            override fun supportsVersion(mcVersion: String?): Boolean = true
        })

        discoverFCLPlugins(context)
    }

    @JvmStatic
    fun discoverFCLPlugins(context: Context) {
        val fclPlugins = LibraryPlugin.discoverAllPlugins(context)
        for (plugin in fclPlugins) {
            val libDir = plugin.libraryPath
            val metaData = plugin.getMetaData()
            val envString = metaData.getString(LibraryPlugin.METADATA_FCL_ENVIRONMENT)
            val boatEnv = metaData.getString(LibraryPlugin.METADATA_FCL_BOAT_ENV)
            val pojavEnv = metaData.getString(LibraryPlugin.METADATA_FCL_POJAV_ENV)
            val vzh = metaData.getString(LibraryPlugin.METADATA_FCL_DESCRIPTION)
            val rendererNameMetadata = metaData.getString(LibraryPlugin.METADATA_FCL_RENDERER)
            val minVerStr = metaData.getString(LibraryPlugin.METADATA_FCL_MIN_MC_VER)
            val maxVerStr = metaData.getString(LibraryPlugin.METADATA_FCL_MAX_MC_VER)

            registerPlugin(object : NativePlugin {
                override fun getPaths(): Array<String> = arrayOf(libDir)

                override fun getJVMEnv(): Map<String, String> {
                    val envMap = HashMap<String, String>()
                    parseEnvString(envString, libDir, envMap)
                    parseEnvString(boatEnv, libDir, envMap)
                    parseEnvString(pojavEnv, libDir, envMap)
                    return envMap
                }

                override val rendererName: String?
                    get() = rendererNameMetadata

                override val displayName: String?
                    get() = vzh

                override fun supportsVersion(mcVersion: String?): Boolean {
                    if (mcVersion == null) return true
                    val current = ComparableVersionString.parse(mcVersion)
                    if (!current.isValid) return true

                    if (!minVerStr.isNullOrEmpty()) {
                        val min = ComparableVersionString.parse(minVerStr)
                        if (min.isValid && current < min) return false
                    }

                    if (!maxVerStr.isNullOrEmpty()) {
                        val max = ComparableVersionString.parse(maxVerStr)
                        if (max.isValid && current > max) return false
                    }

                    return true
                }
            })
            Log.i(TAG, "Discovered FCL plugin: ${plugin.appId}" + if (rendererNameMetadata != null) " (Renderer: $rendererNameMetadata)" else "")
        }
    }

    private fun parseEnvString(envString: String?, libDir: String, envMap: MutableMap<String, String>) {
        if (envString.isNullOrEmpty()) return
        val pairs = envString.split("[ ;]".toRegex()).toTypedArray()
        for (pair in pairs) {
            val kv = pair.split("=".toRegex(), 2).toTypedArray()
            if (kv.size == 2) {
                val key = kv[0].trim()
                val value = kv[1].trim().replace("{nativeLibraryDir}", libDir)
                if (key.isNotEmpty()) {
                    envMap[key] = value
                    Log.i(TAG, "Env: $key=$value")
                }
            }
        }
    }

    private fun createPthreadShim(destDir: File) {
        val shim = File(destDir, "libpthread.so.0")
        if (shim.exists()) return

        val sysLibDirs = arrayOf("/system/lib64", "/system/lib", "/apex/com.android.runtime/lib64/bionic", "/apex/com.android.runtime/lib/bionic")
        var sourcePthread: File? = null
        for (dir in sysLibDirs) {
            val f = File(dir, "libpthread.so")
            if (f.exists()) {
                sourcePthread = f
                break
            }
        }

        if (sourcePthread != null) {
            try {
                android.system.Os.symlink(sourcePthread.absolutePath, shim.absolutePath)
                Log.i("jrelog", "Created libpthread.so.0 shim (symlink)")
            } catch (e: Exception) {
                Log.e("jrelog", "Failed to create libpthread.so.0 shim", e)
            }
        }
    }

    @JvmStatic
    fun getRuntimeLibraryPath(): String = getRuntimeLibraryPath(null)

    @JvmStatic
    fun getRuntimeLibraryPath(mcVersion: String?): String {
        val sb = StringBuilder()
        for (plugin in sPlugins) {
            if (mcVersion != null && !plugin.supportsVersion(mcVersion)) continue

            val pluginRenderer = plugin.rendererName
            if (pluginRenderer != null && pluginRenderer != LauncherPreferences.PREF_RENDERER) continue

            for (path in plugin.getPaths()) {
                if (sb.isNotEmpty()) {
                    sb.append(":")
                }
                sb.append(path)
            }
        }
        return sb.toString()
    }

    @JvmStatic
    fun getRuntimeJVMEnv(): Map<String, String> = getRuntimeJVMEnv(null)

    @JvmStatic
    fun getRuntimeJVMEnv(mcVersion: String?): Map<String, String> {
        val env = HashMap<String, String>()
        for (plugin in sPlugins) {
            if (mcVersion != null && !plugin.supportsVersion(mcVersion)) continue

            val pluginRenderer = plugin.rendererName
            if (pluginRenderer != null && pluginRenderer != LauncherPreferences.PREF_RENDERER) continue

            env.putAll(plugin.getJVMEnv())
        }
        return env
    }
}
