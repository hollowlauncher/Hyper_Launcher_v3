package com.ashmeet.hyperlauncher.LauncherPreference.Preference

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.Architecture.is32BitsDevice
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.multirt.MultiRTUtils
import net.kdt.pojavlaunch.utils.JREUtils
import java.io.IOException
import kotlin.math.ceil

object LauncherPreferences {
    const val PREF_KEY_CURRENT_INSTANCE = "currentInstance"
    const val PREF_KEY_SKIP_NOTIFICATION_CHECK = "skipNotificationPermissionCheck"

    @JvmField
    var DEFAULT_PREF: SharedPreferences? = null

    @get:JvmStatic
    val prefs: SharedPreferences
        get() = DEFAULT_PREF!!

    @JvmField
    var PREF_RENDERER = "opengles2"

    @JvmField
    var PREF_IGNORE_NOTCH = false

    @JvmField
    var PREF_BUTTONSIZE = 100f

    @JvmField
    var PREF_MOUSESCALE = 1f

    @JvmField
    var PREF_LONGPRESS_TRIGGER = 300

    @JvmField
    var PREF_DEFAULTCTRL_PATH: String = ""

    @JvmField
    var PREF_CUSTOM_JAVA_ARGS: String? = null

    @JvmField
    var PREF_FORCE_ENGLISH = false

    const val PREF_VERSION_REPOS = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    @JvmField
    var PREF_DISABLE_GESTURES = false

    @JvmField
    var PREF_DISABLE_SWAP_HAND = false

    @JvmField
    var PREF_MOUSESPEED = 1f

    @JvmField
    var PREF_RAM_ALLOCATION = 0

    @JvmField
    var PREF_DEFAULT_RUNTIME: String? = null

    @JvmField
    var PREF_SUSTAINED_PERFORMANCE = false

    @JvmField
    var PREF_VIRTUAL_MOUSE_START = false

    @JvmField
    var PREF_USE_ALTERNATE_SURFACE = false

    @JvmField
    var PREF_JAVA_SANDBOX = true

    @JvmField
    var PREF_SCALE_FACTOR = 1f

    @JvmField
    var PREF_ENABLE_GYRO = false

    @JvmField
    var PREF_GYRO_SENSITIVITY = 1f

    @JvmField
    var PREF_GYRO_SAMPLE_RATE = 16

    @JvmField
    var PREF_GYRO_SMOOTHING = true

    @JvmField
    var PREF_GYRO_INVERT_X = false

    @JvmField
    var PREF_GYRO_INVERT_Y = false

    @JvmField
    var PREF_FORCE_VSYNC = false

    @JvmField
    var PREF_USE_ANGLE = false

    @JvmField
    var PREF_BUTTON_ALL_CAPS = true

    @JvmField
    var PREF_DUMP_SHADERS = false

    @JvmField
    var PREF_DEADZONE_SCALE = 1f

    @JvmField
    var PREF_BIG_CORE_AFFINITY = false

    @JvmField
    var PREF_ZINK_PREFER_SYSTEM_DRIVER = false

    @JvmField
    var PREF_ZINK_FORCE_LEGACY = false

    @JvmField
    var PREF_VERIFY_MANIFEST = true

    @JvmField
    var PREF_DOWNLOAD_SOURCE = "default"

    @JvmField
    var PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = false

    @JvmField
    var PREF_VSYNC_IN_ZINK = true

    @JvmField
    var PREF_FULLSCREEN_LAUNCHER = true

    @JvmField
    var PREF_RAPID_START = true

    @JvmField
    var PREF_VERIFY_FILES = true

    @JvmField
    var PREF_FREEDRENO_SYSMEM = false

    @JvmField
    var PREF_KEYBOARD_AUTOPANNING = true

    @JvmField
    var PREF_MIGRATION_NOTICE = true

    @JvmField
    var PREF_ALSOFT_FORCE_OPENSL = false

    @JvmField
    var PREF_SCREEN_TRANSITION = "bounce"

    @JvmField
    var PREF_THEME = "system"

    @JvmField
    var PREF_LANGUAGE = "en"

    @JvmField
    var PREF_CUSTOM_THEME = false

    @JvmField
    var PREF_THEME_COLOR = -0xc0ae4b // 0xFF3F51B5

    @JvmField
    var PREF_LAST_CONTENT_SOURCE = 0

    @JvmField
    var PREF_HIDE_SIDEBAR = false

    @JvmField
    var PREF_DRAWER_PULL_SIZE_PERC = 100f

    @JvmField
    var PREF_DRAWER_PULL_POS_X = -1f

    @JvmField
    var PREF_DRAWER_PULL_POS_Y = -1f

    @JvmField
    var PREF_DRAWER_PULL_BG_OPACITY = 65

    @JvmField
    var PREF_DRAWER_PULL_ICON_OPACITY = 100

    @JvmField
    var PREF_DRAWER_PULL_HOLD_TO_MOVE = true

    @JvmField
    var PREF_DRAWER_PULL_BACKGROUND = true

    @JvmField
    var PREF_DRAWER_PULL_ICON_PATH: String? = null

    @JvmField
    var PREF_POINTER_ICON_PATH: String? = null

    @JvmField
    var PREF_POINTER_HOTSPOT_X = 0

    @JvmField
    var PREF_POINTER_HOTSPOT_Y = 0

    @JvmStatic
    fun loadPreferences(ctx: Context?) {
        if (ctx == null) return
        Tools.initStorageConstants(ctx)
        val isDevicePowerful = isDevicePowerful(ctx)
        val pref = DEFAULT_PREF ?: return

        PREF_RENDERER = pref.getString("renderer", "holy") ?: "holy"
        PREF_BUTTONSIZE = pref.getInt("buttonscale", 100).toFloat()
        PREF_MOUSESCALE = pref.getInt("mousescale", 100) / 100f
        PREF_MOUSESPEED = pref.getInt("mousespeed", 100).toFloat() / 100f
        PREF_IGNORE_NOTCH = pref.getBoolean("ignoreNotch", false)
        PREF_LONGPRESS_TRIGGER = pref.getInt("timeLongPressTrigger", 300)
        PREF_DEFAULTCTRL_PATH = pref.getString("defaultCtrl", "") ?: ""
        if (PREF_DEFAULTCTRL_PATH.isEmpty()) {
            PREF_DEFAULTCTRL_PATH = Tools.CTRLDEF_FILE
        }
        PREF_FORCE_ENGLISH = pref.getBoolean("force_english", false)
        PREF_DISABLE_GESTURES = pref.getBoolean("disableGestures", false)
        PREF_DISABLE_SWAP_HAND = pref.getBoolean("disableDoubleTap", false)
        PREF_RAM_ALLOCATION = pref.getInt("allocation", findBestRAMAllocation(ctx))
        PREF_CUSTOM_JAVA_ARGS = pref.getString("javaArgs", "")
        PREF_SUSTAINED_PERFORMANCE = pref.getBoolean("sustainedPerformance", isDevicePowerful)
        PREF_VIRTUAL_MOUSE_START = pref.getBoolean("mouse_start", false)
        PREF_USE_ALTERNATE_SURFACE = pref.getBoolean("alternate_surface", isDevicePowerful)
        PREF_JAVA_SANDBOX = pref.getBoolean("java_sandbox", true)
        PREF_SCALE_FACTOR = pref.getInt("resolutionRatio", findBestResolution(ctx, isDevicePowerful)) / 100f
        PREF_ENABLE_GYRO = pref.getBoolean("enableGyro", false)
        PREF_GYRO_SENSITIVITY = pref.getInt("gyroSensitivity", 100).toFloat() / 100f
        PREF_GYRO_SAMPLE_RATE = pref.getInt("gyroSampleRate", 16)
        PREF_GYRO_SMOOTHING = pref.getBoolean("gyroSmoothing", true)
        PREF_GYRO_INVERT_X = pref.getBoolean("gyroInvertX", false)
        PREF_GYRO_INVERT_Y = pref.getBoolean("gyroInvertY", false)
        PREF_FORCE_VSYNC = pref.getBoolean("force_vsync", isDevicePowerful)
        PREF_USE_ANGLE = pref.getBoolean("use_angle", false)
        PREF_BUTTON_ALL_CAPS = pref.getBoolean("buttonAllCaps", true)
        PREF_DUMP_SHADERS = pref.getBoolean("dump_shaders", false)
        PREF_DEADZONE_SCALE = pref.getInt("gamepad_deadzone_scale", 100).toFloat() / 100f
        PREF_BIG_CORE_AFFINITY = pref.getBoolean("bigCoreAffinity", false)
        PREF_ZINK_PREFER_SYSTEM_DRIVER = pref.getBoolean("zinkPreferSystemDriver", false)
        PREF_DOWNLOAD_SOURCE = pref.getString("downloadSource", "default") ?: "default"
        PREF_VERIFY_MANIFEST = pref.getBoolean("verifyManifest", true)
        PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = pref.getBoolean(PREF_KEY_SKIP_NOTIFICATION_CHECK, false)
        PREF_VSYNC_IN_ZINK = pref.getBoolean("vsync_in_zink", true)
        PREF_FULLSCREEN_LAUNCHER = pref.getBoolean("fullscreen_launcher", true)
        PREF_VERIFY_FILES = pref.getBoolean("checkGameFiles", true)
        PREF_RAPID_START = pref.getBoolean("fastStartupCheck", true)
        PREF_FREEDRENO_SYSMEM = pref.getBoolean("freedrenoSysmem", false)
        PREF_KEYBOARD_AUTOPANNING = pref.getBoolean("keyboardAutoPanning", true)
        PREF_ZINK_FORCE_LEGACY = pref.getBoolean("zinkForceLegacy", false)
        PREF_MIGRATION_NOTICE = pref.getBoolean("migrationNotice", true)
        PREF_ALSOFT_FORCE_OPENSL = pref.getBoolean("alsoftForceOpenSL", false)
        PREF_SCREEN_TRANSITION = pref.getString("screen_transition", "bounce") ?: "none"
        PREF_THEME = pref.getString("app_theme", "system") ?: "system"
        PREF_LANGUAGE = pref.getString("app_language", "en") ?: "en"
        PREF_CUSTOM_THEME = pref.getBoolean("app_custom_theme", false)

        if ("custom" == PREF_THEME) {
            PREF_THEME = "system"
            PREF_CUSTOM_THEME = true
            pref.edit {
                putString("app_theme", "system")
                putBoolean("app_custom_theme", true)
            }
        }

        PREF_THEME_COLOR = pref.getInt("app_theme_color", -0xc0ae4b)
        PREF_LAST_CONTENT_SOURCE = pref.getInt("last_content_source", 0)
        PREF_HIDE_SIDEBAR = pref.getBoolean("hide_sidebar", false)
        PREF_DRAWER_PULL_SIZE_PERC = pref.getFloat("drawer_pull_size_perc", 50f)
        PREF_DRAWER_PULL_POS_X = pref.getFloat("drawer_pull_pos_x", -1f)
        PREF_DRAWER_PULL_POS_Y = pref.getFloat("drawer_pull_pos_y", -1f)
        PREF_DRAWER_PULL_BG_OPACITY = pref.getInt("drawer_pull_opacity", 33)
        PREF_DRAWER_PULL_ICON_OPACITY = pref.getInt("drawer_pull_icon_opacity", 100)
        PREF_DRAWER_PULL_HOLD_TO_MOVE = pref.getBoolean("drawer_pull_hold_to_move", false)
        PREF_DRAWER_PULL_BACKGROUND = pref.getBoolean("drawer_pull_background", true)
        PREF_DRAWER_PULL_ICON_PATH = pref.getString("drawer_pull_icon_path", null)
        PREF_POINTER_ICON_PATH = pref.getString("pointer_icon_path", null)
        PREF_POINTER_HOTSPOT_X = pref.getInt("pointer_hotspot_x", 0)
        PREF_POINTER_HOTSPOT_Y = pref.getInt("pointer_hotspot_y", 0)
        updateNightMode()

        val argLwjglLibname = "-Dorg.lwjgl.opengl.libname="
        val customJavaArgs = PREF_CUSTOM_JAVA_ARGS
        if (customJavaArgs != null) {
            for (arg in JREUtils.parseJavaArguments(customJavaArgs)) {
                if (arg.startsWith(argLwjglLibname)) {
                    pref.edit {
                        putString("javaArgs", customJavaArgs.replace(arg, ""))
                    }
                }
            }
        }
        
        if (pref.contains("defaultRuntime")) {
            PREF_DEFAULT_RUNTIME = pref.getString("defaultRuntime", "")
        } else {
            val runtimes = MultiRTUtils.getRuntimes()
            if (runtimes.isEmpty()) {
                PREF_DEFAULT_RUNTIME = ""
                return
            }
            PREF_DEFAULT_RUNTIME = runtimes[0].name
            pref.edit { putString("defaultRuntime", PREF_DEFAULT_RUNTIME) }
        }
    }

    @JvmStatic
    fun isPluginLibraryEnabled(name: String): Boolean {
        return prefs.getBoolean("pref_plugin_lib_$name", true)
    }

    @JvmStatic
    fun setPluginLibraryEnabled(name: String, enabled: Boolean) {
        prefs.edit { putBoolean("pref_plugin_lib_$name", enabled) }
    }

    @JvmStatic
    fun updateNightMode() {
        when (PREF_THEME) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun findBestRAMAllocation(ctx: Context?): Int {
        if (ctx == null) return 2048
        val deviceRam = Tools.getTotalDeviceMemory(ctx)
        if (deviceRam < 1024) return 296
        if (deviceRam < 1536) return 448
        if (deviceRam < 2048) return 656
        if (is32BitsDevice()) return 696
        if (deviceRam < 3064) return 936
        if (deviceRam < 4096) return 1144
        if (deviceRam < 6144) return 1536
        return 2048
    }

    private fun findBestResolution(context: Context?, isDevicePowerful: Boolean): Int {
        if (context == null) return 100
        val metrics = context.resources.displayMetrics
        val minSide = minOf(metrics.widthPixels, metrics.heightPixels)
        val targetSide = if (isDevicePowerful) 1080 else 720
        if (minSide <= targetSide) return 100
        val ratio = (100f * targetSide / minSide)
        val increment = context.resources.getInteger(R.integer.resolution_seekbar_increment)
        return (ceil((ratio / increment).toDouble()) * increment).toInt()
    }

    private fun isDevicePowerful(context: Context?): Boolean {
        if (context == null) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        if (Tools.getTotalDeviceMemory(context) <= 4096) return false
        val metrics = context.resources.displayMetrics
        if (minOf(metrics.widthPixels, metrics.heightPixels) < 1080) return false
        if (Runtime.getRuntime().availableProcessors() <= 4) return false
        if (hasAllCoreSameFreq()) return false
        return true
    }

    private fun hasAllCoreSameFreq(): Boolean {
        val coreCount = Runtime.getRuntime().availableProcessors()
        try {
            val freq0 = Tools.read("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            val freqX = Tools.read("/sys/devices/system/cpu/cpu" + (coreCount - 1) + "/cpufreq/cpuinfo_max_freq")
            if (freq0 == freqX) return true
        } catch (e: IOException) {
            Log.e("LauncherPreferences", "Failed to read CPU frequencies", e)
        }
        return false
    }
}
