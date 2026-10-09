package org.soft2u.miband_5_display.ui.activity.display;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;

import org.json.JSONException;
import org.json.JSONObject;
import org.soft2u.miband_5_display.MainActivity;
import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.fragment.display.DisplaySearchFragment;
import org.soft2u.miband_5_display.utils.General;

import java.util.Arrays;

public class DisplayTagActivity extends AppCompatActivity {

    private final String TAG = "DisplayTagActivity";

    private FrameLayout addViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_tag);

        addViewContainer = findViewById(R.id.ad_view_container);
        // ads banner bottom
        MobileAds.initialize(this, (InitializationStatus initializationStatus) ->
                loadBanner()
        );

        Bundle extra = getIntent().getExtras();
        String jsonReceiver = extra.getString("JSON"); // used for this activity and pass to fragment
        readJsonData(jsonReceiver);

        if(savedInstanceState == null) {
            DisplaySearchFragment fragment = new DisplaySearchFragment();
            fragment.setArguments(extra);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

        findViewById(R.id.btn_back).setOnClickListener((View v) ->
                finish()
        );

        findViewById(R.id.btn_home).setOnClickListener((View v) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |  Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void readJsonData(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            // eg. {"author":"lqlam","note":"Tac gia"}
//            String title = obj.names().getString(0) + ": " + obj.get(obj.names().getString(0));
            String title = obj.getString(obj.names().getString(1)) + ": " + obj.getString(obj.names().getString(0));
            ((TextView) findViewById(R.id.tv_theme_title_header)).setText(title);
        } catch (JSONException ex) {
            Log.e(TAG,"cannot parse json string.  " + ex.toString());
            Toast.makeText(this, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
        }
    }
    private void loadBanner() {
        RequestConfiguration requestConfiguration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(
                "7B21EEA5F5F327CF30F5F208575D0FCD",
                AdRequest.DEVICE_ID_EMULATOR)).build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        AdView adView = new AdView(this);
        if(General.isDebuggable(this))
            adView.setAdUnitId(getString(R.string.ad_banner_unit_id_1_test));
        else
            adView.setAdUnitId(getString(R.string.ad_banner_unit_id_1));
        addViewContainer.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
}