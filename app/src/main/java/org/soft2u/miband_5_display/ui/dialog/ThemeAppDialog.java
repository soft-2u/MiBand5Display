package org.soft2u.miband_5_display.ui.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemeAppDialog extends DialogFragment {

    private FragmentActivity mActivity;
    private int mode;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity){
            mActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public ThemeAppDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_theme_app, container, false);

        ((RadioButton) root.findViewById(R.id.rdbtn_theme_app_auto)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
        });

        ((RadioButton) root.findViewById(R.id.rdbtn_theme_app_light)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            }
        });

        ((RadioButton) root.findViewById(R.id.rdbtn_theme_app_dark)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            }
        });

        (root.findViewById(R.id.rl_bkg_trans)).setOnClickListener((View v) ->
            dismiss()
        );

        (root.findViewById(R.id.btn_cancel)).setOnClickListener((View v) ->
            dismiss()
        );

        (root.findViewById(R.id.btn_ok)).setOnClickListener((View v) -> {
            setTheme(mode);
            dismiss();
        });

        switch (new PrefManager(mActivity).getNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                ((RadioButton) root.findViewById(R.id.rdbtn_theme_app_auto)).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                ((RadioButton) root.findViewById(R.id.rdbtn_theme_app_light)).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                ((RadioButton) root.findViewById(R.id.rdbtn_theme_app_dark)).setChecked(true);
                break;
            default:
                break;
        }

        return root;
    }

    private void saveNightMode(int mode) {
        new PrefManager(mActivity).setNightMode(mode);
    }

    private void setTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
        saveNightMode(mode);
//        requireActivity().recreate();
    }
}
