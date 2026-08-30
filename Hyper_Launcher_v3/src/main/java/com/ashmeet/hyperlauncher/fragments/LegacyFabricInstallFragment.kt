package com.ashmeet.hyperlauncher.fragments

import net.kdt.pojavlaunch.modloaders.FabriclikeUtils

class LegacyFabricInstallFragment : FabriclikeInstallFragment(FabriclikeUtils.LEGACY_FABRIC_UTILS, TAG) {
    companion object {
        const val TAG = "LegacyFabricInstallFragment"
    }
}
