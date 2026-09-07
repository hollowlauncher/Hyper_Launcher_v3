package net.kdt.pojavlaunch.plugins;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibraryPlugin {
    private static final String TAG = "LibraryPlugin";

    // FCL Metadata Keys
    public static final String METADATA_FCL_PLUGIN = "FCLNativePlugin";
    public static final String METADATA_FCL_DESCRIPTION = "des";
    public static final String METADATA_FCL_ENVIRONMENT = "environment";
    public static final String METADATA_FCL_MIN_MC_VER = "minMCVer";
    public static final String METADATA_FCL_MAX_MC_VER = "maxMCVer";

    // Known plugins constants
    public static final String ID_ANGLE_PLUGIN = "git.mojo.angle";
    public static final String ID_FFMPEG_PLUGIN = "git.mojo.ffmpeg";
    public static final String ID_ZINK_PLUGIN = "git.mojo.zink";

    private final String appId;
    private final String libraryPath;
    private final String apkPath;
    private final Bundle metaData;

    private LibraryPlugin(String app, String libraryPath, String apkPath, Bundle metaData){
        this.appId = app;
        this.libraryPath = libraryPath;
        this.apkPath = apkPath;
        this.metaData = metaData;
    }

    public static LibraryPlugin fromApplicationInfo(ApplicationInfo info) {
        return new LibraryPlugin(info.packageName, info.nativeLibraryDir, info.publicSourceDir, info.metaData);
    }

    public static LibraryPlugin discoverPlugin(Context ctx, String appId){
        try {
            ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(appId, PackageManager.GET_META_DATA);
            return fromApplicationInfo(info);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "Plugin not installed: " + appId);
            return null;
        } catch (Exception e){
            Log.e(TAG, "Plugin discover failed: " + e.getMessage());
            return null;
        }
    }

    public static List<LibraryPlugin> discoverAllPlugins(Context ctx) {
        List<LibraryPlugin> plugins = new ArrayList<>();
        PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo info : installedApps) {
            if (info.metaData != null && info.metaData.containsKey(METADATA_FCL_PLUGIN)) {
                plugins.add(fromApplicationInfo(info));
            }
        }
        return plugins;
    }

    public String getId(){
        return appId;
    }

    public String getLibraryPath(){
        return libraryPath;
    }

    public String getApkPath() {
        return apkPath;
    }

    public Bundle getMetaData() {
        return metaData != null ? metaData : new Bundle();
    }

    public String resolveAbsolutePath(String library) {
        return new File(libraryPath, library).getAbsolutePath();
    }

    public boolean checkLibraries(String... libs){
        for(String lib : libs){
            if(!(new File(libraryPath, lib).exists())) return false;
        }
        return true;
    }
}
