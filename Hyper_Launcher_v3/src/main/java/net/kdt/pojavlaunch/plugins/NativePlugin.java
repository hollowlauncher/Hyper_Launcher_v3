package net.kdt.pojavlaunch.plugins;

import java.util.Map;

public interface NativePlugin {
    String[] getPaths();
    Map<String, String> getJVMEnv();
    default boolean supportsVersion(String mcVersion) { return true; }
}
