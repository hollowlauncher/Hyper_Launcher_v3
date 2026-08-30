package com.ashmeet.hyperlauncher.fragments

import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.modloaders.ForgelikeUtils

class NeoforgeInstallFragment : ForgelikeInstallFragment(ForgelikeUtils.NEOFORGE_UTILS, TAG) {
    override fun getTitleText(): Int = R.string.neoforge_dl_select_version
    override fun getNoDataMsg(): Int = R.string.neoforge_dl_no_installer

    companion object {
        const val TAG = "NeoforgeInstallFragment"
    }
}
