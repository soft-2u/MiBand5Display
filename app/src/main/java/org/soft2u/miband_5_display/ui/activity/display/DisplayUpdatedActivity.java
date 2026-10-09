package org.soft2u.miband_5_display.ui.activity.display;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;

import org.soft2u.miband_5_display.MainActivity;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.fragment.display.DisplayUpdatedFragment;
import org.soft2u.miband_5_display.utils.General;

import java.util.Arrays;

public class DisplayUpdatedActivity extends AppCompatActivity {

    private final String TAG = "DisplayUpdatedActivity";

    private FrameLayout addViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_updated);

        addViewContainer = findViewById(R.id.ad_view_container);
        // ads banner bottom
        MobileAds.initialize(this, (InitializationStatus initializationStatus) ->
                loadBanner()
        );

        if(savedInstanceState == null) {
            DisplayUpdatedFragment fragment = new DisplayUpdatedFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment.newInstance("updated")).commit();
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