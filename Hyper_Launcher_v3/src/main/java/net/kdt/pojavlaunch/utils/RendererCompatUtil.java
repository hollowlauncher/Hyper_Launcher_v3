package net.kdt.pojavlaunch.utils;

import static android.os.Build.VERSION.SDK_INT;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import net.ashmeet.hyperlauncher.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.plugins.NativePluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class RendererCompatUtil {
    private static RenderersList sCompatibleRenderers;

    public static boolean checkVulkanSupport(PackageManager packageManager) {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL) &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION);
    }

    private static boolean hasNativeLibrary(String name) {
        if (new File(Tools.NATIVE_LIB_DIR, name).exists()) return true;
        String pluginPaths = NativePluginManager.getRuntimeLibraryPath();
        if (!pluginPaths.isEmpty()) {
            for (String path : pluginPaths.split(":")) {
                if (new File(path, name).exists()) return true;
            }
        }
        return false;
    }

    /** Return the renderers that are compatible with this device */
    public static RenderersList getCompatibleRenderers(Context context) {
        if(sCompatibleRenderers != null) return sCompatibleRenderers;
        Resources resources = context.getResources();
        String[] defaultRenderers = resources.getStringArray(R.array.renderer_values);
        String[] defaultRendererNames = resources.getStringArray(R.array.renderer);
        boolean deviceHasVulkan = checkVulkanSupport(context.getPackageManager());
        // Current Mesa requires API29+
        boolean deviceCompatibleMesa = SDK_INT >= 29 && hasNativeLibrary("libEGL_mesa.so");
        boolean deviceHasOpenGLES3 = JREUtils.getDetectedVersion() >= 3;
        // LTW and MobileGlues are optional dependencies
        boolean appHasLtw = hasNativeLibrary("libltw.so");
        boolean appHasMobileGlues = hasNativeLibrary("libmobileglues.so");
        List<String> rendererIds = new ArrayList<>(defaultRenderers.length);
        List<String> rendererNames = new ArrayList<>(defaultRendererNames.length);
        for(int i = 0; i < defaultRenderers.length; i++) {
            String rendererId = defaultRenderers[i];
            if(rendererId.contains("vulkan") && !deviceHasVulkan) continue;
            if(rendererId.contains("zink") && !deviceCompatibleMesa) continue;
            // freedreno is available only on Adreno GPUs
            if(rendererId.contains("freedreno") && (!(GLInfoUtils.getGlInfo().isAdreno()) || !deviceCompatibleMesa)) continue;
            if(rendererId.contains("ltw") && (!deviceHasOpenGLES3 || !appHasLtw)) continue;
            if(rendererId.contains("mobileglues") && (!deviceHasOpenGLES3 || !appHasMobileGlues)) continue;
            rendererIds.add(rendererId);
            rendererNames.add(defaultRendererNames[i]);
        }
        sCompatibleRenderers = new RenderersList(rendererIds,
                rendererNames.toArray(new String[0]));

        return sCompatibleRenderers;
    }

    /** Checks if the renderer Id is compatible with the current device */
    public static boolean checkRendererCompatible(Context context, String rendererName) {
        return getCompatibleRenderers(context).rendererIds.contains(rendererName);
    }

    /** Releases the cache of compatible renderers. */
    public static void releaseRenderersCache() {
        sCompatibleRenderers = null;
        System.gc();
    }

    public static class RenderersList {
        public final List<String> rendererIds;
        public final String[] rendererDisplayNames;

        public RenderersList(List<String> rendererIds, String[] rendererDisplayNames) {
            this.rendererIds = rendererIds;
            this.rendererDisplayNames = rendererDisplayNames;
        }
    }
}