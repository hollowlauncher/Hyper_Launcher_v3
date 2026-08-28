package dev.koraizen.imgui.android;

import android.os.Build;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Prepares the Android/Bionic JNI library before the first imgui-java class is initialized. */
public final class AndroidImGuiNativeLoader {
    private static final Logger LOGGER = Logger.getLogger(AndroidImGuiNativeLoader.class.getName());
    private static volatile boolean prepared;

    private AndroidImGuiNativeLoader() {}

    public static synchronized boolean prepare() {
        if (prepared) return true;

        String abi = detectAbi();
        String resource = "/native/android/" + abi + "/libimgui-java64.so";

        try (InputStream input = AndroidImGuiNativeLoader.class.getResourceAsStream(resource)) {
            if (input == null) throw new IOException("Missing native resource: " + resource);

            byte[] bytes = readAllBytes(input);
            String hash = sha256(bytes).substring(0, 16);

            File tempRoot = executableTempRoot();
            File directory = new File(tempRoot, "koraizen-imgui-android/1.90.0/" + abi + "/" + hash);

            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + directory);
            }

            File library = new File(directory, "libimgui-java64.so");

            if (!library.exists() || library.length() != bytes.length) {
                File temporary = File.createTempFile("imgui-", ".tmp", directory);
                try (FileOutputStream out = new FileOutputStream(temporary)) {
                    out.write(bytes);
                }

                if (!temporary.renameTo(library)) {
                    // Fallback to manual copy if rename fails
                    try (FileOutputStream out = new FileOutputStream(library)) {
                        out.write(bytes);
                    }
                    temporary.delete();
                }
            }

            System.setProperty("imgui.library.path", directory.getAbsolutePath());
            System.setProperty("imgui.library.name", "libimgui-java64.so");
            
            // Support for Axiom's ImGui fork (imgui-moulberry92)
            System.setProperty("imgui.moulberry92.library.path", directory.getAbsolutePath());
            System.setProperty("imgui.moulberry92.library.name", "libimgui-moulberry92-java64.so");

            prepared = true;
            LOGGER.info("Prepared imgui-java Android JNI: abi=" + abi + ", sha256=" + hash);
            return true;
        } catch (Throwable error) {
            LOGGER.log(Level.SEVERE, "Unable to prepare imgui-java Android JNI for " + abi, error);
            return false;
        }
    }

    public static String detectAbi() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (arch.equals("aarch64") || arch.equals("arm64") || arch.equals("arm64-v8a")) return "arm64-v8a";
        if (arch.startsWith("arm") || arch.equals("armeabi-v7a")) return "armeabi-v7a";
        if (arch.equals("x86_64") || arch.equals("amd64")) return "x86_64";
        throw new IllegalStateException("Unsupported Android ABI: " + arch);
    }

    private static File executableTempRoot() throws IOException {
        String tmp = System.getenv("TMPDIR");
        if (tmp != null && !tmp.isEmpty()) {
            File path = new File(tmp).getAbsoluteFile();
            String value = path.getPath().replace('\\', '/');

            if ((value.startsWith("/data/user/") || value.startsWith("/data/data/")) &&
                    path.isDirectory() && path.canWrite()) {
                return path;
            }
        }

        File fallback = new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile();
        String value = fallback.getPath().replace('\\', '/');

        if (value.startsWith("/storage/") || value.startsWith("/sdcard/")) {
            throw new IOException("Shared Android storage may be mounted noexec: " + fallback);
        }

        return fallback;
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static String sha256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
