package com.ashmeet.hyperlauncher.fragments

import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.modloaders.ForgelikeUtils

class ForgeInstallFragment : ForgelikeInstallFragment(ForgelikeUtils.FORGE_UTILS, TAG) {
    override fun getTitleText(): Int = R.string.forge_dl_select_version
    override fun getNoDataMsg(): Int = R.string.forge_dl_no_installer

    companion object {
        const val TAG = "ForgeInstallFragment"
    }
}
