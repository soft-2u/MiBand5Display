package org.soft2u.miband_5_display.ui.activity.display;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;

import org.json.JSONObject;
import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.dialog.SortDialog;
import org.soft2u.miband_5_display.ui.fragment.display.DisplayNewFragment;
import org.soft2u.miband_5_display.utils.General;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DisplayNewActivity extends AppCompatActivity {

    private final String TAG = "DisplayNewActivity";
    private final String LANG_ID = "lang_view_id";
    private final String LANG_TAG = "lang_code";
    private final String DISPLAY_ID = "display_view_id";
    private final String DISPLAY_TAG = "display_code";
    private String filterHolder = "", orderHolder = "";
    private final ArrayList<Map<String, String>> langList = new ArrayList<>();
    private final ArrayList<Map<String, String>> displayList = new ArrayList<>();
    private String watchType = "digital"; // default

    private TextView btnWatchAnalog, btnWatchDigital, btnWatchBoth;
    private ImageView btnFilter, btnSort;
    private FrameLayout addViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_new);

		btnFilter = findViewById(R.id.btn_menu_filter);
		btnSort = findViewById(R.id.btn_menu_sort);

		CheckBox ckbLangMulti = findViewById(R.id.ckb_filter_by_lang_multi);
        CheckBox ckbLangEn = findViewById(R.id.ckb_filter_by_lang_en);
        CheckBox ckbLangVi = findViewById(R.id.ckb_filter_by_lang_vi);
        CheckBox ckbLangJa = findViewById(R.id.ckb_filter_by_lang_ja);
        CheckBox ckbLangKo = findViewById(R.id.ckb_filter_by_lang_ko);
        CheckBox ckbLangZh = findViewById(R.id.ckb_filter_by_lang_zh);
        CheckBox ckbLangRu = findViewById(R.id.ckb_filter_by_lang_ru);
        CheckBox ckbLangIt = findViewById(R.id.ckb_filter_by_lang_it);
        CheckBox ckbLangEs = findViewById(R.id.ckb_filter_by_lang_es);
        CheckBox ckbLangPt = findViewById(R.id.ckb_filter_by_lang_pt);
        CheckBox ckbLangPl = findViewById(R.id.ckb_filter_by_lang_pl);
        CheckBox ckbLangTh = findViewById(R.id.ckb_filter_by_lang_th);
        CheckBox ckbLangFr = findViewById(R.id.ckb_filter_by_lang_fr);
        CheckBox ckbLangDe = findViewById(R.id.ckb_filter_by_lang_de);
        CheckBox ckbLangId = findViewById(R.id.ckb_filter_by_lang_id);
        CheckBox ckbLangTr = findViewById(R.id.ckb_filter_by_lang_tr);
        CheckBox ckbLangBe = findViewById(R.id.ckb_filter_by_lang_be);
        CheckBox ckbLangUk = findViewById(R.id.ckb_filter_by_lang_uk);
        CheckBox ckbLangEl = findViewById(R.id.ckb_filter_by_lang_el);
        CheckBox ckbLangLv = findViewById(R.id.ckb_filter_by_lang_lv);
        CheckBox ckbLangCz = findViewById(R.id.ckb_filter_by_lang_cz);
        CheckBox ckbLangRo = findViewById(R.id.ckb_filter_by_lang_ro);
        CheckBox ckbLangNl = findViewById(R.id.ckb_filter_by_lang_nl);
        CheckBox ckbLangMy = findViewById(R.id.ckb_filter_by_lang_my);
        CheckBox ckbLangSk = findViewById(R.id.ckb_filter_by_lang_sk);
        CheckBox ckbLangAr = findViewById(R.id.ckb_filter_by_lang_ar);

        CheckBox ckbDisplaySteps = findViewById(R.id.ckb_filter_by_display_steps);
        CheckBox ckbDisplayBattery = findViewById(R.id.ckb_filter_by_display_battery);
        CheckBox ckbDisplayDate = findViewById(R.id.ckb_filter_by_display_date);
        CheckBox ckbDisplayDistance = findViewById(R.id.ckb_filter_by_display_distance);
        CheckBox ckbDisplaySeconds = findViewById(R.id.ckb_filter_by_display_seconds);
        CheckBox ckbDisplayHeart = findViewById(R.id.ckb_filter_by_display_heart);
        CheckBox ckbDisplayCalorie = findViewById(R.id.ckb_filter_by_display_calorie);
        CheckBox ckbDisplayAnimated = findViewById(R.id.ckb_filter_by_display_animated);
        CheckBox ckbDisplayWeather = findViewById(R.id.ckb_filter_by_display_weather);
        CheckBox ckbDisplayStepsProgress = findViewById(R.id.ckb_filter_by_display_steps_progress);

        btnWatchAnalog = findViewById(R.id.btn_filter_by_watch_analog);
        btnWatchDigital = findViewById(R.id.btn_filter_by_watch_digital);
        btnWatchBoth = findViewById(R.id.btn_filter_by_watch_both);

        addViewContainer = findViewById(R.id.ad_view_container);
        // ads banner bottom
        MobileAds.initialize(this, (InitializationStatus initializationStatus) ->
                loadBanner()
        );

        if(savedInstanceState == null) {
            DisplayNewFragment fragment = new DisplayNewFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment.newInstance("new")).commit();
        }

        findViewById(R.id.btn_back).setOnClickListener((View v) ->
                finish()
        );

        btnSort.setOnClickListener((View v) -> {
            FragmentManager fm = getSupportFragmentManager();
            SortDialog dg = SortDialog.newInstance(orderHolder);
            dg.setCancelable(true);
            dg.show(fm, TAG);
        });

        btnFilter.setOnClickListener((View v) ->
                ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.END)
        );
		
        findViewById(R.id.btn_filter).setOnClickListener((View v) -> {
            filter();
            btnFilter.setImageResource(R.drawable.ic_menu_filter_selected);
        });
		
        findViewById(R.id.btn_clear_filter).setOnClickListener((View v) -> {
            clear();
            btnFilter.setImageResource(R.drawable.ic_menu_filter);
        });

        ckbLangMulti.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangMulti)
        );

        ckbLangEn.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangEn)
        );

        ckbLangVi.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangVi)
        );

        ckbLangJa.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangJa)
        );

        ckbLangKo.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangKo)
        );

        ckbLangZh.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangZh)
        );

        ckbLangRu.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangRu)
        );

        ckbLangIt.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangIt)
        );

        ckbLangEs.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangEs)
        );

        ckbLangPt.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangPt)
        );

        ckbLangPl.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangPl)
        );

        ckbLangTh.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangTh)
        );

        ckbLangFr.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangFr)
        );

        ckbLangDe.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangDe)
        );

        ckbLangId.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangId)
        );

        ckbLangTr.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangTr)
        );

        ckbLangBe.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangBe)
        );

        ckbLangUk.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangUk)
        );

        ckbLangEl.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangEl)
        );

        ckbLangLv.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangLv)
        );

        ckbLangCz.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangCz)
        );

        ckbLangRo.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangRo)
        );

        ckbLangNl.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangNl)
        );

        ckbLangMy.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangMy)
        );

        ckbLangSk.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangSk)
        );

        ckbLangAr.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbLangValue(ckbLangAr)
        );

        ckbDisplayBattery.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplayBattery)
        );

        ckbDisplayDate.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplayDate)
        );

        ckbDisplayDistance.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplayDistance)
        );

        ckbDisplaySeconds.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplaySeconds)
        );

        ckbDisplaySteps.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplaySteps)
        );

        ckbDisplayHeart.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplayHeart)
        );

        ckbDisplayCalorie.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplayCalorie)
        );

        ckbDisplayAnimated.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplayAnimated)
        );

        ckbDisplayWeather.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplayWeather)
        );

        ckbDisplayStepsProgress.setOnCheckedChangeListener((buttonView, isChecked) ->
                onChangeCkbDisplayValue(ckbDisplayStepsProgress)
        );

        btnWatchAnalog.setOnClickListener(this::onChangeRdbtnWatchValue
        );

        btnWatchDigital.setOnClickListener(this::onChangeRdbtnWatchValue
        );

        btnWatchBoth.setOnClickListener(this::onChangeRdbtnWatchValue
        );
    }

    private String preFilter() {
        JSONObject jsonObj = new JSONObject();
        for (int i = 0; i < langList.size(); i++) {
            try {
                jsonObj.put("lang_"+(i+1), langList.get(i).get(LANG_TAG));
            } catch (Exception e) {
                Log.d(TAG, "lang filter error");
            }
        }

        for (int i = 0; i < displayList.size(); i++) {
            try {
                jsonObj.put("disp_"+(i+1), displayList.get(i).get(DISPLAY_TAG));
            } catch (Exception e) {
                Log.d(TAG, "display filter error");
            }
        }

        try {
            jsonObj.put("watc_1", watchType);
        } catch (Exception e) {
            Log.d(TAG, "watch filter error");
        }

        return jsonObj.toString();
    }

    private void filter() {
        filterHolder = preFilter();
        ((DisplayNewFragment) getForegroundFragment()).clearAdapter();
        ((DisplayNewFragment) getForegroundFragment()).new getAll(1, orderHolder, filterHolder).executeAsync();
        Toast.makeText(this, R.string.toast_success_with_filter, Toast.LENGTH_SHORT).show();
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
    }

    private void clear() {
        ((DisplayNewFragment) getForegroundFragment()).clearAdapter();
        ((DisplayNewFragment) getForegroundFragment()).new getAll(1, orderHolder, "").executeAsync();
        Toast.makeText(this, R.string.toast_success_with_clear, Toast.LENGTH_SHORT).show();
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();

        //reset
        displayList.clear();
        langList.clear();
        watchType = "digital";
        btnWatchDigital.setTextAppearance(this, R.style.ButtonToggleLeftSelected);
        btnWatchDigital.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_left_selected, getTheme()));
        btnWatchBoth.setTextAppearance(this, R.style.ButtonToggleRight);
        btnWatchBoth.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_right, getTheme()));
        btnWatchAnalog.setTextAppearance(this, R.style.ButtonToggleNoRadius);
        btnWatchAnalog.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_no_radius, getTheme()));
    }

    public void sort(String orderBy) {
        orderHolder = orderBy;
        if(!orderHolder.equals(""))
            btnSort.setImageResource(R.drawable.ic_menu_sort_selected);
        else
            btnSort.setImageResource(R.drawable.ic_menu_sort);

        ((DisplayNewFragment) getForegroundFragment()).clearAdapter();
        ((DisplayNewFragment) getForegroundFragment()).new getAll(1, orderHolder, filterHolder).executeAsync();
    }

    private void onChangeCkbLangValue(CheckBox checkBox) {
        Map<String, String> lang = new HashMap<>();
        if (checkBox.isChecked()) {
            lang.put(LANG_ID, String.valueOf(checkBox.getId()));
            lang.put(LANG_TAG, String.valueOf(checkBox.getTag()));
            langList.add(lang);
        } else
            for (int i = 0; i < langList.size(); i++)
                if (langList.get(i).get(LANG_ID).equals(String.valueOf(checkBox.getId())) && !checkBox.isChecked()) {
                    langList.remove(i);
                    break;
                }
    }

    private void onChangeCkbDisplayValue(CheckBox checkBox) {
        Map<String, String> display = new HashMap<>();
        if(checkBox.isChecked()) {
            display.put(DISPLAY_ID, String.valueOf(checkBox.getId()));
            display.put(DISPLAY_TAG, String.valueOf(checkBox.getTag()));
            displayList.add(display);
        } else
            for (int i = 0; i < displayList.size(); i++)
                if(displayList.get(i).get(DISPLAY_ID).equals(String.valueOf(checkBox.getId())) && !checkBox.isChecked()) {
                    displayList.remove(i);
                    break;
                }
    }

    private void onChangeRdbtnWatchValue(View textView) {
        if(textView.getTag().equals("digital")) {
            btnWatchDigital.setTextAppearance(this, R.style.ButtonToggleLeftSelected);
            btnWatchDigital.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_left_selected, getTheme()));
            btnWatchBoth.setTextAppearance(this, R.style.ButtonToggleRight);
            btnWatchBoth.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_right, getTheme()));
            btnWatchAnalog.setTextAppearance(this, R.style.ButtonToggleNoRadius);
            btnWatchAnalog.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_no_radius, getTheme()));
        }
        else if(textView.getTag().equals("analog")) {
            btnWatchAnalog.setTextAppearance(this, R.style.ButtonToggleNoRadiusSelected);
            btnWatchAnalog.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_no_radius_selected, getTheme()));
            btnWatchBoth.setTextAppearance(this, R.style.ButtonToggleRight);
            btnWatchBoth.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_right, getTheme()));
            btnWatchDigital.setTextAppearance(this, R.style.ButtonToggleLeft);
            btnWatchDigital.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_left, getTheme()));
        }
        else {
            btnWatchAnalog.setTextAppearance(this, R.style.ButtonToggleNoRadius);
            btnWatchAnalog.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_no_radius, getTheme()));
            btnWatchBoth.setTextAppearance(this, R.style.ButtonToggleRightSelected);
            btnWatchBoth.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_right_selected, getTheme()));
            btnWatchDigital.setTextAppearance(this, R.style.ButtonToggleLeft);
            btnWatchDigital.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_left, getTheme()));
        }

        watchType = String.valueOf(textView.getTag());
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

    private Fragment getForegroundFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }
}