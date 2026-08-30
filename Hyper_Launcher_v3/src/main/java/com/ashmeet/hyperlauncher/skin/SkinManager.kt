package com.ashmeet.hyperlauncher.skin

import com.ashmeet.hyperlauncher.skin.model.SkinModelType
import com.ashmeet.hyperlauncher.skin.server.OfflineYggdrasilServer
import com.ashmeet.hyperlauncher.utils.LocalUuidUtils
import com.ashmeet.hyperlauncher.utils.LocalUuidUtils.toFormattedUuid
import java.io.File

/**
 * Facade over skin validation, UUID generation, and the Yggdrasil server.
 */
class SkinManager(private val analyzer: SkinAnalyzerFacade) {

    /**
     * Platform-agnostic interface. Implement using [AndroidSkinAnalyzer]
     */
    interface SkinAnalyzerFacade {
        /** Return null if bytes are not a valid Minecraft skin (wrong dimensions). */
        fun prepareSkin(bytes: ByteArray): PlayerSkin?
        fun prepareCape(bytes: ByteArray): PlayerCape
    }

    val server = OfflineYggdrasilServer()
    private var port: Int = 0

    @Throws(InvalidSkinException::class)
    fun prepareAccount(
        username: String,
        skinFile: File? = null,
        capeFile: File? = null,
        modelOverride: SkinModelType? = null
    ): PreparedAccount {
        val skinBytes = skinFile?.takeIf { it.exists() }?.readBytes()
        val capeBytes = capeFile?.takeIf { it.exists() }?.readBytes()

        val skin: PlayerSkin? = skinBytes?.let {
            val base = analyzer.prepareSkin(it)
                ?: throw InvalidSkinException("${skinFile?.name ?: "Skin file"} must be 64×64 or 64×32 pixels")

            if (modelOverride != null && modelOverride != base.model)
                base.copy(model = modelOverride)
            else base
        }

        val cape: PlayerCape? = capeBytes?.let { analyzer.prepareCape(it) }

        val model = skin?.model ?: SkinModelType.NONE
        val profileId = LocalUuidUtils.generateProfileId(username, model)

        server.addCharacter(
            username  = username,
            profileId = profileId,
            skin      = skin,
            cape      = cape
        )

        if (skinFile != null && skinFile.exists()) {
            val permanentSkin = File(net.kdt.pojavlaunch.Tools.DIR_CACHE, "skin-$profileId.png")
            if (skinFile.absolutePath != permanentSkin.absolutePath) {
                skinFile.copyTo(permanentSkin, overwrite = true)
            }
        }

        return PreparedAccount(
            username      = username,
            profileId     = profileId,
            formattedUuid = profileId.toFormattedUuid(),
            skinModel     = model
        )
    }

    /** Start the local server. Call this before launching the game. */
    fun startServer(): Int {
        port = server.start()
        return port
    }

    /** Stop the server once the game process has exited. */
    fun stopServer() = server.stop()

    /** The JVM argument base URL for authlib-injector. */
    val authlibUrl: String get() = "http://127.0.0.1:$port"
}

/** Returned by [SkinManager.prepareAccount]; pass these values to Minecraft's launch args. */
data class PreparedAccount(
    val username: String,
    val profileId: String,
    val formattedUuid: String,
    val skinModel: SkinModelType
)

class InvalidSkinException(message: String) : Exception(message)

@JvmField
val androidSkinAnalyzerFacade = object : SkinManager.SkinAnalyzerFacade {
    override fun prepareSkin(bytes: ByteArray) = AndroidSkinAnalyzer.prepareSkin(bytes)
    override fun prepareCape(bytes: ByteArray) = AndroidSkinAnalyzer.prepareCape(bytes)
}
