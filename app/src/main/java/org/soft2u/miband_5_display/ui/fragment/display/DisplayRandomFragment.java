package org.soft2u.miband_5_display.ui.fragment.display;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.soft2u.miband_5_display.MainActivity;
import org.soft2u.miband_5_display.OnLoadMoreListener;
import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.activity.display.DisplayDetailActivity;
import org.soft2u.miband_5_display.adapter.DisplayAdapter;
import org.soft2u.miband_5_display.model.ThemeModel;
import org.soft2u.miband_5_display.ui.GridAutofitLayoutManager;
import org.soft2u.miband_5_display.utils.AsyncTaskExecutor;
import org.soft2u.miband_5_display.utils.General;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DisplayRandomFragment extends Fragment implements DisplayAdapter.ItemClickListener {

    private Context mContext;
    private FragmentActivity mActivity;
    private final String TAG = "DisplayRandomFragment";

    private RecyclerView gridTheme;
    private LinearLayout llPopupContainer;
    private TextView tvMessage;
    private TextView btnReload;

    private RequestQueue requestQueue;
    private DisplayAdapter adapter;
    private final List<ThemeModel> themes = new ArrayList<>();
    private int min = 1;
    private boolean isFirstRun = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity){
            mActivity = (FragmentActivity) context;
            mContext = context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        mContext = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(mActivity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_display_random, container, false);
        llPopupContainer = root.findViewById(R.id.ll_popup_container);
        tvMessage = root.findViewById(R.id.tv_message);
        btnReload = root.findViewById(R.id.btn_reload);
        gridTheme = root.findViewById(R.id.grid_theme);

        gridTheme.setLayoutManager(new GridAutofitLayoutManager(mContext,160));
        gridTheme.setItemAnimator(null);

        new getAll(min).executeAsync();
        adapter = new DisplayAdapter(getContext(), themes, gridTheme);

        OnLoadMoreListener onLoadMoreListener = () -> {
            // min = 0 = end. no more data
            if(min != 0) {
                if (themes.size() % 12 == 0) {
                    // 1.add progress item
                    themes.add(null);
                    gridTheme.post(() -> adapter.notifyItemInserted(themes.size() - 1));
                    new getAll(min).executeAsync();
                }
            }
        };

        gridTheme.setAdapter(adapter);
        adapter.setOnLoadMoreListener(onLoadMoreListener);
        adapter.setClickListener(this);

        btnReload.setOnClickListener((View v) ->
                new getAll(min).executeAsync()
        );

        return root;
    }

    public class getAll extends AsyncTaskExecutor<String> {
        int min;
        boolean isConnectTimeOut = false;

        public getAll(int min){
            this.min = min;
        }

        @Override
        protected void onPreExecute() {
            if (!new General().isInternetConnection(mContext)) {
                llPopupContainer.setVisibility(View.VISIBLE);
                tvMessage.setText(R.string.toast_notice_no_internet_access);
                btnReload.setVisibility(View.VISIBLE);
                return;
            }
            super.onPreExecute();
        }

        boolean isConnect = true;
        long currentTime;
        @Override
        protected Void doInBackground(String... params) {
            isConnect = new General().isInternetConnection(mContext);
            currentTime = System.currentTimeMillis();
            while(!isConnect) {
                isConnect = new General().isInternetConnection(mContext);
                if(isConnect)
                    break;
                if(currentTime + 15000 == System.currentTimeMillis()) {
                    // 15s timeout
                    isConnectTimeOut = true;
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute() {
            if(isConnectTimeOut) {
                llPopupContainer.setVisibility(View.VISIBLE);
                tvMessage.setText(R.string.toast_notice_connection_timed_out);
                btnReload.setVisibility(View.VISIBLE);
            } else {
                llPopupContainer.setVisibility(View.GONE);
                getAll(min);
            }
        }

        @Override
        protected void onCancelled() {
            // update UI on task cancelled
        }
    }

    private void getAll(int min) {
        String themeIdObject = getRandom(min);
        String URL = "https://miband4display-server.csm2.duckdns.org/index.php?act=random";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, (String response) -> {
            if (response.equals("[{\"msg\":\"DATA_NOT_FOUND\"}]")) {
                if(isFirstRun) {
                    llPopupContainer.setVisibility(View.VISIBLE);
                    btnReload.setVisibility(View.GONE);
                    tvMessage.setText(R.string.msg_list_empty);
                } else {
                    this.min = 0; // end. no more data
                    // 2.remove progress item
                    themes.remove(themes.size() - 1);
                    adapter.notifyDataSetChanged();
                    adapter.setLoaded();
                }
                return;
            }
            try {
                JSONArray arr = new JSONArray(response);
                if(arr.length() > 0) {
                    if(!isFirstRun) {
                        // 2.remove progress item
                        themes.remove(themes.size() - 1);
                    }
                    for(int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        int id = obj.getInt("themeid");
                        String title = obj.getString("title");
                        String description = obj.getString("description");
                        String imgUrl = "https://miband4display-server.csm2.duckdns.org/public/files/display/cover/" + obj.getString("img_url");
                        String language = obj.getString("language");
                        String changes = obj.getString("changes");
                        int langAmount = obj.getString("language").split(",").length;
                        float size = Float.parseFloat(obj.getString("size"));
                        int download = Integer.parseInt(obj.getString("download"));

                        ThemeModel theme = new ThemeModel();
                        theme.setId(id);
                        theme.setTitle(title);
                        theme.setDescription(description);
                        theme.setImgUrl(imgUrl);
                        theme.setLanguage(language);
                        theme.setSize(size);
                        theme.setDownload(download);
                        theme.setUpdate(changes);
                        theme.setLangAmount(langAmount);
                        theme.setJson(obj.toString());
                        themes.add(theme);
//                        adapter.notifyItemInserted(i);
                    }
                    this.min++;
                    adapter.notifyDataSetChanged();
                    adapter.setLoaded();
                    isFirstRun = false;
                } else {
                    Log.d(TAG,"No data found");
                }
            } catch (JSONException e) {
                Log.d(TAG,"cannot parse json string. " + e.toString());
                llPopupContainer.setVisibility(View.VISIBLE);
                btnReload.setVisibility(View.VISIBLE);
                tvMessage.setText(R.string.toast_error_there_was_a_problem);
            }}, (VolleyError error) -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                Log.e(TAG, "TimeoutError or NoConnectionError");
            } else if (error instanceof AuthFailureError) {
                Log.e(TAG, "AuthFailureError");
            } else if (error instanceof ServerError) {
                llPopupContainer.setVisibility(View.VISIBLE);
                btnReload.setVisibility(View.VISIBLE);
                tvMessage.setText(R.string.msg_server_cannot_access);
                Log.d(TAG,"Server error or return a invalid_json or html. " + error.toString());
            } else if (error instanceof NetworkError) {
                Log.e(TAG, "NetworkError");
            } else if (error instanceof ParseError) {
                Log.e(TAG, "ParseError");
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("theme_id_array", themeIdObject);
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
    }

    @Override
    public void onItemClick(View view, int position) {
        if(llPopupContainer.getVisibility() == View.VISIBLE)
            return;

        String json = ((TextView) view.findViewById(R.id.tv_theme_json)).getText().toString();
        Intent intent = new Intent(mActivity, DisplayDetailActivity.class);
        intent.putExtra("JSON", json);
        startActivity(intent);
    }

    private String getRandom(int min) {
        int mainNum = (int) Math.floor(Math.random() * (1600 - min) + min);
        List<Integer> pgArray = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            pgArray.add(mainNum + i);
        }
        return pgArray.toString();
    }
}