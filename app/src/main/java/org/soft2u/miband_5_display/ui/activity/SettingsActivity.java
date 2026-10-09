package org.soft2u.miband_5_display.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.soft2u.miband_5_display.MainActivity;
import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.fragment.PreferenceFragment;
import org.soft2u.miband_5_display.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private boolean isSetLocale = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppLanguage();
        setContentView(R.layout.activity_settings);

        //If you want to insert data in your settings
        PreferenceFragment preferenceFragment = new PreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_settings, preferenceFragment).commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isSetLocale) {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            this.finishAffinity();
        }
    }

    public void localeChanged() {
        isSetLocale = true;
    }

    private void setAppLanguage() {
        String localeApp = new PrefManager(this).getLocale();
        String localeSys = String.valueOf(getResources().getConfiguration().locale).substring(0, 2);
        // first time: empty
        if(localeApp.equals(""))
            localeApp = localeSys;

        LocaleHelper.setLocale(this, localeApp);

//        Locale locale = new Locale(localeApp);
//
//        Locale.setDefault(locale);
//        Configuration config = getBaseContext().getResources().getConfiguration();
//        config.setLocale(locale);
//        config.setLayoutDirection(locale);
////        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//        getBaseContext().createConfigurationContext(config);
    }

    @Override
    protected void attachBaseContext(Context base) {
        LocaleHelper localeHelper = new LocaleHelper();
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
