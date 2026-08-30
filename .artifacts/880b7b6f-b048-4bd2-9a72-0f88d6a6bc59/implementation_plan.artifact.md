# Hyper Launcher Plugin System Integration Plan

This plan outlines the steps to integrate a dynamic plugin discovery and loading system into Hyper Launcher, specifically targeting the `com.ashmeet.hyperplugin` bundle while maintaining decoupling.

## Proposed Changes

### [Manifest]

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/ASHMEET/Documents/Projects/Hyper_Launcher_v3/Hyper_Launcher_v3/src/main/AndroidManifest.xml)
- Add `<queries>` for `com.ashmeet.hyperplugin`.
- Add intent-based query for `net.kdt.pojavlaunch.PLUGIN_TYPE` for future-proofing.

---

### [Plugins Core]

#### [MODIFY] [LibraryPlugin.java](file:///C:/Users/ASHMEET/Documents/Projects/Hyper_Launcher_v3/Hyper_Launcher_v3/src/main/java/net/kdt/pojavlaunch/plugins/LibraryPlugin.java)
- Add constants: `PLUGIN_TYPE_META`, `PLUGIN_LIBS_META`, `PLUGIN_API_VERSION_META`.
- Store `Bundle metaData` in the instance.
- Update `discoverPlugin` to use `PackageManager.GET_META_DATA` and store it.
- Add helper methods: `getApiVersion()`, `getLibsString()`, `getMetaData()`.

#### [NEW] [BundledLibrary.java](file:///C:/Users/ASHMEET/Documents/Projects/Hyper_Launcher_v3/Hyper_Launcher_v3/src/main/java/net/kdt/pojavlaunch/plugins/BundledLibrary.java)
- Data class representing a library from the plugin.
- Fields: `soName`, `version`, `LoadStrategy`, `optional`.
- `LoadStrategy` enum: `SYSTEM_LOAD`, `DLOPEN_GLOBAL`.

#### [MODIFY] [NativePluginManager.java](file:///C:/Users/ASHMEET/Documents/Projects/Hyper_Launcher_v3/Hyper_Launcher_v3/src/main/java/net/kdt/pojavlaunch/plugins/NativePluginManager.java)
- Implement `discoverHyperPluginBundle(Context)`.
- Implement `parseApiVersion` and version checking.
- Parse `PLUGIN_LIBS` (format `name:version,name:version`) into `BundledLibrary` objects.
- Maintain a registry of discovered `BundledLibrary` entries.
- Add logic to register `NativePlugin` for the bundle path.
- Add `getDiscoveredLibraries()` to surface status to the UI.

---

### [Integration & Loading]

#### [MODIFY] [PojavApplication.java](file:///C:/Users/ASHMEET/Documents/Projects/Hyper_Launcher_v3/Hyper_Launcher_v3/src/main/java/net/kdt/pojavlaunch/PojavApplication.java)
- Call `NativePluginManager.discoverHyperPluginBundle(this)` in `onCreate`.

#### [MODIFY] [JREUtils.java](file:///C:/Users/ASHMEET/Documents/Projects/Hyper_Launcher_v3/Hyper_Launcher_v3/src/main/java/net/kdt/pojavlaunch/utils/JREUtils.java) (or similar)
- During native process setup, read `HYPERPLUGIN_PATH` from `NativePluginManager` and add it to the environment.
- Handle `SYSTEM_LOAD` libraries: iterate discovered libraries and call `System.load()` if they are marked as `SYSTEM_LOAD` and not disabled by preferences.

---

### [Settings & Preferences]

#### [MODIFY] [LauncherPreferences.kt](file:///C:/Users/ASHMEET/Documents/Projects/Hyper_Launcher_v3/Hyper_Launcher_v3/src/main/java/com/ashmeet/hyperlauncher/LauncherPreference/Preference/LauncherPreferences.kt)
- Add generic preference keys for enabling/disabling optional plugin libraries.
- Helper to get/set plugin library state.

#### [NEW] [PluginSettingsScreen.kt](file:///C:/Users/ASHMEET/Documents/Projects/Hyper_Launcher_v3/Hyper_Launcher_v3/src/main/java/com/ashmeet/hyperlauncher/screens/layouts/settings/PluginSettingsScreen.kt)
- Compose UI showing:
    - Bundle installation status.
    - API Version.
    - List of discovered libraries with their load status.
    - Toggles for optional libraries (like `discord-rpc`).

#### [MODIFY] [MainSettingsScreen.kt](file:///C:/Users/ASHMEET/Documents/Projects/Hyper_Launcher_v3/Hyper_Launcher_v3/src/main/java/com/ashmeet/hyperlauncher/screens/layouts/settings/MainSettingsScreen.kt)
- Add "Plugins" entry in the settings list.

---

## Verification Plan

### Automated Tests
- Unit tests for `NativePluginManager.parseLibsString` to ensure correct parsing of the metadata format.
- Mock `PackageManager` to test `LibraryPlugin.discoverPlugin` discovery logic.

### Manual Verification
1. Install a dummy plugin APK with `com.ashmeet.hyperplugin` package and metadata.
2. Verify "Plugins" appears in Settings.
3. Verify library list correctly reflects metadata contents.
4. Toggle an optional library and check `System.load` logs (if debuggable).
5. Uninstall plugin and verify launcher still runs without errors.
