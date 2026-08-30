package net.kdt.pojavlaunch.plugins;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences;

import net.kdt.pojavlaunch.Architecture;
import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NativePluginManager {
    private static final String TAG = "NativePluginManager";
    public static final int SUPPORTED_PLUGIN_API_VERSION = 1;

    private static final List<NativePlugin> sPlugins = new ArrayList<>();
    private static final List<BundledLibrary> sDiscoveredLibraries = new ArrayList<>();

    public static void registerPlugin(NativePlugin plugin) {
        sPlugins.add(plugin);
    }

    public static void discoverAarPlugins(Context context) {
        registerPlugin(new NativePlugin() {
            @Override
            public String[] getPaths() {
                return new String[]{context.getApplicationInfo().nativeLibraryDir};
            }

            @Override
            public Map<String, String> getJVMEnv() {
                return new HashMap<>();
            }
        });
    }

    public static void discoverHyperPluginBundle(Context context) {
        LibraryPlugin bundle = LibraryPlugin.discoverPlugin(context, LibraryPlugin.ID_HYPER_PLUGIN);
        if (bundle == null) return;

        String pluginType = bundle.getMetaData().getString(LibraryPlugin.METADATA_PLUGIN_TYPE);
        if (!"native-bundle".equals(pluginType)) {
            Log.w(TAG, "HyperPlugin bundle has invalid type: " + pluginType);
            return;
        }

        int apiVersion = bundle.getMetaData().getInt(LibraryPlugin.METADATA_API_VERSION, 0);
        if (apiVersion > SUPPORTED_PLUGIN_API_VERSION) {
            Log.w(TAG, "HyperPlugin bundle API v" + apiVersion + " newer than supported; skipping");
            return;
        }

        String libsMetadata = bundle.getMetaData().getString(LibraryPlugin.METADATA_PLUGIN_LIBS);
        if (libsMetadata != null) {
            parseAndRegisterLibraries(libsMetadata, bundle.getLibraryPath());
        }

        // Copy libraries to local cache to bypass linker restrictions on modern Android
        String finalLibraryPath = syncPluginLibraries(bundle);

        registerPlugin(new NativePlugin() {
            @Override
            public String[] getPaths() {
                return new String[]{finalLibraryPath};
            }

            @Override
            public Map<String, String> getJVMEnv() {
                Map<String, String> env = new HashMap<>();
                env.put("HYPERPLUGIN_PATH", finalLibraryPath);
                return env;
            }
        });
    }

    private static String syncPluginLibraries(LibraryPlugin bundle) {
        String sourcePath = bundle.getLibraryPath();
        File sourceDir = new File(sourcePath);
        File destDir = new File(Tools.DIR_CACHE, "hyper_plugin_libs");
        if (!destDir.exists()) destDir.mkdirs();

        // Create libpthread.so.0 shim for desktop Linux libraries
        createPthreadShim(destDir);

        Log.i("jrelog", "Syncing libraries for bundle: " + bundle.getId());

        for (BundledLibrary lib : sDiscoveredLibraries) {
            // 1. Try to find the actual file and its name
            String actualSoName = lib.soName;
            File sourceFile = new File(sourceDir, lib.soName);
            boolean found = false;

            if (sourceFile.exists()) {
                found = true;
            } else {
                // Try versioned name
                String versionedName = "lib" + lib.name + "-" + lib.version + ".so";
                sourceFile = new File(sourceDir, versionedName);
                if (sourceFile.exists()) {
                    actualSoName = versionedName;
                    found = true;
                }
            }

            File destFile = new File(destDir, actualSoName);
            if (found) {
                if (!destFile.exists() || destFile.length() != sourceFile.length()) {
                    try {
                        copyFile(sourceFile, destFile);
                        Log.i("jrelog", "Synced from nativeLibraryDir: " + actualSoName);
                    } catch (IOException e) {
                        Log.e("jrelog", "Failed to copy from nativeLibraryDir: " + actualSoName, e);
                    }
                }
            } else {
                // 2. Try to extract from APK
                try {
                    // Try exact name
                    if (extractFromApk(bundle.getApkPath(), lib.soName, destFile)) {
                        Log.i("jrelog", "Extracted from APK: " + lib.soName);
                        found = true;
                    } else if (!lib.version.equals("unknown")) {
                        // Try versioned name
                        String versionedName = "lib" + lib.name + "-" + lib.version + ".so";
                        destFile = new File(destDir, versionedName);
                        if (extractFromApk(bundle.getApkPath(), versionedName, destFile)) {
                            Log.i("jrelog", "Extracted from APK (versioned): " + versionedName);
                            actualSoName = versionedName;
                            found = true;
                        }
                    }
                } catch (IOException e) {
                    Log.e("jrelog", "Failed to extract from APK: " + lib.soName, e);
                }
            }

            if (found) {
                // Always update registry with the name we actually found and synced
                updateLibrarySoName(lib.name, actualSoName);
            } else {
                Log.e("jrelog", "Could not find library: " + lib.name);
            }
        }

        return destDir.getAbsolutePath();
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    android.system.Os.symlink(sourcePthread.getAbsolutePath(), shim.getAbsolutePath());
                    Log.i("jrelog", "Created libpthread.so.0 shim (symlink)");
                } else {
                    copyFile(sourcePthread, shim);
                    Log.i("jrelog", "Created libpthread.so.0 shim (copy)");
                }
            } catch (Exception e) {
                Log.e("jrelog", "Failed to create libpthread.so.0 shim", e);
            }
        }
    }

    private static void updateLibrarySoName(String name, String newSoName) {
        for (int i = 0; i < sDiscoveredLibraries.size(); i++) {
            BundledLibrary old = sDiscoveredLibraries.get(i);
            if (old.name.equals(name)) {
                sDiscoveredLibraries.set(i, new BundledLibrary(old.name, newSoName, old.version, old.strategy, old.optional));
                break;
            }
        }
    }

    private static boolean extractFromApk(String apkPath, String soName, File destFile) throws IOException {
        if (apkPath == null) return false;
        try (ZipFile zipFile = new ZipFile(apkPath)) {
            // Native libs are usually in lib/arm64-v8a/, lib/armeabi-v7a/, etc.
            // We'll search for the entry ending with the lib name.
            int arch = Architecture.getDeviceArchitecture();
            String zipPath = null;
            if (arch == Architecture.ARCH_ARM64) zipPath = "lib/arm64-v8a/" + soName;
            else if (arch == Architecture.ARCH_ARM) zipPath = "lib/armeabi-v7a/" + soName;
            else if (arch == Architecture.ARCH_X86_64) zipPath = "lib/x86_64/" + soName;
            else if (arch == Architecture.ARCH_X86) zipPath = "lib/x86/" + soName;

            ZipEntry entry = null;
            if (zipPath != null) entry = zipFile.getEntry(zipPath);
            
            if (entry == null) {
                // Fallback: search all entries
                java.util.Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry e = entries.nextElement();
                    if (e.getName().endsWith("/" + soName)) {
                        entry = e;
                        break;
                    }
                }
            }

            if (entry != null) {
                try (InputStream is = zipFile.getInputStream(entry);
                     FileOutputStream os = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static void copyFile(File source, File dest) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    private static void parseAndRegisterLibraries(String libsMetadata, String sourcePath) {
        String[] entries = libsMetadata.split(",");
        File sourceDir = new File(sourcePath);

        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length >= 1) {
                String name = parts[0].trim();
                if (name.isEmpty()) continue;
                String version = parts.length > 1 ? parts[1].trim() : "unknown";

                BundledLibrary.LoadStrategy strategy = BundledLibrary.LoadStrategy.SYSTEM_LOAD;
                boolean optional = true;
                
                // Try to find the actual .so file to determine the correct soName
                String soName = "lib" + name + ".so";
                if (sourceDir.exists()) {
                    File plainLib = new File(sourceDir, soName);
                    File versionedLib = new File(sourceDir, "lib" + name + "-" + version + ".so");
                    if (!plainLib.exists() && versionedLib.exists()) {
                        soName = versionedLib.getName();
                    }
                }

                if (name.toLowerCase().contains("physx") || name.toLowerCase().contains("rapier")) {
                    strategy = BundledLibrary.LoadStrategy.DLOPEN_GLOBAL;
                    optional = false;
                } else if (name.equals("discord-rpc")) {
                    optional = true;
                }

                sDiscoveredLibraries.add(new BundledLibrary(name, soName, version, strategy, optional));
            }
        }
    }

    public static List<BundledLibrary> getDiscoveredLibraries() {
        return sDiscoveredLibraries;
    }

    public static boolean isLibraryDiscovered(String name) {
        for (BundledLibrary lib : sDiscoveredLibraries) {
            if (lib.name.equals(name)) return true;
        }
        return false;
    }

    public static boolean isLibraryEnabled(String name) {
        for (BundledLibrary lib : sDiscoveredLibraries) {
            if (lib.name.equals(name)) {
                return !lib.optional || LauncherPreferences.isPluginLibraryEnabled(name);
            }
        }
        return false;
    }

    public static void loadOptionalLibraries(Context context) {
        String hyperPath = getRuntimeJVMEnv().get("HYPERPLUGIN_PATH");
        Log.i("jrelog", "Attempting to load optional libraries. HyperPath: " + hyperPath);
        if (hyperPath == null) return;

        for (BundledLibrary lib : sDiscoveredLibraries) {
            Log.i("jrelog", "Discovered lib: " + lib.name + ", strategy: " + lib.strategy + ", optional: " + lib.optional);
            if (lib.strategy == BundledLibrary.LoadStrategy.SYSTEM_LOAD) {
                if (LauncherPreferences.isPluginLibraryEnabled(lib.name)) {
                    try {
                        File libFile = new File(hyperPath, lib.soName);
                        if (libFile.exists()) {
                            if (libFile.canRead()) {
                                System.load(libFile.getAbsolutePath());
                                Log.i("jrelog", "Successfully loaded plugin library: " + lib.name + " from " + libFile.getAbsolutePath());
                            } else {
                                Log.e("jrelog", "Plugin library file NOT READABLE: " + libFile.getAbsolutePath());
                            }
                        } else {
                            Log.w("jrelog", "Plugin library file missing: " + libFile.getAbsolutePath());
                        }
                    } catch (UnsatisfiedLinkError e) {
                        Log.e("jrelog", "Failed to load plugin library: " + lib.name, e);
                    }
                } else {
                    Log.i("jrelog", "Plugin library disabled by user: " + lib.name);
                }
            }
        }
    }

    public static String getRuntimeLibraryPath() {
        StringBuilder sb = new StringBuilder();
        for (NativePlugin plugin : sPlugins) {
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
        Map<String, String> env = new HashMap<>();
        for (NativePlugin plugin : sPlugins) {
            env.putAll(plugin.getJVMEnv());
        }
        return env;
    }
}
