package net.kdt.pojavlaunch.modloaders.modpacks.api.modloader;

import net.kdt.pojavlaunch.instances.InstanceInstaller;
import net.kdt.pojavlaunch.modloaders.ForgelikeUtils;
import java.io.IOException;

public class ForgelikeLoaderInstaller implements LoaderInstaller {
    private final ForgelikeUtils utils;
    private final String mcVersion;
    private final String loaderVersion;

    public ForgelikeLoaderInstaller(ForgelikeUtils utils, String mcVersion, String loaderVersion) {
        this.utils = utils;
        this.mcVersion = mcVersion;
        this.loaderVersion = loaderVersion;
    }

    @Override
    public boolean requiresGuiInstallation() {
        return true;
    }

    @Override
    public InstanceInstaller createInstaller() throws IOException {
        return utils.createInstaller(mcVersion, loaderVersion);
    }

    @Override
    public String installHeadlessly() throws IOException {
        return null;
    }

    @Override
    public String getVersionId() {
        if (utils == ForgelikeUtils.FORGE_UTILS) {
            return mcVersion + "-forge-" + loaderVersion;
        }
        if (utils == ForgelikeUtils.NEOFORGE_UTILS) {
            return "neoforge-" + loaderVersion;
        }
        return null;
    }
}
