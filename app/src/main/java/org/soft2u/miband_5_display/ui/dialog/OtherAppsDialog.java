package org.soft2u.miband_5_display.ui.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.utils.General;

/**
 * A simple {@link Fragment} subclass.
 */
public class OtherAppsDialog extends DialogFragment {

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity){
            mContext = context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    public OtherAppsDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_other_apps, container, false);


        String miband5 = mContext.getResources().getString(R.string.other_app_miband5);
        ((TextView) root.findViewById(R.id.tv_miband5display)).setText(new General().getSpannedText(miband5));

        ((TextView) root.findViewById(R.id.tv_miband5display)).setOnClickListener((View view) -> {
            String appPackageName = "org.x2u.miband5display";
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException ex) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

        ((Button) root.findViewById(R.id.btn_share)).setOnClickListener((View view) -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"Great MiBand 5 Display app at: https://play.google.com/store/apps/details?id=" + "org.x2u.miband5display");
            sendIntent.setType("text/plain");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                startActivity(sendIntent);
            } else {
                startActivity(Intent.createChooser(sendIntent, "Share via"));
            }
        });

        ((Button) root.findViewById(R.id.btn_copy_link)).setOnClickListener((View view) -> {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("link copied", "https://play.google.com/store/apps/details?id=" + "org.x2u.miband5display");
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(getActivity(), R.string.toast_link_copied, Toast.LENGTH_SHORT).show();
        });

        (root.findViewById(R.id.rl_bkg_trans)).setOnClickListener((View v) ->
                dismiss()
        );

        return root;
    }
}
