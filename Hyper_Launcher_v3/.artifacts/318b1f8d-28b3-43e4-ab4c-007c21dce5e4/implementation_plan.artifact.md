# Add DrawerPullButton to ControlsEditorScreen and GameControlsScreen

The `DrawerPullButton` is missing from the `ControlsEditorScreen` and `GameControlsScreen`, which prevents users from easily opening the navigation drawer. This plan involves adding the `DrawerPullButton` as an overlay in both screens.

## Proposed Changes

### [Hyper_Launcher_v3]

#### [MODIFY] [ControlsEditorScreen.kt](file:///C:/Users/ASHMEET/Documents/Projects/Apps/Hyper_Launcher/Hyper_Launcher_v3/src/main/java/com/ashmeet/hyperlauncher/screens/activity/game/controls/ControlsEditorScreen.kt)
- Add import for `DrawerPullButton`.
- Add `DrawerPullButton` using `AndroidView` inside the main `Box`.
- Set up a click listener on `DrawerPullButton` to open the navigation drawer using `drawerState.open()`.

#### [MODIFY] [GameControlsScreen.kt](file:///C:/Users/ASHMEET/Documents/Projects/Apps/Hyper_Launcher/Hyper_Launcher_v3/src/main/java/com/ashmeet/hyperlauncher/screens/activity/game/controls/GameControlsScreen.kt)
- Add import for `DrawerPullButton`.
- Add `DrawerPullButton` using `AndroidView` inside the main `Box`.
- Set up a click listener on `DrawerPullButton` to open the navigation drawer using `drawerState.open()`.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the Controls Editor.
- Verify that the `DrawerPullButton` (Settings icon) is visible.
- Verify that tapping the button opens the side drawer.
- Navigate to the Game Controls screen (in-game) and verify the button is also present and functional there.
