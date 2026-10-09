package org.soft2u.miband_5_display.utils;

public enum FILE {
    DISPLAY("display.bin"),
    COVER("cover.png"),
    INFO("infos.xml"),
    RESOURCE("resource.res");

    public String value;

    FILE(String value) {
        this.value = value;
    }

    private String getValue() {
        return value;
    }
}
