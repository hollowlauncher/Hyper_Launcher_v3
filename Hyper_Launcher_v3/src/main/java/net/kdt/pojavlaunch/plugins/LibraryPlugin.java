package net.kdt.pojavlaunch.plugins;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

public class LibraryPlugin {
    private static final String TAG = "LibraryPlugin";

    // Plugin Metadata Keys
    public static final String METADATA_PLUGIN_TYPE = "net.kdt.pojavlaunch.PLUGIN_TYPE";
    public static final String METADATA_PLUGIN_LIBS = "net.kdt.pojavlaunch.PLUGIN_LIBS";
    public static final String METADATA_API_VERSION = "net.kdt.pojavlaunch.PLUGIN_API_VERSION";

    // Known plugins constants
    public static final String ID_ANGLE_PLUGIN = "git.mojo.angle";
    public static final String ID_FFMPEG_PLUGIN = "git.mojo.ffmpeg";
    public static final String ID_ZINK_PLUGIN = "git.mojo.zink";
    public static final String ID_HYPER_PLUGIN = "com.ashmeet.hyperplugin";

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

    public static LibraryPlugin discoverPlugin(Context ctx, String appId){
        try {
            ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(appId, PackageManager.GET_META_DATA);
            return new LibraryPlugin(appId, info.nativeLibraryDir, info.publicSourceDir, info.metaData);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "Plugin not installed: " + appId);
            return null;
        } catch (Exception e){
            Log.e(TAG, "Plugin discover failed: " + e.getMessage());
            return null;
        }
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
