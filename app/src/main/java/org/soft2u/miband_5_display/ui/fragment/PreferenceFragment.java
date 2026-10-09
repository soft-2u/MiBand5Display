package org.soft2u.miband_5_display.ui.fragment;

import static android.content.Context.ACTIVITY_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.dialog.AboutDialog;
import org.soft2u.miband_5_display.ui.dialog.LanguageAppDialog;
import org.soft2u.miband_5_display.ui.dialog.OtherAppsDialog;
import org.soft2u.miband_5_display.ui.dialog.SupportDevDialog;
import org.soft2u.miband_5_display.ui.dialog.ThemeAppDialog;
import org.soft2u.miband_5_display.utils.Install;

import java.io.File;
import java.util.Objects;

public class PreferenceFragment extends PreferenceFragmentCompat {

    private final String TAG = "PreferenceFragment";
    private FragmentActivity mActivity;
    private final int REQUEST_PERMISSION_STORAGE = 1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity){
            mActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.main_settings, rootKey);

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            Preference preference1 = findPreference("clear_mifit_display_folder");
            ((PreferenceCategory) findPreference("general")).removePreference(preference1);

            Preference preference2 = findPreference("reset_install_method");
            ((PreferenceCategory) findPreference("general")).removePreference(preference2);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        switch (key){
            case "theme":
                FragmentManager fm3 = mActivity.getSupportFragmentManager();
                ThemeAppDialog dg3 = new ThemeAppDialog();
                dg3.show(fm3, "CHANGE_THEME_DIALOG");
                return true;
            case "language":
                FragmentManager fm2 = mActivity.getSupportFragmentManager();
                LanguageAppDialog dg2 = new LanguageAppDialog();
                dg2.show(fm2, "CHANGE_LANGUAGE_DIALOG");
                return true;
            case "clear_mifit_display_folder":
                showAlertClearMiFit();
                return true;
            case "clear_app_cache":
                clearAppCache();
                return true;
            case "reset_install_method":
                new PrefManager(mActivity).setRememberMethodInstall("");
                Toast.makeText(mActivity, R.string.toast_success, Toast.LENGTH_SHORT).show();
                return true;
            case "rate":
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Install.APPLICATION_ID.value)));
                } catch (android.content.ActivityNotFoundException ex) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + Install.APPLICATION_ID.value)));
                }
                return true;
            case "donate":
                FragmentManager fm4 = mActivity.getSupportFragmentManager();
                SupportDevDialog dg4 = new SupportDevDialog();
                dg4.show(fm4, "SUPPORT_DEV_DIALOG");
                return true;
            case "other_apps":
                FragmentManager fm = mActivity.getSupportFragmentManager();
                OtherAppsDialog dg = new OtherAppsDialog();
                dg.show(fm, "OTHER_APPS_DIALOG");
                return true;
            case "about":
                FragmentManager fm5 = mActivity.getSupportFragmentManager();
                AboutDialog dg5 = new AboutDialog();
                dg5.show(fm5, "CHANGE_LANGUAGE_DIALOG");
                return true;
        }
        return false;
    }

    private void showAlertClearMiFit() {
        if (!isStoragePermissionGranted(mActivity, REQUEST_PERMISSION_STORAGE))
            return;

        File sd = Environment.getExternalStorageDirectory();
        String MI_DATA_PATH = "/Android/data/" + Install.APPLICATION_MI_FIT_ID.value + "/files";
        String SKIN_FOLDER = "watch_skin_local";
        String SKIN_PATH = MI_DATA_PATH + File.separator + SKIN_FOLDER;
        File displayFolder = new File(sd, SKIN_PATH);

        if(displayFolder.exists()) {
            String folderSize;
            long bytes = getFolderSize(displayFolder);
            if (bytes < 1024)
                folderSize = bytes + "Bytes";
            else {
                int exp = (int) (Math.log(bytes) / Math.log(1024));
                String pre = ("KMGTPE").charAt(exp - 1) + "";
                folderSize = String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
            }

            String msgDialog = getResources().getString(R.string.dg_msg_clear_mi_fit_display_folder, folderSize);

            AlertDialog.Builder builder1 = new AlertDialog.Builder(mActivity);
            builder1.setTitle(R.string.dg_title_clear_mi_fit_display_folder);
            builder1.setMessage(msgDialog);
            builder1.setCancelable(true)
                    .setPositiveButton(R.string.msg_alert_yes, (DialogInterface dialog, int id) -> {
                            deleteSubFolders(displayFolder);
                            Toast.makeText(mActivity, R.string.toast_success, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                    })
                    .setNegativeButton(R.string.msg_alert_no, (DialogInterface dialog, int id) ->
                            dialog.cancel()
                    );
            builder1.show();
        } else
            Toast.makeText(mActivity, R.string.toast_mi_fit_app_not_found, Toast.LENGTH_SHORT).show();
    }

    private void clearAppCache() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) mActivity.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
                Toast.makeText(mActivity, R.string.toast_success, Toast.LENGTH_SHORT).show();
            } else {
                String packageName = mActivity.getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear "+packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getFolderSize(File f) {
        long size = 0;
        if (f.isDirectory()) {
            for (File file : Objects.requireNonNull(f.listFiles())) {
                size += getFolderSize(file);
            }
        } else {
            size = f.length();
        }
        return size;
    }

    private void deleteSubFolders(File folder)
    {
        File files[] = folder.listFiles();

        if (files == null) {
            return;
        }
        for (File f : files)
        {
            if (f.isDirectory())
            {
                deleteSubFolders(f);
            }
            //no else, or you'll never get rid of this folder!
            f.delete();
        }
    }

    public boolean isStoragePermissionGranted(Activity mActivity, int requestCode) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (mActivity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"Write Permission is granted");
                return true;
            } else {
                Log.d(TAG,"Write Permission is denied. Start request again");
                //requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return false;
            }
        } else {
            // Permission is automatically granted on sdk<23 upon installation
            Log.d(TAG,"Write Permission already granted");
            return true;
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Log.d(TAG,"Permission was granted");
                } else {
                    // PERMISSION NOT GRANTED
                    // with only one request
                    boolean showRationale = false;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    if (!showRationale) {
                        // Checked "Never ask again"
                        Toast.makeText(mActivity, R.string.toast_permissions_write_storage_denied, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_STORAGE);
                    } else {
                        // select Deny
                        Toast.makeText(mActivity, R.string.toast_permissions_write_storage_denied, Toast.LENGTH_LONG).show();
                    }
                    Log.d(TAG,"Write Permission request is denied");
                }
            }
    );

//    public void deleteFile(String filePath){
//        if(filePath.startsWith("content://")){
//            ContentResolver contentResolver = getActivity().getContentResolver();
//            contentResolver.delete(Uri.parse(filePath), null, null);
//        }else {
//            File file = new File(filePath);
//            if(file.exists()) {
//                if (file.delete()) {
//                    Log.e(TAG, "File deleted.");
//                }else {
//                    Log.e(TAG, "Failed to delete file!");
//                }
//            }else {
//                Log.e(TAG, "File not exist!");
//            }
//        }
//    }
}
