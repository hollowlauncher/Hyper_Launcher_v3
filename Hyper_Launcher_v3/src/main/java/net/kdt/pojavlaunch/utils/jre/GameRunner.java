package net.kdt.pojavlaunch.utils.jre;

import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences;
import com.ashmeet.hyperlauncher.skin.PlayerSkin;
import com.ashmeet.hyperlauncher.skin.SkinManager;
import com.ashmeet.hyperlauncher.skin.SkinManagerKt;

import net.ashmeet.hyperlauncher.R;
import net.kdt.pojavlaunch.Architecture;
import net.kdt.pojavlaunch.JVersionList;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.AuthType;
import net.kdt.pojavlaunch.authenticator.accounts.Account;
import net.kdt.pojavlaunch.instances.Instance;
import net.kdt.pojavlaunch.lifecycle.LifecycleAwareAlertDialog;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.plugins.NativePluginManager;
import com.ashmeet.hyperlauncher.utils.DateUtils;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.utils.GLInfoUtils;
import net.kdt.pojavlaunch.utils.GameOptionsUtils;
import net.kdt.pojavlaunch.utils.JREUtils;
import net.kdt.pojavlaunch.utils.JSONUtils;
import net.kdt.pojavlaunch.utils.MCOptionUtils;
import net.kdt.pojavlaunch.utils.OldVersionsUtils;
import net.kdt.pojavlaunch.utils.RendererCompatUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GameRunner {
    /**
     * Optimization mods based on Sodium can mitigate the render distance issue. Check if Sodium
     * or its derivative is currently installed to skip the render distance check.
     * @param gameDir current game directory
     * @return whether sodium or a sodium-based mod is installed
     */
    private static boolean hasSodium(File gameDir) {
        File modsDir = new File(gameDir, "mods");
        File[] mods = modsDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        if(mods == null) return false;
        for(File file : mods) {
            String name = file.getName();
            if(name.contains("sodium") ||
                    name.contains("embeddium") ||
                    name.contains("rubidium")) return true;
        }
        return false;
    }

    /**
     * Check if Angelica is currently installed to allow usage of LTW
     * @param gameDir current game directory
     * @return whether Angelica is installed
     */
    private static boolean hasAngelica(File gameDir) {
        File modsDir = new File(gameDir, "mods");
        File[] mods = modsDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        if(mods == null) return false;
        for(File file : mods) {
            String name = file.getName();
            if(name.contains("angelica")) return true;
        }
        return false;
    }

    /**
     * Initialize OpenGL and do checks to see if the GPU of the device is affected by the render
     * distance issue.

     * Currently only checks whether the user has an Adreno GPU capable of OpenGL ES 3.

     * This issue is caused by a very severe limit on the amount of GL buffer names that could be allocated
     * by the Adreno properietary GLES driver.

     * @return whether the GPU is affected by the Large Thin Wrapper render distance issue on vanilla
     */

    private static boolean affectedByRenderDistanceIssue(JVersionList.Version version) {
        if(LauncherPreferences.PREF_USE_ANGLE) return false;
        GLInfoUtils.GLInfo info = GLInfoUtils.getGlInfo();
        return info.isAdreno() &&
                info.glesMajorVersion >= 3 &&
                // 1.21.5 fixes the RD issue, released on march 25 2025
                DateUtils.dateBefore(DateUtils.getOriginalReleaseDate(version), 2025, 2, 25);
    }

    private static boolean checkRenderDistance(JVersionList.Version version, File gamedir) {
        if(!affectedByRenderDistanceIssue(version)) return false;
        if(hasSodium(gamedir)) return false;
        try {
            MCOptionUtils.load();
        }catch (Exception e) {
            Log.e("Tools", "Failed to load config", e);
        }
        int renderDistance = GameOptionsUtils.parseIntDefault(MCOptionUtils.get("renderDistance"),12);
        // 7 is the render distance "magic number" above which MC creates too many buffers
        // for Adreno's OpenGL ES implementation
        return renderDistance > 7;
    }

    private static boolean isGl4esCompatible(JVersionList.Version version) {
        return DateUtils.dateBefore(DateUtils.getOriginalReleaseDate(version), 2025, 1, 7);
    }

    private static boolean isCompatContext(JVersionList.Version version) {
        // Day before the release date of 21w10a, the first OpenGL 3 Core Minecraft version
        return DateUtils.dateBefore(DateUtils.getOriginalReleaseDate(version), 2021, 3, 9);
    }

    private static boolean showDialog(AppCompatActivity activity, int message) throws InterruptedException {
        LifecycleAwareAlertDialog.DialogCreator dialogCreator = ((alertDialog, dialogBuilder) ->
                dialogBuilder.setMessage(activity.getString(message))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, (d, w)->{}));
        return LifecycleAwareAlertDialog.haltOnDialog(activity.getLifecycle(), activity, dialogCreator);
    }

    // Autoswitch to a modern renderer if supported, otherwise - crash with resId dialog message. Returns renderer strings if succeeded
    private static String switchModernRenderer(RendererCompatUtil.RenderersList supported, Instance instance, AppCompatActivity activity, int resId) throws InterruptedException, IOException {
        String targetRenderer = null;
        if (supported.rendererIds.contains("mobileglues")) {
            targetRenderer = "mobileglues";
        } else if (supported.rendererIds.contains("opengles3_ltw")) {
            targetRenderer = "opengles3_ltw";
        }

        if(targetRenderer != null) {
            instance.renderer = targetRenderer;
            instance.write();
            return targetRenderer;
        }else {
            showDialog(activity, resId);
            System.exit(0);
            return null;
        }
    }

    public static void launchGame(final AppCompatActivity activity, Account account,
                                  Instance instance, String versionId, File[] classpath, String rendererName) throws Throwable {
        int freeDeviceMemory = Tools.getFreeDeviceMemory(activity);
        int localeString;
        int freeAddressSpace = Architecture.is32BitsDevice() ? Tools.getMaxContinuousAddressSpaceSize() : -1;
        Log.i("MemStat", "Free RAM: " + freeDeviceMemory + " Addressable: " + freeAddressSpace);
        if(freeDeviceMemory > freeAddressSpace && freeAddressSpace != -1) {
            freeDeviceMemory = freeAddressSpace;
            localeString = R.string.address_memory_warning_msg;
        } else {
            localeString = R.string.memory_warning_msg;
        }

        if(LauncherPreferences.PREF_RAM_ALLOCATION > freeDeviceMemory) {
            int finalDeviceMemory = freeDeviceMemory;
            LifecycleAwareAlertDialog.DialogCreator dialogCreator = (dialog, builder) ->
                    builder.setMessage(activity.getString(localeString, finalDeviceMemory, LauncherPreferences.PREF_RAM_ALLOCATION))
                            .setPositiveButton(android.R.string.ok, (d, w)->{});

            if(LifecycleAwareAlertDialog.haltOnDialog(activity.getLifecycle(), activity, dialogCreator)) {
                return; // If the dialog's lifecycle has ended, return without
                // actually launching the game, thus giving us the opportunity
                // to start after the activity is shown again
            }
        }
        File gamedir = instance.getGameDirectory();
        JVersionList.Version versionInfo = Tools.getVersionInfo(versionId);

        // Switch renderer to GL4ES when running a compat context version on LTW or MobileGlues
        boolean isModernWrapper = rendererName.equals("opengles3_ltw") || rendererName.equals("mobileglues");
        if(isCompatContext(versionInfo) && !hasAngelica(gamedir) && isModernWrapper) {
            instance.renderer = rendererName = "opengles2";
            instance.write();
        }

        boolean isGl4es = rendererName.equals("opengles2");
        RendererCompatUtil.RenderersList supportedRenderers = RendererCompatUtil.getCompatibleRenderers(activity);
        
        // Block Sodium from running with GL4ES on 1.17+
        if(!isCompatContext(versionInfo) && isGl4es && hasSodium(gamedir)) {
            rendererName = switchModernRenderer(supportedRenderers, instance, activity, R.string.compat_sodium_not_supported);
        }

        // Switch renderer to a modern one when running 1.21.5
        if(!isGl4esCompatible(versionInfo) && isGl4es) {
            rendererName = switchModernRenderer(supportedRenderers, instance, activity, R.string.compat_version_not_supported);
        }
        RendererCompatUtil.releaseRenderersCache();

        isModernWrapper = rendererName.equals("opengles3_ltw") || rendererName.equals("mobileglues");

        if(isModernWrapper && checkRenderDistance(versionInfo, gamedir)) {
            if(showDialog(activity, R.string.ltw_render_distance_warning_msg)) return;
            // If the code goes here, it means that the user clicked "OK". Fix the render distance.
            try {
                MCOptionUtils.set("renderDistance", "7");
                MCOptionUtils.save();
            }catch (Exception e) {
                Log.e("Tools", "Failed to fix render distance setting", e);
            }
        }

        GameOptionsUtils.fixOptions(isModernWrapper);

        if(isModernWrapper && GLInfoUtils.getGlInfo().forcedMsaa) {
            if(showDialog(activity, R.string.ltw_4x_msaa_warning_msg)) return;
        }

        int requiredJavaVersion = 8;
        if(versionInfo.javaVersion != null) requiredJavaVersion = versionInfo.javaVersion.majorVersion;

        Runtime runtime = MultiRTUtils.forceReread(pickRuntime(instance, requiredJavaVersion));

        // Pre-process specific files
        disableSplash(gamedir);
        List<String> launchArgs = getMoJsonClientArgs(account, versionInfo, gamedir);

        // Select the appropriate openGL version
        OldVersionsUtils.selectOpenGlVersion(versionInfo);

        ArrayList<String> launchClassPath = new ArrayList<>(classpath.length);
        for(File classpathEntry : classpath) {
            String entryPath = classpathEntry.getAbsolutePath();
            if(!classpathEntry.exists()) {
                Log.w("GameRunner", "Skipped classpath entry " + entryPath + " because it is missing");
            }
            launchClassPath.add(entryPath);
        }
        launchClassPath.trimToSize();

        List<String> javaArgList = new ArrayList<>();

        if (versionInfo.logging != null && versionInfo.logging.client != null && versionInfo.logging.client.file != null) {
            String configFile = Tools.DIR_DATA + "/security/" + versionInfo.logging.client.file.id.replace("client", "log4j-rce-patch");
            if (!new File(configFile).exists()) {
                configFile = Tools.DIR_GAME_NEW + "/" + versionInfo.logging.client.file.id;
            }
            javaArgList.add("-Dlog4j.configurationFile=" + configFile);
        }

        File versionSpecificNativesDir = new File(Tools.DIR_CACHE, "natives/"+versionId);
        if(versionSpecificNativesDir.exists()) {
            String dirPath = versionSpecificNativesDir.getAbsolutePath();
            String pluginPaths = NativePluginManager.getRuntimeLibraryPath();
            String combinedPath = dirPath + ":" + Tools.NATIVE_LIB_DIR;
            if (!pluginPaths.isEmpty()) {
                combinedPath = pluginPaths + ":" + combinedPath;
            }

            javaArgList.add("-Djava.library.path="+combinedPath);
            javaArgList.add("-Djna.boot.library.path="+dirPath);
            javaArgList.add("-Djna.library.path="+combinedPath);
        }

        File lwjglExtractDir = new File(Tools.DIR_CACHE, "lwjgl_native/"+versionId);
        FileUtils.ensureDirectory(lwjglExtractDir);
        javaArgList.add("-Dorg.lwjgl.system.SharedLibraryExtractPath="+lwjglExtractDir.getAbsolutePath());

        addAuthlibInjectorArgs(javaArgList, account);
        if (account.authType == AuthType.LOCAL) {
            SkinManager skinManager = new SkinManager(SkinManagerKt.androidSkinAnalyzerFacade);
            skinManager.startServer();

            PlayerSkin playerSkin = null;
            if (account.skinPath != null) {
                File skinFile = new File(account.skinPath);
                if (skinFile.exists()) {
                    try {
                        byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(skinFile);
                        playerSkin = SkinManagerKt.androidSkinAnalyzerFacade.prepareSkin(bytes);
                    } catch (Exception e) {
                        Log.e("GameRunner", "Failed to load local skin", e);
                    }
                }
            }

            skinManager.getServer().addCharacter(
                    account.username,
                    account.profileId,
                    playerSkin,
                    null
            );
            
            String injectorPath = Tools.DIR_DATA + "/authlib-injector/authlib-injector.jar";
            File injectorJar = new File(injectorPath);
            if (injectorJar.exists() && injectorJar.length() > 0) {
                javaArgList.add("-javaagent:" + injectorPath + "=" + skinManager.getAuthlibUrl());
            } else {
                Log.e("GameRunner", "authlib-injector.jar is missing or empty! Skipping javaagent to prevent crash.");
            }
        }

        javaArgList.addAll(getMoJsonJvmArgs(versionId));

        javaArgList.addAll(JREUtils.parseJavaArguments(instance.getLaunchArgs()));

        // Apply MioLibPatcher agent
        try {
            String patcherPath = Tools.DIR_DATA + "/MioLibPatcher/MioLibPatcher.jar";
            File patcherJar = new File(patcherPath);
            Log.i("GameRunner", "Checking for MioLibPatcher at: " + patcherPath);
            
            if (!patcherJar.exists() || patcherJar.length() == 0) {
                patcherPath = Tools.DIR_DATA + "/launcher/MioLibPatcher.jar";
                patcherJar = new File(patcherPath);
                Log.i("GameRunner", "Checking for MioLibPatcher at alternative path: " + patcherPath);
            }

            if (patcherJar.exists() && patcherJar.length() > 0) {
                String agentArg = "-javaagent:" + patcherJar.getAbsolutePath();
                javaArgList.add(agentArg);
                Log.i("GameRunner", "SUCCESS: Applied MioLibPatcher agent: " + agentArg);
            } else {
                Log.e("GameRunner", "CRITICAL ERROR: MioLibPatcher.jar NOT FOUND or EMPTY! Axiom will crash.");
                // List files in DIR_DATA to help debug
                File launcherDir = new File(Tools.DIR_DATA, "launcher");
                if (launcherDir.exists()) {
                    Log.i("GameRunner", "Files in " + launcherDir.getAbsolutePath() + ": " + java.util.Arrays.toString(launcherDir.list()));
                } else {
                    Log.i("GameRunner", "Launcher directory does not exist in DIR_DATA: " + Tools.DIR_DATA);
                }
            }
        } catch (Throwable t) {
            Log.e("GameRunner", "Failed to apply MioLibPatcher agent", t);
        }

        try {
            String nativeLibDir = activity.getApplicationInfo().nativeLibraryDir;
            String pluginPath = NativePluginManager.getRuntimeJVMEnv().get("HYPERPLUGIN_PATH");
            File imguiLib = findImguiNative(activity);

            if (imguiLib != null) {
                Log.i("GameRunner", "Found ImGui native at: " + imguiLib.getAbsolutePath());
                File imguiDir = new File(Tools.DIR_CACHE, "imgui_natives");
                if (!imguiDir.exists() && !imguiDir.mkdirs()) {
                    Log.e("GameRunner", "Failed to create ImGui natives directory");
                }
                
                String[] forkNames = {"libimgui-moulberry92-java64.so", "libimgui-moulberry-java64.so", "libimgui-java64.so"};
                for (String forkName : forkNames) {
                    File forkLib = new File(imguiDir, forkName);
                    if (!forkLib.exists() || forkLib.length() != imguiLib.length()) {
                        org.apache.commons.io.FileUtils.copyFile(imguiLib, forkLib);
                    }
                }
                
                String libName = imguiLib.getName();
                javaArgList.add("-Dimgui.library.path=" + imguiLib.getParent());
                javaArgList.add("-Dimgui.library.name=" + libName);

                javaArgList.add("-Dimgui.moulberry.library.path=" + imguiLib.getParent());
                javaArgList.add("-Dimgui.moulberry.library.name=" + libName);
                javaArgList.add("-Dimgui.moulberry92.library.path=" + imguiLib.getParent());
                javaArgList.add("-Dimgui.moulberry92.library.name=" + libName);
                
                // Set native.path as well, some loaders use this
                javaArgList.add("-Dimgui.moulberry.native.path=" + imguiDir.getAbsolutePath());
                javaArgList.add("-Dimgui.moulberry92.native.path=" + imguiDir.getAbsolutePath());
            } else {
                Log.w("GameRunner", "ImGui native library not found");
                javaArgList.add("-Dimgui.library.path=" + nativeLibDir);
                javaArgList.add("-Dimgui.library.name=libimgui-java.so");
                javaArgList.add("-Dimgui.moulberry92.library.path=" + nativeLibDir);
                javaArgList.add("-Dimgui.moulberry92.library.name=libimgui-java.so");
            }

            File zstdLib = new File(nativeLibDir, "libzstd-jni_dh-1.5.7-6.so");
            if (!zstdLib.exists() && pluginPath != null) {
                zstdLib = new File(pluginPath, "libzstd-jni_dh-1.5.7-6.so");
            }

            if (zstdLib.exists()) {
                String zstdLibName = "zstd-jni_dh-1.5.7-6";
                javaArgList.add("-Dzstd.libname=" + zstdLibName);
                javaArgList.add("-Dzstd.libpath=" + zstdLib.getParent());
                javaArgList.add("-Ddhzstd.libname=" + zstdLibName);
                javaArgList.add("-Ddhzstd.libpath=" + zstdLib.getParent());
            }

            File rapierLib = new File(nativeLibDir, "libsable_rapier.so");
            if (!rapierLib.exists()) rapierLib = new File(nativeLibDir, "libPhysXJniBindings_64.so");

            // Check plugin path if not found in app native dir
            String hyperPluginPath = NativePluginManager.getRuntimeJVMEnv().get("HYPERPLUGIN_PATH");
            if (!rapierLib.exists() && hyperPluginPath != null) {
                rapierLib = new File(hyperPluginPath, "libsable_rapier.so");
                if (!rapierLib.exists()) rapierLib = new File(hyperPluginPath, "libPhysXJniBindings_64.so");
            }
            
            if (rapierLib.exists()) {
                javaArgList.add("-Dsable_rapier_path=" + rapierLib.getAbsolutePath());
                
                // Also pre-populate the PhysX cache to be safe
                try {
                    String physxVersion = "2.3.2"; // From crash log
                    File physxCacheDir = new File(Tools.DIR_CACHE, "de.fabmax.physx-jni/" + physxVersion);
                    if (!physxCacheDir.exists() && !physxCacheDir.mkdirs()) {
                        Log.e("GameRunner", "Failed to create PhysX cache directory");
                    }
                    File physxCacheFile = new File(physxCacheDir, "libPhysXJniBindings_64.so");
                    if (!physxCacheFile.exists() || physxCacheFile.length() != rapierLib.length()) {
                        org.apache.commons.io.FileUtils.copyFile(rapierLib, physxCacheFile);
                        Log.i("GameRunner", "Pre-populated PhysX cache at: " + physxCacheFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    Log.e("GameRunner", "Failed to pre-populate PhysX cache", e);
                }
            }
        } catch (Throwable t) {
            Log.e("GameRunner", "Failed to prepare natives", t);
        }

        JREUtils.setEnviroimentForGame(activity, rendererName);
        JREUtils.chdir(instance.getGameDirectory().getAbsolutePath());

        // Load optional plugin libraries early so they can be discovered by the renderer loader
        NativePluginManager.loadOptionalLibraries(activity);

        String rendererLibrary = JREUtils.loadGraphicsLibrary(rendererName);
        if(rendererLibrary == null) {
            Log.i("GameRunner", "Falling back to GL4ES 1.1.4");
            rendererName = "opengles2";
            rendererLibrary = JREUtils.loadGraphicsLibrary(rendererName);
        }
        if(rendererLibrary == null) {
            if(showDialog(activity, R.string.gr_err_renderer_load_Failed)) return;
            System.exit(0);
        }
        javaArgList.add("-Dorg.lwjgl.opengl.libname=libGLMojo.so");
        javaArgList.add("-Dorg.lwjgl.freetype.libname="+ Tools.NATIVE_LIB_DIR+"/libfreetype.so");

        activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.autoram_info_msg,LauncherPreferences.PREF_RAM_ALLOCATION), Toast.LENGTH_SHORT).show());

        Log.i("GameRunner", "Running with "+ launchArgs.toString());

        try {
            JavaRunner.nativeSetupExit(activity);
            JavaRunner.startJvm(runtime, javaArgList, launchClassPath, versionInfo.mainClass, launchArgs);
        }catch (VMLoadException e) {
            LifecycleAwareAlertDialog.DialogCreator dialogCreator = (dialog, builder) ->
                    builder.setMessage(e.toString(activity)).setPositiveButton(android.R.string.ok, (d, w)->{});

            if(LifecycleAwareAlertDialog.haltOnDialog(activity.getLifecycle(), activity, dialogCreator)) {
                return;
            }
        }

        Tools.restartLauncherActivity(activity);
        Tools.fullyExit();
    }

    private static void disableSplash(File dir) {
        File configDir = new File(dir, "config");
        if(FileUtils.ensureDirectorySilently(configDir)) {
            File forgeSplashFile = new File(dir, "config/splash.properties");
            String forgeSplashContent = "enabled=true";
            try {
                if (forgeSplashFile.exists()) {
                    forgeSplashContent = Tools.read(forgeSplashFile.getAbsolutePath());
                }
                if (forgeSplashContent.contains("enabled=true")) {
                    Tools.write(forgeSplashFile,
                            forgeSplashContent.replace("enabled=true", "enabled=false"));
                }
            } catch (IOException e) {
                Log.w(Tools.APP_NAME, "Could not disable Forge 1.12.2 and below splash screen!", e);
            }
        } else {
            Log.w(Tools.APP_NAME, "Failed to create the configuration directory");
        }
    }

    private static void addAuthlibInjectorArgs(List<String> javaArgList, Account account) {
        String injectorUrl = account.authType.injectorUrl;
        if(injectorUrl == null) return;
        
        String injectorPath = Tools.DIR_DATA + "/authlib-injector/authlib-injector.jar";
        File injectorJar = new File(injectorPath);
        if (injectorJar.exists() && injectorJar.length() > 0) {
            javaArgList.add("-javaagent:" + injectorPath + "=" + injectorUrl);
        } else {
            Log.e("GameRunner", "authlib-injector.jar is missing or empty! Skipping javaagent to prevent crash.");
        }
    }

    private static List<String> getMoJsonJvmArgs(String versionName) {
        JVersionList.Version versionInfo = Tools.getVersionInfo(versionName, true);
        // Parse Forge 1.17+ additional JVM Arguments
        if (versionInfo.inheritsFrom == null || versionInfo.arguments == null || versionInfo.arguments.jvm == null) {
            return Collections.emptyList();
        }

        Map<String, String> varArgMap = new ArrayMap<>();
        varArgMap.put("classpath_separator", ":");
        varArgMap.put("library_directory", Tools.DIR_HOME_LIBRARY);
        varArgMap.put("version_name", versionInfo.id);
        varArgMap.put("natives_directory", Tools.NATIVE_LIB_DIR);

        List<String> clientVmArgs = new ArrayList<>();
        if (versionInfo.arguments != null) {
            for (Object arg : versionInfo.arguments.jvm) {
                if (arg instanceof String) {
                    clientVmArgs.add((String) arg);
                } //TODO: implement (?maybe?)
            }
        }
        return JSONUtils.insertJSONValueList(clientVmArgs, varArgMap);
    }

    private static List<String> getMoJsonClientArgs(Account profile, JVersionList.Version versionInfo, File gameDir) {
        String username = profile.username;
        String versionName = versionInfo.id;
        if (versionInfo.inheritsFrom != null) {
            versionName = versionInfo.inheritsFrom;
        }

        String userType = "mojang";
        Date creationDate = DateUtils.getOriginalReleaseDate(versionInfo);
        // Minecraft 22w43a which adds chat reporting (and signing) was released on
        // 26th October 2022. So, if the date is not before that (meaning it is equal or higher)
        // change the userType to MSA to fix the missing signature
        if(!DateUtils.dateBefore(creationDate, 2022, 9, 26)) {
            userType = "msa";
        }


        Map<String, String> varArgMap = new ArrayMap<>();
        varArgMap.put("auth_session", profile.accessToken); // For legacy versions of MC
        varArgMap.put("auth_access_token", profile.accessToken);
        varArgMap.put("auth_player_name", username);
        varArgMap.put("auth_uuid", profile.profileId.replace("-", ""));
        varArgMap.put("auth_xuid", profile.xuid);
        varArgMap.put("assets_root", Tools.ASSETS_PATH);
        varArgMap.put("assets_index_name", versionInfo.assets);
        varArgMap.put("game_assets", Tools.ASSETS_PATH);
        varArgMap.put("game_directory", gameDir.getAbsolutePath());
        varArgMap.put("user_properties", "{}");
        varArgMap.put("user_type", userType);
        varArgMap.put("version_name", versionName);
        varArgMap.put("version_type", versionInfo.type);

        List<String> clientArgs = new ArrayList<>();
        if (versionInfo.arguments != null && versionInfo.arguments.game != null) {
            // Support Minecraft 1.13+
            for (Object arg : versionInfo.arguments.game) {
                if (arg instanceof String) {
                    clientArgs.add((String) arg);
                } //TODO: implement else clause
            }
        }
        if(versionInfo.minecraftArguments != null){
            clientArgs.addAll(splitAndFilterEmpty(versionInfo.minecraftArguments));
        }
        return JSONUtils.insertJSONValueList(clientArgs, varArgMap);
    }

    private static List<String> splitAndFilterEmpty(String argStr) {
        List<String> strList = new ArrayList<>();
        for (String arg : argStr.split(" ")) {
            if (!arg.isEmpty()) {
                strList.add(arg);
            }
        }
        return strList;
    }

    public static @NonNull String pickRuntime(Instance instance, int targetJavaVersion) {
        String runtime = Tools.getSelectedRuntime(instance);
        String profileRuntime = instance.selectedRuntime;
        Runtime pickedRuntime = MultiRTUtils.read(runtime);
        if(runtime == null || pickedRuntime.javaVersion == 0 || pickedRuntime.javaVersion < targetJavaVersion) {
            String preferredRuntime = MultiRTUtils.getNearestJreName(targetJavaVersion);
            if(preferredRuntime == null) throw new RuntimeException("Failed to autopick runtime!");
            if(profileRuntime != null) {
                instance.selectedRuntime = preferredRuntime;
                instance.maybeWrite();
            }
            runtime = preferredRuntime;
        }
        return runtime;
    }

    private static File findImguiNative(AppCompatActivity activity) {
        String nativeLibDir = activity.getApplicationInfo().nativeLibraryDir;
        String pluginPath = NativePluginManager.getRuntimeJVMEnv().get("HYPERPLUGIN_PATH");
        String[] possibleNames = {"libimgui-java.so", "libimgui.so", "libimgui-moulberry-java.so", "libimgui-moulberry92-java.so"};

        // Check app native dir first
        for (String name : possibleNames) {
            File f = new File(nativeLibDir, name);
            if (f.exists()) {
                return f;
            }
        }

        // Check plugin path second
        if (pluginPath != null) {
            for (String name : possibleNames) {
                File f = new File(pluginPath, name);
                if (f.exists()) {
                    return f;
                }
            }
        }
        return null;
    }
}
