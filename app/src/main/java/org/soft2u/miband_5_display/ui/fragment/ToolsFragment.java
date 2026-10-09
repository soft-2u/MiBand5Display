package org.soft2u.miband_5_display.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.dialog.InstallOfflineDialog;

public class ToolsFragment extends Fragment {

    private Context mContext;
    private FragmentActivity mActivity;

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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        if(getActivity() != null) {
//            ((MainActivity) getActivity()).hideMenuItem(R.id.menu_action_search);
//            ((MainActivity) getActivity()).hideMenuItem(R.id.menu_action_sort);
//            ((MainActivity) getActivity()).hideMenuItem(R.id.menu_action_filter);
//        }

        View root = inflater.inflate(R.layout.fragment_tools, container, false);

        (root.findViewById(R.id.btn_install_offline)).setOnClickListener((View view) -> {
            FragmentManager fm = mActivity.getSupportFragmentManager();
            InstallOfflineDialog dg = new InstallOfflineDialog();
            dg.setCancelable(false);
            dg.show(fm, "");
        });

        (root.findViewById(R.id.btn_create_your_own)).setOnClickListener((View view) -> {
//            Intent intent = new Intent(mActivity, DIYActivity.class);
//            startActivity(intent);
            Toast.makeText(mContext, "I'm developing... please wait!", Toast.LENGTH_SHORT).show();
        });

        return root;
    }
}