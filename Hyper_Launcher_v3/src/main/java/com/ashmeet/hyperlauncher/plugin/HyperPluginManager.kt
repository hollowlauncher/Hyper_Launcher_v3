package com.ashmeet.hyperlauncher.plugin

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.plugins.NativePluginManager
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object HyperPluginManager {
    private const val TAG = "HyperPlugin"
    private val gson = GsonBuilder().setPrettyPrinting().create()

    @JvmStatic
    fun applyHooks(activity: AppCompatActivity, javaArgList: MutableList<String>, versionId: String, gameDir: File) {
        val mcVersion = resolveMinecraftVersion(versionId)

        applyModPatches(gameDir)
        discoverExternalPlugins(File(Tools.DIR_DATA, "plugins"), mcVersion)
        Tools.DIR_GAME_NEW?.let { discoverExternalPlugins(File(it, "plugins"), mcVersion) }

        setupLibraryPaths(javaArgList, versionId, mcVersion)
        applyPluginEnvironment(javaArgList, mcVersion)
        applyMioLibPatcher(javaArgList)
        prepareNatives(activity, javaArgList, mcVersion)
    }

    private fun applyModPatches(gameDir: File) {
        try {
            patchPhysicsMod(gameDir)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply mod patches", e)
        }
    }

    private fun patchPhysicsMod(gameDir: File) {
        val modsDir = File(gameDir, "mods")
        if (!modsDir.exists()) return

        val modFiles = modsDir.listFiles { _, name -> 
            name.startsWith("physics-mod-") && name.endsWith(".jar") 
        } ?: return

        for (modFile in modFiles) {
            val marker = File(modsDir, ".patched_" + modFile.name)
            if (marker.exists()) continue

            Log.i(TAG, "Checking Physics Mod for crash-causing mixin: ${modFile.name}")
            if (patchJarMixinConfig(modFile, "physicsmod.mixins.json", "MixinAbstractContraptionEntity")) {
                try {
                    marker.createNewFile()
                } catch (_: Exception) {
                    Log.w(TAG, "Failed to create patch marker: ${marker.name}")
                }
            }
        }
    }

    private fun patchJarMixinConfig(jarFile: File, configName: String, mixinToRemove: String): Boolean {
        val tempJar = File(jarFile.parent, jarFile.name + ".tmp")
        
        try {
            ZipFile(jarFile).use { zip ->
                val configEntry = zip.getEntry(configName) ?: return false
                val json = zip.getInputStream(configEntry).use { input ->
                    JsonParser.parseReader(InputStreamReader(input, StandardCharsets.UTF_8)).asJsonObject
                }

                val mixins = json.getAsJsonArray("mixins") ?: return false
                val updatedMixins = JsonArray()
                var removed = false

                for (element in mixins) {
                    if (element.asString == mixinToRemove) {
                        removed = true
                        continue
                    }
                    updatedMixins.add(element)
                }

                if (!removed) return false

                Log.i(TAG, "Patching $configName in ${jarFile.name}: Removing $mixinToRemove")
                json.add("mixins", updatedMixins)
                val updatedJsonStr = gson.toJson(json)


                ZipOutputStream(FileOutputStream(tempJar)).use { zos ->
                    val entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (entry.name == configName) {
                            zos.putNextEntry(ZipEntry(configName))
                            zos.write(updatedJsonStr.toByteArray(StandardCharsets.UTF_8))
                            zos.closeEntry()
                        } else {
                            zos.putNextEntry(ZipEntry(entry.name))
                            zip.getInputStream(entry).use { input ->
                                input.copyTo(zos)
                            }
                            zos.closeEntry()
                        }
                    }
                }
            }
            if (jarFile.delete()) {
                if (!tempJar.renameTo(jarFile)) {
                    Log.e(TAG, "Failed to rename patched JAR back to original name")
                    return false
                }
                Log.i(TAG, "Successfully patched ${jarFile.name}")
                return true
            } else {
                Log.e(TAG, "Failed to delete original JAR for replacement")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while patching JAR: ${jarFile.name}", e)
        } finally {
            if (tempJar.exists()) tempJar.delete()
        }
        return false
    }

    @JvmStatic
    fun addDynamicPlugin(path: String, env: Map<String, String> = emptyMap(), mcVersion: String? = null) {
        val pluginDir = File(path)
        if (!pluginDir.exists() || !pluginDir.isDirectory) {
            Log.w(TAG, "Cannot add dynamic plugin: $path is not a valid directory")
            return
        }

        NativePluginManager.registerPlugin(object : net.kdt.pojavlaunch.plugins.NativePlugin {
            override fun getPaths(): Array<String> = arrayOf(path)
            override fun getJVMEnv(): Map<String, String> = env
            override fun supportsVersion(targetVersion: String?): Boolean {
                if (mcVersion == null || targetVersion == null) return true
                return mcVersion == targetVersion
            }
        })
        Log.i(TAG, "Registered dynamic plugin at: $path")
    }

    @JvmStatic
    fun discoverExternalPlugins(directory: File, mcVersion: String?) {
        if (!directory.exists() || !directory.isDirectory) return

        val files = directory.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                val soFiles = file.listFiles { _, name -> name.endsWith(".so") }
                if (soFiles != null && soFiles.isNotEmpty()) {
                    addDynamicPlugin(file.absolutePath, mcVersion = mcVersion)
                }
            }
        }
    }

    private fun resolveMinecraftVersion(versionId: String): String {
        return try {
            val vInfo = Tools.getVersionInfo(versionId)
            vInfo.inheritsFrom ?: vInfo.id
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve MC version for $versionId", e)
            versionId
        }
    }

    private fun setupLibraryPaths(javaArgList: MutableList<String>, versionId: String, mcVersion: String) {
        val versionSpecificNativesDir = File(Tools.DIR_CACHE, "natives/$versionId")
        val pluginPaths = NativePluginManager.getRuntimeLibraryPath(mcVersion)
        val nativeLibDir = Tools.NATIVE_LIB_DIR

        if (versionSpecificNativesDir.exists()) {
            val dirPath = versionSpecificNativesDir.absolutePath
            var combinedPath = "$dirPath:$nativeLibDir"
            if (pluginPaths.isNotEmpty()) {
                combinedPath = "$pluginPaths:$combinedPath"
            }

            javaArgList.add("-Djava.library.path=$combinedPath")
            javaArgList.add("-Djna.boot.library.path=$dirPath")
            javaArgList.add("-Djna.library.path=$combinedPath")
        } else if (pluginPaths.isNotEmpty()) {
            val combinedPath = "$pluginPaths:$nativeLibDir"
            javaArgList.add("-Djava.library.path=$combinedPath")
            javaArgList.add("-Djna.library.path=$combinedPath")
        }
    }

    private fun applyPluginEnvironment(javaArgList: MutableList<String>, mcVersion: String) {
        val pluginEnv = NativePluginManager.getRuntimeJVMEnv(mcVersion)
        for ((key, value) in pluginEnv) {
            if (key != "HYPERPLUGIN_PATH") {
                javaArgList.add("-D$key=$value")
            }
        }
    }

    private fun applyMioLibPatcher(javaArgList: MutableList<String>) {
        try {
            val possiblePaths = arrayOf(
                "${Tools.DIR_DATA}/MioLibPatcher/MioLibPatcher.jar",
                "${Tools.DIR_DATA}/launcher/MioLibPatcher.jar"
            )

            var patcherJar: File? = null
            for (path in possiblePaths) {
                val f = File(path)
                if (f.exists() && f.length() > 0L) {
                    patcherJar = f
                    break
                }
            }

            if (patcherJar != null) {
                javaArgList.add("-javaagent:${patcherJar.absolutePath}")
                Log.i(TAG, "SUCCESS: Applied MioLibPatcher agent: ${patcherJar.absolutePath}")
            } else {
                Log.e(TAG, "CRITICAL ERROR: MioLibPatcher.jar NOT FOUND! Axiom/Physics will crash.")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to apply MioLibPatcher agent", t)
        }
    }

    private fun prepareNatives(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String) {
        try {
            preparePhysX(activity, javaArgList, mcVersion)
            prepareRapier(activity, javaArgList, mcVersion)
            prepareImGui(activity, javaArgList, mcVersion)
            prepareZstd(activity, javaArgList)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to prepare natives", t)
        }
    }

    private fun preparePhysX(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String) {
        val physxLib = findNative(activity, mcVersion, "libPhysXJniBindings_64.so", "libphysx-jni.so")
        if (physxLib != null) {
            javaArgList.add("-Dmiolibpatcher.physx_redirect_path=${physxLib.absolutePath}")
            Log.i(TAG, "Found Official PhysX native at: ${physxLib.absolutePath}")
        }
    }

    private fun prepareRapier(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String) {
        val rapierLib = findNative(activity, mcVersion, "libsable_rapier.so")
        if (rapierLib != null) {
            javaArgList.add("-Dmiolibpatcher.sablerapier_path=${rapierLib.absolutePath}")
            Log.i(TAG, "Found Rapier native at: ${rapierLib.absolutePath}")
        }
    }

    @Throws(IOException::class)
    private fun prepareImGui(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String) {
        val nativeLibDir = activity.applicationInfo.nativeLibraryDir
        val imguiLib = findNative(activity, mcVersion, "libimgui-java.so", "libimgui.so", "libimgui-moulberry-java.so", "libimgui-moulberry92-java.so")

        if (imguiLib != null) {
            Log.i(TAG, "Found ImGui native at: ${imguiLib.absolutePath}")
            val imguiDir = File(Tools.DIR_CACHE, "imgui_natives")
            if (!imguiDir.exists() && !imguiDir.mkdirs()) {
                Log.e(TAG, "Failed to create ImGui natives directory")
            }

            val forkNames = arrayOf("libimgui-moulberry92-java64.so", "libimgui-moulberry-java64.so", "libimgui-java64.so")
            for (forkName in forkNames) {
                val forkLib = File(imguiDir, forkName)
                if (!forkLib.exists() || forkLib.length() != imguiLib.length()) {
                    FileUtils.copyFile(imguiLib, forkLib)
                }
            }

            val libName = imguiLib.name
            val libPath = imguiLib.parent
            javaArgList.add("-Dimgui.library.path=$libPath")
            javaArgList.add("-Dimgui.library.name=$libName")
            javaArgList.add("-Dimgui.moulberry.library.path=$libPath")
            javaArgList.add("-Dimgui.moulberry.library.name=$libName")
            javaArgList.add("-Dimgui.moulberry92.library.path=$libPath")
            javaArgList.add("-Dimgui.moulberry92.library.name=$libName")
            javaArgList.add("-Dimgui.moulberry.native.path=${imguiDir.absolutePath}")
            javaArgList.add("-Dimgui.moulberry92.native.path=${imguiDir.absolutePath}")
        } else {
            Log.w(TAG, "ImGui native library not found, using defaults")
            javaArgList.add("-Dimgui.library.path=$nativeLibDir")
            javaArgList.add("-Dimgui.library.name=libimgui-java.so")
            javaArgList.add("-Dimgui.moulberry92.library.path=$nativeLibDir")
            javaArgList.add("-Dimgui.moulberry92.library.name=libimgui-java.so")
        }
    }

    private fun prepareZstd(activity: AppCompatActivity, javaArgList: MutableList<String>) {
        val nativeLibDir = activity.applicationInfo.nativeLibraryDir
        val zstdLib = File(nativeLibDir, "libzstd-jni_dh-1.5.7-6.so")
        if (zstdLib.exists()) {
            val zstdLibName = "zstd-jni_dh-1.5.7-6"
            val zstdPath = zstdLib.parent
            javaArgList.add("-Dzstd.libname=$zstdLibName")
            javaArgList.add("-Dzstd.libpath=$zstdPath")
            javaArgList.add("-Ddhzstd.libname=$zstdLibName")
            javaArgList.add("-Ddhzstd.libpath=$zstdPath")
        }
    }

    private fun findNative(activity: AppCompatActivity, mcVersion: String, vararg possibleNames: String): File? {
        val nativeLibDir = activity.applicationInfo.nativeLibraryDir
        val pluginPaths = NativePluginManager.getRuntimeLibraryPath(mcVersion)

        for (name in possibleNames) {
            val f = File(nativeLibDir, name)
            if (f.exists()) return f
        }

        if (pluginPaths.isNotEmpty()) {
            val paths = pluginPaths.split(":").toTypedArray()
            for (path in paths) {
                for (name in possibleNames) {
                    val f = File(path, name)
                    if (f.exists()) return f
                }
            }
        }
        return null
    }
}
