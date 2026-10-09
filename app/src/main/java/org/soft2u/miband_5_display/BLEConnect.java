package org.soft2u.miband_5_display;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.soft2u.miband_5_display.utils.HuamiService;
import org.soft2u.miband_5_display.utils.MiBand5FirmwareInfo;
import org.soft2u.miband_5_display.utils.MyCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BLEConnect {
    private final String TAG = "BLEConnect";
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Map<UUID, BluetoothGattCharacteristic> mAvailableCharacteristics;
    private BluetoothGatt mBluetoothGatt;
    private final MiBand5FirmwareInfo firmware;

    public BLEConnect(MiBand5FirmwareInfo firmware) {
        this.firmware = firmware;
    }

    public boolean connect(Context mContext, String str) {
        BluetoothDevice device = null;
        if (!BluetoothAdapter.checkBluetoothAddress(str)) {
            Log.d(TAG, "Bluetooth address is invalid");
        } else {
            device = mBluetoothAdapter.getRemoteDevice(str);
        }

        if (device == null) {
            Toast.makeText(mContext, "Mi Smart Band 5 not found. Please open Mi Fit app and connect", Toast.LENGTH_SHORT).show();
            return false;
        }

//        mBluetoothAdapter.stopLeScan(leScanCallback);
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.d(TAG, "Connected GATT");
        return true;
    }

    private void readBatteryInfo() {
        mBluetoothGatt.readCharacteristic(getCharacteristic(HuamiService.UUID_CHARACTERISTIC_6_BATTERY_INFO));
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == gatt.STATE_CONNECTED) {
                Log.d(TAG, "BLE connected");
                mBluetoothGatt.discoverServices();
            } else if (newState == gatt.STATE_DISCONNECTED) {
                Log.d(TAG, "BLE disconnected");
                mBluetoothGatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "BLE discovered");
                gattServicesDiscovered(gatt.getServices());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "battery percent: " + characteristic.getValue()[1]);
        }
    };

    private void gattServicesDiscovered(List<BluetoothGattService> list) {
        if (list == null) {
            Log.d(TAG, "No GATT services are discovered");
            return;
        }
        Map<UUID, BluetoothGattCharacteristic> hashMap = new HashMap<>();
        for (BluetoothGattService bluetoothGattService : list) {
            List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
            if (characteristics != null) {
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics)
                    hashMap.put(bluetoothGattCharacteristic.getUuid(), bluetoothGattCharacteristic);
                mAvailableCharacteristics = hashMap;
            }
        }
//        ((MyCallback) new InstallOfflineDialog()).onDataGot(mAvailableCharacteristics, mBluetoothGatt, firmware);
//        setBleNotification();

        if (hashMap.size() == 0)
            Log.d(TAG, "Supported LE service did not return any characteristics");
    }

    private void setBleNotification() {
        Log.d(TAG, "setBleNotification 1");
        if (mBluetoothGatt == null) {
            Log.d(TAG, "ACTION_CONNECTING_FAIL");
            return;
        }
        UUID[] uuidArr = {HuamiService.UUID_CHARACTERISTIC_FIRMWARE, HuamiService.UUID_CHARACTERISTIC_6_BATTERY_INFO};
        for (UUID uuid : uuidArr) {
            BluetoothGattCharacteristic characteristic = getCharacteristic(uuid);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(HuamiService.UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            Log.d(TAG, "setBleNotification 2");
        }
    }

    private BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        if (mAvailableCharacteristics != null) {
            return mAvailableCharacteristics.get(uuid);
        }
        return null;
    }
}
