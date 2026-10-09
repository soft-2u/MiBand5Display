package org.soft2u.miband_5_display.ui.dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.utils.General;
import org.soft2u.miband_5_display.utils.Install;
import org.soft2u.miband_5_display.utils.BlePermissionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ScanBLEDialog extends DialogFragment {

    private Context mContext;
    private FragmentActivity mActivity;
    private static final String TAG = "ConnectBLE";
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private int REQUEST_ENABLE_BT = 1003;
    private final int REQUEST_BLE_CONNECT = 1005;

    private ListView lvBLEFound;
    private List<String> values = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private SimpleAdapter leDeviceListAdapter;
    private BluetoothManager bluetoothManager;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_scan_ble, container, false);
        lvBLEFound = root.findViewById(R.id.lv_ble_found);
        adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        lvBLEFound.setAdapter(adapter);
        lvBLEFound.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            String deviceName = adapter.getItem(position).split("\n")[0];
            String deviceMACAddress = adapter.getItem(position).split("\n")[1];
            String[] letters = deviceName.split(" ");
            for(int i = 0; i < letters.length; i++) {
                if(letters[i].equals("Band") && letters[i + 1].equals("4")) {
                    new PrefManager(mActivity).setBLEName(deviceName);
                    new PrefManager(mActivity).setBLEAddress(deviceMACAddress);
                    Toast.makeText(mContext, R.string.toast_success_connect, Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });

        bluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        (root.findViewById(R.id.rl_bkg_trans)).setOnClickListener((View v) ->
            dismiss()
        );

        (root.findViewById(R.id.btn_scan)).setOnClickListener((View v) -> {
            values.clear();
            adapter.notifyDataSetChanged();
            if(new General().isPackageInstalled(mActivity, Install.APPLICATION_MI_FIT_ID.value)) {
                // Android 12+: getConnectedDevices()/isEnabled() need BLUETOOTH_CONNECT at runtime
                if (!BlePermissionHelper.isBleConnectGranted(mContext)) {
                    BlePermissionHelper.requestBleConnect(ScanBLEDialog.this, REQUEST_BLE_CONNECT);
                    return;
                }
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
                    requestEnableBluetooth();
                else {
                    listConnectedDevices();
                }
            }
            else
                Toast.makeText(mContext, R.string.toast_notice_need_mi_fit_paired_already, Toast.LENGTH_LONG).show();
        });

        return root;
    }

    private void requestEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
//                scanLeDevice(true);
                listConnectedDevices();
            } else
                Toast.makeText(mContext, R.string.toast_require_turn_on_bluetooth, Toast.LENGTH_SHORT).show();
        }
    }

    private void listConnectedDevices() {
        List<BluetoothDevice> devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice device : devices) {
            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                values.add(device.getName() + "\n" + device.getAddress());
                adapter.notifyDataSetChanged();
            }
        }
        if (values.size() == 0) {
            Toast.makeText(mContext, R.string.toast_notice_bluetooth_le_not_found, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLE_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
                    requestEnableBluetooth();
                else
                    listConnectedDevices();
            } else {
                Toast.makeText(mContext, R.string.toast_permissions_ble_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            new Handler().postDelayed(() -> {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(leScanCallback);
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(leScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            mActivity.runOnUiThread(() -> {
                values.add(device.getName() + "\n" + device.getAddress());
                adapter.notifyDataSetChanged();
            });
        }
    };
}

