package org.soft2u.miband_5_display.ui.fragment.icon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;

import org.json.JSONException;
import org.json.JSONObject;
import org.soft2u.miband_5_display.model.IconPreviewModel;
import org.soft2u.miband_5_display.utils.FILE;
import org.soft2u.miband_5_display.utils.General;
import org.soft2u.miband_5_display.utils.LocaleHelper;
import org.soft2u.miband_5_display.utils.OnSwipeTouchListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IconDetailOneFragment extends Fragment {

//    private String TAG = "IconDetailOneFragment";
//    private int ICON_ID_HOLDER;
//    private String jsonReceiver;
//    private String resVersion = ""; // of device user
//    private IconPreviewModel currentPreview;
//
//    private int REQUEST_PERMISSION_STORAGE = 1;
//    private final int REQUEST_INSTALL_METHOD = 1002;
//    private final int REQUEST_ENABLE_BT = 1003;
//
//    private ImageView ivPreview;
//    private Button btnRequest;
//    private Button btnDownload;
//    private ConstraintLayout btnInstall;
//    private ProgressBar progressBar;
//
//    private String BLEAddress = "";
//    private CustomResultReceiver resultReceiver;
//    private AsyncTask<String, Integer, String> dlTask;
//
//    private Context mContext;
//    private FragmentActivity mActivity;
//
//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//
//        if (context instanceof FragmentActivity){
//            mActivity = (FragmentActivity) context;
//            mContext = context;
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mActivity = null;
//        mContext = null;
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                                Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_icon_detail_one, container, false);
//        jsonReceiver = mActivity.getIntent().getExtras().getString("DATA_JSON");
//
//        btnInstall = v.findViewById(R.id.btn_install);
//        btnRequest = v.findViewById(R.id.btn_request);
//        btnDownload = v.findViewById(R.id.btn_download);
//        Button btnWatchBack = v.findViewById(R.id.btn_watch_back);
//        ivPreview = v.findViewById(R.id.iv_preview_icon);
//
//        btnDownload.setOnClickListener((View view) -> {
//            String resFileUrl = "https://miband4display-server.csm2.duckdns.org/public/files/resource/binary/" + ICON_ID_HOLDER + "/" + resVersion + ".res";
//            download(resFileUrl, FILE.RESOURCE.value);
//        });
//
//        btnInstall.setOnClickListener((View view) ->
//            install()
//        );
//
//        ivPreview.setOnTouchListener(new OnSwipeTouchListener(mActivity) {
//            @Override
//            public void onSwipeUp() {
//                super.onSwipeUp();
//                Log.d(TAG, "swipe up");
//                if(currentPreview.getNext() != null) {
//                    currentPreview = currentPreview.getNext();
//                    updatePreview(ivPreview, currentPreview);
//                }
//            }
//
//            @Override
//            public void onSwipeDown() {
//                super.onSwipeDown();
//                Log.d(TAG, "swipe down");
//                if(currentPreview.getPrev() != null) {
//                    currentPreview = currentPreview.getPrev();
//                    updatePreview(ivPreview, currentPreview);
//                }
//            }
//
//            @Override
//            public void onTap() {
//                super.onTap();
//                Log.d(TAG, "tap");
//                if(currentPreview.getEnter() != null) {
//                    currentPreview = currentPreview.getEnter();
//                    updatePreview(ivPreview, currentPreview);
//                }
//            }
//
//            @Override
//            public void onHold() {
//                super.onHold();
//                Log.d(TAG, "hold");
//            }
//
//            @Override
//            public void onScroll2(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                // https://stackoverflow.com/questions/3058164/android-scrolling-an-imageview
//                super.onScroll2(e1, e2, distanceX, distanceY);
//            }
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                v.performClick();
//
////                float curX, curY, mx = 0, my = 0;
////                switch (event.getAction()) {
////                    case MotionEvent.ACTION_DOWN:
////                        mx = event.getX();
////                        my = event.getY();
////                        break;
////                    case MotionEvent.ACTION_MOVE:
////                        curX = event.getX();
////                        curY = event.getY();
////                        ivPreview.scrollBy((int) (mx - curX), (int) (my - curY));
////                        mx = curX;
////                        my = curY;
////                        break;
////                    case MotionEvent.ACTION_UP:
////                        curX = event.getX();
////                        curY = event.getY();
////                        ivPreview.scrollBy((int) (mx - curX), (int) (my - curY));
////                        break;
////                }
//
//                return super.onTouch(v, event);
//            }
//        });
//
//        btnWatchBack.setOnClickListener((View v) -> {
//            if(currentPreview.getBack() != null) {
//                currentPreview = currentPreview.getBack();
//                updatePreview(ivPreview, currentPreview);
//            }
//        });
//
//        readJsonData(jsonReceiver);
//
//        // init
//        IconPreviewModel icoStatus = new IconPreviewModel();
//        IconPreviewModel icoHeartRate = new IconPreviewModel();
//        IconPreviewModel icoWorkout = new IconPreviewModel();
//        IconPreviewModel icoWeather = new IconPreviewModel();
//        IconPreviewModel icoNotifications = new IconPreviewModel();
//        IconPreviewModel icoMore = new IconPreviewModel();
//
//        IconPreviewModel icoHeartRate_2 = new IconPreviewModel();
//        IconPreviewModel icoWorkout_2 = new IconPreviewModel();
//        IconPreviewModel icoMore_2 = new IconPreviewModel();
//
//        icoStatus.setFileName("icoStatus.png");
//        icoStatus.setNext(icoHeartRate);
//        icoStatus.setPrev(icoMore);
//
//        icoHeartRate.setFileName("icoHeartRate.png");
//        icoHeartRate.setNext(icoWorkout);
//        icoHeartRate.setPrev(icoStatus);
//        icoHeartRate.setEnter(icoHeartRate_2);
//
//        icoWorkout.setFileName("icoWorkout.png");
//        icoWorkout.setNext(icoWeather);
//        icoWorkout.setPrev(icoHeartRate);
//        icoWorkout.setEnter(icoWorkout_2);
//
//        icoWeather.setFileName("icoWeather.png");
//        icoWeather.setNext(icoNotifications);
//        icoWeather.setPrev(icoWorkout);
//
//        icoNotifications.setFileName("icoNotifications.png");
//        icoNotifications.setNext(icoMore);
//        icoNotifications.setPrev(icoWeather);
//
//        icoMore.setFileName("icoMore.png");
//        icoMore.setNext(icoStatus);
//        icoMore.setPrev(icoNotifications);
//        icoMore.setEnter(icoMore_2);
//
//        icoHeartRate_2.setFileName("icoHeartRate_2.gif");
//        icoHeartRate_2.setBack(icoHeartRate);
//
//        icoWorkout_2.setFileName("icoWorkout_2.gif");
//        icoWorkout_2.setBack(icoHeartRate);
//
//        icoMore_2.setFileName("icoMore_2.gif");
//        icoMore_2.setBack(icoHeartRate);
//
//        currentPreview = icoStatus;
//        updatePreview(ivPreview, currentPreview);
//    }
//
//    private void updatePreview(ImageView imageView, IconPreviewModel currentPreview) {
//        Glide.with(this)
//                .load(new File(mActivity.getExternalFilesDir(null), currentPreview.getFileName()))
//                .into(imageView);
//    }
//
//    private void download(String resFileUrl, String fileName) {
//        if (!new General().isInternetConnection(mActivity)) {
//            Toast.makeText(mActivity, R.string.toast_notice_no_internet_download, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (isStoragePermissionGranted(mActivity, REQUEST_PERMISSION_STORAGE)) {
//            dlTask = new DownloadFileFromURL(resFileUrl, fileName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }
//    }
//
//    private void install() {
//        ((TextView) findViewById(R.id.tv_desc_flashing_fw_notice)).setVisibility(View.VISIBLE);
//        ((ImageView) findViewById(R.id.iv_black_watch)).setAlpha((float) 0.1);
//    }
//
//    /**
//     * @param json - json string got from IconFragment
//     */
//    private void readJsonData(String json) {
//        try {
//            JSONObject obj = new JSONObject(json);
//            ICON_ID_HOLDER = Integer.parseInt(obj.getString("iconid"));
//            String previewFolderUrl = "https://miband4display-server.csm2.duckdns.org/public/files/resource/preview/"+ICON_ID_HOLDER +"/";
//            String title = obj.getString("title");
//            String author = obj.getString("author");
//            String update = obj.getString("update");
//
//            download(previewFolderUrl + "icoStatus.png", "icoStatus.png");
//            download(previewFolderUrl + "icoHeartRate.png", "icoHeartRate.png");
//            download(previewFolderUrl + "icoWorkout.png", "icoWorkout.png");
//            download(previewFolderUrl + "icoWeather.png", "icoWeather.png");
//            download(previewFolderUrl + "icoNotifications.png", "icoNotifications.png");
//            download(previewFolderUrl + "icoMore.png", "icoMore.png");
//            download(previewFolderUrl + "icoHeartRate_2.gif", "icoHeartRate_2.gif");
//
//            ((TextView) findViewById(R.id.tv_icon_title)).setText(title);
//            ((TextView) findViewById(R.id.tv_icon_author)).setText(author);
//            if(!update.equals("null")) {
//                (findViewById(R.id.tv_icon_update_label)).setVisibility(View.VISIBLE);
//                ((TextView) findViewById(R.id.tv_icon_update_label)).append(" " + update);
//            }
//
//            // check firmware compatible
//            boolean isCompatible = false;
//            String fwApplyObject = obj.getString("fw_apply");
//            List<String> langCodes = splitFwApplyObject(fwApplyObject);
//            for (String version : langCodes) {
//                if(version.equals(resVersion)) {
//                    isCompatible = true;
//                    break;
//                }
//            }
//
//            if(!isCompatible) {
//                btnRequest.setVisibility(View.VISIBLE);
//                btnDownload.setVisibility(View.GONE);
//                btnInstall.setVisibility(View.GONE);
//                ((TextView) findViewById(R.id.tv_notice_fw_incompatible)).setVisibility(View.VISIBLE);
//                ((TextView) findViewById(R.id.tv_desc_flashing_fw_recommend)).setVisibility(View.GONE);
//            }
//
//        } catch (JSONException ex) {
//            Log.d(TAG,"cannot parse json string.  " + ex.toString());
//            Toast.makeText(mActivity, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @SuppressLint("StaticFieldLeak")
//    private class DownloadFileFromURL extends AsyncTask<String, Integer, String> {
//        private boolean isSuccess = true;
//        private boolean isCancel = false; // press Cancel by user
//        private boolean isConnect = true;
//        private ProgressDialog progressDialog;
//
//        private String fileUrl, saveName;
//
//        DownloadFileFromURL(String fileUrl, String saveName) {
//            this.fileUrl = fileUrl;
//            this.saveName = saveName;
//        }
//
//        /**
//         * Before starting background thread Show Progress Bar Dialog
//         * */
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressDialog = new ProgressDialog(mActivity);
//            progressDialog.setMessage(getResources().getString(R.string.label_downloading));
//            progressDialog.setMax(100);
//            progressDialog.setCancelable(true);
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.button_cancel), (DialogInterface dialog, int which) -> {
//                isCancel = true;
//            });
//            progressDialog.setOnDismissListener(DialogInterface::cancel
//            );
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            progressDialog.show();
//        }
//
//        /**
//         * Downloading file in background thread
//         * */
//        @Override
//        protected String doInBackground(String... params) {
//            int count;
//            URL url;
//            int lengthOfFile;
//
//            try {
//                url = new URL(fileUrl);
//                URLConnection connection = url.openConnection();
//                connection.connect();
//
//                // this will be useful so that you can show a tipical 0-100%
//                // progress bar
//                lengthOfFile = connection.getContentLength();
//            } catch (IOException ex) {
//                isSuccess = false;
//                Log.e(TAG, "Open connection error: " + ex.getMessage());
//                return null;
//            }
//
//            try {
//                // download the file
//                InputStream input = new BufferedInputStream(url.openStream(),8192);
//                File file = new File(mActivity.getExternalFilesDir(null), saveName);
//                OutputStream output = new FileOutputStream(file);
//
//                byte[] data = new byte[1024];
//                long total = 0;
//                while (total < lengthOfFile) {
//                    if(!new General().isInternetConnection(mActivity)) {
//                        isConnect = false;
//                        Log.d(TAG, "No internet access");
//                        break;
//                    } else {
//                        count = input.read(data);
//                        if(count == -1) {
//                            isSuccess = false;
//                            Log.d(TAG, "error");
//                            break;
//                        }
//                        if(isCancel) {
//                            Log.d(TAG, "stopped");
//                            break;
//                        }
//                        total += count;
////                        Log.d(TAG, String.valueOf(total));
//                        // publishing the progress....
//                        // After this onProgressUpdate will be called
//                        publishProgress((int) ((total * 100) / lengthOfFile));
//                        // writing data to file
//                        output.write(data, 0, count);
//                    }
//                }
//
//                // flushing output
//                output.flush();
//                // closing streams
//                output.close();
//                input.close();
//
//            } catch (IOException e) {
//                isSuccess = false;
//                Log.e(TAG, "Error writing: " + e.getMessage());
//            }
//            return null;
//        }
//
//        /**
//         * Updating progress bar
//         * */
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            // setting progress percentage
//            super.onProgressUpdate(values);
//            progressDialog.setProgress(values[0]);
//        }
//
//        @Override
//        protected void onCancelled(String s) {
//            super.onCancelled(s);
//            Log.d(TAG, "canceled");
//        }
//
//        /**
//         * After completing background task Dismiss the progress dialog
//         * **/
//        @Override
//        protected void onPostExecute(String fileUrl) {
//            if(isConnect && isSuccess) {
//                if(saveName.equals(FILE.RESOURCE.value)) {
//                    // download finished
//                    btnDownload.setVisibility(View.GONE);
//                    btnInstall.setVisibility(View.VISIBLE);
//                    Toast.makeText(mActivity, R.string.toast_file_downloaded_finished, Toast.LENGTH_SHORT).show();
//                    updateDownloads(ICON_ID_HOLDER);
//                }
//            } else if (!isSuccess) {
//                Toast.makeText(mActivity, R.string.toast_notice_download_fail, Toast.LENGTH_SHORT).show();
//                reportError(ICON_ID_HOLDER, "DISPLAY_DOWNLOAD_LINK_ERROR", false);
//            } else {
//                Toast.makeText(mActivity, R.string.toast_notice_no_internet_download, Toast.LENGTH_SHORT).show();
//            }
//
////            dlTask.cancel(true);
//            progressDialog.dismiss();
//        }
//    }
//
//    private void reportError(int iconId, String error, boolean showToast) {
//        String suffixUrl = "act=report&iconid=" + iconId + "&error="+error+"";
//        new General().executeVolley(suffixUrl, mActivity);
//        if(showToast)
//            Toast.makeText(mActivity, R.string.toast_send_report_success, Toast.LENGTH_SHORT).show();
//    }
//
//    private void updateViews(int iconId) {
//        String suffixUrl = "act=icondetail&job=updateview&iconid=" + iconId + "";
//        new General().executeVolley(suffixUrl, mActivity);
//    }
//
//    private void updateDownloads(int iconId) {
//        String suffixUrl = "act=icondetail&job=updatedownload&iconid=" + iconId + "";
//        new General().executeVolley(suffixUrl, mActivity);
//    }
//
//    private List<String> splitFwApplyObject(String input) {
//        int startIndex = input.indexOf("{") + 1;
//        int endIndex = input.indexOf("}");
//        String input2 = input.substring(startIndex, endIndex);
//
//        String[] langCodes = input2.split(",");
//        return new ArrayList<>(Arrays.asList(langCodes));
//    }
//
//    public boolean isStoragePermissionGranted(Activity mActivity, int requestCode) {
//        if (android.os.Build.VERSION.SDK_INT >= 23) {
//            if (mActivity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG,"Write Permission is granted");
//                return true;
//            } else {
//                Log.d(TAG,"Write Permission is denied. Start request again");
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
//                return false;
//            }
//        } else {
//            // Permission is automatically granted on sdk<23 upon installation
//            Log.d(TAG,"Write Permission already granted");
//            return true;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
//            Log.d(TAG,"Permission: " +permissions[0]+ " was " +grantResults[0]);
//        } else {
//            // with only one request
//            boolean showRationale = false;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                showRationale = shouldShowRequestPermissionRationale(permissions[0]);
//            }
//            if (!showRationale) {
//                // Checked "Never ask again"
//                Toast.makeText(mActivity, R.string.toast_permissions_write_storage_denied_2, Toast.LENGTH_LONG).show();
//
//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
//                intent.setData(uri);
//                startActivityForResult(intent, REQUEST_PERMISSION_STORAGE);
//            } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0])) {
//                // select Deny
//                Toast.makeText(mContext, R.string.toast_permissions_write_storage_denied, Toast.LENGTH_LONG).show();
//            }
//            Log.d(TAG,"Write Permission request is denied");
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if(dlTask != null)
//            dlTask.cancel(true);
//    }
}
