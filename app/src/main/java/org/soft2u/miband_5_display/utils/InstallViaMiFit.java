package org.soft2u.miband_5_display.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.soft2u.miband_5_display.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InstallViaMiFit {

    private final String TAG = "InstallViaMiFit";
    private final String SPECIFIED_FOLDER = "LQL9x2usQGxi8HgzD7ginnxpwc6XHsLv3z730sq1";

    /**
     * @return true if the Shizuku service (Shizuku Manager / Sui) is running.
     */
    public static boolean isShizukuAvailable() {
        return ShizukuHelper.isAvailable();
    }

    /**
     * @return true if the user has granted our app Shizuku permission.
     */
    public static boolean isShizukuGranted() {
        return ShizukuHelper.isGranted();
    }

    /**
     * Ask Shizuku Manager for permission. The result is delivered to listeners
     * registered via Shizuku.addRequestPermissionResultListener().
     */
    public static void requestShizukuPermission(int requestCode) {
        ShizukuHelper.requestPermission(requestCode);
    }

    /**
     * Mi Fit's watch-face dir: /storage/emulated/0/Android/data/com.xiaomi.hm.health/files/watch_skin_local
     * On Android 11+ a normal app cannot write here, so all copies go through Shizuku shell.
     */
    private File getMiFitSkinDir() {
        return new File(Environment.getExternalStorageDirectory(),
                "Android/data/" + Install.APPLICATION_MI_FIT_ID.value + "/files/watch_skin_local");
    }

    private boolean ensureShizuku(Context mContext) {
        if (!isShizukuAvailable()) {
            Toast.makeText(mContext, R.string.toast_shizuku_not_running, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!isShizukuGranted()) {
            Toast.makeText(mContext, R.string.toast_shizuku_permission_required, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean copyToMiFit(Context mContext, File srcFile, File dstFile) {
        if (!ShizukuHelper.copyFile(srcFile, dstFile)) {
            String msg = mContext.getResources().getString(R.string.toast_notice_copy_fail, srcFile.getName());
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * @param folder - generated folder
     */
    public boolean copyFileToMiFit(Context mContext, String folder) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(mContext, R.string.toast_notice_storage_is_unmount, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!ensureShizuku(mContext)) return false;

        String[] srcFilesName = {FILE.DISPLAY.value, FILE.COVER.value, FILE.INFO.value};
        File dataDir = getMiFitSkinDir();
        for (String fileName : srcFilesName) {
            File srcFile = new File(mContext.getExternalFilesDir(null), fileName);
            if (!srcFile.exists()) {
                String msg = mContext.getResources().getString(R.string.toast_file_not_found, fileName);
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                return false;
            }
            File destination = new File(dataDir, folder + "/" + fileName);
            if (!copyToMiFit(mContext, srcFile, destination)) return false;
        }
        return true;
    }

    ///////////

    public boolean copyBINFile(Context mContext, String filePath) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(mContext, R.string.toast_notice_storage_is_unmount, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!ensureShizuku(mContext)) return false;

        File srcFile = new File(filePath);
        if (!srcFile.isAbsolute()) {
            // Path relative to the shared-storage root, e.g. "Download/xxx.bin"
            srcFile = new File(Environment.getExternalStorageDirectory(), filePath);
        }
        File destination = new File(getMiFitSkinDir(), SPECIFIED_FOLDER + "/" + FILE.DISPLAY.value);
        return copyToMiFit(mContext, srcFile, destination);
    }

    public boolean copyCoverFile(Context mContext) {
        if (!ensureShizuku(mContext)) return false;

        AssetManager assetManager = mContext.getAssets();
        String[] files;
        try {
            files = assetManager.list("themeinfo");
        } catch (IOException ex) {
            Log.d(TAG, "Failed to get asset file list. ", ex);
            return false;
        }

        if (files != null) {
            for (String filename : files) {
                // Stage the asset into our own external files dir, then copy via Shizuku
                File staged = new File(mContext.getExternalFilesDir(null), filename);
                if (!staged.exists() && !extractAsset(assetManager, "themeinfo/" + filename, staged)) {
                    return false;
                }
                File destination = new File(getMiFitSkinDir(), SPECIFIED_FOLDER + "/" + filename);
                if (!copyToMiFit(mContext, staged, destination)) return false;
            }
        }
        return true;
    }

    private boolean extractAsset(AssetManager assetManager, String assetPath, File outFile) {
        try (InputStream in = assetManager.open(assetPath);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to extract asset: " + assetPath, e);
            return false;
        }
    }

    public boolean copyInfoFile(Context mContext) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(mContext, R.string.toast_notice_storage_is_unmount, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!ensureShizuku(mContext)) return false;

        File srcFile = new File(mContext.getExternalFilesDir(null), FILE.INFO.value);
        if (!srcFile.exists()) {
            String msg = mContext.getResources().getString(R.string.toast_file_not_found, FILE.INFO.value);
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            return false;
        }
        File destination = new File(getMiFitSkinDir(), SPECIFIED_FOLDER + File.separator + FILE.INFO.value);
        return copyToMiFit(mContext, srcFile, destination);
    }
}
