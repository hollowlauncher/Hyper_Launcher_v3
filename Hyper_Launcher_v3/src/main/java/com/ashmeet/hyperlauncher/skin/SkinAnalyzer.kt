package com.ashmeet.hyperlauncher.skin

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.graphics.get
import com.ashmeet.hyperlauncher.skin.model.SkinModelType
import java.security.MessageDigest

/** SHA-256 hex digest of a ByteArray. Used for texture cache keys + URLs. */
fun ByteArray.sha256Hex(): String =
    MessageDigest.getInstance("SHA-256").digest(this)
        .joinToString("") { "%02x".format(it) }

/**
 * Detects whether a 64×64 skin uses Alex (slim, 3px) or Steve (wide, 4px) arms
 * by inspecting the 1-pixel-wide strips that only appear in Steve skins.
 *
 * If those strips are completely transparent the skin is Alex; otherwise Steve.
 * Classic 64×32 skins always return STEVE (slim arms are not supported there).
 *
 * @param imageHeight    Height of the skin image (32 = classic, 64 = modern)
 * @param getPixelAlpha  Returns alpha (0–255) for pixel at (x, y)
 */
fun detectSkinModel(imageHeight: Int, getPixelAlpha: (Int, Int) -> Int): SkinModelType {
    if (imageHeight == 32) return SkinModelType.STEVE

    fun allTransparent(xs: IntRange, ys: IntRange) =
        xs.all { x -> ys.all { y -> getPixelAlpha(x, y) == 0 } }

    val isSlim =
        allTransparent(50..51, 16..19) &&
        allTransparent(54..55, 20..31) &&
        allTransparent(42..43, 48..51) &&
        allTransparent(46..47, 52..63)

    return if (isSlim) SkinModelType.ALEX else SkinModelType.STEVE
}

object AndroidSkinAnalyzer {

    /** Returns true if [bytes] is a valid 64×64 or 64×32 Minecraft skin. */
    fun validate(bytes: ByteArray): Boolean {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        return (opts.outWidth == 64 && opts.outHeight == 64) ||
               (opts.outWidth == 64 && opts.outHeight == 32)
    }

    /** Reads pixel transparency to determine Steve vs Alex arm model. */
    fun detectModel(bytes: ByteArray): SkinModelType {
        val opts = BitmapFactory.Options()
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            ?: return SkinModelType.STEVE
        return try {
            detectSkinModel(opts.outHeight) { x, y -> Color.alpha(bmp[x, y]) }
        } finally {
            if (!bmp.isRecycled) bmp.recycle()
        }
    }

    /**
     * Validates + auto-detects model, returns a ready-to-use [PlayerSkin],
     * or null if the file is not a valid skin.
     */
    fun prepareSkin(bytes: ByteArray): PlayerSkin? {
        if (!validate(bytes)) return null
        return PlayerSkin(bytes = bytes, hash = bytes.sha256Hex(), model = detectModel(bytes))
    }

    /** Wraps cape bytes into a [PlayerCape]. No special validation needed. */
    fun prepareCape(bytes: ByteArray): PlayerCape =
        PlayerCape(bytes = bytes, hash = bytes.sha256Hex())
}
