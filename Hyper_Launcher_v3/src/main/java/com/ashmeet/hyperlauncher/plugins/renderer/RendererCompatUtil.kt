package com.ashmeet.hyperlauncher.plugins.renderer

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.Tools
import com.ashmeet.hyperlauncher.plugins.manager.NativePlugin
import com.ashmeet.hyperlauncher.plugins.manager.NativePluginManager
import net.kdt.pojavlaunch.utils.GLInfoUtils
import net.kdt.pojavlaunch.utils.JREUtils
import java.io.File
import java.util.ArrayList

object RendererCompatUtil {
    private var sCompatibleRenderers: RenderersList? = null

    @JvmStatic
    fun checkVulkanSupport(packageManager: PackageManager): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL) &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
    }

    private fun hasNativeLibrary(name: String): Boolean {
        if (File(Tools.NATIVE_LIB_DIR, name).exists()) return true

        val pluginPaths = NativePluginManager.getRuntimeLibraryPath()

        if (pluginPaths.isNotEmpty()) {
            for (path in pluginPaths.split(":").toTypedArray()) {
                if (File(path, name).exists()) return true
            }
        }
        return false
    }

    @JvmStatic
    fun getCompatibleRenderers(context: Context): RenderersList {
        sCompatibleRenderers?.let { return it }

        val resources = context.resources
        val defaultRenderers = resources.getStringArray(R.array.renderer_values)
        val defaultRendererNames = resources.getStringArray(R.array.renderer)

        val deviceHasVulkan = checkVulkanSupport(context.packageManager)
        val deviceCompatibleMesa = Build.VERSION.SDK_INT >= 29 && hasNativeLibrary("libEGL_mesa.so")
        val deviceHasOpenGLES3 = JREUtils.getDetectedVersion() >= 3
        val appHasLtw = hasNativeLibrary("libltw.so")
        val appHasMobileGlues = hasNativeLibrary("libmobileglues.so")

        val rendererIds = ArrayList<String>(defaultRenderers.size)
        val rendererNames = ArrayList<String>(defaultRendererNames.size)

        for (i in defaultRenderers.indices) {
            val rendererId = defaultRenderers[i]
            if (rendererId.contains("vulkan") && !deviceHasVulkan) continue
            if (rendererId.contains("zink") && !deviceCompatibleMesa) continue
            if (rendererId.contains("freedreno") && (!GLInfoUtils.getGlInfo().isAdreno || !deviceCompatibleMesa)) continue
            if (rendererId.contains("ltw") && (!deviceHasOpenGLES3 || !appHasLtw)) continue
            if (rendererId.contains("mobileglues") && (!deviceHasOpenGLES3 || !appHasMobileGlues)) continue
            rendererIds.add(rendererId)
            rendererNames.add(defaultRendererNames[i])
        }

        for (plugin in NativePluginManager.getPlugins()) {
            val rendererId = plugin.rendererName
            if (rendererId != null && !rendererIds.contains(rendererId)) {
                rendererIds.add(rendererId)
                val displayName = plugin.displayName
                rendererNames.add(displayName ?: ("FCL: $rendererId"))
            }
        }

        val list = RenderersList(
            rendererIds,
            rendererNames.toTypedArray()
        )
        sCompatibleRenderers = list

        return list
    }

    @JvmStatic
    fun checkRendererCompatible(context: Context, rendererName: String): Boolean {
        return getCompatibleRenderers(context).rendererIds.contains(rendererName)
    }

    @JvmStatic
    fun releaseRenderersCache() {
        sCompatibleRenderers = null
        System.gc()
    }

    class RenderersList(@JvmField val rendererIds: List<String>, @JvmField val rendererDisplayNames: Array<String>)
}
