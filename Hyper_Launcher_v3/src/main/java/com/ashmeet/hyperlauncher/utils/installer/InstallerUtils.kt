package com.ashmeet.hyperlauncher.utils.installer

fun isMcVersionCompatible(v1: String, v2: String): Boolean {
    if (v1 == v2) return true

    val releaseRegex = Regex("""^1\.\d+(\.\d+)*$""")
    val isR1 = v1.matches(releaseRegex)
    val isR2 = v2.matches(releaseRegex)

    // Special check for RC, Pre-release, and Snapshots to be extra strict
    val isNonRelease1 = v1.contains("-rc", ignoreCase = true) || v1.contains("-pre", ignoreCase = true) || v1.contains(Regex("""\d+w\d+[a-z]"""))
    val isNonRelease2 = v2.contains("-rc", ignoreCase = true) || v2.contains("-pre", ignoreCase = true) || v2.contains(Regex("""\d+w\d+[a-z]"""))

    // If one is a stable release and the other is a non-release type, they are incompatible
    if ((isR1 && isNonRelease2) || (isR2 && isNonRelease1)) return false

    // Strict: don't mix release and non-release strictly defined by regex
    if (isR1 != isR2) return false

    if (isR1) {
        // Both are stable releases. Check if they share the same minor version (e.g., 1.21.x)
        val parts1 = v1.split(".")
        val parts2 = v2.split(".")
        if (parts1.size >= 2 && parts2.size >= 2 && parts1[1] == parts2[1]) {
            // If both specify a patch version, they must match
            if (parts1.size >= 3 && parts2.size >= 3) {
                return parts1[2] == parts2[2]
            }
            // Otherwise, allow family match (e.g. 1.21 matches 1.21.1)
            return true
        }
    }

    // For snapshots/RCs/Pre-releases, they must be an exact match (already handled) 
    // or very closely related, but usually we just want exact matches for them.
    return false
}
