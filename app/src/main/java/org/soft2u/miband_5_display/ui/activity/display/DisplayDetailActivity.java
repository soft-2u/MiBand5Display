package org.soft2u.miband_5_display.ui.activity.display;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.soft2u.miband_5_display.MainActivity;
import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.database.AppDatabase;
import org.soft2u.miband_5_display.entity.ThemeFavorite;
import org.soft2u.miband_5_display.entity.ThemeHistory;
import org.soft2u.miband_5_display.entity.ThemeRequest;
import org.soft2u.miband_5_display.ui.dialog.DisplayDetailDialog;
import org.soft2u.miband_5_display.ui.dialog.LanguageRequestTransDialog;
import org.soft2u.miband_5_display.ui.fragment.display.DisplayRandomFragment;
import org.soft2u.miband_5_display.utils.FlowLayout;
import org.soft2u.miband_5_display.utils.General;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DisplayDetailActivity extends AppCompatActivity {

    private String jsonReceiver;
    private int THEME_ID;
    private String DONATION_ID;
    private String REQUEST_TRANS;
    private final String TAG = "DisplayDetailActivity";

    private ImageView btnDonate, ivCover;
    private AdLoader adLoader;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_detail);

        ivCover = findViewById(R.id.iv_theme_cover);
        ImageView btnAddFavorite = findViewById(R.id.btn_add_favorite);
        ImageView btnRequestTrans = findViewById(R.id.btn_request);
        btnDonate = findViewById(R.id.btn_donate);
        Button btnInstall = findViewById(R.id.btn_install);

        if(savedInstanceState == null) {
            DisplayRandomFragment defaultFragment = new DisplayRandomFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, defaultFragment).commit();
        }

        Bundle extra = getIntent().getExtras();
        jsonReceiver = extra.getString("JSON");
        readJsonData(jsonReceiver);

        setRequestBtnState(btnRequestTrans);
        setFavoriteBtnState(btnAddFavorite);

        addHistory(THEME_ID);
        updateViews(THEME_ID);

        findViewById(R.id.btn_back).setOnClickListener((View v) ->
            finish()
        );

        findViewById(R.id.btn_home).setOnClickListener((View v) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |  Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnDonate.setOnClickListener((View v) ->
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/paypalme2/" + DONATION_ID)))
        );

        btnAddFavorite.setOnClickListener((View v) -> {
            if(btnAddFavorite.getTag().equals("unlike")) {
                btnAddFavorite.setImageResource(R.drawable.ic_action_favorites_selected);
                btnAddFavorite.setTag("like");
                addFavorite(THEME_ID);
            } else {
                btnAddFavorite.setImageResource(R.drawable.ic_action_favorite);
                btnAddFavorite.setTag("unlike");
                removeFavorite(THEME_ID);
            }
        });

        btnRequestTrans.setOnClickListener((View v) -> {
            if(btnRequestTrans.getTag().equals("none")) {
                FragmentManager fm = getSupportFragmentManager();
                LanguageRequestTransDialog dg = LanguageRequestTransDialog.newInstance(THEME_ID);
                dg.show(fm, TAG);
            } else {
                String msg = getResources().getString(R.string.toast_requested_translate_already, REQUEST_TRANS);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        btnInstall.setOnClickListener((View v) -> {
            FragmentManager fm = getSupportFragmentManager();
            DisplayDetailDialog dg = DisplayDetailDialog.newInstance(jsonReceiver);
            dg.setCancelable(true);
            dg.show(fm, "theme detail dialog");
        });

        // ads fullscreen
        MobileAds.initialize(this, (InitializationStatus initializationStatus) -> {
            loadNativeAd();
            loadInterstitialAd();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int countdown = new PrefManager(DisplayDetailActivity.this).getCountDown();
        Log.i(TAG, String.valueOf(countdown));

        if (countdown != 0)
            new PrefManager(DisplayDetailActivity.this).setCountDown(countdown - 1);
        else {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(this);
            } else {
                Log.d(TAG, "The interstitial wasn't loaded yet.");
            }
        }
    }

    private void readJsonData(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            THEME_ID = Integer.parseInt(obj.getString("themeid"));
            DONATION_ID = obj.getString("donate_url");
            String coverFileUrl = "https://miband4display-server.csm2.duckdns.org/public/files/display/cover/" + obj.getString("img_url");
            String isDisplaySteps = obj.getString("isdisplay_steps");
            String isdisplayStepsProgress = obj.getString("isdisplay_steps_progress");
            String isDisplayDate = obj.getString("isdisplay_date");
            String isDisplayHeart = obj.getString("isdisplay_heart");
            String isDisplayBattery = obj.getString("isdisplay_battery");
            String isDisplaySeconds = obj.getString("isdisplay_seconds");
            String isDisplayDistance = obj.getString("isdisplay_distance");
            String isDisplayCal = obj.getString("isdisplay_cal");
            String isDisplayAnimated = obj.getString("isdisplay_animated");
            String isDisplayWeather = obj.getString("isdisplay_weather");

            Glide.with(this)
                    .load(coverFileUrl)
                    .placeholder(R.drawable.ic_blank_photo)
                    .into(ivCover);

            // authors
            JSONArray authorObject = new JSONArray(obj.getString("author"));
            for (int i = 0; i < authorObject.length(); i++) {
                TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextViewAuthor));
                textView.setText(authorObject.getString(i));
                textView.setTag(authorObject.getString(i));
                textView.setOnClickListener(tvAuthorItemClick);
                ((FlowLayout) findViewById(R.id.flowlayout_author_container)).addView(textView);
            }

            // languages
            JSONArray languageObject = new JSONArray(obj.getString("language"));
            for (int i = 0; i < languageObject.length(); i++) {
                TextView textView = new TextView(this);
                textView.setText(new General().country(languageObject.getString(i)));
                textView.setPadding(0,0,12,0);
                ((FlowLayout) findViewById(R.id.flowlayout_language_container)).addView(textView);
            }

            // tags
            if(!obj.getString("description").equals("null")) {
                JSONArray tagObject = new JSONArray(obj.getString("description"));
                for (int i = 0; i < tagObject.length(); i++) {
                    TextView textView = new TextView(new ContextThemeWrapper(this, R.style.ButtonToggle));
                    textView.setText(tagObject.getString(i));
                    textView.setTag(tagObject.getString(i));
                    textView.setOnClickListener(tvTagItemClick);
                    ((FlowLayout) findViewById(R.id.flowlayout_tag_container)).addView(textView);
                }
            } else {
                // hide
                findViewById(R.id.flowlayout_tag_container).setVisibility(View.GONE);
            }

            // updates
            if(!obj.getString("changes").equals("null")) {
                (findViewById(R.id.ll_update_container)).setVisibility(View.VISIBLE);
                (findViewById(R.id.tv_theme_update_label)).setVisibility(View.VISIBLE);

                JSONArray updateObject = new JSONArray(obj.getString("changes"));
                for (int i = 0; i < updateObject.length(); i++) {
                    TextView textView = new TextView(this);
                    textView.setText(updateObject.getString(i));
                    ((LinearLayout) findViewById(R.id.ll_update_container)).addView(textView);
                }
            }

            ((TextView) findViewById(R.id.tv_theme_title)).setText(obj.getString("title"));
            ((TextView) findViewById(R.id.tv_theme_title_header)).setText(obj.getString("title"));
            ((AppCompatTextView) findViewById(R.id.tv_downloads)).setText(obj.getString("download"));
            ((AppCompatTextView) findViewById(R.id.tv_views)).setText(obj.getString("view"));

            // Features
            if(isDisplayBattery.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_battery_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_battery));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
            if(isDisplayDate.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_date_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_date));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
            if(isDisplaySteps.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_steps_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_steps));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
            if(isdisplayStepsProgress.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_step_progress_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_steps_progress));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
            if(isDisplayDistance.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_distance_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_distance));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
            if(isDisplayCal.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_calorie_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_calorie));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
            if(isDisplayHeart.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_heart_rate_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_heart_rate));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
            if(isDisplaySeconds.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_seconds_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_seconds));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
            if(isDisplayWeather.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_weather_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_weather));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
            if(isDisplayAnimated.equals("1")) {
                AppCompatImageView ivFeature = new AppCompatImageView(new ContextThemeWrapper(this, R.style.ImageViewFeature));
                ivFeature.setImageResource(R.drawable.ic_features_animated_33x24);
                ivFeature.setTag(getResources().getString(R.string.checkbox_filter_by_display_animated));
                ivFeature.setPadding(10,13,10,10);
                ivFeature.setOnClickListener(tvFeatureItemClick);
                ((LinearLayout) findViewById(R.id.ll_theme_feature_container)).addView(ivFeature);
            }
        } catch (JSONException ex) {
            Log.e(TAG,"cannot parse json string.  " + ex.toString());
            Toast.makeText(this, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
        }

        if(DONATION_ID.equals("null")) {
            btnDonate.setVisibility(View.GONE);
        }
    }

    private void setFavoriteBtnState(ImageView btnAddFavorite) {
        List<ThemeFavorite> themeFavoriteList = AppDatabase.getAppDatabase(this).ThemeFavoriteDAO().getAll();
        for (ThemeFavorite item : themeFavoriteList) {
            if(item.getThemeid() == THEME_ID) {
                btnAddFavorite.setImageResource(R.drawable.ic_action_favorites_selected);
                btnAddFavorite.setTag("like");
                break;
            }
        }
    }

    private final TextView.OnClickListener tvFeatureItemClick = (View v) -> Toast.makeText(this, v.getTag().toString(), Toast.LENGTH_SHORT).show();

    private final TextView.OnClickListener tvTagItemClick = (View v) -> {
        String json = "{\"tag\":\""+v.getTag().toString()+"\",\"note\":\""+getResources().getString(R.string.label_tags)+"\"}"; // {TAG:"pokemon","note":"the"}
        Intent intent = new Intent(this, DisplayTagActivity.class);
        intent.putExtra("JSON", json);
        startActivity(intent);
    };

    private final TextView.OnClickListener tvAuthorItemClick = (View v) -> {
        String json = "{\"author\":\""+v.getTag().toString()+"\",\"note\":\""+getResources().getString(R.string.label_author)+"\"}"; // {"author":"lqlam","note":"Tac gia"}
        Intent intent = new Intent(this, DisplayTagActivity.class);
        intent.putExtra("JSON", json);
        startActivity(intent);
    };

    private void addFavorite(int themeId) {
        ThemeFavorite themeFavorite = new ThemeFavorite();
        themeFavorite.setThemeid(themeId);
        themeFavorite.setDate_add(new Date());
        AppDatabase.getAppDatabase(this).ThemeFavoriteDAO().insertAll(themeFavorite);
        Toast.makeText(this, R.string.toast_add_to_favorite_success, Toast.LENGTH_SHORT).show();
    }

    private void removeFavorite(int themeId) {
        AppDatabase.getAppDatabase(this).ThemeFavoriteDAO().delete(themeId);
        Toast.makeText(this, R.string.toast_remove_from_favorite_success, Toast.LENGTH_SHORT).show();
    }

    private void addHistory(int themeId) {
        ThemeHistory themeHistory = new ThemeHistory();
        themeHistory.setThemeid(themeId);
        themeHistory.setDate_add(new Date());
        AppDatabase.getAppDatabase(this).ThemeHistoryDAO().insert(themeHistory);
    }

    private void updateViews(int themeId) {
        String suffixUrl = "act=themedetail&job=updateview&themeid=" + themeId + "";
        new General().executeVolley(suffixUrl, this);
    }

    private void setRequestBtnState(ImageView btnRequestTrans) {
        List<ThemeRequest> themeRequestTransList = AppDatabase.getAppDatabase(this).ThemeRequestDAO().getAll();
        for (ThemeRequest item : themeRequestTransList) {
            if(item.getThemeid() == THEME_ID) {
//                btnRequestTrans.setTextColor(getResources().getColor(R.color.textUnSelect));
                btnRequestTrans.setTag("requested");
                REQUEST_TRANS = item.getRequest_content();
                break;
            }
        }
    }

    private void loadNativeAd() {
        String adNativeUnitId;
        if(General.isDebuggable(this))
            adNativeUnitId = getResources().getString(R.string.ad_native_unit_id_test);
        else
            adNativeUnitId = getResources().getString(R.string.ad_native_unit_id);
        adLoader = new AdLoader.Builder(this, adNativeUnitId)
                .forNativeAd(nativeAd -> {
                    // If this callback occurs after the activity is destroyed, you
                    // must call destroy and return or you may get a memory leak.
                    if (isDestroyed()) {
                        nativeAd.destroy();
                        return;
                    }

                    // Show the ad.
                    FrameLayout frameLayout = findViewById(R.id.fl_ad_placeholder);
                    NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.native_ad_layout, null);
                    populateNativeAdView(nativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);

                    if (adLoader.isLoading()) {
                        // The AdLoader is still loading ads.
                        // Expect more adLoaded or onAdFailedToLoad callbacks.
                    } else {
                        // The AdLoader has finished loading ads.
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
						Log.e(TAG, "Failed to load native ad: " + adError);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void loadInterstitialAd() {
        RequestConfiguration requestConfiguration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(
                "7B21EEA5F5F327CF30F5F208575D0FCD",
                AdRequest.DEVICE_ID_EMULATOR)).build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        String adInterstitialUnitId;
        if(General.isDebuggable(this))
            adInterstitialUnitId = getResources().getString(R.string.ad_interstitial_unit_id_test);
        else
            adInterstitialUnitId = getResources().getString(R.string.ad_interstitial_unit_id);
        InterstitialAd.load(this, adInterstitialUnitId, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        Log.d(TAG, "The ad was dismissed.");
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        // Called when fullscreen content failed to show.
                        Log.d(TAG, "The ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        mInterstitialAd = null;
                        Log.d(TAG, "The ad was shown.");
                        new PrefManager(DisplayDetailActivity.this).setCountDown(new Random().nextInt(8 - 5) + 5);
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i(TAG, loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view. Media content will be automatically populated in the media view once
		// adView.setNativeAd() is called.
		MediaView mediaView = adView.findViewById(R.id.ad_media);
		mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
		adView.setMediaView(mediaView);
		
		// Set other ad assets.
		adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
		adView.setBodyView(adView.findViewById(R.id.ad_body));
		adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
		adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

		// The headline is guaranteed to be in every NativeAd.
		((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

		// These assets aren't guaranteed to be in every NativeAd, so it's important to
		// check before trying to display them.
		if (nativeAd.getBody() == null) {
		  adView.getBodyView().setVisibility(View.INVISIBLE);
		} else {
		  adView.getBodyView().setVisibility(View.VISIBLE);
		  ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
		}

		if (nativeAd.getStarRating() == null || nativeAd.getStarRating() < 3) {
		  adView.getStarRatingView().setVisibility(View.INVISIBLE);
		} else {
		  ((RatingBar) adView.getStarRatingView())
			  .setRating(nativeAd.getStarRating().floatValue());
		  adView.getStarRatingView().setVisibility(View.VISIBLE);
		}

		if (nativeAd.getAdvertiser() == null) {
		  adView.getAdvertiserView().setVisibility(View.INVISIBLE);
		} else {
		  ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
		  adView.getAdvertiserView().setVisibility(View.VISIBLE);
		}

		// This method tells the Google Mobile Ads SDK that you have finished populating your
		// native ad view with this native ad. The SDK will populate the adView's MediaView
		// with the media content from this native ad.
		adView.setNativeAd(nativeAd);
    }
}