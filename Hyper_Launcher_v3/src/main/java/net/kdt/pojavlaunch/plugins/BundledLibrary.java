package net.kdt.pojavlaunch.plugins;

public class BundledLibrary {
    public enum LoadStrategy {
        SYSTEM_LOAD,
        DLOPEN_GLOBAL
    }

    public final String name;
    public final String soName;
    public final String version;
    public final LoadStrategy strategy;
    public final boolean optional;

    public BundledLibrary(String name, String soName, String version, LoadStrategy strategy, boolean optional) {
        this.name = name;
        this.soName = soName;
        this.version = version;
        this.strategy = strategy;
        this.optional = optional;
    }
}
