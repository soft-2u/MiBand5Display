package org.soft2u.miband_5_display.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.utils.Install;

/**
 * A simple {@link Fragment} subclass.
 */
public class InstallMethodDialog extends DialogFragment {

    private FragmentActivity mActivity;
    private String installMethodChoice = "";
    private String EXPORT_TREE;
    private CheckBox ckbRememberMethod;

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
        mActivity = null;
    }

    public InstallMethodDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            assert getDialog().getWindow() != null;
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_install_method, container, false);
        Button btnInstallMethod1 = root.findViewById(R.id.button_install_method_1);
        Button btnInstallMethod2  = root.findViewById(R.id.button_install_method_2);
        ckbRememberMethod = root.findViewById(R.id.ckb_remember_install_method);

//        if (Build.VERSION.SDK_INT >= 30) {
//            btnInstallMethod2.setVisibility(View.GONE);
//        }

        btnInstallMethod1.setOnClickListener((View view) -> {
            if (ckbRememberMethod.isChecked())
                new PrefManager(mActivity).setRememberMethodInstall(Install.METHOD_BLUETOOTH.value);
            installMethodChoice = Install.METHOD_BLUETOOTH.value;
            dismiss();
        });

        btnInstallMethod2.setOnClickListener((View view) -> {
            if(ckbRememberMethod.isChecked())
                new PrefManager(mActivity).setRememberMethodInstall(Install.METHOD_MIFIT.value);
            installMethodChoice = Install.METHOD_MIFIT.value;
            dismiss();
        });

        (root.findViewById(R.id.rl_bkg_trans)).setOnClickListener((View v) ->
                dismiss()
        );

        return root;
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Intent intent = new Intent().putExtra("INSTALL_METHOD_CHOICE", installMethodChoice);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}
