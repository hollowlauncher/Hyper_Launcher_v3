package com.ashmeet.hyperlauncher.skin

import com.ashmeet.hyperlauncher.skin.model.SkinModelType

/**
 * Skin texture: raw PNG bytes, its SHA-256 hex hash, and the arm model.
 * The hash is used both as a cache key and as the URL path segment served
 * by the local texture endpoint (/textures/{hash}).
 */
data class PlayerSkin(
    val bytes: ByteArray,
    val hash: String,
    val model: SkinModelType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerSkin) return false
        return hash == other.hash
    }

    override fun hashCode(): Int = hash.hashCode()
}

/**
 * Cape texture: raw PNG bytes and its SHA-256 hex hash.
 */
data class PlayerCape(
    val bytes: ByteArray,
    val hash: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerCape) return false
        return hash == other.hash
    }

    override fun hashCode(): Int = hash.hashCode()
}
