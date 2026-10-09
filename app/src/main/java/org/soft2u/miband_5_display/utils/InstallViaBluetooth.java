package org.soft2u.miband_5_display.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.soft2u.miband_5_display.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class InstallViaBluetooth {
    private static final String TAG = "InstallViaBluetooth";

    private BluetoothAdapter mBluetoothAdapter;
    private Map<UUID, BluetoothGattCharacteristic> mAvailableCharacteristics;
    private BluetoothGatt mBluetoothGatt;

    private Context mContext;
    private MiBand5FirmwareInfo firmware;
    private int packetLengthOnce = 20;
    private int currentLength;
    private boolean isSending = false;
    public List<SendDataAction> sendDataList = new ArrayList<>();

    public void init(Context context, String BLEAddress, MiBand5FirmwareInfo firmware) {
        this.mContext = context;
        this.firmware = firmware;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            this.mBluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        connect(BLEAddress);
    }

    private void connect(String BLEAddress) {
        BluetoothDevice device = null;
        if (!BluetoothAdapter.checkBluetoothAddress(BLEAddress)) {
            Log.w(TAG, "Bluetooth address is invalid");
        } else {
            device = mBluetoothAdapter.getRemoteDevice(BLEAddress);
        }

        if (device == null) {
            Toast.makeText(mContext, "Mi Smart Band 5 not found. Please open Mi Fit app and connect", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mBluetoothGatt != null) {
            Log.d(TAG, "BLE disconnected first");
            mBluetoothGatt.disconnect();
        }

//        mBluetoothAdapter.stopLeScan(leScanCallback);
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.d(TAG, "Connected GATT");
    }

    private void readBatteryInfo() {
        mBluetoothGatt.readCharacteristic(getCharacteristic(HuamiService.UUID_CHARACTERISTIC_6_BATTERY_INFO));
    }

    private void onDescriptorWritten(BluetoothGattDescriptor descriptor, int status) {
        String status2 = status == BluetoothGatt.GATT_SUCCESS ? " (success)" : " (failed: " + status + ")";
        Log.d(TAG, "descriptor write: " + descriptor.getUuid() + status2);
        readBatteryInfo();
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "BLE connected");
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
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
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            onDescriptorWritten(descriptor, status);
        }

        // Android 13+ (API 33): the framework dispatches to this overload instead of the one above
        @Override
        public void onDescriptorWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
            onDescriptorWritten(descriptor, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            onCharacteristicReadInternal(characteristic.getValue());
        }

        // Android 13+ (API 33): the framework dispatches to this overload instead of the one above
        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);
            onCharacteristicReadInternal(value);
        }

        private void onCharacteristicReadInternal(byte[] value) {
            int batteryPhone = getBatteryPercentage(mContext);
            if (value != null && value.length > 1) {
                Log.d(TAG, "battery mi smart band 5 percent: " + value[1]);
                Log.d(TAG, "battery phone percent: " + batteryPhone);
//            for (int i = 0; i < characteristic.getValue().length; i++)
//                Log.d(TAG, characteristic.getValue()[i] + "");
                 // more than 15% of battery
                if(value[1] >= 15) {
                    if(batteryPhone >= 15)
                        sendFwInfo(firmware);
                    else
                        Toast.makeText(mContext, R.string.toast_notice_low_battery_phone, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, R.string.toast_notice_low_battery_mi_band, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            UUID uuid = characteristic.getUuid();
            byte[] messageBytes = characteristic.getValue();

            String status2 = status == BluetoothGatt.GATT_SUCCESS ? " (success)" : " (failed: " + status + ")";
            Log.d(TAG, "characteristic write: " + characteristic.getUuid() + status2);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Firmware flashing failed: " + new String(messageBytes));
            }

            if (uuid.equals(HuamiService.UUID_CHARACTERISTIC_FIRMWARE_DATA)) {
                currentLength = currentLength + packetLengthOnce;
//                BLEActivity.handler.sendEmptyMessage((BLEActivity.currentLength * 100) / BLEActivity.allDataLength);
//                Log.d(TAG, "response: ok. UUID_CHARACTERISTIC_FIRMWARE_DATA: ");
            }
            isSending = false;
            doSend();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            onCharacteristicChangedInternal(characteristic.getUuid(), characteristic.getValue());
        }

        // Android 13+ (API 33): the framework dispatches to this overload instead of the one above
        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            super.onCharacteristicChanged(gatt, characteristic, value);
            onCharacteristicChangedInternal(characteristic.getUuid(), value);
        }
//            BLEActivity.sendBleBroadcast(BLEActivity.ACTION_DATA_AVAILABLE, bluetoothGattCharacteristic);
    };

    private void gattServicesDiscovered(List<BluetoothGattService> list) {
        if (list == null) {
            Log.w(TAG, "No GATT services are discovered");
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

        if (!hashMap.isEmpty()) {
            setBLENotification();
        } else {
            Toast.makeText(mContext, "Supported LE service did not return any characteristics", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Supported LE service did not return any characteristics");
        }
    }

    private void setBLENotification() {
        if (mBluetoothGatt == null) {
            Log.e(TAG, "ACTION_CONNECTING_FAIL");
            return;
        }

        UUID[] uuidArr = {HuamiService.UUID_CHARACTERISTIC_FIRMWARE, HuamiService.UUID_CHARACTERISTIC_6_BATTERY_INFO};
//        UUID[] uuidArr = {HuamiService.UUID_CHARACTERISTIC_USER_INFO};
        for (UUID uuid : uuidArr) {
            BluetoothGattCharacteristic characteristic = getCharacteristic(uuid);
            if (characteristic != null) {
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);

                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(HuamiService.UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION);
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                    Log.d(TAG, "setBleNotification");
                }
            }
        }
    }

    private BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        if (mAvailableCharacteristics != null) {
            return mAvailableCharacteristics.get(uuid);
        }
        return null;
    }

//    private MiBand5FirmwareInfo getFirmwareInfo(byte[] bytes) {
//        return new MiBand5FirmwareInfo(bytes);
//    }

    private void sendData(final UUID uuid, final byte[] value) {
        sendDataList.add(() -> {
//            Log.d(TAG, "writing to characteristic: " + formatBytes(value));
            BluetoothGattCharacteristic characteristic = getCharacteristic(uuid);
            if (characteristic != null) {
                characteristic.setValue(value);
                boolean isSuccess = mBluetoothGatt.writeCharacteristic(characteristic);
                if (!isSuccess) {
                    Log.e(TAG, "write fail");
                }
            } else {
                Log.e(TAG, "characteristic not found for uuid: " + uuid);
            }
        });
        doSend();
    }

    private void sendFirmwareData(MiBand5FirmwareInfo firmware) {
        int length = firmware.getSize();
        Log.d(TAG, "-- Send firmware data");
        packetLengthOnce = 180; //180 or 20???
        int packets = length / packetLengthOnce;
        Log.d(TAG, "packets: " + packets);
        currentLength = 0;

        try {
            int firmwareProgress = 0;
            sendData(HuamiService.UUID_CHARACTERISTIC_FIRMWARE, getFirmwareStartCommand());
            for (int i = 0; i < packets; i++) {

                byte[] fwChunk = Arrays.copyOfRange(firmware.getValue(), i * packetLengthOnce, i * packetLengthOnce + packetLengthOnce);
                sendData(HuamiService.UUID_CHARACTERISTIC_FIRMWARE_DATA, fwChunk);
                firmwareProgress += packetLengthOnce;

                int progressPercent = (int) ((((float) firmwareProgress) / length) * 100);
                if ((i > 0) && (i % 100 == 0)) {
                    Log.d(TAG, "progress: " + progressPercent + "%");
                    sendData(HuamiService.UUID_CHARACTERISTIC_FIRMWARE, new byte[]{HuamiService.COMMAND_FIRMWARE_UPDATE_SYNC});
                }
            }

            if (firmwareProgress < length) {
                byte[] lastChunk = Arrays.copyOfRange(firmware.getValue(), packets * packetLengthOnce, length);
                sendData(HuamiService.UUID_CHARACTERISTIC_FIRMWARE_DATA, lastChunk);
            }
            sendData(HuamiService.UUID_CHARACTERISTIC_FIRMWARE, new byte[]{HuamiService.COMMAND_FIRMWARE_UPDATE_SYNC});
        } catch (Exception ex) {
            Toast.makeText(mContext, R.string.toast_error_sending_binary_info, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot send binary to Mi Smart Band 5:" + ex);
        }
    }

    public void sendFwInfo(MiBand5FirmwareInfo firmware) {
        if (firmware.getType() == HuamiFirmwareType.INVALID) {
            Toast.makeText(mContext, "File invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        this.firmware = firmware;
        Log.d(TAG, "-- SendFwInfo");

        try {
            byte[] sizeBytes = fromUint24(firmware.getSize());
            byte[] bytes = new byte[10];
            int i = 0;
            bytes[i++] = HuamiService.COMMAND_FIRMWARE_INIT;
            bytes[i++] = firmware.getType().getValue();
            bytes[i++] = sizeBytes[0];
            bytes[i++] = sizeBytes[1];
            bytes[i++] = sizeBytes[2];
            bytes[i++] = 0; // TODO: what is that?
            int crc32 = firmware.getCrc32();
            byte[] crcBytes = fromUint32(crc32);
            bytes[i++] = crcBytes[0];
            bytes[i++] = crcBytes[1];
            bytes[i++] = crcBytes[2];
            bytes[i] = crcBytes[3];

            sendData(HuamiService.UUID_CHARACTERISTIC_FIRMWARE, bytes);
        } catch (Exception ex) {
            Toast.makeText(mContext, R.string.toast_error_sending_binary_info, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot send binary info: " + ex);
        }
    }

    private void sendReboot() {
        Log.d(TAG, "reboot");
        sendData(HuamiService.UUID_CHARACTERISTIC_FIRMWARE, new byte[]{5});
    }

    private synchronized void doSend() {
        if (!isSending) {
            if (!sendDataList.isEmpty()) {
                isSending = true;
                SendDataAction sendDataAction = sendDataList.remove(0);
                if (sendDataAction != null) {
                    Log.d(TAG, "send. remain: " + sendDataList.size());
                    sendDataAction.sendData();
                }
            }
        }
    }

    private byte[] getFirmwareStartCommand() {
//        Log.d(TAG, "test: " +  formatBytes(new byte[]{HuamiService.COMMAND_FIRMWARE_START_DATA}));
        return new byte[]{3, 1};
    }

    private void onCharacteristicChangedInternal(UUID uuid, byte[] value) {
        if (HuamiService.UUID_CHARACTERISTIC_FIRMWARE.equals(uuid)) {
            Log.d(TAG, "Characteristic Changed");
            handleFwNotification(value);
        } else {
            Log.d(TAG, "Characteristic cannot changed");
        }
    }

    private void handleFwNotification(byte[] value) {
        if (value.length != 3 && value.length != 11) {
            Log.w(TAG, "Notifications should be 3 or 11 bytes long.");
            return;
        }

        boolean success = value[2] == HuamiService.SUCCESS;
        Log.d(TAG, "handleFwNotification. success: " + success + " " + value[0] + " " + value[1] + " " + value[2]);

        if (value[0] == HuamiService.RESPONSE && success) {
            try {
                switch (value[1]) {
                    case HuamiService.COMMAND_FIRMWARE_INIT:
                        sendFirmwareData(firmware);
                        break;
                    case HuamiService.COMMAND_FIRMWARE_START_DATA:
                        sendChecksum(firmware);
                        break;
                    case HuamiService.COMMAND_FIRMWARE_CHECKSUM:
                        if (firmware.getType() == HuamiFirmwareType.FIRMWARE) {
                            sendReboot();
                        } else {
                            Log.d(TAG, "updatefirmwareoperation_update_complete");
                            mBluetoothGatt.disconnect();
                            stopService(NotifyService.class, mContext);
                        }
                        break;
                    case HuamiService.COMMAND_FIRMWARE_REBOOT:
                        Log.d(TAG, "Reboot command successfully sent. updatefirmwareoperation_update_complete");
                        break;
                    default:
                        Log.w(TAG, "byte[1]: " + value[1] + " is invalid");
                        break;
                }
            } catch (Exception ex) {
                Log.e(TAG, "Problem with the firmware transfer. DO NOT REBOOT your Mi Band!");
            }
        } else {
            Log.e(TAG, "Problem with the firmware metadata transfer");
        }
    }

    private void sendChecksum(MiBand5FirmwareInfo firmware) {
        Log.d(TAG, "-- Send checksum");
        byte[] fromUint32 = fromUint32(firmware.getCrc32());
        sendData(HuamiService.UUID_CHARACTERISTIC_FIRMWARE, new byte[]{4, fromUint32[0], fromUint32[1], fromUint32[2], fromUint32[3]});
    }

    private static byte[] fromUint32(int i) {
        return new byte[]{(byte) (i & 255), (byte) ((i >> 8) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 24) & 255)};
    }

    // new
    private static byte[] fromUint24(int value) {
        return new byte[] {
                (byte) (value & 0xff),
                (byte) ((value >> 8) & 0xff),
                (byte) ((value >> 16) & 0xff),
        };
    }

    private int getBatteryPercentage(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
             BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
             return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
             IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
             Intent batteryStatus = context.registerReceiver(null, iFilter);

             int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
             int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

             double batteryPct = level / (double) scale;

             return (int) (batteryPct * 100);
       }
    }

    public void stopService(Class<?> serviceClass, Context context) {
        Intent service = new Intent(context, serviceClass);
        context.stopService(service);
    }
}

