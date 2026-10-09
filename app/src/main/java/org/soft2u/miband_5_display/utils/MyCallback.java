package org.soft2u.miband_5_display.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Map;
import java.util.UUID;

public interface MyCallback {
//    void onDataGot(Map<UUID, BluetoothGattCharacteristic> availableCharacteristics, BluetoothGatt bluetoothGatt, MiBand5FirmwareInfo firmware);
    void setInstallWorking(boolean isBusy);
}
