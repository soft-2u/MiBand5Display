package org.soft2u.miband_5_display.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class General {
    private String TAG = "General";

    public boolean isInternetConnection(Context context) {
        if(context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            return isConnected;
        } else return false;
    }

    public boolean isInternetAvailable() {
        final String command = "ping -c 1 google.com";
        try {
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String country(String langCode) {
        switch (langCode) {
            case "multi":
                return "Multilingual";
            case "en":
                return "English";
            case "en-gb":
                return "British";
            case "en-us":
                return "American";
            case "vi":
                return "Vietnamese";
            case "ja":
                return "Japanese";
            case "th":
                return "Thai";
            case "ko":
                return "Korean";
            case "zh":
                return "Chinese";
            case "fr":
                return "French";
            case "ru":
                return "Russian";
            case "id":
                return "Indonesian";
            case "de":
                return "German";
            case "pt":
                return "Portuguese";
            case "it":
                return "Italian";
            case "es":
                return "Spanish";
            case "hu":
                return "Hungarian";
            case "pl":
                return "Polish";
            case "tr":
                return "Turkish";
            case "be":
                return "Belarusian";
            case "uk":
                return "Ukrainian";
            case "el":
                return "Greek";
            case "lv":
                return "Latvian";
            case "cz":
                return "Czech";
            case "ro":
                return "Romanian";
            case "nl":
                return "Dutch";
            case "my":
                return "Burmese";
            case "sk":
                return "Slovak";
            case "ar":
                return "Arabic";
            case "ca":
                return "Catalan";
            case "sl":
                return "Slovenian";
            case "fa":
                return "Persian";
        }
        return "Unknown";
    }

    public String country2(String langCode) {
        switch (langCode) {
            case "multi":
                return "Multilingual";
            case "en-d":
                return "British";
            case "en-m":
                return "American";
            case "vi":
                return "Vietnamese";
            case "ja":
                return "Japanese";
            case "th":
                return "Thai";
            case "ko":
                return "Korean";
            case "zh":
                return "Chinese";
            case "fr":
                return "French";
            case "ru":
                return "Russian";
            case "id":
                return "Indonesian";
            case "de":
                return "German";
            case "pt":
                return "Portuguese";
            case "it":
                return "Italian";
            case "es":
                return "Spanish";
            case "hu":
                return "Hungarian";
            case "pl":
                return "Polish";
            case "tr":
                return "Turkish";
            case "be":
                return "Belarusian";
            case "uk":
                return "Ukrainian";
            case "el":
                return "Greek";
            case "lv":
                return "Latvian";
            case "cz":
                return "Czech";
            case "ro":
                return "Romanian";
            case "nl":
                return "Dutch";
            case "my":
                return "Burmese";
            case "sk":
                return "Slovak";
            case "ar":
                return "Arabic";
            case "ca":
                return "Catalan";
            case "sl":
                return "Slovenian";
            case "fa":
                return "Persian";
        }
        return "Unknown";
    }

    public String langCode(String country) {
        switch (country) {
            case "English":
                return "en";
            case "Vietnamese":
                return "vi";
            case "Japanese":
                return "ja";
            case "Thai":
                return "th";
            case "Korean":
                return "ko";
            case "Chinese":
                return "zh";
            case "French":
                return "fr";
            case "Russian":
                return "ru";
            case "Indonesian":
                return "id";
            case "German":
                return "de";
            case "Spanish":
                return "es";
            case "Portuguese":
                return "pt";
            case "Polish":
                return "pl";
            case "Italian":
                return "it";
            case "Turkish":
                return "tr";
            case "Hungarian":
                return "hu";
            case "Belarusian":
                return "be";
            case "Ukrainian":
                return "uk";
            case "Greek":
                return "el";
            case "Latvian":
                return "lv";
            case "Czech":
                return "cz";
            case "Romanian":
                return "ro";
            case "Dutch":
                return "nl";
            case "Burmese":
                return "my";
            case "Slovak":
                return "sk";
            case "Arabic":
                return "ar";
            case "Catalan":
                return "ca";
            case "Slovenian":
                return "sl";
            case "Persian":
                return "fa";
        }
        return "en";
    }

    public String langApp(String langCode) {
        switch (langCode) {
            case "en":
                return "English";
            case "vi":
                return "Vietnamese";
            case "ja":
                return "Japanese";
            case "ko":
                return "Korean";
            case "zh":
                return "Chinese";
            case "ru":
                return "Russian";
            case "de":
                return "German";
            case "pt":
                return "Portuguese";
            case "it":
                return "Italian";
            case "es":
                return "Spanish";
            case "pl":
                return "Polish";
            case "tr":
                return "Turkish";
        }
        return "Unknown";
    }

    public void executeVolley(String url, Context pContext) {
        url = "https://miband4display-server.csm2.duckdns.org/index.php?" + url;
//        Log.d(TAG, url);
        String finalUrl = url;
        if(pContext != null) {
            com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(pContext);
            com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(com.android.volley.Request.Method.GET, url, (String response) -> {

            }, (com.android.volley.VolleyError error) ->
                    android.util.Log.d(TAG, "Cannot execute volley url: " + finalUrl + " " + error.toString())
            );
            queue.add(request);
        }
    }

//    public void executeVolley(String url, Context pContext) {
//        url = "https://miband4display-server.csm2.duckdns.org/index.php?" + url;
////        Log.d(TAG, url);
//        String finalUrl = url;
//        if(pContext != null) {
//            RequestFuture<String> future = RequestFuture.newFuture();
//            com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(pContext);
//            com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(com.android.volley.Request.Method.GET, url, future, future);
//            queue.add(request);
//            try {
//                String response = future.get();
//                android.util.Log.d(TAG, "Volley response: " + response);
//            } catch (InterruptedException | ExecutionException ex) {
//                android.util.Log.d(TAG, "Cannot execute volley url: " + finalUrl + " " + ex.toString());
//            }
//        }
//    }

    public android.text.Spanned getSpannedText(String text) {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            return android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_COMPACT);
        else
            return android.text.Html.fromHtml(text);
    }

    public boolean isPackageInstalled(Activity mActivity, String packageName) {
        try {
            mActivity.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public String getRealPath(String filePath) {
        Log.d(TAG, "Uri: " + filePath);
        if(filePath.contains("storage/emulated/0/"))
            filePath = filePath.split("storage/emulated/0/")[1];
        else if(filePath.contains("external_files/"))
            filePath = filePath.split("external_files/")[1];
        else if(filePath.contains(":"))
            filePath = filePath.split(":")[1];
        else if(filePath.contains("sdcard/"))
            filePath = filePath.split("sdcard/")[1];
        return filePath;
    }

    public File getMiFitDataDir() {
        String MiFitDataPath = "Android/data/" + Install.APPLICATION_MI_FIT_ID.value + "/files/watch_skin_local";
        File skinFolder = new File(Environment.getExternalStorageDirectory(), MiFitDataPath);
        if (!skinFolder.exists()) {
            skinFolder.mkdir();
        }
        return skinFolder;
    }

    public byte[] readFile(File file) {
        // Open file
        try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } catch (Exception ex) {
            Log.e("read error: ", ex.toString());
            return null;
        }
    }

    public boolean copyFile(File source, File dest) {
        FileChannel sourceChannel;
        FileChannel destChannel;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            sourceChannel.close();
            destChannel.close();
        } catch (IOException ex) {
            Log.e(TAG, "Failed to copy file: " + source + ". " + ex.toString());
            return false;
        }
        return true;
    }

    public boolean copyFile2(File source, File dest) {
        InputStream is;
        OutputStream os;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            is.close();
            os.close();
        } catch (IOException ex) {
            Log.e(TAG, "Failed to copy file: " + source + ". " + ex.toString());
            return false;
        }
        return true;
    }

    public void startService(Context context, String action, String filePath, String BLEAddress) {
        Intent service = new Intent(context, NotifyService.class);
        service.setAction(action);
        service.putExtra("FILE_PATH", filePath);
        service.putExtra("BLE_ADDRESS", BLEAddress);
        context.startService(service);
    }

    public static boolean isDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
