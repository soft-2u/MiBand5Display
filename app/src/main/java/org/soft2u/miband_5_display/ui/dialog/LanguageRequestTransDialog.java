package org.soft2u.miband_5_display.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.database.AppDatabase;
import org.soft2u.miband_5_display.entity.ThemeRequest;
import org.soft2u.miband_5_display.utils.General;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 */
public class LanguageRequestTransDialog extends DialogFragment {

    private Context mContext;
    private FragmentActivity mActivity;
    private RequestQueue requestQueue;
    private int THEME_ID_RECEIVER;
    private String USER_ID;
    private final String TAG = "LanguageRequestTransDg";

    private RewardedAd mRewardedAd;
    private ProgressBar progressBar;

    public LanguageRequestTransDialog() {
        // Required empty public constructor
    }

    public static LanguageRequestTransDialog newInstance(int themeId) {
        Bundle args = new Bundle();
        args.putInt("THEME_ID", themeId);
        LanguageRequestTransDialog f = new LanguageRequestTransDialog();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity){
            mContext = context;
            mActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(mActivity);
        if(getArguments() != null)
            THEME_ID_RECEIVER = getArguments().getInt("THEME_ID", 1);

        MobileAds.initialize(mActivity, initializationStatus ->
                loadRewardedAd()
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_language_request_trans, container, false);
        progressBar = root.findViewById(R.id.progress_bar);
        ListView lvLanguage = root.findViewById(R.id.lv_language);
        String[] values = new String[] {
                getResources().getString(R.string.checkbox_filter_by_lang_en),
                getResources().getString(R.string.checkbox_filter_by_lang_vi),
                getResources().getString(R.string.checkbox_filter_by_lang_ko),
                getResources().getString(R.string.checkbox_filter_by_lang_ja),
                getResources().getString(R.string.checkbox_filter_by_lang_zh),
                getResources().getString(R.string.checkbox_filter_by_lang_ru),
                getResources().getString(R.string.checkbox_filter_by_lang_it),
                getResources().getString(R.string.checkbox_filter_by_lang_de),
                getResources().getString(R.string.checkbox_filter_by_lang_es),
                getResources().getString(R.string.checkbox_filter_by_lang_tr),
                getResources().getString(R.string.checkbox_filter_by_lang_pt),
                getResources().getString(R.string.checkbox_filter_by_lang_pl),
                getResources().getString(R.string.checkbox_filter_by_lang_th),
                getResources().getString(R.string.checkbox_filter_by_lang_fr),
                getResources().getString(R.string.checkbox_filter_by_lang_hu),
                getResources().getString(R.string.checkbox_filter_by_lang_id),
                getResources().getString(R.string.checkbox_filter_by_lang_be),
                getResources().getString(R.string.checkbox_filter_by_lang_uk),
                getResources().getString(R.string.checkbox_filter_by_lang_el),
                getResources().getString(R.string.checkbox_filter_by_lang_lv),
                getResources().getString(R.string.checkbox_filter_by_lang_cz),
                getResources().getString(R.string.checkbox_filter_by_lang_ro),
                getResources().getString(R.string.checkbox_filter_by_lang_nl),
                getResources().getString(R.string.checkbox_filter_by_lang_my),
                getResources().getString(R.string.checkbox_filter_by_lang_sk),
                getResources().getString(R.string.checkbox_filter_by_lang_ar),
                getResources().getString(R.string.checkbox_filter_by_lang_ca)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        lvLanguage.setAdapter(adapter);

        if(new PrefManager(mActivity).getUserId().isEmpty()) {
            USER_ID = UUID.randomUUID().toString();
            new PrefManager(mActivity).setUserId(USER_ID);
        } else {
            USER_ID = new PrefManager(mActivity).getUserId();
        }

        lvLanguage.setOnItemClickListener((adapterView, view, position, l) -> {
            String value = adapter.getItem(position);
            if(value != null) {
                if (!new General().isInternetConnection(mContext)) {
                    Toast.makeText(mContext, R.string.toast_notice_no_internet_access, Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder1 = new AlertDialog.Builder(mActivity);
                builder1.setTitle(R.string.dg_title_request_more);
                builder1.setMessage(R.string.dg_msg_request_more);
                builder1.setCancelable(true)
                        .setPositiveButton(R.string.msg_alert_yes, (dialog, id) ->
                            executeRequest(value)
                        )
                        .setNegativeButton(R.string.msg_alert_no, (dialog, id) ->
                            dialog.cancel()
                        );
                builder1.show();
            }
        });

        (root.findViewById(R.id.rl_bkg_trans)).setOnClickListener(v ->
                dismiss()
        );

        return root;
    }

    private void loadRewardedAd() {
        RequestConfiguration requestConfiguration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(
                "7B21EEA5F5F327CF30F5F208575D0FCD",
                AdRequest.DEVICE_ID_EMULATOR)).build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        String adRewardedUnitId;
        if(General.isDebuggable(mContext))
            adRewardedUnitId = getResources().getString(R.string.ad_rewarded_unit_id_test);
        else
            adRewardedUnitId = getResources().getString(R.string.ad_rewarded_unit_id);
        RewardedAd.load(mContext, adRewardedUnitId, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.d(TAG, "Ads failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ads showed.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ads was dismissed.");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mRewardedAd = null;
            }
        });
    }

    private void executeRequest(String requestContent) {
        progressBar.setVisibility(View.VISIBLE);
        if(mRewardedAd != null)
            Toast.makeText(mContext, R.string.toast_loading_ad, Toast.LENGTH_SHORT).show();
            
        new Thread(() -> {
            int waited = 0;
            while (mRewardedAd == null && waited < 10000) {
                try {
                    Thread.sleep(500);
                    waited += 500;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            if (mActivity != null) {
                mActivity.runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (mRewardedAd != null) {
                        mRewardedAd.show(mActivity, rewardItem -> {
                            Log.d(TAG, "The user earned the reward.");
                            executeSecondRequest(THEME_ID_RECEIVER, requestContent, USER_ID);
                        });
                        loadRewardedAd();
                    } else {
                        Log.d(TAG, "The rewarded ads wasn't ready yet.");
                        Toast.makeText(mContext, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    
    private void executeSecondRequest(int themeId, String requestContent, String userId) {
        new Thread(() -> {
            boolean isSuccess = sendRequest(themeId, requestContent, userId);
            if (mActivity != null) {
                mActivity.runOnUiThread(() -> {
                    if(isSuccess) {
                        saveRequest(themeId, requestContent, userId);
                    }
                    else
                        Toast.makeText(mContext, R.string.toast_send_request_fail, Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
        }).start();
    }

    private void saveRequest(int themeId, String requestContent, String userId) {
        ThemeRequest themeRequest = new ThemeRequest();
        themeRequest.setThemeid(themeId);
        themeRequest.setDate_add(new Date());
        themeRequest.setRequest_content(requestContent);
        themeRequest.setRequest_id(userId);
        themeRequest.setRequest_status_code(0);
        AppDatabase.getAppDatabase(mContext).ThemeRequestDAO().insert(themeRequest);
        Toast.makeText(mContext, R.string.toast_add_to_request_success, Toast.LENGTH_SHORT).show();
    }

    private boolean sendRequest(int themeId, String requestContent, String userId) {
        RequestFuture<String> future = RequestFuture.newFuture();
        String URL = "https://miband4display-server.csm2.duckdns.org/index.php?act=requesttrans&job=send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, future, future) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("theme_id", String.valueOf(themeId));
                params.put("request_content", requestContent);
                params.put("user_id", userId);
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

        try {
            String response = future.get(); // this line will block
            Log.d(TAG, response);
            if(response.equals("SUCCESS"))
                return true;
        } catch (InterruptedException | ExecutionException ex) {
            Log.d(TAG, ex.toString());
        }
        return false;
    }
}
