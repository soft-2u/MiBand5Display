package org.soft2u.miband_5_display.utils;

import java.util.UUID;

public class HuamiService {

    public static final byte SUCCESS = 0x01;
    public static final byte RESPONSE = 0x10; // 10 or 16???
    public static final UUID UUID_CHARACTERISTIC_6_BATTERY_INFO = UUID.fromString("00000006-0000-3512-2118-0009af100700");
    public static final UUID UUID_CHARACTERISTIC_FIRMWARE = UUID.fromString("00001531-0000-3512-2118-0009af100700");
    public static final UUID UUID_CHARACTERISTIC_FIRMWARE_DATA = UUID.fromString("00001532-0000-3512-2118-0009af100700");
    public static final UUID UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString(String.format("0000%s-0000-1000-8000-00805f9b34fb", "2902"));
    public static final UUID UUID_CHARACTERISTIC_USER_INFO = UUID.fromString(String.format("0000%s-0000-1000-8000-00805f9b34fb", "FF04"));
    public static final UUID UUID_CHARACTERISTIC_3_CONFIGURATION = UUID.fromString(String.format("00000003-0000-3512-2118-0009af100700"));
    public static final UUID UUID_CHARACTERISTIC_8_USER_SETTINGS = UUID.fromString("00000008-0000-3512-2118-0009af100700");

    public static final byte COMMAND_FIRMWARE_INIT = 0x01; // to UUID_CHARACTERISTIC_FIRMWARE, followed by fw file size in bytes
    public static final byte COMMAND_FIRMWARE_START_DATA = 0x03; // to UUID_CHARACTERISTIC_FIRMWARE
    public static final byte COMMAND_FIRMWARE_CHECKSUM = 0x04; // to UUID_CHARACTERISTIC_FIRMWARE
    public static final byte COMMAND_FIRMWARE_REBOOT = 0x05; // to UUID_CHARACTERISTIC_FIRMWARE
    public static final byte COMMAND_FETCH_DATA = 2;
    public static final byte COMMAND_FIRMWARE_UPDATE_SYNC = 0x00;
}
