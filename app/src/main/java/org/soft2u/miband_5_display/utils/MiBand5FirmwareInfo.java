package org.soft2u.miband_5_display.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

public class MiBand5FirmwareInfo {
    private static final byte[] FW_HEADER = {49, 0, 0, 0, 0, 0, 0, 0, 0, 0, -100, -29, 125, 92, 0, 4};
    private static final int FW_HEADER_OFFSET = 16;

//    private static final byte[] FT_HEADER = {72, 77, 90, 75};
    private static final byte[] NEWFT_HEADER = {78, 69, 90, 75};

    private static final byte[] NEWRES_HEADER = {78, 69, 82, 69, 83};
    private static final byte[] RES_HEADER = {72, 77, 82, 69, 83};
    private static final int RES_HEADER_OFFSET = 13;
    private static final int COMPRESSED_RES_HEADER_OFFSET = 9;
    private static final int COMPRESSED_RES_HEADER_OFFSET_NEW = 13;

    private static final byte[] WATCHFACE_HEADER = {72, 77, 68, 73, 65, 76};
    private byte[] value;
    private int crc16;
    private int crc32;

    public HuamiFirmwareType getType() {
        return type;
    }

    public int getSize() {
        return this.value.length;
    }

    public byte[] getValue() {
        return this.value;
    }

    public int getCrc16() {
        return this.crc16;
    }

    public int getCrc32() {
        return this.crc32;
    }

    public int getFirmwareVersion() {
        return getCrc16();
    }

    public HuamiFirmwareType type;
    // TODO(MiBand5): bảng dưới đây là CRC của firmware Mi Band 4.
    // Thay bằng CRC của các bản firmware Mi Band 5 khi có file .fw/.res mẫu,
    // hoặc bỏ hẳn nếu không cần hiển thị tên version.
    private static Map<Integer, String> crcToVersion = new HashMap<>();

    static {
        crcToVersion.put(8969, "1.0.5.22");
        crcToVersion.put(43437, "1.0.5.66");
        crcToVersion.put(31632, "1.0.6.00");
        crcToVersion.put(6856, "1.0.7.14");
        crcToVersion.put(50145, "1.0.7.60");

        // resources
        crcToVersion.put(27412, "1.0.5.22");
        crcToVersion.put(5466, "1.0.5.66");
        crcToVersion.put(20047, "1.0.6.00");
        crcToVersion.put(62914, "1.0.7.14");
        crcToVersion.put(17303, "1.0.7.60");

        // font
        crcToVersion.put(31978, "1");
    }

    public MiBand5FirmwareInfo(byte[] bArr) {
        this.value = bArr;
        if (bArr == null || bArr.length < 20) {
            this.type = HuamiFirmwareType.INVALID;
            return;
        }
        this.crc16 = getCRC16(bArr);
        this.crc32 = getCRC32(bArr);
        this.type = determineFirmwareType(bArr);
    }

    /* access modifiers changed from: protected */
    public HuamiFirmwareType determineFirmwareType(byte[] bArr) {
//        Log.d("byte: ", Arrays.toString(bArr));
        if (startsWith(bArr, WATCHFACE_HEADER)) {
            return HuamiFirmwareType.WATCHFACE;
        }
        if (equals(bArr, FW_HEADER, FW_HEADER_OFFSET)) {
            return HuamiFirmwareType.FIRMWARE;
        }
        if (startsWith(bArr, NEWFT_HEADER)) {
            return HuamiFirmwareType.FONT;
        }
        if (equals(bArr, RES_HEADER, RES_HEADER_OFFSET) || startsWith(bArr, RES_HEADER) || startsWith(bArr, NEWRES_HEADER)) {
            return HuamiFirmwareType.RES;
        }
        if (equals(bArr, RES_HEADER, COMPRESSED_RES_HEADER_OFFSET) || equals(bArr, NEWRES_HEADER, COMPRESSED_RES_HEADER_OFFSET) || equals(bArr, NEWRES_HEADER, COMPRESSED_RES_HEADER_OFFSET_NEW)) {
            return HuamiFirmwareType.RES_COMPRESSED;
        }
        return HuamiFirmwareType.INVALID;
    }

    /* access modifiers changed from: protected */
    public Map<Integer, String> getCrcMap() {
        return crcToVersion;
    }

    private static boolean equals(byte[] array1, byte[] array2, int offset) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length < offset) {
            return false;
        }
        for (int i = 0; i < array2.length; i++) {
            if (array1[i + offset] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean startsWith(byte[] cur, byte[] val) {
        if (cur == null || val == null) return false;
        if (cur.length < val.length) return false;
        for (int i = 0; i < val.length; i++) {
            if (cur[i] != val[i]) return false;
        }
        return true;
    }

    public static int getCRC16(byte[] seq) {
        int crc = 0xFFFF;

        for (byte b : seq) {
            crc = ((crc >>> 8) | (crc << 8)) & 0xffff;
            crc ^= (b & 0xff);//byte to int, trunc sign
            crc ^= ((crc & 0xff) >> 4);
            crc ^= (crc << 12) & 0xffff;
            crc ^= ((crc & 0xFF) << 5) & 0xffff;
        }
        crc &= 0xffff;
        return crc;
    }

    private static int getCRC32(byte[] seq) {
        CRC32 crc32 = new CRC32();
        crc32.update(seq);
        return (int) crc32.getValue();
    }
}
