package org.soft2u.miband_5_display.ui.activity;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.dialog.FeedbackDialog;
import org.soft2u.miband_5_display.utils.General;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        if(getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_help);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        WebView mWebView = findViewById(R.id.webview_help);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if(new PrefManager(this).getLocale().equals("")) {
                String localeSys = String.valueOf(getResources().getConfiguration().locale).substring(0, 2);
                if (!new General().langApp(localeSys).equals("Unknown") && !new General().langApp(localeSys).equals("English"))
                    mWebView.loadUrl("file:///android_asset/www/help-" + localeSys + ".html");
                else
                    mWebView.loadUrl("file:///android_asset/www/help.html");
            } else {
                String localeReceiver = extras.getString("LOCALE");
                assert localeReceiver != null;
                if (!new General().country(localeReceiver).equals("Unknown") && !new General().country(localeReceiver).equals("English"))
                    mWebView.loadUrl("file:///android_asset/www/help-" + localeReceiver + ".html");
                else
                    mWebView.loadUrl("file:///android_asset/www/help.html");
            }
        }

        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.getSettings().setSupportMultipleWindows(false);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());

        if(new PrefManager(this).getNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(mWebView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
            }
        }

        findViewById(R.id.btn_feedback).setOnClickListener((View view) -> {
            FragmentManager fm4 = getSupportFragmentManager();
            FeedbackDialog dg4 = new FeedbackDialog();
            dg4.show(fm4, "feedback dialog");
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
