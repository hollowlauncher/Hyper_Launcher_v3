package com.ashmeet.hyperlauncher.utils

import net.kdt.pojavlaunch.JVersionList
import net.kdt.pojavlaunch.Tools
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utils for date-based activation for certain launcher workarounds.
 * Handles null/missing dates by returning a default date (Minecraft 1.0 Release: 2011-11-18).
 */
object DateUtils {
    private val DEFAULT_DATE: Date by lazy {
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2011-11-18")!!
    }

    /**
     * Parse the release date of a game version from the JMinecraftVersionList.Version time or releaseTime fields
     * @param releaseTime the time or releaseTime string from JMinecraftVersionList.Version
     * @return the date object, or a default date if null/invalid
     */
    @JvmStatic
    fun parseReleaseDate(releaseTime: String?): Date {
        if (releaseTime.isNullOrBlank()) return DEFAULT_DATE
        return try {
            var time = releaseTime
            val tIndexOf = time.indexOf('T')
            if (tIndexOf != -1) time = time.substring(0, tIndexOf)
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(time) ?: DEFAULT_DATE
        } catch (_: Exception) {
            DEFAULT_DATE
        }
    }

    /**
     * Checks if the Date object is before the date denoted by
     * year, month, dayOfMonth parameters
     * @param date the Date object that we compare against
     * @param year the year
     * @param month the month (zero-based)
     * @param dayOfMonth the day of the month
     * @return true if the Date is before year, month, dayOfMonth, false otherwise
     */
    @JvmStatic
    fun dateBefore(date: Date, year: Int, month: Int, dayOfMonth: Int): Boolean {
        return date.before(GregorianCalendar(year, month, dayOfMonth).time)
    }

    /**
     * Extracts the original release date of a game version, ignoring any mods (if present)
     * @param gameVersion the JMinecraftVersionList.Version object
     * @return the game's original release date
     */
    @JvmStatic
    fun getOriginalReleaseDate(gameVersion: JVersionList.Version): Date {
        return try {
            val actualVersion = if (Tools.isValidString(gameVersion.inheritsFrom)) {
                Tools.getVersionInfo(gameVersion.inheritsFrom, true)
            } else {
                // The launcher's inheritor mutilates the version object, causing it to have the original
                // version's ID but modded version's dates. Work around it by re-reading the version without
                // inheriting.
                Tools.getVersionInfo(gameVersion.id, true)
            }
            parseReleaseDate(actualVersion.releaseTime)
        } catch (_: Exception) {
            DEFAULT_DATE
        }
    }
}
