package org.soft2u.miband_5_display.ui.dialog;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.soft2u.miband_5_display.CustomResultReceiver;
import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.activity.HelpActivity;
import org.soft2u.miband_5_display.utils.FILE;
import org.soft2u.miband_5_display.utils.FlowLayout;
import org.soft2u.miband_5_display.utils.General;
import org.soft2u.miband_5_display.utils.BlePermissionHelper;
import org.soft2u.miband_5_display.utils.Install;
import org.soft2u.miband_5_display.utils.InstallViaMiFit;
import org.soft2u.miband_5_display.utils.NotifyService;

import rikka.shizuku.Shizuku;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DisplayDetailDialog extends BottomSheetDialogFragment implements CustomResultReceiver.AppReceiver {

    private FragmentActivity mActivity;

    private String jsonReceiver;

    HashMap<String, List<HashMap<String, String>>> lang = new HashMap<>();
    List<String> lsVarients = new ArrayList<>();
    List<String> lsDateFormat = new ArrayList<>();

    private int THEME_ID;
    private String TITLE, OPTION_SELECTED, LANG_SELECTED = "", DATE_FORMAT_SELECTED = "", VARIANT_SELECTED = "";
    private final String TAG = "ThemeDetailDialog";
    private String coverFileUrl;

    private final int REQUEST_PERMISSION_STORAGE = 1;
    private final int REQUEST_INSTALL_METHOD = 1002;
    private final int REQUEST_ENABLE_BT = 1003;
    private final int REQUEST_BLE_CONNECT = 1005;
    private String pendingBleAddress = "";
    private final int REQUEST_SHIZUKU = 1004;

    private final Shizuku.OnRequestPermissionResultListener shizukuPermissionListener = (requestCode, grantResult) -> {
        if (requestCode == REQUEST_SHIZUKU) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                installMethod2(); // retry now that Shizuku is granted
            } else {
                Toast.makeText(mActivity, R.string.toast_shizuku_permission_required, Toast.LENGTH_LONG).show();
            }
        }
    };

    View root;
    private Button btnDownload, btnInstall;
    private ProgressBar progressBar;
    private TextView btnSave, tvOptionSelected;

    private AsyncTask<String, Integer, String> dlTask;

    private String BLEAddress = "";
    private CustomResultReceiver resultReceiver;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        dlTask = null;
    }

    public DisplayDetailDialog() {

    }

    public static DisplayDetailDialog newInstance(String json) {
        Bundle args = new Bundle();
        args.putString("JSON", json);
        DisplayDetailDialog f = new DisplayDetailDialog();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jsonReceiver = getArguments().getString("JSON", "{'key':'NULL'}");
        }
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener);
    }

    @Override
    public void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener);
        super.onDestroy();
    }

    @Override
    public int getTheme() {
        return super.getTheme();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

//        getDialog().setOnShowListener((DialogInterface dialog) -> {
//            BottomSheetDialog d = (BottomSheetDialog) dialog;
//            View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//            BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
//        });

        root = inflater.inflate(R.layout.fragment_dialog_display_detail, container, false);

        btnSave = root.findViewById(R.id.btn_save);

        btnDownload = root.findViewById(R.id.btn_download);
        btnInstall = root.findViewById(R.id.btn_install);
        progressBar = root.findViewById(R.id.progress_bar);
        tvOptionSelected = root.findViewById(R.id.tv_option_selected);

        btnSave.setOnClickListener((View view) -> {
            save();
        });

        root.findViewById(R.id.btn_help).setOnClickListener((View view) ->
                showHelpActivity(new PrefManager(mActivity).getLocale())
        );

        btnDownload.setOnClickListener((View view) -> {
            if(dlTask != null) {
                if(dlTask.cancel(true)) {
                    dlTask = null;
                    Log.d(TAG, "task ended");
                } else
                    Log.d(TAG, "task not end");
            }

            download();
        });

        btnInstall.setOnClickListener((View view) ->
                install()
        );

        readJsonData(jsonReceiver, root);

        return root;
    }

    private void download() {
        if(!OPTION_SELECTED.equals("")) {
            if (!new General().isInternetConnection(mActivity)) {
                Toast.makeText(mActivity, R.string.toast_notice_no_internet_download, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                Toast.makeText(mActivity, R.string.toast_notice_storage_is_unmount, Toast.LENGTH_SHORT).show();
                return;
            }

            if (isStoragePermissionGranted(mActivity, REQUEST_PERMISSION_STORAGE)) {
                String url = "https://miband4display-server.csm2.duckdns.org/public/files/display/binary/" + OPTION_SELECTED + ".bin";
                dlTask = new DownloadFileFromURL(url, "data_folder", FILE.DISPLAY.value).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                dlTask = new DownloadFileFromURL(coverFileUrl, "data_folder", FILE.COVER.value).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private void save() {
        if(OPTION_SELECTED!= null && !OPTION_SELECTED.equals("")) {
            if (!new General().isInternetConnection(mActivity)) {
                Toast.makeText(mActivity, R.string.toast_notice_no_internet_download, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                Toast.makeText(mActivity, R.string.toast_notice_storage_is_unmount, Toast.LENGTH_SHORT).show();
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: shared Downloads is written via MediaStore, no system
                // permission needed - just confirm with the user first.
                showDownloadsAccessDialog(this::startBinDownload);
            } else if (isStoragePermissionGranted(mActivity, REQUEST_PERMISSION_STORAGE)) {
                startBinDownload();
            }
        }
    }

    private void startBinDownload() {
        String url = "https://miband4display-server.csm2.duckdns.org/public/files/display/binary/" + OPTION_SELECTED + ".bin";
        dlTask = new DownloadFileFromURL(url, "downloads_folder", TITLE + "_" + LANG_SELECTED + "_" + DATE_FORMAT_SELECTED + "_" + VARIANT_SELECTED + ".bin" ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Informs the user that the .bin file will be saved into the shared
     * Downloads folder and asks for confirmation before downloading.
     */
    private void showDownloadsAccessDialog(Runnable onConfirm) {
        new AlertDialog.Builder(mActivity)
                .setTitle(R.string.dialog_downloads_access_title)
                .setMessage(getString(R.string.dialog_downloads_access_message, getString(R.string.app_name)))
                .setPositiveButton(R.string.button_ok, (dialog, which) -> onConfirm.run())
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    /**
     * Android 10+: create an entry in the shared Downloads collection via MediaStore.
     * No storage permission is required for our own files.
     *
     * @return OutputStream to write the file, or null on failure.
     */
    private OutputStream openMediaStoreDownloadsStream(String saveName) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, saveName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
            values.put(MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/" + getResources().getString(R.string.app_name));
            ContentResolver resolver = mActivity.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return null;
            return resolver.openOutputStream(uri);
        } catch (Exception e) {
            Log.e(TAG, "MediaStore insert failed: " + e.getMessage());
            return null;
        }
    }

    private boolean writeInfoFile(String str, String fileName) {
        try {
            File file = new File(mActivity.getExternalFilesDir(null), fileName);
            FileWriter out = new FileWriter(file);
            out.write(str);
            out.close();
        }
        catch (IOException e) {
            Log.e(TAG, "Write Info file failed: " + e.toString());
            Toast.makeText(mActivity, "Write Info file failed. Please contact developer!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void install() {
        String remember = new PrefManager(mActivity).getRememberMethodInstall();
        if(remember.equals(Install.METHOD_BLUETOOTH.value)) {
            BLEAddress = new PrefManager(mActivity).getBLEAddress();
            Log.i(TAG, BLEAddress);
            installMethod1(BLEAddress);
        } else if(remember.equals(Install.METHOD_MIFIT.value)) {
            installMethod2();
        } else {
            FragmentManager fm = mActivity.getSupportFragmentManager();
            InstallMethodDialog dg = new InstallMethodDialog();
            dg.show(fm, "INSTALL_METHOD_DIALOG");
            dg.setTargetFragment(DisplayDetailDialog.this, REQUEST_INSTALL_METHOD);
        }
    }

    public void installMethod1(String BLEAddress) {
        // Android 12+: isEnabled()/connectGatt() need BLUETOOTH_CONNECT at runtime
        if (!BlePermissionHelper.isBleConnectGranted(mActivity)) {
            pendingBleAddress = BLEAddress;
            BlePermissionHelper.requestBleConnect(this, REQUEST_BLE_CONNECT);
            return;
        }
        if(new General().isPackageInstalled(mActivity, Install.APPLICATION_MI_FIT_ID.value)) {
            BluetoothManager bluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
                requestEnableBluetooth();
            else
            {
                if(!BLEAddress.equals("")) {
//                    InstallViaBluetooth installViaBluetooth = new InstallViaBluetooth(mActivity, firmware);
////                    installViaBluetooth.connect(BLEAddress);
                    startService(mActivity, "updateFirmwareFromSys", FILE.DISPLAY.value, BLEAddress);
                    setInstallWorking(true);
                }
                else {
                    // scan to find BLEAddress
                    FragmentManager fm2 = mActivity.getSupportFragmentManager();
                    ScanBLEDialog dg = new ScanBLEDialog();
                    dg.show(fm2, "SCAN_BLE_DIALOG");
                }
            }
        }
        else
            Toast.makeText(mActivity, R.string.toast_notice_need_mi_fit_paired_already, Toast.LENGTH_LONG).show();
    }

    private void installMethod2() {
        if (!InstallViaMiFit.isShizukuAvailable()) {
            Toast.makeText(mActivity, R.string.toast_shizuku_not_running, Toast.LENGTH_LONG).show();
            return;
        }
        if (!InstallViaMiFit.isShizukuGranted()) {
            InstallViaMiFit.requestShizukuPermission(REQUEST_SHIZUKU);
            return;
        }
        String str = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?><watch_skin_info><name>" + TITLE + "</name><time_stamp>" + new Date().getTime() + "</time_stamp></watch_skin_info>";
        if(!writeInfoFile(str, FILE.INFO.value)) {
            return;
        }

        if(new General().isPackageInstalled(mActivity, Install.APPLICATION_MI_FIT_ID.value))
        {
            String generatedFolder = "LQL" + new Date().getTime();
            if(new InstallViaMiFit().copyFileToMiFit(mActivity, generatedFolder)) {
                Intent intent = mActivity.getPackageManager().getLaunchIntentForPackage(Install.APPLICATION_MI_FIT_ID.value);
                startActivity(intent);

                // rate request
//                    if(new PrefManager(mActivity).getStatusShowRateRequest()) {
//                        FragmentManager fm = mActivity.getSupportFragmentManager();
//                        RequestRateDialog dg = new RequestRateDialog();
//                        dg.show(fm, "REQUEST_RATE_DIALOG");
//                    }
            } else {
                Toast.makeText(mActivity, "Write data to Mi Fit app failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mActivity, R.string.toast_mi_fit_app_not_found, Toast.LENGTH_LONG).show();
        }
    }

    private void requestEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void reportError(int themeId, String error, boolean showToast) {
        String suffixUrl = "act=report&themeid=" + themeId + "&error="+error+"";
        new General().executeVolley(suffixUrl, mActivity);
        if(showToast)
            Toast.makeText(mActivity, R.string.toast_send_report_success, Toast.LENGTH_SHORT).show();
    }

    private void updateDownloads(int themeId) {
        String suffixUrl = "act=themedetail&job=updatedownload&themeid=" + themeId + "";
        new General().executeVolley(suffixUrl, mActivity);
    }

    private void readJsonData(String json, View view) {
        try {
            JSONObject obj = new JSONObject(json);
            THEME_ID = Integer.parseInt(obj.getString("themeid"));
            coverFileUrl = "https://miband4display-server.csm2.duckdns.org/public/files/display/cover/" + obj.getString("img_url");
            TITLE = obj.getString("title");

            // read languages
            JSONArray languageObject = new JSONArray(obj.getString("url"));
            for (int i = 0; i < languageObject.length(); i++) {
                readLanguage(languageObject.getString(i));
            }

            // languages
            for (String key : lang.keySet()) {
                TextView textView = new TextView(new ContextThemeWrapper(mActivity, R.style.ButtonToggle));
                textView.setText(key);
                textView.setTag("none");
                textView.setOnClickListener(tvLanguageItemClick);
                ((FlowLayout) view.findViewById(R.id.flowlayout_language_container)).addView(textView);
            }

            // date format
            if(lsDateFormat.size() > 0) {
                Set<String> lsDateFormatMin = new LinkedHashSet<>(lsDateFormat); // remove duplicates
                for (String dateFormat : lsDateFormatMin) {
                    TextView textView = new TextView(new ContextThemeWrapper(mActivity, R.style.ButtonToggle));
                    textView.setText(dateFormat);
                    textView.setTag("none");
                    textView.setOnClickListener(tvDateFormatItemClick);
                    ((FlowLayout) view.findViewById(R.id.flowlayout_date_format_container)).addView(textView);
                }
            } else {
                view.findViewById(R.id.tv_theme_date_format_label).setVisibility(View.GONE);
                view.findViewById(R.id.flowlayout_date_format_container).setVisibility(View.GONE);
            }

            // variants
            Set<String> lsVarientsMin = new LinkedHashSet<>(lsVarients); // remove duplicates
            if(lsVarientsMin.size() == 1 && (new ArrayList<>(lsVarientsMin)).get(0).equals("Standard") || lsVarientsMin.size() == 0) {
                view.findViewById(R.id.tv_theme_variant_label).setVisibility(View.GONE);
                view.findViewById(R.id.flowlayout_variant_container).setVisibility(View.GONE);
            } else {
                for (String variant : lsVarientsMin) {
                    TextView textView = new TextView(new ContextThemeWrapper(mActivity, R.style.ButtonToggle));
                    textView.setText(variant);
                    textView.setTag("none");
                    textView.setOnClickListener(tvVariantItemClick);
                    ((FlowLayout) view.findViewById(R.id.flowlayout_variant_container)).addView(textView);
                }
            }
        } catch (JSONException ex) {
            Log.e(TAG,"cannot parse json string.  " + ex.toString());
            Toast.makeText(mActivity, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        /*
         * Step 3: Handle the results from the intent service here!
         * */
        if(resultCode == 1)
            setInstallWorking(false);
    }

    @Override
    public void onStop() {
        super.onStop();

        /*
         * Step 4: don't forget to clear receiver in order to avoid leaks.
         * */
        if(resultReceiver != null) {
            resultReceiver.clearAppReceiver();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadFileFromURL extends AsyncTask<String, Integer, String> {
        private boolean isSuccess = true;
        private boolean isCancel = false; // press Cancel by user
        private boolean isConnect = true;
        private ProgressDialog progressDialog;

        private String fileUrl, saveTo, saveName;

        DownloadFileFromURL(String fileUrl, String saveTo, String saveName) {
            this.fileUrl = fileUrl;
            this.saveTo = saveTo;
            this.saveName = saveName;
        }

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setMessage(getResources().getString(R.string.label_downloading));
            progressDialog.setMax(100);
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.button_cancel), (DialogInterface dialog, int which) -> {
                isCancel = true;
            });
            progressDialog.setOnDismissListener(DialogInterface::cancel
            );
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... params) {
            int count;
            URL url;
            int lengthOfFile;

            try {
                url = new URL(fileUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                lengthOfFile = connection.getContentLength();
            } catch (IOException e) {
                isSuccess = false;
                Log.e(TAG, "Open connection error: " + e.getMessage());
                return null;
            }

            try {
                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),8192);
                OutputStream output;
                if (saveTo.equals("downloads_folder") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+: shared Downloads via MediaStore, no permission needed
                    output = openMediaStoreDownloadsStream(saveName);
                    if (output == null) {
                        isSuccess = false;
                        Log.e(TAG, "Cannot create MediaStore entry in Downloads");
                        input.close();
                        return null;
                    }
                } else {
                    File file;
                    if(saveTo.equals("downloads_folder")) {
                        // if not exist (legacy path, Android 9 and below)
                        File subFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getResources().getString(R.string.app_name));
                        if(!subFolder.exists())
                            subFolder.mkdir();
                        file = new File(subFolder, saveName);
                    }
                    else
                        file = new File(mActivity.getExternalFilesDir(null), saveName);
                    output = new FileOutputStream(file);
                }

                byte[] data = new byte[1024];
                long total = 0;
                while (total < lengthOfFile) {
                    if(!new General().isInternetConnection(mActivity)) {
                        isConnect = false;
                        Log.e(TAG, "No internet access");
                        break;
                    } else {
                        count = input.read(data);
                        if(count == -1) {
                            isSuccess = false;
                            Log.e(TAG, "error");
                            break;
                        }
                        if(isCancel) {
                            Log.e(TAG, "stopped");
                            break;
                        }
                        total += count;
//                        Log.d(TAG, String.valueOf(total));
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress((int) ((total * 100) / lengthOfFile));
                        // writing data to file
                        output.write(data, 0, count);
                    }
                }

                // flushing output
                output.flush();
                // closing streams
                output.close();
                input.close();

            } catch (IOException e) {
                isSuccess = false;
                Log.e(TAG, "Error writing: " + e.getMessage());
            }
            return null;
        }

        /**
         * Updating progress bar
         * */
        @Override
        protected void onProgressUpdate(Integer... values) {
            // setting progress percentage
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            Log.d(TAG, "canceled");
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String fileUrl) {
            if(isConnect && isSuccess) {
                // download finished
                if(saveTo.equals("downloads_folder")) {
                    String msg = getResources().getString(R.string.toast_success) + ". " + "Download/" + getResources().getString(R.string.app_name) + "/" + saveName;
                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
                    updateDownloads(THEME_ID);
                } else {
                    if (saveName.equals(FILE.DISPLAY.value)) {
                        Toast.makeText(mActivity, R.string.toast_file_downloaded_finished, Toast.LENGTH_LONG).show();
                        btnDownload.setEnabled(false);
                        btnDownload.setTextColor(getResources().getColor(R.color.color_on_primary_disabled));
                        btnInstall.setEnabled(true);
                        btnInstall.setTextColor(getResources().getColor(R.color.color_on_primary));
                        updateDownloads(THEME_ID); // only update 1 times
                    }
                }
            } else if (!isSuccess) {
                Toast.makeText(mActivity, R.string.toast_notice_download_fail, Toast.LENGTH_SHORT).show();
                reportError(THEME_ID, "DISPLAY_DOWNLOAD_LINK_ERROR", false);
            } else {
                Toast.makeText(mActivity, R.string.toast_notice_no_internet_download, Toast.LENGTH_SHORT).show();
            }

//            dlTask.cancel(true);
            progressDialog.dismiss();
        }
    }

    public boolean isStoragePermissionGranted(Activity mActivity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped storage: app-specific dirs and MediaStore need no runtime storage permission
            Log.d(TAG,"Storage permission not needed on Android 10+");
            return true;
        }
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (mActivity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"Write Permission is granted");
                return true;
            } else {
                Log.d(TAG,"Write Permission is denied. Start request again");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                return false;
            }
        } else {
            // Permission is automatically granted on sdk<23 upon installation
            Log.d(TAG,"Write Permission already granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLE_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                installMethod1(pendingBleAddress);
            } else {
                Toast.makeText(mActivity, R.string.toast_permissions_ble_denied, Toast.LENGTH_LONG).show();
            }
            return;
        }
        if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.d(TAG,"Permission: " +permissions[0]+ " was " +grantResults[0]);
        } else {
            // with only one request
            boolean showRationale = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                showRationale = shouldShowRequestPermissionRationale(permissions[0]);
            }
            if (!showRationale) {
                // Checked "Never ask again"
                Toast.makeText(mActivity, R.string.toast_permissions_write_storage_denied_2, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_STORAGE);
            } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0])) {
                // select Deny
                Toast.makeText(mActivity, R.string.toast_permissions_write_storage_denied, Toast.LENGTH_LONG).show();
            }
            Log.d(TAG,"Write Permission request is denied");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_INSTALL_METHOD:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String installMethodChoice = bundle.getString("INSTALL_METHOD_CHOICE", "");
                    if (installMethodChoice.equals(Install.METHOD_BLUETOOTH.value)) {
                        BLEAddress = new PrefManager(mActivity).getBLEAddress();
                        Log.i(TAG, BLEAddress);
                        installMethod1(BLEAddress);
                    } else if (installMethodChoice.equals(Install.METHOD_MIFIT.value))
                        installMethod2();
                }
                break;
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) {
                    if(!BLEAddress.equals("")) {
//                        InstallViaBluetooth installViaBluetooth = new InstallViaBluetooth(mActivity, firmware);
//                        installViaBluetooth.connect(BLEAddress);
                        new General().startService(mActivity, "updateFirmwareFromSys", FILE.DISPLAY.value, BLEAddress);
                        setInstallWorking(true);
                    }
                    else {
                        // scan to find BLEAddress
                        FragmentManager fm2 = mActivity.getSupportFragmentManager();
                        ScanBLEDialog dg = new ScanBLEDialog();
                        dg.show(fm2, "SCAN_BLE_DIALOG");
                    }
                } else
                    Toast.makeText(mActivity, R.string.toast_require_turn_on_bluetooth, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public boolean isPackageInstalled(Activity mActivity, String packageName) {
        try {
            mActivity.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void readLanguage(String fileName) {
        String[] nameParts = fileName.split("-");
        String language = new General().country(nameParts[1]);

        if(nameParts.length == 2) {
            // no variant
            // eg 11-en, 12-es
            HashMap<String, String> value = new HashMap<>();
            value.put("variant", "");
            value.put("date format", "");
            value.put("file", fileName);

            List<HashMap<String, String>> lsFile;
            if(lang.containsKey(language))
                lsFile = new ArrayList<>(Objects.requireNonNull(lang.get(language)));
            else
                lsFile = new ArrayList<>();

            lsFile.add(value);
            lang.put(language, lsFile);
            lsVarients.add("Standard"); // value: no use. Only use key
        } else {
            // 3 or 4
            if(nameParts.length == 3) {
                // eg 14-en-blue, 11-multi-d, 16-vi-red
                HashMap<String, String> value = new HashMap<>();
                switch (nameParts[2]) {
                    case "d":
                        lsDateFormat.add("dd.mm");
                        value.put("variant", "");
                        value.put("date format", "dd.mm");
                        value.put("file", fileName);
                        break;
                    case "m":
                        lsDateFormat.add("mm.dd");
                        value.put("variant", "");
                        value.put("date format", "mm.dd");
                        value.put("file", fileName);
                        break;
                    default:
                        lsVarients.add(nameParts[2]);
                        value.put("variant", nameParts[2]);
                        value.put("date format", "");
                        value.put("file", fileName);
                        break;
                }

                List<HashMap<String, String>> lsFile;
                if(lang.containsKey(language))
                    lsFile = new ArrayList<>(Objects.requireNonNull(lang.get(language)));
                else
                    lsFile = new ArrayList<>();

                lsFile.add(value);
                lang.put(language, lsFile);
            } else {
                // eg 15-en-d-blue
                Log.i(TAG, fileName);
                Log.i(TAG, nameParts[3]);

                HashMap<String, String> value = new HashMap<>();
                switch (nameParts[2]) {
                    case "d":
                        lsDateFormat.add("dd.mm");
                        lsVarients.add(nameParts[3]);
                        value.put("variant", nameParts[3]);
                        value.put("date format", "dd.mm");
                        value.put("file", fileName);
                        break;
                    case "m":
                        lsDateFormat.add("mm.dd");
                        lsVarients.add(nameParts[3]);
                        value.put("variant", nameParts[3]);
                        value.put("date format", "mm.dd");
                        value.put("file", fileName);
                        break;
                }

                List<HashMap<String, String>> lsFile;
                if(lang.containsKey(language))
                    lsFile = new ArrayList<>(Objects.requireNonNull(lang.get(language)));
                else
                    lsFile = new ArrayList<>();

                lsFile.add(value);
                lang.put(language, lsFile);
            }
        }
    }

    public void setInstallWorking(boolean isBusy) {
        if(isBusy) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            btnDownload.setEnabled(false);
            btnDownload.setTextColor(getResources().getColor(R.color.color_on_primary_disabled));
        }
        btnInstall.setEnabled(false);
    }

    private final TextView.OnClickListener tvLanguageItemClick = (View v) -> {
        OPTION_SELECTED = "";
        btnDownload.setEnabled(false);
        btnDownload.setTextColor(getResources().getColor(R.color.color_on_primary_disabled));
        btnSave.setTextColor(getResources().getColor(R.color.color_on_primary_disabled));
        btnInstall.setEnabled(false);
        btnInstall.setTextColor(getResources().getColor(R.color.color_on_primary_disabled));

        if(!v.getTag().toString().equals("selected")) {
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_language_container)).getChildCount(); i++)
                if (((FlowLayout) root.findViewById(R.id.flowlayout_language_container)).getChildAt(i).getTag().equals("selected")) {
                    ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_language_container)).getChildAt(i)).setTextAppearance(mActivity, R.style.ButtonToggle);
                    ((FlowLayout) root.findViewById(R.id.flowlayout_language_container)).getChildAt(i).setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                    ((FlowLayout) root.findViewById(R.id.flowlayout_language_container)).getChildAt(i).setTag("none");
                    break;
                }

            ((TextView) v).setTextAppearance(mActivity, R.style.ButtonToggleSelected);
            v.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle_selected, mActivity.getTheme()));
            v.setTag("selected");
            LANG_SELECTED = ((TextView) v).getText().toString();
            DATE_FORMAT_SELECTED = VARIANT_SELECTED = "";

            List<HashMap<String, String>> lsFile = lang.get(LANG_SELECTED);
            // date format
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildCount(); i++) {
                TextView tvDateFormat = ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i));
                boolean isFound = false;
                for (HashMap<String, String> file : lsFile) {
                    if (tvDateFormat.getText().equals(file.get("date format"))) {
                        isFound = true;
                        tvDateFormat.setTextAppearance(mActivity, R.style.ButtonToggle);
                        tvDateFormat.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                        tvDateFormat.setTag("none");
                        break;
                    }
                }

                if (!isFound) {
                    tvDateFormat.setTextAppearance(mActivity, R.style.ButtonToggleDisabled);
                    tvDateFormat.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle_disabled, mActivity.getTheme()));
                    tvDateFormat.setTag("disabled");
                }
            }

            // variant
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildCount(); i++) {
                TextView tvVariant = ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i));
                boolean isFound = false;
                for (HashMap<String, String> file : lsFile) {
                    if (tvVariant.getText().equals(file.get("variant"))) {
                        isFound = true;
                        tvVariant.setTextAppearance(mActivity, R.style.ButtonToggle);
                        tvVariant.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                        tvVariant.setTag("none");
                        break;
                    }
                }

                if (!isFound) {
                    tvVariant.setTextAppearance(mActivity, R.style.ButtonToggleDisabled);
                    tvVariant.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle_disabled, mActivity.getTheme()));
                    tvVariant.setTag("disabled");
                }
            }

            // no date/variant OR only one date/variant
            if(lsFile.size() == 1) {
                tvOptionSelected.append(" " + LANG_SELECTED);
                OPTION_SELECTED = lsFile.get(0).get("file");
                btnDownload.setEnabled(true);
                btnDownload.setTextColor(getResources().getColor(R.color.color_on_primary));
                btnSave.setTextColor(getResources().getColor(R.color.color_primary));
            }
            setTextSelected();
        } else {
            // unselect itself
            ((TextView) v).setTextAppearance(mActivity, R.style.ButtonToggle);
            v.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
            v.setTag("none");
            LANG_SELECTED = "";
            setTextSelected();

            // recover status date
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildCount(); i++)
                if (((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i).getTag().equals("selected") || ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i).getTag().equals("disabled")) {
                    ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i)).setTextAppearance(mActivity, R.style.ButtonToggle);
                    ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i).setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                    ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i).setTag("none");
                }

            // recover status variant
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildCount(); i++)
                if (((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i).getTag().equals("selected") || ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i).getTag().equals("disabled")) {
                    ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i)).setTextAppearance(mActivity, R.style.ButtonToggle);
                    ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i).setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                    ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i).setTag("none");
                }
        }
    };

    private final TextView.OnClickListener tvDateFormatItemClick = (View v) -> {
        if(v.getTag().toString().equals("none")) {
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildCount(); i++)
                if (((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i).getTag().equals("selected")) {
                    ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i)).setTextAppearance(mActivity, R.style.ButtonToggle);
                    ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i).setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                    ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i).setTag("none");
                    break;
                }

            ((TextView) v).setTextAppearance(mActivity, R.style.ButtonToggleSelected);
            v.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle_selected, mActivity.getTheme()));
            v.setTag("selected");
            DATE_FORMAT_SELECTED = ((TextView) v).getText().toString();

            if(LANG_SELECTED.equals(""))
                return;

            // variant
            List<HashMap<String, String>> lsFile = lang.get(LANG_SELECTED);
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildCount(); i++) {
                TextView tvVariant = ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i));
                boolean isFound = false;
                for (HashMap<String, String> file : lsFile) {
                    if(file.get("date format").equals(DATE_FORMAT_SELECTED)) {
                        if (tvVariant.getText().equals(file.get("variant"))) {
                            if(tvVariant.getTag().equals("disabled")) {
                                tvVariant.setTextAppearance(mActivity, R.style.ButtonToggle);
                                tvVariant.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                                tvVariant.setTag("none");
                            }
                            isFound = true;

                            break;
                        }
                    }
                }

                if(!isFound) {
                    tvVariant.setTextAppearance(mActivity, R.style.ButtonToggleDisabled);
                    tvVariant.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle_disabled, mActivity.getTheme()));
                    tvVariant.setTag("disabled");
                }
            }
        } else if(v.getTag().toString().equals("selected")){
            // unselect itself
            ((TextView) v).setTextAppearance(mActivity, R.style.ButtonToggle);
            v.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
            v.setTag("none");
            DATE_FORMAT_SELECTED = "";
            btnInstall.setEnabled(false);

            if(LANG_SELECTED.equals(""))
                return;

            // recover status variant
            List<HashMap<String, String>> lsFile = lang.get(LANG_SELECTED);
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildCount(); i++) {
                TextView tvVariant = ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i));
                boolean isFound = false;
                for (HashMap<String, String> file : lsFile) {
                    if (tvVariant.getText().equals(file.get("variant"))) {
                        isFound = true;
                        if(tvVariant.getTag().equals("disabled")) {
                            tvVariant.setTextAppearance(mActivity, R.style.ButtonToggle);
                            tvVariant.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                            tvVariant.setTag("none");
                        }
                        break;
                    }
                }

                if (!isFound) {
                    tvVariant.setTextAppearance(mActivity, R.style.ButtonToggleDisabled);
                    tvVariant.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle_disabled, mActivity.getTheme()));
                    tvVariant.setTag("disabled");
                }
            }
        }

        setTextSelected();
        OPTION_SELECTED = getFileNameDownload();
        if(!OPTION_SELECTED.equals("")) {
            btnDownload.setEnabled(true);
            btnDownload.setTextColor(getResources().getColor(R.color.color_on_primary));
            btnSave.setTextColor(getResources().getColor(R.color.color_primary));
        } else {
            btnDownload.setEnabled(false);
            btnDownload.setTextColor(getResources().getColor(R.color.color_on_primary_disabled));
            btnSave.setTextColor(getResources().getColor(R.color.color_on_primary_disabled));
        }
    };

    private final TextView.OnClickListener tvVariantItemClick = (View v) -> {
        if(v.getTag().toString().equals("none")) {
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildCount(); i++)
                if (((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i).getTag().equals("selected")) {
                    ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i)).setTextAppearance(mActivity, R.style.ButtonToggle);
                    ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i).setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                    ((FlowLayout) root.findViewById(R.id.flowlayout_variant_container)).getChildAt(i).setTag("none");
                    break;
                }

            ((TextView) v).setTextAppearance(mActivity, R.style.ButtonToggleSelected);
            v.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle_selected, mActivity.getTheme()));
            v.setTag("selected");
            VARIANT_SELECTED = ((TextView) v).getText().toString();

            if(LANG_SELECTED.equals(""))
                return;

            // date format
            List<HashMap<String, String>> lsFile = lang.get(LANG_SELECTED);
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildCount(); i++) {
                TextView tvDateFormat = ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i));
                boolean isFound = false;
                for (HashMap<String, String> file : lsFile) {
                    if(file.get("variant").equals(VARIANT_SELECTED)) {
                        if (tvDateFormat.getText().equals(file.get("date format"))) {
                            if(tvDateFormat.getTag().equals("disabled")) {
                                tvDateFormat.setTextAppearance(mActivity, R.style.ButtonToggle);
                                tvDateFormat.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                                tvDateFormat.setTag("none");
                            }
                            isFound = true;
                            break;
                        }
                    }
                }

                if(!isFound) {
                    tvDateFormat.setTextAppearance(mActivity, R.style.ButtonToggleDisabled);
                    tvDateFormat.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle_disabled, mActivity.getTheme()));
                    tvDateFormat.setTag("disabled");
                }
            }
        } else if(v.getTag().toString().equals("selected")) {
            //unselect
            ((TextView) v).setTextAppearance(mActivity, R.style.ButtonToggle);
            v.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
            v.setTag("none");
            VARIANT_SELECTED = "";
            btnInstall.setEnabled(false);

            if(LANG_SELECTED.equals(""))
                return;

            // recover status date format
            List<HashMap<String, String>> lsFile = lang.get(LANG_SELECTED);
            for (int i = 0; i < ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildCount(); i++) {
                TextView tvDateFormat = ((TextView) ((FlowLayout) root.findViewById(R.id.flowlayout_date_format_container)).getChildAt(i));
                boolean isFound = false;
                for (HashMap<String, String> file : lsFile) {
                    if (tvDateFormat.getText().equals(file.get("date format"))) {
                        isFound = true;
                        if(tvDateFormat.getTag().equals("disabled")) {
                            tvDateFormat.setTextAppearance(mActivity, R.style.ButtonToggle);
                            tvDateFormat.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle, mActivity.getTheme()));
                            tvDateFormat.setTag("none");
                        }
                        break;
                    }
                }

                if (!isFound) {
                    tvDateFormat.setTextAppearance(mActivity, R.style.ButtonToggleDisabled);
                    tvDateFormat.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.button_toggle_disabled, mActivity.getTheme()));
                    tvDateFormat.setTag("disabled");
                }
            }
        }

        setTextSelected();
        OPTION_SELECTED = getFileNameDownload();
        if(!OPTION_SELECTED.equals("")) {
            btnDownload.setEnabled(true);
            btnDownload.setTextColor(getResources().getColor(R.color.color_on_primary));
            btnSave.setTextColor(getResources().getColor(R.color.color_primary));
        } else {
            btnDownload.setEnabled(false);
            btnDownload.setTextColor(getResources().getColor(R.color.color_on_primary_disabled));
            btnSave.setTextColor(getResources().getColor(R.color.color_on_primary_disabled));
        }
    };

    public void startService(Context context, String action, String filePath, String BLEAddress) {
        Intent service = new Intent(context, NotifyService.class);
        resultReceiver = new CustomResultReceiver(new Handler(), this);
        service.setAction(action);
        service.putExtra("FILE_PATH", filePath);
        service.putExtra("BLE_ADDRESS", BLEAddress);
        service.putExtra("receiver", resultReceiver);
        context.startService(service);
    }

    private void showHelpActivity(String locale) {
        Intent intent = new Intent(mActivity, HelpActivity.class);
        intent.putExtra("LOCALE", locale);
        startActivity(intent);
    }

    private void setTextSelected() {
        String dateFormatSelected = "", variantSelected = "";
        if(!VARIANT_SELECTED.equals(""))
            variantSelected = " + " + VARIANT_SELECTED;
        if(!DATE_FORMAT_SELECTED.equals(""))
            dateFormatSelected = " + " + DATE_FORMAT_SELECTED;

        String value = getResources().getString(R.string.textview_option_selected) + " " + LANG_SELECTED + dateFormatSelected + variantSelected;
        tvOptionSelected.setText(value);
    }

    private String getFileNameDownload() {
        List<HashMap<String, String>> lsFile = lang.get(LANG_SELECTED);
        for(HashMap<String, String> file : lsFile) {
            if(file.get("date format").equals(DATE_FORMAT_SELECTED) && file.get("variant").equals(VARIANT_SELECTED))
                return file.get("file");
        }
        return "";
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(dlTask != null)
            dlTask.cancel(true);
    }
}

