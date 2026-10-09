package org.soft2u.miband_5_display.ui.dialog;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.utils.Install;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestRateDialog extends DialogFragment {


    public RequestRateDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_request_rate, container, false);

        (root.findViewById(R.id.btn_close)).setOnClickListener((View v) -> {
            new PrefManager(getActivity()).setStatusShowRateRequest(false);
            dismiss();
        });

        (root.findViewById(R.id.btn_rate)).setOnClickListener((View v) -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Install.APPLICATION_ID.value)));
            } catch (android.content.ActivityNotFoundException ex) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + Install.APPLICATION_ID.value)));
            }
        });

        return root;
    }
}
