package com.ashmeet.hyperlauncher.fragments

import net.kdt.pojavlaunch.modloaders.FabriclikeUtils

class QuiltInstallFragment : FabriclikeInstallFragment(FabriclikeUtils.QUILT_UTILS, TAG) {
    companion object {
        const val TAG = "QuiltInstallFragment"
    }
}
