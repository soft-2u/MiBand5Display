package org.soft2u.miband_5_display.ui.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
public class AboutDialog extends DialogFragment {

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

    public AboutDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_about, container, false);

        TextView tvAboutAppName, tvAboutContact, tvAboutCredit1, tvAboutCredit2, tvAboutCredit3;
        tvAboutAppName = root.findViewById(R.id.tv_about_app_name);
        tvAboutContact = root.findViewById(R.id.tv_about_contact);
        tvAboutCredit1 = root.findViewById(R.id.tv_about_credit_1);
        tvAboutCredit2 = root.findViewById(R.id.tv_about_credit_2);
        tvAboutCredit3 = root.findViewById(R.id.tv_about_credit_3);

        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = " " + pInfo.versionName;
            tvAboutAppName.append(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String contact = mContext.getResources().getString(R.string.about_content_2);
        tvAboutContact.setText(new General().getSpannedText(contact));
        String credit1 = mContext.getResources().getString(R.string.about_content_3);
        tvAboutCredit1.setText(new General().getSpannedText(credit1));
        String credit2 = mContext.getResources().getString(R.string.about_content_4);
        tvAboutCredit2.setText(new General().getSpannedText(credit2));
        String credit3 = mContext.getResources().getString(R.string.about_content_5);
        tvAboutCredit3.setText(new General().getSpannedText(credit3));

        tvAboutContact.setOnClickListener((View view) -> {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("email copied", tvAboutContact.getText().toString().substring(tvAboutContact.getText().toString().lastIndexOf(":") + 1));
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(getActivity(), R.string.toast_contact_copied, Toast.LENGTH_SHORT).show();
        });

        (root.findViewById(R.id.rl_bkg_trans)).setOnClickListener((View v) ->
                dismiss()
        );

        return root;
    }
}
