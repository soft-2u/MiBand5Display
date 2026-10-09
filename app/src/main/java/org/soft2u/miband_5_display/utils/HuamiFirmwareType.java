package org.soft2u.miband_5_display.utils;

public enum HuamiFirmwareType {
    FIRMWARE((byte) 0),
    FONT((byte) 1),
    RES((byte) 2),
    RES_COMPRESSED((byte) -126),
    GPS((byte) 3),
    GPS_CEP((byte) 4),
    GPS_ALMANAC((byte) 5),
    WATCHFACE((byte) 8),
    FONT_LATIN((byte) 11),
    INVALID(Byte.MIN_VALUE);

    private byte value;

    HuamiFirmwareType(byte b) {
        this.value = b;
    }

    public byte getValue() {
        return this.value;
    }
}
