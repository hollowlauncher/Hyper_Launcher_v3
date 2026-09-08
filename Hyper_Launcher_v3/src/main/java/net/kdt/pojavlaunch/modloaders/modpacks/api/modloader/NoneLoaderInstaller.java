package net.kdt.pojavlaunch.modloaders.modpacks.api.modloader;

import net.kdt.pojavlaunch.instances.InstanceInstaller;
import java.io.IOException;

public class NoneLoaderInstaller implements LoaderInstaller {
    private final String mcVersion;

    public NoneLoaderInstaller(String mcVersion) {
        this.mcVersion = mcVersion;
    }

    @Override
    public boolean requiresGuiInstallation() {
        return false;
    }

    @Override
    public InstanceInstaller createInstaller() throws IOException {
        return null;
    }

    @Override
    public String installHeadlessly() throws IOException {
        return mcVersion;
    }

    @Override
    public String getVersionId() {
        return mcVersion;
    }
}
