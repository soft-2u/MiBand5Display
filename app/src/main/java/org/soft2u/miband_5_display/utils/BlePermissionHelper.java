package org.soft2u.miband_5_display.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

/**
 * Runtime permission helper for Bluetooth on Android 12+ (API 31).
 * <p>
 * Since Android 12, BLUETOOTH_CONNECT must be granted at runtime for:
 * getConnectedDevices(), getRemoteDevice(), connectGatt(), GATT read/write,
 * BluetoothAdapter.isEnabled(), BluetoothDevice.getName()/getType(), etc.
 * Without it these calls throw SecurityException.
 */
public class BlePermissionHelper {

    /**
     * @return true if the app may perform BLE operations right now.
     * Below Android 12 the manifest permission is enough for connecting
     * to an already-known device, so this returns true.
     */
    public static boolean isBleConnectGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Ask for BLUETOOTH_CONNECT on Android 12+. The result is delivered to the
     * fragment's onRequestPermissionsResult(). No-op below Android 12.
     */
    public static void requestBleConnect(Fragment fragment, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            fragment.requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, requestCode);
        }
    }

    /**
     * Ask for BLUETOOTH_CONNECT on Android 12+. No-op below Android 12.
     */
    public static void requestBleConnect(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, requestCode);
        }
    }
}
