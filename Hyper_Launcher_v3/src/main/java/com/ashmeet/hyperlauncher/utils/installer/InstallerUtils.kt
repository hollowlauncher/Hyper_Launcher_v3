package com.ashmeet.hyperlauncher.utils.installer

fun isMcVersionCompatible(v1: String, v2: String): Boolean {
    if (v1 == v2) return true
    val releaseRegex = Regex("""^1\.\d+(\.\d+)*$""")
    val isR1 = v1.matches(releaseRegex)
    val isR2 = v2.matches(releaseRegex)
    val isNonRelease1 = v1.contains("-rc", ignoreCase = true) || v1.contains("-pre", ignoreCase = true) || v1.contains(Regex("""\d+w\d+[a-z]"""))
    val isNonRelease2 = v2.contains("-rc", ignoreCase = true) || v2.contains("-pre", ignoreCase = true) || v2.contains(Regex("""\d+w\d+[a-z]"""))
    if ((isR1 && isNonRelease2) || (isR2 && isNonRelease1)) return false
    if (isR1 != isR2) return false

    if (isR1) {
        val parts1 = v1.split(".")
        val parts2 = v2.split(".")
        if (parts1.size >= 2 && parts2.size >= 2 && parts1[1] == parts2[1]) {
            if (parts1.size >= 3 && parts2.size >= 3) {
                return parts1[2] == parts2[2]
            }
            return true
        }
    }
    return false
}
