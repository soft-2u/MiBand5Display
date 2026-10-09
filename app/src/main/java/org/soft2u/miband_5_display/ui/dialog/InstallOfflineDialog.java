package org.soft2u.miband_5_display.ui.dialog;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.soft2u.miband_5_display.CustomResultReceiver;
import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.activity.HelpActivity;
import org.soft2u.miband_5_display.utils.FILE;
import org.soft2u.miband_5_display.utils.General;
import org.soft2u.miband_5_display.utils.BlePermissionHelper;
import org.soft2u.miband_5_display.utils.Install;
import org.soft2u.miband_5_display.utils.InstallViaMiFit;
import org.soft2u.miband_5_display.utils.MiBand5FirmwareInfo;
import org.soft2u.miband_5_display.utils.NotifyService;

import rikka.shizuku.Shizuku;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class InstallOfflineDialog extends DialogFragment implements CustomResultReceiver.AppReceiver {

    private Context mContext;
    private FragmentActivity mActivity;
    private String TAG = "InstallOfflineDialog";
    private final int REQUEST_PERMISSION_OPEN_FILE = 1001;
    private final int REQUEST_INSTALL_METHOD = 1002;
    private final int REQUEST_ENABLE_BT = 1003;
    private final int REQUEST_BLE_CONNECT = 1005;
    private String pendingBleAddress = "";
    private final int REQUEST_SHIZUKU = 1004;
    private String pendingShizukuFilePath = "";

    private final Shizuku.OnRequestPermissionResultListener shizukuPermissionListener = (requestCode, grantResult) -> {
        if (requestCode == REQUEST_SHIZUKU) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                installMethod2(pendingShizukuFilePath); // retry now that Shizuku is granted
            } else {
                Toast.makeText(mContext, R.string.toast_shizuku_permission_required, Toast.LENGTH_LONG).show();
            }
        }
    };
    private Button btnOpen;
    private ConstraintLayout btnInstall;
    private ProgressBar progressBar;
    private TextView tvFileName;

    private String filePath = "";
    private String BLEAddress = "";
    private CustomResultReceiver resultReceiver;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity){
            mActivity = (FragmentActivity) context;
            mContext = context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        mContext = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener);
    }

    @Override
    public void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener);
        super.onDestroy();
    }

    public InstallOfflineDialog() {
        // Required empty public constructor
    }

    public static InstallOfflineDialog newInstance(String filePath) {
        // receive path when file manager open
        Bundle args = new Bundle();
        args.putString("FILE_PATH", filePath);
        InstallOfflineDialog f = new InstallOfflineDialog();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            filePath = getArguments().getString("FILE_PATH", "");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            assert getDialog().getWindow() != null;
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_install_offline, container, false);
        btnOpen = root.findViewById(R.id.btn_open_file);
        btnInstall = root.findViewById(R.id.btn_install);
        progressBar = root.findViewById(R.id.progress_bar);
        TextView tvHowToInstall = root.findViewById(R.id.tv_how_to_install);
        tvFileName = root.findViewById(R.id.tv_file_name);

        String installInstructions = mContext.getResources().getString(R.string.label_install_instructions);
        tvHowToInstall.setText(new General().getSpannedText(installInstructions));

        btnOpen.setOnClickListener((View view) -> {
            if (isStoragePermissionGranted(mActivity, REQUEST_PERMISSION_OPEN_FILE)) {
                openFile();
            }

            // Test storage path
//            Log.d(TAG, String.valueOf(Environment.getStorageDirectory()));
//            Log.d(TAG, String.valueOf(new File(Environment.getStorageDirectory(), "emulated/0/Android/data").exists()));

//            Log.d(TAG, String.valueOf(Environment.getExternalStorageDirectory()));
//            Log.d(TAG, String.valueOf(mContext.getExternalFilesDir(null)));

//            Log.d(TAG, "stro " + String.valueOf(new File("/storage/emulated/0/Android/data").exists()));
//            Log.d(TAG, "stro " + String.valueOf(new File("/storage/emulated/0/Android/data").canWrite()));
//
//            Log.d(TAG, "mnt " + String.valueOf(new File("/mnt/sdcard").exists()));
//            Log.d(TAG, "mnt " + String.valueOf(new File("/mnt/sdcard").canWrite()));

            // File folderList[] = new File(Environment.getRootDirectory(), "").listFiles();
//            Log.d(TAG, "" + folderList.length);
//            for (File folder : folderList) {
//                Log.d(TAG, "" + folder);
//                Log.d(TAG, "" + folder.getAbsolutePath());
//            }
        });

        btnInstall.setOnClickListener((View view) -> {
            String remember = new PrefManager(mActivity).getRememberMethodInstall();
            if(remember.equals(Install.METHOD_BLUETOOTH.value)) {
                BLEAddress = new PrefManager(mActivity).getBLEAddress();
                installMethod1(BLEAddress);
            } else if(remember.equals(Install.METHOD_MIFIT.value)) {
                installMethod2(filePath);
            } else {
                FragmentManager fm = mActivity.getSupportFragmentManager();
                InstallMethodDialog dg = new InstallMethodDialog();
                dg.show(fm, "INSTALL_METHOD_DIALOG");
                dg.setTargetFragment(InstallOfflineDialog.this, REQUEST_INSTALL_METHOD);
            }
        });

        tvHowToInstall.setOnClickListener((View view) -> {
            Intent intent = new Intent(mActivity, HelpActivity.class);
            intent.putExtra("LOCALE", new PrefManager(mActivity).getLocale());
            startActivity(intent);
        });

        (root.findViewById(R.id.btn_close)).setOnClickListener((View view) -> dismiss());

        if(!filePath.equals("")) {
            // opened from file manager / browser
            if (isStoragePermissionGranted(mActivity, REQUEST_PERMISSION_OPEN_FILE)) {
                if(isFileInvalid(filePath))
                    Toast.makeText(mContext, R.string.toast_notice_open_file_invalid, Toast.LENGTH_SHORT).show();

                Log.d(TAG, new General().getRealPath(filePath));
            }
        }

        return root;
    }

    public void installMethod1(String BLEAddress) {
        // Android 12+: isEnabled()/connectGatt() need BLUETOOTH_CONNECT at runtime
        if (!BlePermissionHelper.isBleConnectGranted(mContext)) {
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
//                    InstallViaBluetooth installViaBluetooth = new InstallViaBluetooth(mContext, firmware);
////                    installViaBluetooth.connect(BLEAddress);
                    startService(mContext, "updateFirmwareFromUser", filePath, BLEAddress);
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
            Toast.makeText(mContext, R.string.toast_notice_need_mi_fit_paired_already, Toast.LENGTH_LONG).show();
    }

    private void requestEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void installMethod2(String filePath) {
        if (!InstallViaMiFit.isShizukuAvailable()) {
            Toast.makeText(mContext, R.string.toast_shizuku_not_running, Toast.LENGTH_LONG).show();
            return;
        }
        if (!InstallViaMiFit.isShizukuGranted()) {
            pendingShizukuFilePath = filePath;
            InstallViaMiFit.requestShizukuPermission(REQUEST_SHIZUKU);
            return;
        }
        String fileRealPath = new General().getRealPath(filePath);
        String str = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?><watch_skin_info><name>" + fileRealPath.substring(fileRealPath.lastIndexOf("/") + 1) + "</name><time_stamp>" + new Date().getTime() + "</time_stamp></watch_skin_info>";
        if(!writeInfoFile(str, FILE.INFO.value)) {
            return;
        }

        if(new General().isPackageInstalled(mActivity, Install.APPLICATION_MI_FIT_ID.value))
        {
            if(new InstallViaMiFit().copyBINFile(mContext, fileRealPath) && new InstallViaMiFit().copyInfoFile(mContext)) {
                if(new InstallViaMiFit().copyCoverFile(mContext)) {
                    dismiss();
                    Intent intent = mActivity.getPackageManager().getLaunchIntentForPackage(Install.APPLICATION_MI_FIT_ID.value);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(mContext, "Write data to Mi Fit app failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, R.string.toast_mi_fit_app_not_found, Toast.LENGTH_LONG).show();
        }
    }

    private boolean writeInfoFile(String str, String fileName) {
        try {
            File file = new File(mContext.getExternalFilesDir(null), fileName);
            FileWriter out = new FileWriter(file);
            out.write(str);
            out.close();
        }
        catch (IOException e) {
            Log.e(TAG, "Write Info file failed: " + e.toString());
            Toast.makeText(mContext, "Write Info file failed. Please contact developer!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isStoragePermissionGranted(Activity mActivity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped storage: ACTION_GET_CONTENT and app-specific dirs need no runtime storage permission
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
                Toast.makeText(mContext, R.string.toast_permissions_ble_denied, Toast.LENGTH_LONG).show();
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
                Toast.makeText(mContext, R.string.toast_permissions_write_storage_denied_2, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_OPEN_FILE);
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
            case REQUEST_PERMISSION_OPEN_FILE:
                if (resultCode == RESULT_OK) {
                    Uri currFileURI = data.getData();

                    if (currFileURI != null) {
                        filePath = currFileURI.getPath();
                        Log.d(TAG, new General().getRealPath(filePath));
                        if (isFileInvalid(filePath)) {
                            byte[] byteInput = new General().readFile(new File(Environment.getExternalStorageDirectory(), new General().getRealPath(filePath)));
                            MiBand5FirmwareInfo firmware = new MiBand5FirmwareInfo(byteInput);
                            Log.d(TAG, "bin size: " + firmware.getSize());
                            Log.d(TAG, "bin type: " + firmware.getType());
                            tvFileName.append(" [" + firmware.getType() + "]");
                            BLEAddress = new PrefManager(mActivity).getBLEAddress();
                        } else {
                            Toast.makeText(mContext, R.string.toast_notice_open_file_invalid, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case REQUEST_INSTALL_METHOD:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String installMethodChoice = bundle.getString("INSTALL_METHOD_CHOICE", "");
                    if (installMethodChoice.equals(Install.METHOD_BLUETOOTH.value)) {
                        BLEAddress = new PrefManager(mActivity).getBLEAddress();
                        installMethod1(BLEAddress);
                    } else if (installMethodChoice.equals(Install.METHOD_MIFIT.value))
                        installMethod2(filePath);
                }
                break;
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) {
                    if(!BLEAddress.equals("")) {
//                        InstallViaBluetooth installViaBluetooth = new InstallViaBluetooth(mContext, firmware);
//                        installViaBluetooth.connect(BLEAddress);
                        new General().startService(mContext, "updateFirmwareFromUser", filePath, BLEAddress);
                        setInstallWorking(true);
                    }
                    else {
                        // scan to find BLEAddress
                        FragmentManager fm2 = mActivity.getSupportFragmentManager();
                        ScanBLEDialog dg = new ScanBLEDialog();
                        dg.show(fm2, "SCAN_BLE_DIALOG");
                    }
                } else
                    Toast.makeText(mContext, R.string.toast_require_turn_on_bluetooth, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean isFileInvalid(String filePath) {
        Log.d(TAG, filePath);
        if(filePath.indexOf(".") > 0) {
            String ext = filePath.substring(filePath.lastIndexOf("."));
            if (ext.equals(".bin") || ext.equals(".res") || ext.equals(".ft")) {
                btnOpen.setVisibility(View.GONE);
                btnInstall.setVisibility(View.VISIBLE);
                tvFileName.setVisibility(View.VISIBLE);
                tvFileName.setText(filePath.substring(filePath.lastIndexOf("/") + 1));
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra("CONTENT_TYPE", "*/*");
        // Set your required file type
        intent.setType("application/octet-stream|text/*");
        startActivityForResult(Intent.createChooser(intent, "Open BIN"), REQUEST_PERMISSION_OPEN_FILE);
    }

    public void setInstallWorking(boolean isBusy) {
        if(isBusy) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            btnInstall.setVisibility(View.GONE);
            btnOpen.setVisibility(View.VISIBLE);
            tvFileName.setVisibility(View.GONE);
        }
        btnInstall.setEnabled(!isBusy);
    }

    public void startService(Context context, String action, String filePath, String BLEAddress) {
        Intent service = new Intent(context, NotifyService.class);
        resultReceiver = new CustomResultReceiver(new Handler(), this);
        service.setAction(action);
        service.putExtra("FILE_PATH", filePath);
        service.putExtra("BLE_ADDRESS", BLEAddress);
        service.putExtra("receiver", resultReceiver);
        context.startService(service);
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
}

