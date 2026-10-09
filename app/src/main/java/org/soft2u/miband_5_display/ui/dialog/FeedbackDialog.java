package org.soft2u.miband_5_display.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.utils.General;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedbackDialog extends DialogFragment {

    private Context mContext;
    private FragmentActivity mActivity;
    private RequestQueue requestQueue;

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

    public FeedbackDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_feedback, container, false);
        EditText edtTitle = root.findViewById(R.id.edt_feedback_title);
        EditText edtBody = root.findViewById(R.id.edt_feedback_body);

        (root.findViewById(R.id.btn_send)).setOnClickListener((View view) -> {

            if (!new General().isInternetConnection(mContext)) {
                Toast.makeText(mContext, R.string.toast_notice_no_internet_access, Toast.LENGTH_SHORT).show();
                return;
            }

            String title = edtTitle.getText().toString();
            String body = edtBody.getText().toString();

            if(!body.equals(""))
                new VolleyExcute(title, body).execute();
            else
                Toast.makeText(mContext, R.string.toast_notice_feedback_input_empty, Toast.LENGTH_SHORT).show();
        });

        (root.findViewById(R.id.rl_bkg_trans)).setOnClickListener((View v) ->
                dismiss()
        );

        return root;
    }

    @SuppressLint("StaticFieldLeak")
    private class VolleyExcute extends AsyncTask<String, String, String> {
        boolean isSuccess = false;
        String title, body;

        VolleyExcute(String title, String body) {
            this.title = title;
            this.body = body;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            isSuccess = sendFeedback(title, body);
            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            if(isSuccess)
                Toast.makeText(mContext, R.string.toast_send_feedback_success, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private boolean sendFeedback(String title, String body) {
        RequestFuture<String> future = RequestFuture.newFuture();
        String URL = "https://miband4display-server.csm2.duckdns.org/index.php?act=feedback";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, future, future) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("body", body);
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
            if(response.equals("SUCCESS"))
                return true;
        } catch (InterruptedException | ExecutionException ex) {
            Log.d("FeedbackDialog", ex.toString());
        }
        return false;
    }
}
