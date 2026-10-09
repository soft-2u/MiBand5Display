package org.soft2u.miband_5_display.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.adapter.BaseThemeAdapter;
import org.soft2u.miband_5_display.adapter.BaseThemeElementsAdapter;
import org.soft2u.miband_5_display.model.BaseThemeModel;
import org.soft2u.miband_5_display.utils.General;
import org.soft2u.miband_5_display.utils.Install;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DIYActivity extends AppCompatActivity {
    private RelativeLayout rlReview;
    private String imgPath;
    private String TAG = "DIYActivity";
    private int REQUEST_PERMISSION_STORAGE = 1;
    private Button btnDownload, btnInstall;
    private List<Map<String, String>> listModel = new ArrayList<>();

    private String fileUrl;
    private String baseNameSelect;
    private boolean isSubmitLock = false;

    private ProgressDialog progressDialog;
    private List<BaseThemeModel> baseThemes = new ArrayList<>();
    private BaseThemeAdapter adapter;
    private AsyncTask<String, Integer, String> dlTask;

    TextView tvMessage1, tvMessage2;

    Map<String, String> background = new HashMap<>();
    Map<String, String> hours = new HashMap<>();
    Map<String, String> minutes = new HashMap<>();
    Map<String, String> weekDay = new HashMap<>();
    Map<String, String> ampm = new HashMap<>();

    ImageView ivBackground, ivHours, ivMinutes, ivWeekDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(getSupportActionBar() != null) getSupportActionBar().hide();

        setContentView(R.layout.activity_diy);

        MobileAds.initialize(this, (InitializationStatus initializationStatus) -> { });
        RequestConfiguration requestConfiguration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(
                "7B21EEA5F5F327CF30F5F208575D0FCD",
                AdRequest.DEVICE_ID_EMULATOR)).build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        AdView mAdView = findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        rlReview = findViewById(R.id.rl_diy_review);

        btnDownload = findViewById(R.id.btn_download);
        btnInstall = findViewById(R.id.btn_install);

        ivBackground = findViewById(R.id.iv_background);
        ivHours = findViewById(R.id.iv_hours);
        ivMinutes = findViewById(R.id.iv_minutes);
        ivWeekDay = findViewById(R.id.iv_weekday);

        RelativeLayout btnSubmit = findViewById(R.id.btn_submit);
        ProgressBar progressSubmit = findViewById(R.id.progress_submit);
        TextView titleSubmit = findViewById(R.id.title_submit);


        RecyclerView gridBaseTheme = findViewById(R.id.grid_base_theme);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        gridBaseTheme.setLayoutManager(layoutManager);

        RecyclerView gridBaseThemeElements = findViewById(R.id.grid_base_theme_elements);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        gridBaseThemeElements.setLayoutManager(layoutManager2);

        getAll();
        adapter = new BaseThemeAdapter(this, baseThemes);

        gridBaseTheme.setAdapter(adapter);
        adapter.setClickListener((View view, int position) -> {
//            String fileUrl = "https://miband4display-server.csm2.duckdns.org/public/files/basetheme" + "/" + baseNameSelect + "/" + JSON_FILE_NAME;
//            download(fileUrl, "0000.json", false);
            baseNameSelect = ((TextView) view.findViewById(R.id.tv_folder_name)).getText().toString();
            getBaseElements(gridBaseThemeElements, baseThemes.get(position).getJson());
            setBaseElementsLocation();
        });

        rlReview.setOnClickListener((View view) -> {

        });

        btnSubmit.setOnClickListener((View view) -> {
            if(baseNameSelect == null) {
                Toast.makeText(this, R.string.toast_notice_select_base_theme, Toast.LENGTH_SHORT).show();
                return;
            }
            if(imgPath != null && !isSubmitLock) {
                progressSubmit.setVisibility(View.VISIBLE);
                titleSubmit.setVisibility(View.GONE);
                String encodedString = encodeImageToString(imgPath);
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                String URL = "https://miband4display-server.csm2.duckdns.org/index.php?act=tools&job=diy";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, (String response) -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        JSONObject obj = arr.getJSONObject(0);
                        String errorValue = obj.optString("error", "SUCCESS");
                        switch (errorValue) {
                            case "ENCODED_STRING_NOT_FOUND":
                                Toast.makeText(this, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "ENCODED_STRING_NOT_FOUND");
                                break;
                            case "SAVE_IMAGE_FAIL":
                                Toast.makeText(this, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "SAVE_IMAGE_FAIL");
                                break;
                            case "CREATE_FAIL":
                                Toast.makeText(this, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "CREATE_FAIL");
                                break;
                            case "SUCCESS":
                                String link = obj.getString("link");
                                btnSubmit.setVisibility(View.GONE);
                                btnDownload.setVisibility(View.VISIBLE);
                                fileUrl = link;
                                isSubmitLock = false;
                                break;
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "cannot parse json string. " + e.toString());
                        Log.d(TAG, "response: " + response);
                        Toast.makeText(this, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
                    }
                }, (VolleyError error) -> {
                    Log.d(TAG,"Server error or return a invalid json or html. " + error.toString());
                    Toast.makeText(this, R.string.msg_server_cannot_access, Toast.LENGTH_SHORT).show();
                }){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("encoded_string", encodedString);
                        params.put("base_theme", baseNameSelect);
                        return params;
                    }
                };
                // set timeout: 15s
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        15000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                );
                requestQueue.add(stringRequest);
                isSubmitLock = true;
            }
        });

        btnDownload.setOnClickListener((View view) -> {
            download(fileUrl, "theme1.bin", true);
        });

        btnInstall.setOnClickListener((View view) ->
            install()
        );
    }

//    private void loadBanner(AdView adView) {
//        // Create an ad request. Check your logcat output for the hashed device ID
//        // to get test ads on a physical device, e.g.,
//        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
//        // device."
//        AdRequest adRequest =
//                new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                        .build();
//
//        AdSize adSize = getAdSize();
//        // Step 4 - Set the adaptive ad size on the ad view.
//        adView.setAdSize(adSize);
//
//        // Step 5 - Start loading the ad in the background.
//        adView.loadAd(adRequest);
//    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void getBaseElements(RecyclerView gridBaseThemeElements, String json) {
        listModel.clear();
        BaseThemeElementsAdapter adapter = new BaseThemeElementsAdapter(this, listModel);
        gridBaseThemeElements.setAdapter(adapter);
        adapter.setClickListener((View view, int position) -> {
            if(position == 0) {
//                Intent intent1 = new Intent(this, CropActivity.class);
//                intent1.putExtra("EXTRA_CROP_SHAPE", "crop_image_rectangle");
//                startActivity(intent1, null);
            } else {

            }
        });
        String imgUrl = "https://miband4display-server.csm2.duckdns.org/public/files/basetheme/" + baseNameSelect;

        try {
            JSONArray arr = new JSONArray("[" + json + "]");
            JSONObject obj = arr.getJSONObject(0);

            background.put("X", getValue(obj, "Background;Image;X"));
            background.put("Y", getValue(obj, "Background;Image;Y"));
            background.put("ImageIndex", getValue(obj, "Background;Image;ImageIndex"));
            background.put("Name", "Background");
            background.put("IconPath", imgUrl + "/" + formatIndex(Integer.parseInt(background.get("ImageIndex"))) + ".png");
            listModel.add(background);

            hours.put("X", getValue(obj, "Time;Hours;Tens;X"));
            hours.put("Y", getValue(obj, "Time;Hours;Tens;Y"));
            hours.put("ImageIndex", getValue(obj, "Time;Hours;Tens;ImageIndex"));
            hours.put("Name", "Hours");
            hours.put("IconPath", imgUrl + "/" + formatIndex(Integer.parseInt(hours.get("ImageIndex"))) + ".png");
            listModel.add(hours);

            minutes.put("X", getValue(obj, "Time;Minutes;Tens;X"));
            minutes.put("Y", getValue(obj, "Time;Minutes;Tens;Y"));
            minutes.put("ImageIndex", getValue(obj, "Time;Minutes;Tens;ImageIndex"));
            minutes.put("Name", "Minutes");
            minutes.put("IconPath", imgUrl + "/" + formatIndex(Integer.parseInt(minutes.get("ImageIndex")) + 2) + ".png");
            listModel.add(minutes);

            if(!getValue(obj, "Date;WeekDay;ImageIndex").equals("")) {
                weekDay.put("X", getValue(obj, "Date;WeekDay;X"));
                weekDay.put("Y", getValue(obj, "Date;WeekDay;Y"));
                weekDay.put("ImageIndex", getValue(obj, "Date;WeekDay;ImageIndex"));
                weekDay.put("Name", "WeekDay");
                weekDay.put("IconPath", imgUrl + "/" + formatIndex(Integer.parseInt(weekDay.get("ImageIndex")) + 14) + ".png");
                listModel.add(weekDay);
            }

            if(!getValue(obj, "Date;DayAmPm;ImageIndexAMEN").equals("")) {
                ampm.put("X", getValue(obj, "Date;WeekDay;X"));
                ampm.put("Y", getValue(obj, "Date;WeekDay;Y"));
                ampm.put("ImageIndex", getValue(obj, "Date;DayAmPm;ImageIndexAMEN"));
                ampm.put("Name", "AMPM");
                ampm.put("IconPath", imgUrl + "/" + formatIndex(Integer.parseInt(ampm.get("ImageIndex"))) + ".png");
                listModel.add(ampm);
            }

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.d(TAG, "cannot parse json string. " + e.toString());
        }
    }

    private void setBaseElementsLocation() {
//        new DownloadImageTask(ivBackground).execute(background.get("IconPath"));
//        new DownloadImageTask(ivHours).execute(hours.get("IconPath"));
//        new DownloadImageTask(ivMinutes).execute(minutes.get("IconPath"));

//        Glide.with(this)
//                .load(background.get("IconPath"))
//                .centerInside()
//                .override(Target.SIZE_ORIGINAL * 2)
//                .apply(new RequestOptions().signature(new ObjectKey("signature string")))
//                .into(ivBackground);

        int dp = rlReview.getWidth() / 120;

        Glide.with(this)
                .asBitmap()
                .load(background.get("IconPath"))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                        int w = bitmap.getWidth();
                        ivBackground.setImageBitmap(bitmap);
                        ivBackground.getLayoutParams().width = w * dp;
                        ivBackground.requestLayout();
                        ivBackground.setX(Integer.valueOf(background.get("X")) * dp);
                        ivBackground.setY(Integer.valueOf(background.get("Y")) * dp);
                    }
                });

        Glide.with(this)
                .asBitmap()
                .load(hours.get("IconPath"))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                        int w = bitmap.getWidth();
                        ivHours.setImageBitmap(bitmap);
                        ivHours.getLayoutParams().width = w * dp;
                        ivHours.requestLayout();
                        ivHours.setX(Integer.valueOf(hours.get("X")) * dp);
                        ivHours.setY(Integer.valueOf(hours.get("Y")) * dp);
                    }
                });

        Glide.with(this)
                .asBitmap()
                .load(minutes.get("IconPath"))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                        int w = bitmap.getWidth();
                        ivMinutes.setImageBitmap(bitmap);
                        ivMinutes.getLayoutParams().width = w * dp;
                        ivMinutes.requestLayout();
                        ivMinutes.setX(Integer.valueOf(minutes.get("X")) * dp);
                        ivMinutes.setY(Integer.valueOf(minutes.get("Y")) * dp);
                    }
                });

        Glide.with(this)
                .asBitmap()
                .load(weekDay.get("IconPath"))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                        int w = bitmap.getWidth();
                        ivWeekDay.setImageBitmap(bitmap);
                        ivWeekDay.getLayoutParams().width = w * dp;
                        ivWeekDay.requestLayout();
                        ivWeekDay.setX(Integer.valueOf(weekDay.get("X")) * dp);
                        ivWeekDay.setY(Integer.valueOf(weekDay.get("Y")) * dp);
                    }
                });
//
//        Glide.with(this)
//                .load(hours.get("IconPath"))
//                .into(ivHours);
//        ivHours.setMinimumWidth(ivHours.getWidth() * 2);
//        ivHours.setX(convertPxtoDp(Integer.valueOf(hours.get("X"))));
//        ivHours.setY(convertPxtoDp(Integer.valueOf(hours.get("Y"))));

//        Glide.with(this)
//                .load(hours.get("IconPath"))
//                .centerInside()
//                .apply(new RequestOptions().signature(new ObjectKey("signature string")))
//                .into(ivHours);
////        ivHours.getLayoutParams().width = ivHours.getWidth() * 2;
////        ivHours.requestLayout();
//        ivHours.setX(Integer.valueOf(hours.get("X")));
//        ivHours.setY(Integer.valueOf(hours.get("Y")));
//
//        Glide.with(this)
//                .load(minutes.get("IconPath"))
//                .centerInside()
//                .apply(new RequestOptions().signature(new ObjectKey("signature string")))
//                .into(new DrawableImageViewTarget(ivMinutes));
////        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ivMinutes.getWidth() * 2, ivMinutes.getHeight() * 2);
////        ivMinutes.setLayoutParams(layoutParams);
//        ivMinutes.setX(Integer.valueOf(minutes.get("X")));
//        ivMinutes.setY(Integer.valueOf(minutes.get("Y")));


//        Glide.with(this)
//                .asBitmap()
//                .load(hours.get("IconPath"))
//                .fitCenter()
//                .into(new SimpleTarget<GlideDrawable>() {
//                    @Override
//                    public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
//                        int width = glideDrawable.getIntrinsicWidth();
//                        int height = glideDrawable.getIntrinsicHeight();
//                        viewHolder.image1.setImageDrawable(glideDrawable);
//                    }
//                });
//        Glide.with(this)
//                .asBitmap()
//                .load(minutes.get("IconPath"))
//                .fitCenter()
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap bitmap,
//                                                Transition<? super Bitmap> transition) {
//                        ivMinutes.setImageBitmap(bitmap);
//                        ivMinutes.setX(convertPxtoDp(Integer.valueOf(minutes.get("X"))));
//                        ivMinutes.setY(convertPxtoDp(Integer.valueOf(minutes.get("Y"))));
//                    }
//                });

//        Toast.makeText(this, "1: " + String.valueOf(ivBackground.getWidth()), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "1b: " + String.valueOf(rlReview.getWidth()), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "2: " + String.valueOf(ivMinutes.getWidth()), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "3: " + String.valueOf(ivMinutes.getWidth()), Toast.LENGTH_SHORT).show();
//

//        FutureTarget<File> future = Glide.with(this)
//                .asFile()
//                .load(hours.get("IconPath"))
//                .submit();
//        try {
//            File bitmap = future.get();
//            Bitmap myBitmap = BitmapFactory.decodeFile(bitmap.getAbsolutePath());
//            ivMinutes.setImageBitmap(myBitmap);
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        Glide.with(this)
//                .load(minutes.get("IconPath"))
//                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                .placeholder(R.drawable.ic_blank_photo)
//                .into(ivMinutes);
//        ivMinutes.setX(convertPxtoDp(Integer.valueOf(minutes.get("X"))));
//        ivMinutes.setY(convertPxtoDp(Integer.valueOf(minutes.get("Y"))));


//        try {
//            Glide.with(ivMinutes)
//                    .load(minutes.get("IconPath"))
//                    .submit()
//                    .get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        File diy_bkg = new File(this.getExternalFilesDir(null) + File.separator + "diy_background_1.png");
        if(diy_bkg.exists()) {
            imgPath = diy_bkg.getAbsolutePath();
            Bitmap bmp = BitmapFactory.decodeFile(imgPath);
            rlReview.setBackground(new BitmapDrawable(getResources(), bmp));
        }
    }

    public String encodeImageToString(String imgPath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Must compress the Image to reduce image size to make upload easy
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byte_arr = stream.toByteArray();
        // Encode Image to String
        return Base64.encodeToString(byte_arr, Base64.DEFAULT); //encoded string
    }

    private void download(String fileUrl, String saveName, boolean isAction) {
        if (!new General().isInternetConnection(this)) {
            Toast.makeText(this, R.string.toast_notice_no_internet_download, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isStoragePermissionGranted(this, REQUEST_PERMISSION_STORAGE)) {
            dlTask = new DownloadFileFromURL(saveName, isAction).execute(fileUrl);
        }
    }

    public void getAll() {
        if (!new General().isInternetConnection(this)) {
            tvMessage1.setVisibility(View.VISIBLE);
            tvMessage1.setText(R.string.toast_notice_no_internet_access);
            return;
        }

        if(adapter != null) {
            baseThemes.clear();
            adapter.notifyDataSetChanged();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "https://miband4display-server.csm2.duckdns.org/index.php?act=basetheme";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, (String response) -> {
            if (response.equals("[{\"msg\":\"DATA_NOT_FOUND\"}]")) {
                tvMessage1.setText(R.string.msg_list_empty);
                return;
            }
            try {
                JSONArray arr = new JSONArray(response);
                if(arr.length() > 0) {
                    for(int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String folderName = obj.getString("folder_name");
                        String json = obj.getString("json");
                        String imgUrl = "https://miband4display-server.csm2.duckdns.org/public/files/basetheme/" + folderName + "/" + folderName + "_static.png";
                        BaseThemeModel m = new BaseThemeModel();
                        m.setFolderName(folderName);
                        m.setImgUrl(imgUrl);
                        m.setJson(json);
                        baseThemes.add(m);
                        adapter.notifyItemInserted(i);
                    }
                } else {
                    Log.d(TAG,"No data found");
                    Toast.makeText(this, R.string.msg_result_not_found, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.d(TAG,"cannot parse json string.  " + e.toString());
                Log.d(TAG, "response: " + response);
            }
        }, (VolleyError error) -> {
            Log.d(TAG,"Server error or return a invalid_json or html. " + error.toString());
            tvMessage1.setVisibility(View.VISIBLE);
            tvMessage1.setText(R.string.msg_server_cannot_access);
        });
        // set timeout: 15s
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
        requestQueue.add(stringRequest);
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadFileFromURL extends AsyncTask<String, Integer, String> {
        private boolean isSuccess = true;
        private boolean isCancel = false; // press Cancel by user
        private boolean isConnect = true;
        private boolean isAction;

        private String saveName;

        public DownloadFileFromURL(String saveName, boolean isAction) {
            this.saveName = saveName;
            this.isAction = isAction;
        }

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DIYActivity.this);
            progressDialog.setMessage("Downloading");
            progressDialog.setMax(100);
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.button_cancel), (DialogInterface dialog, int which) -> {
                isCancel = true;
            });
            progressDialog.setOnDismissListener((DialogInterface dialog) -> {
                dlTask.cancel(true);
            });
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            if(isAction) progressDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            URL url;
            int lengthOfFile;

            try {
                url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                lengthOfFile = connection.getContentLength();
            } catch (IOException e) {
                isSuccess = false;
                Log.e(TAG, "Open connection error: " + e.getMessage());
                return null;
            }

//            if(android.os.Build.VERSION.SDK_INT < 29)
//                storagePath = new File(Environment.getExternalStorageDirectory(), "display");
//            else
//                // storage/emulated/0/Android/data/org.soft2u.miband_5_display/files
//                storagePath = new File(this.getExternalFilesDir(null), "display");

            try {
                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),8192);
                OutputStream output;
                if(android.os.Build.VERSION.SDK_INT < 29) {
                    File sd = Environment.getExternalStorageDirectory();
                    String dataPath = "/Android/data";
                    String folderApp = String.valueOf(Install.APPLICATION_ID);
                    String folderFiles = "files";
                    String destinationPath = "";
                    File directory = new File(sd + dataPath + File.separator + folderApp);
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    directory = new File(sd + dataPath + File.separator + folderApp + File.separator + folderFiles);
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    destinationPath = dataPath + File.separator + folderApp + File.separator + folderFiles;
                    output = new FileOutputStream(sd + destinationPath + File.separator + saveName);
                }
                else {
                    output = new FileOutputStream(getExternalFilesDir(null) + File.separator + saveName);
                }

                byte[] data = new byte[1024];
                long total = 0;
                while (total < lengthOfFile) {
                    if(!new General().isInternetConnection(DIYActivity.this)) {
                        isConnect = false;
                        Log.d(TAG, "No internet access");
                        break;
                    } else {
                        count = input.read(data);
                        if(count == -1) {
                            isSuccess = false;
                            Log.d(TAG, "error");
                            break;
                        }
                        if(isCancel) {
                            Log.d(TAG, "stopped");
                            break;
                        }
                        total += count;
//                        Log.d(TAG, String.valueOf(total));
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress((int) ((total * 100) / lengthOfFile));
                        // writing data to file
                        output.write(data, 0, count);
                    }
                }

                // flushing output
                output.flush();
                // closing streams
                output.close();
                input.close();

            } catch (IOException e) {
                isSuccess = false;
                Log.e(TAG, "Error writing: " + e.getMessage());
            }
            return null;
        }

        /**
         * Updating progress bar
         * */
        @Override
        protected void onProgressUpdate(Integer... values) {
            // setting progress percentage
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            Log.d(TAG, "canceled");
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            if(isConnect && isSuccess) {
                // download finished
                if(isAction) {
                    Toast.makeText(DIYActivity.this, R.string.toast_file_downloaded_finished, Toast.LENGTH_SHORT).show();
                    btnDownload.setVisibility(View.GONE);
                    btnInstall.setVisibility(View.VISIBLE);

//                tvHowToInstall.setVisibility(View.VISIBLE);
                    Animation anim = new AlphaAnimation(0.0f, 1.0f);
                    anim.setDuration(500);
                    anim.setStartOffset(20);
                    anim.setRepeatMode(Animation.REVERSE);
                    anim.setRepeatCount(Animation.INFINITE);
//                tvHowToInstall.startAnimation(anim);

//                updateDownloads(themeIdHolder);
                }
            } else if (!isSuccess) {
                Toast.makeText(DIYActivity.this, R.string.toast_notice_download_fail, Toast.LENGTH_SHORT).show();
//                reportError(themeIdHolder, "DISPLAY_DOWNLOAD_LINK_ERROR", false);
            } else {
                Toast.makeText(DIYActivity.this, R.string.toast_notice_no_internet_download, Toast.LENGTH_SHORT).show();
            }
            dlTask.cancel(true);
            progressDialog.dismiss();
        }
    }

    private void install() {
        if(new General().isPackageInstalled(this, "com.xiaomi.hm.health"))
        {
            if(writeThemeFile1() && writeThemeFile2()) {
                if (writeInfoFile()) {
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.xiaomi.hm.health");
                    startActivity(intent);
                }
            }
        } else {
            Toast.makeText(this, R.string.toast_mi_fit_app_not_found, Toast.LENGTH_LONG).show();
        }
    }

    private boolean writeThemeFile1() {
        File sd = Environment.getExternalStorageDirectory();
//            File data = Environment.getDataDirectory();
        String themeFileName = "theme1.bin";

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String midataPath = "/Android/data/com.xiaomi.hm.health/files";
            String folderSkin = "watch_skin_local";
            String folderDisplay = "LQL9x2usQGxi8HgzD7ginnxpwc6XHsLv3z730sq1";
            String destinationPath = "";

            File directory = new File(sd + midataPath + File.separator + folderSkin);
            if (!directory.exists()) {
                directory.mkdir();
            }
            directory = new File(sd + midataPath + File.separator + folderSkin + File.separator + folderDisplay);
            if (!directory.exists()) {
                directory.mkdir();
            }
            destinationPath = midataPath + File.separator + folderSkin + File.separator + folderDisplay;

            File themeDownloadedFile = new File(this.getExternalFilesDir(null), themeFileName);
            File themeApplyFile = new File(sd, destinationPath + File.separator + themeFileName);

            if (themeDownloadedFile.exists()) {
                try {
                    FileChannel src = new FileInputStream(themeDownloadedFile).getChannel();
                    FileChannel dst = new FileOutputStream(themeApplyFile).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } catch (IOException ex) {
                    Log.d(TAG,"1. Failed to copy bin file. " + ex.toString());
                    Toast.makeText(this, R.string.toast_notice_copy_fail, Toast.LENGTH_LONG).show();
                    return false;
                }
            } else {
                String msg = getResources().getString(R.string.toast_file_not_found, themeFileName);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.toast_notice_storage_is_unmount, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean writeThemeFile2() {
        File sd = Environment.getExternalStorageDirectory();
//            File data = Environment.getDataDirectory();
        String themeFileName = "theme1.bin";

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String midataPath = "/Android/data/com.xiaomi.hm.health/files";
            String folderSkin = "watch_skin_local";
            String folderDisplay = "0N37f8yfj637FJ7xi5gbCPGjeN4Mdyb8vZ9s9ILH";
            String destinationPath = "";

            File directory = new File(sd + midataPath + File.separator + folderSkin);
            if (!directory.exists()) {
                directory.mkdir();
            }
            directory = new File(sd + midataPath + File.separator + folderSkin + File.separator + folderDisplay);
            if (!directory.exists()) {
                directory.mkdir();
            }
            destinationPath = midataPath + File.separator + folderSkin + File.separator + folderDisplay;

            File themeDownloadedFile = new File(this.getExternalFilesDir(null), themeFileName);
            File themeApplyFile = new File(sd, destinationPath + File.separator + folderDisplay + ".bin");

            if (themeDownloadedFile.exists()) {
                try {
                    FileChannel src = new FileInputStream(themeDownloadedFile).getChannel();
                    FileChannel dst = new FileOutputStream(themeApplyFile).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } catch (IOException ex) {
                    Log.d(TAG,"2. Failed to copy bin file. " + ex.toString());
                    Toast.makeText(this, R.string.toast_notice_copy_fail, Toast.LENGTH_LONG).show();
                    return false;
                }
            } else {
                String msg = getResources().getString(R.string.toast_file_not_found, themeFileName);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.toast_notice_storage_is_unmount, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean writeInfoFile() {
        File sd = Environment.getExternalStorageDirectory();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String destinationPath = "/Android/data/com.xiaomi.hm.health/files/watch_skin_local/LQL9x2usQGxi8HgzD7ginnxpwc6XHsLv3z730sq1/";
            AssetManager assetManager = this.getAssets();
            String[] files;
            try {
                files = assetManager.list("themeinfo");
            } catch (IOException ex) {
                Log.e(TAG, "Failed to get asset file list. ", ex);
                return false;
            }

            if (files != null)
                for (String filename : files) {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = assetManager.open("themeinfo/" + filename);
                        File outFile = new File(sd, destinationPath + filename);
                        if (!outFile.exists()) {
                            out = new FileOutputStream(outFile);
                            byte[] buffer = new byte[1024];
                            int read;
                            while((read = in.read(buffer)) != -1){
                                out.write(buffer, 0, read);
                            }
                        }
                    } catch(IOException e) {
                        Log.e(TAG, "Failed to copy asset file: " + filename, e);
                        return false;
                    }
                    finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                // NOOP
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                // NOOP
                            }
                        }
                    }
                }
        } else {
            Toast.makeText(this, R.string.toast_notice_storage_is_unmount, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String getValue(JSONObject obj, String keyPath) {
        String[] keys = keyPath.split(";");
        try {
            switch (keys.length) {
                case 3:
                    return obj.getJSONObject(keys[0]).getJSONObject(keys[1]).getString(keys[2]);
                case 4:
                    return obj.getJSONObject(keys[0]).getJSONObject(keys[1]).getJSONObject(keys[2]).getString(keys[3]);
                case 5:
                    return obj.getJSONObject(keys[0]).getJSONObject(keys[1]).getJSONObject(keys[2]).getJSONObject(keys[3]).getString(keys[4]);
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String loadJSONFromStorage(File file) {
        String json;
        try {
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private String formatIndex(int number) {
        if(number < 10)
            return "000" + number;
        else return "00" + number;
    }

    private int convertPxtoDp(int pixelValue) {
        float dpRatio = getResources().getDisplayMetrics().density;
        return (int) (pixelValue * dpRatio);
    }

    public boolean isStoragePermissionGranted(Activity mActivity, int requestCode) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"Write Permission is granted");
                return true;
            } else {
                Log.d(TAG,"Write Permission is denied. Start request again");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                return false;
            }
        } else {
            // Permission is automatically granted on sdk<23 upon installation
            Log.d(TAG,"Write Permission already granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.d(TAG,"Permission: " +permissions[0]+ " was " +grantResults[0]);
        } else {
            // with only one request
            boolean showRationale = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                showRationale = shouldShowRequestPermissionRationale(permissions[0]);
            }
            if (!showRationale) {
                // Checked "Never ask again"
                Toast.makeText(this, R.string.toast_permissions_write_storage_denied_2, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_STORAGE);
            } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0])) {
                // select Deny
                Toast.makeText(this, R.string.toast_permissions_write_storage_denied, Toast.LENGTH_LONG).show();
            }
            Log.d(TAG,"Write Permission request is denied");
        }
    }
}