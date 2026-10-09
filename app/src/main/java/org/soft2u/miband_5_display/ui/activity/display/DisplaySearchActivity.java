package org.soft2u.miband_5_display.ui.activity.display;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.soft2u.miband_5_display.MainActivity;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.fragment.display.DisplaySearchFragment;
import org.soft2u.miband_5_display.utils.FlowLayout;
import org.soft2u.miband_5_display.utils.General;

import java.util.Arrays;
import java.util.Objects;

public class DisplaySearchActivity extends AppCompatActivity {

    private final String TAG = "DisplaySearchActivity";

    private EditText edtSearch;
    private TabLayout tabLayout;
    private FrameLayout addViewContainer;

    boolean isTabLayoutChanged = false, isFirstSearch = true;
    String tabSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_search);

        if(savedInstanceState == null) {
            DisplaySearchFragment fragment = new DisplaySearchFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment);
        }

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.label_title));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.label_author));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.label_color));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadSuggestTitle(findViewById(R.id.flowlayout_container));
                        edtSearch.setHint(R.string.edt_search_title);
                        edtSearch.setEnabled(true);
                        tabSelected = "title";
                        break;
                    case 1:
                        loadSuggestAuthor(findViewById(R.id.flowlayout_container));
                        edtSearch.setHint(R.string.edt_search_author);
                        edtSearch.setEnabled(true);
                        tabSelected = "author";
                        break;
                    case 2:
                        loadSuggestColor(findViewById(R.id.flowlayout_container));
                        edtSearch.setHint(R.string.edt_search_color);
                        edtSearch.setEnabled(false);
                        tabSelected = "color";
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        edtSearch = findViewById(R.id.edt_search);

        addViewContainer = findViewById(R.id.ad_view_container);
        // ads banner bottom
        MobileAds.initialize(this, (InitializationStatus initializationStatus) ->
                loadBanner()
        );

        findViewById(R.id.btn_back).setOnClickListener((View v) ->
            finish()
        );

        findViewById(R.id.btn_home).setOnClickListener((View v) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |  Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        edtSearch.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if(hasFocus) {
                findViewById(R.id.btn_back).setVisibility(View.GONE);
                findViewById(R.id.btn_home).setVisibility(View.GONE);
                findViewById(R.id.btn_cancel_search).setVisibility(View.VISIBLE);
                findViewById(R.id.cl_search_suggest).setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                lp.weight = 1f;
                lp.leftMargin = 35;
                lp.rightMargin = 15;
                lp.gravity = Gravity.CENTER;
                edtSearch.setLayoutParams(lp);
                edtSearch.setTextColor(getResources().getColor(R.color.text_primary));
                if(Objects.requireNonNull(tabLayout.getTabAt(0)).isSelected())
                    edtSearch.setHint(R.string.edt_search_title);
                else if(Objects.requireNonNull(tabLayout.getTabAt(1)).isSelected())
                    edtSearch.setHint(R.string.edt_search_author);
                else {
                    edtSearch.setHint(R.string.edt_search_color);
                    edtSearch.setEnabled(false);
                }

                if(!isTabLayoutChanged) {
                    // init first launch
                    loadSuggestTitle(findViewById(R.id.flowlayout_container));
                    tabSelected = "title";
                    isTabLayoutChanged = true;
                }
            }
        });

        findViewById(R.id.btn_cancel_search).setOnClickListener((View v) -> btnCancelClick());

        edtSearch.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED)
            {
                String content;
                if(!tabSelected.equals("color"))
                    content = edtSearch.getText().toString();
                else
                    content = String.valueOf(getIdColor(edtSearch.getText().toString())); // get Id of color, not name
                // start search (by ime)
                btnSearchClick(tabSelected, content);
                btnCancelClick();
            }
            return false;
        });
    }

    private void btnSearchClick(String field, String content) {
        if(isFirstSearch) {
            String json = "{\""+field+"\":\""+content+"\",\"note\":\"\"}";
            Bundle extra = new Bundle();
            extra.putString("JSON", json);
            DisplaySearchFragment fragment = new DisplaySearchFragment();
            fragment.setArguments(extra);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
            isFirstSearch = false;
        }
        else {
            ((DisplaySearchFragment) getForegroundFragment()).clearAdapter();
            ((DisplaySearchFragment) getForegroundFragment()).new getAll(1, field, content).executeAsync();
        }
    }

    private void btnCancelClick() {
        findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_home).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_cancel_search).setVisibility(View.GONE);
        findViewById(R.id.cl_search_suggest).setVisibility(View.GONE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight =  8f;
        lp.leftMargin = 15;
        lp.rightMargin = 15;
        lp.gravity = Gravity.CENTER;
        edtSearch.setLayoutParams(lp);
        edtSearch.setTextColor(getResources().getColor(R.color.text_secondary));
        edtSearch.setEnabled(true);

        edtSearch.clearFocus();
        findViewById(R.id.toolbar).requestFocus(); // fix clear focus API < 21

        hideSoftKeyboard();
    }

    private void loadSuggestTitle(FlowLayout flowLayout) {
        String[] values = new String[] {
            "Pokemon",
            "Marvel",
            "Dragon Ball",
            "One Piece",
            "Adidas",
            "Nike",
            "anime",
            "cute",
            "walking",
            "space",
            "casio",
            "sport",
            "rainbow",
            "light",
            "digital font",
            "big font",
            "dark"
        };

        flowLayout.removeAllViewsInLayout();

        for (String value : values) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.ButtonToggle));
            textView.setText(value);
            textView.setOnClickListener(tvItem1Click);
            flowLayout.addView(textView);
        }
    }

    private void loadSuggestAuthor(FlowLayout flowLayout) {
        String[] values = new String[] {
            "filipemcalmeida",
            "Artark",
            "Avone",
            "+M4rc0",
            ".Kosmik",
            "EiphThe",
            "Alixinhzai",
            "adamkhoa253",
            "joaocleberps",
            "小洪昵称",
            "小太子",
            "贪睡De猪",
            "shymik",
            "mifit",
            "lqlam"
        };

        flowLayout.removeAllViewsInLayout();

        for (String value : values) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.ButtonToggle));
            textView.setText(value);
            textView.setOnClickListener(tvItem1Click);
            flowLayout.addView(textView);
        }
    }

    private void loadSuggestColor(FlowLayout flowLayout) {
        String[] values = new String[] {
                "multi",
                "black",
                "white",
                "brown",
                "gray",
                "cobalt",
                "teal",
                "cyan",
                "turquoise",
                "light_blue",
                "red",
                "light_coral",
                "green",
                "lime",
                "sage",
                "mint",
                "artichoke",
                "orange",
                "amber",
                "yellow",
                "banana",
                "pink",
                "indigo",
                "magenta"
        };

        flowLayout.removeAllViewsInLayout();

        for (String value : values) {
            AppCompatTextView textView = new AppCompatTextView(new ContextThemeWrapper(this, R.style.ButtonToggle));
            textView.setText(value);
            textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getIdentifier("ic_circle_"+value, "drawable", this.getPackageName()),0,0,0);
            textView.setCompoundDrawablePadding(5);
            textView.setOnClickListener(tvItem2Click);
            flowLayout.addView(textView);
        }
    }

    private final TextView.OnClickListener tvItem1Click = (View v) -> {
        edtSearch.setText(((TextView) v).getText());

        // start search
        btnSearchClick(tabSelected, edtSearch.getText().toString());
        btnCancelClick();
    };

    private final TextView.OnClickListener tvItem2Click = (View v) -> {
        edtSearch.setText(((TextView) v).getText());

        // start search
        btnSearchClick(tabSelected, String.valueOf(getIdColor(edtSearch.getText().toString())));
        btnCancelClick();
    };

    private Fragment getForegroundFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private int getIdColor(String colorName) {
        switch (colorName) {
            case "cyan":
            default:
                return 1;
            case "red":
                return 2;
            case "green":
                return 3;
            case "orange":
                return 4;
            case "pink":
                return 5;
            case "gray":
                return 6;
            case "lime":
                return 7;
            case "cobalt":
                return 8;
            case "teal":
                return 9;
            case "indigo":
                return 10;
            case "amber":
                return 11;
            case "brown":
                return 12;
            case "magenta":
                return 13;
            case "yellow":
                return 14;
            case "white":
                return 15;
            case "turquoise":
                return 16;
            case "black":
                return 17;
            case "multi":
                return 18;
            case "light_blue":
                return 19;
            case "sage":
                return 20;
            case "mint":
                return 21;
            case "banana":
                return 22;
            case "light_coral":
                return 23;
            case "artichoke":
                return 24;
        }
    }

    private void readJsonData(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            // eg. ["author":"lqlam","note":"Tac gia"]
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

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE
        );
        imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
    }
}