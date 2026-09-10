package com.ashmeet.hyperlauncher.plugins.interfaces

interface NativePlugin {
    fun getPaths(): Array<String>
    fun getJVMEnv(): Map<String, String>
    val rendererName: String?
        get() = null
    val displayName: String?
        get() = null

    fun supportsVersion(mcVersion: String?): Boolean = true
}