package org.soft2u.miband_5_display;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.soft2u.miband_5_display.ui.activity.HelpActivity;
import org.soft2u.miband_5_display.ui.activity.SettingsActivity;
import org.soft2u.miband_5_display.ui.activity.display.DisplaySearchActivity;
import org.soft2u.miband_5_display.ui.dialog.InstallOfflineDialog;
import org.soft2u.miband_5_display.ui.dialog.SupportDevDialog;
import org.soft2u.miband_5_display.ui.fragment.display.DisplayFavoriteFragment;
import org.soft2u.miband_5_display.ui.fragment.display.DisplayFragment;
import org.soft2u.miband_5_display.ui.fragment.display.DisplayHistoryFragment;
import org.soft2u.miband_5_display.ui.fragment.display.DisplayRequestFragment;
import org.soft2u.miband_5_display.utils.General;
import org.soft2u.miband_5_display.utils.Install;
import org.soft2u.miband_5_display.utils.LocaleHelper;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    AppUpdateManager appUpdateManager;
    private DrawerLayout drawerLayout;
    private FrameLayout addViewContainer;

    private boolean exit = false;
    private final String TAG = "MainActivity";
    private final int REQUEST_CODE_UPDATE = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppLanguage();
        setContentView(R.layout.activity_main);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isOpen()) {
                    drawerLayout.closeDrawers();
                } else {
                    if (exit) {
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.toast_exit_app, Toast.LENGTH_SHORT).show();
                        exit = true;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> exit = false, 2000);
                    }
                }
            }
        });
        AppCompatDelegate.setDefaultNightMode(new PrefManager(this).getNightMode());

        appUpdateManager = AppUpdateManagerFactory.create(this);

        addViewContainer = findViewById(R.id.ad_view_container);
        // ads banner bottom + fullscreen
        MobileAds.initialize(this, initializationStatus ->
            loadBanner()
        );

        if(savedInstanceState == null) {
            DisplayFragment defaultFragment = new DisplayFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, defaultFragment).commit();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_menu);
        toolbar.setNavigationOnClickListener(view ->
                drawerLayout.openDrawer(GravityCompat.START)
        );
        setActionBarTitle(getResources().getString(R.string.menu_title_1));

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener((@NonNull MenuItem item) -> {
            getNavItemSelected(item, drawerLayout);
            return true;
        });

        View header = navigationView.getHeaderView(0);
        ImageView btnSettings = header.findViewById(R.id.btn_header_settings);
        ImageView btnHelp = header.findViewById(R.id.btn_header_help);

        btnSettings.setOnClickListener(view -> startActivity(new Intent(this, SettingsActivity.class)));

        btnHelp.setOnClickListener(view -> startActivity(new Intent(this, HelpActivity.class).putExtra("LOCALE", new PrefManager(this).getLocale())));

        findViewById(R.id.tv_menu_donate).setOnClickListener(view -> {
            FragmentManager fm4 = getSupportFragmentManager();
            SupportDevDialog dg4 = new SupportDevDialog();
            dg4.show(fm4, TAG);
        });

        Intent intent = getIntent();
        String action = intent.getAction();

        if(Intent.ACTION_VIEW.equals(action)) {
            String scheme = intent.getScheme();
            ContentResolver resolver = getContentResolver();

            if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                Uri uri = intent.getData();
                if (uri != null) {
                    String name = getContentName(resolver, uri);

                    Log.v(TAG, "Content intent detected: " + action + " : " + uri.getPath() + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
                    // /external_files/MiBand5Display/display1.bin
                    FragmentManager fm3 = getSupportFragmentManager();
                    InstallOfflineDialog dg3 = InstallOfflineDialog.newInstance(uri.getPath());
                    dg3.setCancelable(false);
                    dg3.show(fm3, TAG);
                }
            } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                Uri uri = intent.getData();
                if (uri != null) {
                    String name = uri.getLastPathSegment();

                    Log.v(TAG, "File intent detected: " + action + " : " + uri.getPath() + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);

                    FragmentManager fm3 = getSupportFragmentManager();
                    InstallOfflineDialog dg3 = InstallOfflineDialog.newInstance(uri.getPath());
                    dg3.setCancelable(false);
                    dg3.show(fm3, TAG);
                }
            } else if ("http".equals(scheme)) {
                // TODO Import from HTTP!
            } else if ("ftp".equals(scheme)) {
                // TODO Import from FTP!
            }
        }

        addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.main, menu);
                menu.findItem(R.id.menu_action_search).setVisible(true);
                menu.findItem(R.id.menu_action_remove_all).setVisible(false);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_action_search) {
                    Intent intent = new Intent(MainActivity.this, DisplaySearchActivity.class);
                    startActivity(intent);
                } else if (menuItem.getItemId() == R.id.menu_action_remove_all) {
                    removeAllDisplay();
                }
                return true;
            }
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

    @NonNull
    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        DisplayMetrics outMetrics = getResources().getDisplayMetrics();

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private String getContentName(ContentResolver resolver, Uri uri){
        try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    return cursor.getString(nameIndex);
                }
            }
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.FLEXIBLE,
                            // The current activity making the update request.
                            this,
                            // Include a request code to later monitor this update request.
                            REQUEST_CODE_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "SendIntentException", e);
                }
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                popupSnackBarForCompleteUpdate();
            } else {
                Log.e(TAG, "checkForAppUpdateAvailability: something else");
            }
        });

        // Create a listener to track request state updates.
        InstallStateUpdatedListener listener = state -> {
            // Show module progress, log state, or install the update.
            if (state.installStatus() == InstallStatus.DOWNLOADED){
                popupSnackBarForCompleteUpdate();
            }
        };

        // Before starting an update, register a listener for updates.
        appUpdateManager.registerListener(listener);

        // Start an update.

        // When status updates are no longer needed, unregister the listener.
        appUpdateManager.unregisterListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAppLanguage();
        // Checks that the update is not stalled during 'onResume()'.
        // However, you should execute this check at all app entry points.
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackBarForCompleteUpdate();
                    }
                });
    }

    /* Displays the snackBar notification and call to action. */
    private void popupSnackBarForCompleteUpdate() {
        Snackbar snackbar = Snackbar.make(
                findViewById(R.id.activity_main_layout),
                R.string.msg_update_just_been_downloaded,
                Snackbar.LENGTH_INDEFINITE
        );
        snackbar.setAction(R.string.button_restart, view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.color_primary));
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.toast_request_update_app_canceled, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.toast_update_app_downloading_in_background, Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Update flow failed! Result code: " + resultCode);
            }
        }
    }

    private void replaceFragment(Fragment fragment, String actionBarTitle) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        if(!actionBarTitle.isEmpty())
            setActionBarTitle(actionBarTitle);
    }

    private void getNavItemSelected(MenuItem item, DrawerLayout drawerLayout) {
        int id = item.getItemId();
        if (id == R.id.nav_display) {
            replaceFragment(new DisplayFragment(), getResources().getString(R.string.menu_title_1));
        } else if (id == R.id.nav_resource) {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_font) {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_tool_1) {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_tool_2) {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_history) {
            replaceFragment(new DisplayHistoryFragment(), getResources().getString(R.string.tab_title_5));
        } else if (id == R.id.nav_favorite) {
            replaceFragment(new DisplayFavoriteFragment(), getResources().getString(R.string.tab_title_4));
        } else if (id == R.id.nav_request) {
            replaceFragment(new DisplayRequestFragment(), getResources().getString(R.string.tab_title_6));
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"Great MiBand 4 Display app at: https://play.google.com/store/apps/details?id=" + Install.APPLICATION_ID.value);
            sendIntent.setType("text/plain");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                startActivity(sendIntent);
            } else {
                startActivity(Intent.createChooser(sendIntent, "Share via"));
            }
        } else if (id == R.id.nav_rate) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Install.APPLICATION_ID.value)));
            } catch (ActivityNotFoundException ex) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + Install.APPLICATION_ID.value)));
            }
        }
        drawerLayout.closeDrawers();
        item.setCheckable(true);
        item.setChecked(true);
    }

    public void setActionBarTitle(String title) {
        if(getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    private void setAppLanguage() {
        String localeApp = new PrefManager(this).getLocale();
        String localeSys = String.valueOf(ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0)).substring(0, 2);
        // first time: empty
        if(localeApp.isEmpty())
            localeApp = localeSys;

        LocaleHelper.setLocale(this, localeApp);
    }

    private void removeAllDisplay() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle(R.string.dg_title_delete_all);
        builder1.setMessage(R.string.dg_msg_delete_all);
        builder1.setCancelable(true)
                .setPositiveButton(R.string.msg_alert_yes, (dialog, id) -> {
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if(fragment instanceof DisplayHistoryFragment) {
                        ((DisplayHistoryFragment) fragment).removeAll();
                    } else if(fragment instanceof DisplayRequestFragment) {
                        ((DisplayRequestFragment) fragment).removeAll();
                    }
                })
                .setNegativeButton(R.string.msg_alert_no, (dialog, id) ->
                        dialog.cancel()
                );
        builder1.show();
    }



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}