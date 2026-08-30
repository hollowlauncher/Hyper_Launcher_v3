# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\tools\adt-bundle-windows-x86_64-20131030\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# General optimization settings
-keepattributes Signature,AnnotationDefault,EnclosingMethod,InnerClasses,SourceFile,LineNumberTable
-repackageclasses ''
-allowaccessmodification

# We use Reflection on the builder to avoid creating too many objects
 -keep class net.objecthunter.exp4j.ExpressionBuilder**
 -keepclassmembers class net.objecthunter.exp4j.ExpressionBuilder** {
    *;
 }
# Option screens
-keep class com.ashmeet.hyperlauncher.LauncherPreference.LauncherPreferenceFragment { *; }
-keep class com.ashmeet.hyperlauncher.LauncherPreference.LauncherPreferenceJavaFragment { *; }
-keep class com.ashmeet.hyperlauncher.LauncherPreference.LauncherPreferenceVideoFragment { *; }
-keep class com.ashmeet.hyperlauncher.LauncherPreference.LauncherPreferenceControlFragment { *; }
-keep class com.ashmeet.hyperlauncher.LauncherPreference.LauncherPreferenceAppearanceFragment { *; }
-keep class com.ashmeet.hyperlauncher.LauncherPreference.LauncherPreferenceExperimentalFragment { *; }
-keep class com.ashmeet.hyperlauncher.LauncherPreference.LauncherPreferenceMiscellaneousFragment { *; }

# Minecraft JSON classes (GSON mapping and Reflection)
-keep class net.kdt.pojavlaunch.JVersionList** { *; }
-keep class net.kdt.pojavlaunch.modloaders.modpacks.api.** { *; }

# ASM and Bytecode Injectors
-keep class org.objectweb.asm.** { *; }
-keep class org.angelauramc.methodsInjectorAgent.** { *; }
-keep class java.lang.instrument.** { *; }

# Keep AWT/Swing classes if they are bundled (common issue in PojavLauncher)
-keep class java.awt.** { *; }
-keep class javax.swing.** { *; }
-dontwarn java.awt.**
-dontwarn javax.swing.**

# Gson rules
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.TypeAdapter
-keep class com.google.gson.stream.**
-keep class net.kdt.pojavlaunch.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Zstd-jni (Distant Horizons)
-keep class com.github.luben.zstd.** { *; }
-dontwarn com.github.luben.zstd.**

# Prevent enum obfuscation for Gson compatibility
-keepclassmembers enum * { *; }


