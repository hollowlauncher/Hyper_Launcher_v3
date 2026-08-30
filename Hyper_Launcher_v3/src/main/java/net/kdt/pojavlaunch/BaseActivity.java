package net.kdt.pojavlaunch;

import static com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences.PREF_FULLSCREEN_LAUNCHER;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import net.kdt.pojavlaunch.utils.LocaleUtils;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtils.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Edge-to-edge should be enabled before super.onCreate
        if (shouldEnableEdgeToEdge() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            EdgeToEdge.enable(this, 
                SystemBarStyle.dark(Color.TRANSPARENT), 
                SystemBarStyle.dark(Color.TRANSPARENT));
        }

        super.onCreate(savedInstanceState);
        LocaleUtils.setLocale(this);

        applySystemBarConfiguration();
        Tools.getDisplayMetrics(this);
    }

    /**
     * Applies the system bar configuration (visibility and behavior) using modern APIs.
     */
    private void applySystemBarConfiguration() {
        boolean isFullscreen = setFullscreen();
        boolean isEdgeToEdge = shouldEnableEdgeToEdge();
        boolean hideBars = isFullscreen || isEdgeToEdge;

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        if (hideBars) {
            // Hide navigation bars (and status bars if in fullscreen mode)
            int types = WindowInsetsCompat.Type.navigationBars();
            if (isFullscreen) {
                types |= WindowInsetsCompat.Type.statusBars();
            }
            controller.hide(types);
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars());
        }

        // Call Tools.setInsetsMode to handle complex padding/background logic (InsetBackground)
        // and legacy fallbacks for older Android versions.
        Tools.setInsetsMode(this, hideBars, shouldIgnoreNotch());
    }

    /** @return Whether the activity should be set as a fullscreen one */
    public boolean setFullscreen(){
        return PREF_FULLSCREEN_LAUNCHER;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tools.checkStorageInteractive(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Re-apply configuration to ensure bars stay hidden after returning to the activity
        applySystemBarConfiguration();
        Tools.getDisplayMetrics(this);
    }

    /** @return Whether the notch should be ignored */
    protected boolean shouldIgnoreNotch(){
        return true;
    }

    /** @return Whether the activity should enable Edge-to-Edge */
    protected boolean shouldEnableEdgeToEdge() {
        return PREF_FULLSCREEN_LAUNCHER;
    }
}
