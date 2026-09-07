package net.kdt.pojavlaunch.plugins;

import android.content.Context;
import android.util.Log;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.ComparableVersionString;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativePluginManager {
    private static final String TAG = "NativePluginManager";

    private static final List<NativePlugin> sPlugins = new ArrayList<>();

    public static void registerPlugin(NativePlugin plugin) {
        sPlugins.add(plugin);
    }

    public static void discoverAarPlugins(Context context) {
        File destDir = new File(Tools.DIR_CACHE, "hyper_plugin_libs");
        destDir.mkdirs();
        createPthreadShim(destDir);

        registerPlugin(new NativePlugin() {
            @Override
            public String[] getPaths() {
                return new String[]{context.getApplicationInfo().nativeLibraryDir, destDir.getAbsolutePath()};
            }

            @Override
            public Map<String, String> getJVMEnv() {
                Map<String, String> env = new HashMap<>();
                env.put("HYPERPLUGIN_PATH", destDir.getAbsolutePath());
                return env;
            }
        });

        discoverFCLPlugins(context);
    }

    public static void discoverFCLPlugins(Context context) {
        List<LibraryPlugin> fclPlugins = LibraryPlugin.discoverAllPlugins(context);
        for (LibraryPlugin plugin : fclPlugins) {
            final String libDir = plugin.getLibraryPath();
            final String envString = plugin.getMetaData().getString(LibraryPlugin.METADATA_FCL_ENVIRONMENT);
            final String minVerStr = plugin.getMetaData().getString(LibraryPlugin.METADATA_FCL_MIN_MC_VER);
            final String maxVerStr = plugin.getMetaData().getString(LibraryPlugin.METADATA_FCL_MAX_MC_VER);
            
            registerPlugin(new NativePlugin() {
                @Override
                public String[] getPaths() {
                    return new String[]{libDir};
                }

                @Override
                public Map<String, String> getJVMEnv() {
                    Map<String, String> envMap = new HashMap<>();
                    if (envString != null && !envString.isEmpty()) {
                        // Assuming space or semicolon separated key=value pairs
                        String[] pairs = envString.split("[ ;]");
                        for (String pair : pairs) {
                            String[] kv = pair.split("=", 2);
                            if (kv.length == 2) {
                                String key = kv[0].trim();
                                String value = kv[1].trim().replace("{nativeLibraryDir}", libDir);
                                if (!key.isEmpty()) {
                                    envMap.put(key, value);
                                    Log.i(TAG, "Plugin " + plugin.getId() + " env: " + key + "=" + value);
                                }
                            }
                        }
                    }
                    return envMap;
                }

                @Override
                public boolean supportsVersion(String mcVersion) {
                    if (mcVersion == null) return true;
                    ComparableVersionString current = ComparableVersionString.parse(mcVersion);
                    if (!current.isValid()) return true;

                    if (minVerStr != null && !minVerStr.isEmpty()) {
                        ComparableVersionString min = ComparableVersionString.parse(minVerStr);
                        if (min.isValid() && current.compareTo(min) < 0) return false;
                    }

                    if (maxVerStr != null && !maxVerStr.isEmpty()) {
                        ComparableVersionString max = ComparableVersionString.parse(maxVerStr);
                        if (max.isValid() && current.compareTo(max) > 0) return false;
                    }

                    return true;
                }
            });
            Log.i(TAG, "Discovered FCL plugin: " + plugin.getId());
        }
    }

    private static void createPthreadShim(File destDir) {
        File shim = new File(destDir, "libpthread.so.0");
        if (shim.exists()) return;
        
        // On Android, libpthread.so is usually a symlink to libc.so or integrated.
        // We'll try to find it in system lib dirs.
        String[] sysLibDirs = {"/system/lib64", "/system/lib", "/apex/com.android.runtime/lib64/bionic", "/apex/com.android.runtime/lib/bionic"};
        File sourcePthread = null;
        for (String dir : sysLibDirs) {
            File f = new File(dir, "libpthread.so");
            if (f.exists()) {
                sourcePthread = f;
                break;
            }
        }
        
        if (sourcePthread != null) {
            try {
                // Use a symlink if possible (requires API 21+)
                android.system.Os.symlink(sourcePthread.getAbsolutePath(), shim.getAbsolutePath());
                Log.i("jrelog", "Created libpthread.so.0 shim (symlink)");
            } catch (Exception e) {
                Log.e("jrelog", "Failed to create libpthread.so.0 shim", e);
            }
        }
    }

    public static String getRuntimeLibraryPath() {
        return getRuntimeLibraryPath(null);
    }

    public static String getRuntimeLibraryPath(String mcVersion) {
        StringBuilder sb = new StringBuilder();
        for (NativePlugin plugin : sPlugins) {
            if (mcVersion != null && !plugin.supportsVersion(mcVersion)) continue;
            for (String path : plugin.getPaths()) {
                if (sb.length() > 0) {
                    sb.append(":");
                }
                sb.append(path);
            }
        }
        return sb.toString();
    }

    public static Map<String, String> getRuntimeJVMEnv() {
        return getRuntimeJVMEnv(null);
    }

    public static Map<String, String> getRuntimeJVMEnv(String mcVersion) {
        Map<String, String> env = new HashMap<>();
        for (NativePlugin plugin : sPlugins) {
            if (mcVersion != null && !plugin.supportsVersion(mcVersion)) continue;
            env.putAll(plugin.getJVMEnv());
        }
        return env;
    }
}
