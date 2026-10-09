package org.soft2u.miband_5_display.utils;

public enum Install {
    METHOD_BLUETOOTH("install_via_bluetooth"),
    METHOD_MIFIT("install_with_mifit"),

    APPLICATION_MI_FIT_ID("com.xiaomi.hm.health"),
    APPLICATION_ID("org.soft2u.miband_5_display");

    public String value;

    Install(String value) {
        this.value = value;
    }
}

