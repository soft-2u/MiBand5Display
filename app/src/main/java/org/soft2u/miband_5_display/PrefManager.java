package org.soft2u.miband_5_display;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public PrefManager(Context context) {
        pref = context.getSharedPreferences("PREF_APP", Context.MODE_PRIVATE);
        editor = pref.edit();
    }

//    //IS_FIRST_TIME_LAUNCH
//    public void setFirstTimeLaunch(boolean isFirstTime) {
//        editor.putBoolean("IS_FIRST_TIME_LAUNCH", isFirstTime);
//        editor.commit();
//    }
//
//    public boolean isFirstTimeLaunch() {
//        return pref.getBoolean("IS_FIRST_TIME_LAUNCH", true);
//    }
//
//    //IS_AFTER_TIME_LAUNCH
//    public void setAfterFirstTimeLaunch(boolean isAfterFirstTime) {
//        editor.putBoolean("IS_AFTER_TIME_LAUNCH", isAfterFirstTime);
//        editor.commit();
//    }
//
//    public boolean isAfterFirstTimeLaunch() { return pref.getBoolean("IS_AFTER_TIME_LAUNCH", true); }


//    //
//    public void setStatusFilterButton(boolean applied) {
//        editor.putBoolean("STATUS_FILTER_BUTTON", applied);
//        editor.commit();
//    }
//
//    public Boolean getStatusFilterButton() { return pref.getBoolean("STATUS_FILTER_BUTTON", false); }

    //
    public void setLocale(String locale) {
        editor.putString("LANGUAGE_APP", locale);
        editor.commit();
    }

    public String getLocale() { return pref.getString("LANGUAGE_APP", ""); }

    // count down show ads
    public void setCountDown(int countDown) {
        Log.i("COUNT_DOWN_SHOW_ADS", String.valueOf(countDown));
        editor.putInt("COUNT_DOWN_SHOW_ADS", countDown);
        editor.commit();
    }

    public int getCountDown() { return pref.getInt("COUNT_DOWN_SHOW_ADS", 5); }

    // count down show ads
    public void setOfferTrans(int offer) {
        editor.putInt("OFFER_TRANS", offer);
        editor.commit();
    }

    public int getOfferTrans() { return pref.getInt("OFFER_TRANS", 1); }

    // rate request
    public void setStatusShowRateRequest(boolean isShow) {
        editor.putBoolean("SHOW_RATE_REQUEST", isShow);
        editor.commit();
    }

    public Boolean getStatusShowRateRequest() { return pref.getBoolean("SHOW_RATE_REQUEST", true); }

    // night mode
    public void setNightMode(int mode) {
        editor.putInt("NIGHT_MODE", mode);
        editor.commit();
    }

    public int getNightMode() { return pref.getInt("NIGHT_MODE", -1); }

    //
    public void setUserId(String userId) {
        editor.putString("USER_ID", userId);
        editor.commit();
    }

    public String getUserId() { return pref.getString("USER_ID", ""); }

    //
    public void setBLEAddress(String deviceMACAddr) {
        editor.putString("BLE_ADDRESS", deviceMACAddr);
        editor.commit();
    }

    public String getBLEAddress() { return pref.getString("BLE_ADDRESS", ""); }

    //
    public void setBLEName(String deviceName) {
        editor.putString("BLE_NAME", deviceName);
        editor.commit();
    }

    public String getBLEName() { return pref.getString("BLE_NAME", ""); }

    public void setRememberMethodInstall(String methodName) {
        editor.putString("METHOD_NAME", methodName);
        editor.commit();
    }

    public String getRememberMethodInstall() { return pref.getString("METHOD_NAME", ""); }
}