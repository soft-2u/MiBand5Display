package org.soft2u.miband_5_display.utils;

import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import rikka.shizuku.Shizuku;

/**
 * Runs shell commands with Shizuku (ADB / shell-level privileges).
 * <p>
 * Used to copy watch-face files into Mi Fit's private data dir
 * ({@code Android/data/com.xiaomi.hm.health/...}) on Android 11+, where a normal
 * app can no longer write without MANAGE_EXTERNAL_STORAGE (banned by Google Play
 * for non file-manager apps).
 */
public class ShizukuHelper {
    private static final String TAG = "ShizukuHelper";

    public static class ShellResult {
        public int exitCode = -1;
        public String stdout = "";
        public String stderr = "";

        public boolean isOk() {
            return exitCode == 0;
        }
    }

    /**
     * @return true if the Shizuku service (Shizuku Manager / Sui) is running.
     */
    public static boolean isAvailable() {
        try {
            return Shizuku.pingBinder();
        } catch (Exception e) {
            Log.w(TAG, "Shizuku.pingBinder failed", e);
            return false;
        }
    }

    /**
     * @return true if the user has granted our app Shizuku permission.
     */
    public static boolean isGranted() {
        try {
            return isAvailable()
                    && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            Log.w(TAG, "Shizuku.checkSelfPermission failed", e);
            return false;
        }
    }

    /**
     * Ask Shizuku Manager for permission. The result is delivered to listeners
     * registered via {@link Shizuku#addRequestPermissionResultListener(Shizuku.OnRequestPermissionResultListener)}.
     */
    public static void requestPermission(int requestCode) {
        try {
            Shizuku.requestPermission(requestCode);
        } catch (Exception e) {
            Log.e(TAG, "Shizuku.requestPermission failed", e);
        }
    }

    /**
     * Run a command (no shell parsing, arguments passed directly) with Shizuku
     * privileges, e.g. {@code exec("cp", src, dst)}.
     */
    public static ShellResult exec(String... command) {
        ShellResult result = new ShellResult();
        if (!isGranted()) {
            result.stderr = "Shizuku permission not granted";
            return result;
        }
        Process process = null;
        try {
            process = Shizuku.newProcess(command, null, null);
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader be = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = br.readLine()) != null) out.append(line).append('\n');
            while ((line = be.readLine()) != null) err.append(line).append('\n');
            result.exitCode = process.waitFor();
            result.stdout = out.toString();
            result.stderr = err.toString();
            if (!result.isOk()) {
                Log.e(TAG, "Command failed (" + result.exitCode + "): "
                        + String.join(" ", command) + "\n" + result.stderr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Shell exec failed: " + String.join(" ", command), e);
            result.stderr = e.toString();
        } finally {
            if (process != null) process.destroy();
        }
        return result;
    }

    /**
     * mkdir -p via Shizuku.
     */
    public static boolean mkdirs(String absDir) {
        return exec("mkdir", "-p", absDir).isOk();
    }

    /**
     * Copy a single file via Shizuku, creating parent dirs as needed.
     * Works even when source/destination live in another app's private dir.
     */
    public static boolean copyFile(File src, File dst) {
        if (src == null || !src.exists()) {
            Log.e(TAG, "copyFile: source not found: " + src);
            return false;
        }
        File parent = dst.getParentFile();
        if (parent != null && !mkdirs(parent.getAbsolutePath())) {
            Log.e(TAG, "copyFile: mkdir failed: " + parent);
            return false;
        }
        return exec("cp", src.getAbsolutePath(), dst.getAbsolutePath()).isOk();
    }
}
