package com.ashmeet.hyperlauncher.skin.model

import androidx.annotation.Keep

/**
 * Arm model of a Minecraft player skin.
 *
 * [targetParity] is used by LocalUuidUtils to encode the model into the UUID's
 * parity bits so Minecraft can determine the arm width without a session server.
 *
 * STEVE = 4px wide arms (default, classic)
 * ALEX  = 3px wide arms (slim)
 * NONE  = no skin assigned; UUID is returned unmodified
 */
@Keep
enum class SkinModelType(val targetParity: Int) {
    NONE(-1),
    STEVE(0),
    ALEX(1)
}