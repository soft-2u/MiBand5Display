package org.soft2u.miband_5_display.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public interface GattCallback {
    /**
     * @param gatt
     * @param status
     * @param newState
     * @see BluetoothGattCallback#onConnectionStateChange(BluetoothGatt, int, int)
     */
    void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

    /**
     * @param gatt
     * @see BluetoothGattCallback#onServicesDiscovered(BluetoothGatt, int)
     */
    void onServicesDiscovered(BluetoothGatt gatt, int status);

    /**
     * @param gatt
     * @param characteristic
     * @param status
     * @see BluetoothGattCallback#onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int)
     */
    void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    /**
     * @param gatt
     * @param characteristic
     * @param status
     * @see BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)
     */
    boolean onCharacteristicWrite(BluetoothGatt gatt,
                                  BluetoothGattCharacteristic characteristic, int status);

    /**
     * @param gatt
     * @param characteristic
     * @see BluetoothGattCallback#onCharacteristicChanged(BluetoothGatt, BluetoothGattCharacteristic)
     */
    boolean onCharacteristicChanged(BluetoothGatt gatt,
                                    BluetoothGattCharacteristic characteristic);

    /**
     * @param gatt
     * @param descriptor
     * @param status
     * @see BluetoothGattCallback#onDescriptorRead(BluetoothGatt, BluetoothGattDescriptor, int)
     */
    boolean onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                             int status);

    /**
     * @param gatt
     * @param descriptor
     * @param status
     * @see BluetoothGattCallback#onDescriptorWrite(BluetoothGatt, BluetoothGattDescriptor, int)
     */
    boolean onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                              int status);
//
//    /**
//     * @see BluetoothGattCallback#onReliableWriteCompleted(BluetoothGatt, int)
//     * @param gatt
//     * @param status
//     */
//    public void onReliableWriteCompleted(BluetoothGatt gatt, int status);

    /**
     * @param gatt
     * @param rssi
     * @param status
     * @see BluetoothGattCallback#onReadRemoteRssi(BluetoothGatt, int, int)
     */
    void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);

    void onMtuChanged(BluetoothGatt gatt, int mtu, int status);

//    /**
//     * @see BluetoothGattCallback#onMtuChanged(BluetoothGatt, int, int)
//     * @param gatt
//     * @param mtu
//     * @param status
//     */
//    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status);
}
