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
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.adapter.DisplayAdapterMini;
import org.soft2u.miband_5_display.adapter.DisplayAdapterSlideshow;
import org.soft2u.miband_5_display.model.ThemeModel;
import org.soft2u.miband_5_display.ui.activity.display.DisplayCategoryActivity;
import org.soft2u.miband_5_display.ui.activity.display.DisplayDetailActivity;
import org.soft2u.miband_5_display.ui.activity.display.DisplayNewActivity;
import org.soft2u.miband_5_display.ui.activity.display.DisplayUpdatedActivity;
import org.soft2u.miband_5_display.utils.AsyncTaskExecutor;
import org.soft2u.miband_5_display.utils.General;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayFragment extends Fragment implements DisplayAdapterMini.ItemClickListener {

    private Context mContext;
    private FragmentActivity mActivity;
    private final String TAG = "DisplayFragment";

    private LinearLayout llPopupContainer;
    private TextView tvMessage;
    private TextView btnReload;

    private RequestQueue requestQueue;
    private DisplayAdapterSlideshow adapter1;
    private DisplayAdapterMini adapter2;
    private final List<ThemeModel> themes1 = new ArrayList<>();
    private final List<ThemeModel> themes2 = new ArrayList<>();

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

        View root = inflater.inflate(R.layout.fragment_display, container, false);
        llPopupContainer = root.findViewById(R.id.ll_popup_container);
        tvMessage = root.findViewById(R.id.tv_message);
        btnReload = root.findViewById(R.id.btn_reload);
        LinearLayout categoryContainer = root.findViewById(R.id.group_layout_category);

        RecyclerView lsDisplayNew = root.findViewById(R.id.list_display_new);
        lsDisplayNew.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));

        RecyclerView lsDisplayUpdate = root.findViewById(R.id.list_display_update);
        lsDisplayUpdate.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));

        new getAll().executeAsync();
        adapter1 = new DisplayAdapterSlideshow(getContext(), themes1);
        adapter2 = new DisplayAdapterMini(getContext(), themes2, lsDisplayUpdate);

        lsDisplayNew.setAdapter(adapter1);
        lsDisplayUpdate.setAdapter(adapter2);
        adapter1.setClickListener((view, position) -> {
            Intent intent = new Intent(mActivity, DisplayNewActivity.class);
            startActivity(intent);
        });

        if(savedInstanceState == null) {
            DisplayRandomFragment defaultFragment = new DisplayRandomFragment();
            getChildFragmentManager().beginTransaction().add(R.id.fragment_container, defaultFragment).commit();
        }

        adapter2.setClickListener(this);

        btnReload.setOnClickListener((View v) ->
                new getAll().executeAsync()
        );

        root.findViewById(R.id.tv_display_update_view_all).setOnClickListener((View v) -> {
            Intent intent = new Intent(mActivity, DisplayUpdatedActivity.class);
            startActivity(intent);
        });

        setCategoryList(categoryContainer);

        return root;
    }

    public class getAll extends AsyncTaskExecutor<String> {
        boolean isConnectTimeOut = false;

        public getAll(){
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
                getNew();
                getUpdate();
            }
        }

        @Override
        protected void onCancelled() {
            // update UI on task cancelled
        }
    }

    private void getNew() {
        String URL = "https://miband4display-server.csm2.duckdns.org/index.php?act=slideshow";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, (String response) -> {
            if (response.equals("[{\"msg\":\"DATA_NOT_FOUND\"}]")) {
                return;
            }
            try {
                JSONArray arr = new JSONArray(response);
                if(arr.length() > 0) {
                    for(int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        int id = obj.getInt("themeid");
                        String title = obj.getString("title");
                        String author = obj.getString("author").substring(2, obj.getString("author").length() - 2); // ["lqlam"] -> lqlam
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
                        theme.setAuthor(author);
                        theme.setDescription(description);
                        theme.setImgUrl(imgUrl);
                        theme.setLanguage(language);
                        theme.setSize(size);
                        theme.setDownload(download);
                        theme.setUpdate(changes);
                        theme.setLangAmount(langAmount);
                        theme.setJson(obj.toString());
                        themes1.add(theme);
//                        adapter.notifyItemInserted(i);
                    }
                    adapter1.notifyDataSetChanged();
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
                params.put("page", String.valueOf(1));
                params.put("order_by", "");
                params.put("filter_by", "");
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

    private void getUpdate() {
        String URL = "https://miband4display-server.csm2.duckdns.org/index.php?act=updated";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, (String response) -> {
            if (response.equals("[{\"msg\":\"DATA_NOT_FOUND\"}]")) {
                return;
            }
            try {
                JSONArray arr = new JSONArray(response);
                if(arr.length() > 0) {
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
                        themes2.add(theme);
//                        adapter.notifyItemInserted(i);
                    }
                    adapter2.notifyDataSetChanged();
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
                params.put("page", String.valueOf(1));
                params.put("order_by", "");
                params.put("filter_by", "");
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

    private void setCategoryList(LinearLayout categoryContainer) {
        String[] categoriesName = new String[] {
                getResources().getString(R.string.category_simple),
                getResources().getString(R.string.category_movie),
                getResources().getString(R.string.category_cute),
                getResources().getString(R.string.category_sports),
                getResources().getString(R.string.category_dark),
                getResources().getString(R.string.category_simpulate),
                getResources().getString(R.string.category_technology)
        };

        int[] categoriesKey = new int[] {
                2,
                5,
                8,
                9,
                11,
                12,
                13
        };

        for (int i = 0; i < categoriesName.length; i++) {
            TextView tvCategory = new TextView(new ContextThemeWrapper(mActivity, R.style.ButtonToggle));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 30, 0);
            tvCategory.setClickable(true);
            tvCategory.setText(categoriesName[i]);
            tvCategory.setTag(categoriesKey[i]);
            tvCategory.setLayoutParams(params);

            tvCategory.setOnClickListener((View view) -> {
                Intent intent = new Intent(mActivity, DisplayCategoryActivity.class);
                intent.putExtra("CTG_ID_SELECTED", view.getTag().toString());
                startActivity(intent);
            });
            categoryContainer.addView(tvCategory);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        String json = ((TextView) view.findViewById(R.id.tv_theme_json)).getText().toString();
        Intent intent = new Intent(mActivity, DisplayDetailActivity.class);
        intent.putExtra("JSON", json);
        startActivity(intent);
    }
}