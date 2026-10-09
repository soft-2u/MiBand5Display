package org.soft2u.miband_5_display.ui.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;

import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.activity.display.DisplayNewActivity;
import org.soft2u.miband_5_display.ui.fragment.display.DisplayNewFragment;

import java.util.Objects;

public class SortDialog extends DialogFragment {

    private String radioValueChecked = "";
    private Context mContext;
    private FragmentActivity mActivity;

    public static SortDialog newInstance(String order) {
        Bundle args = new Bundle();
        args.putString("ORDER_DATA", order);
        SortDialog f = new SortDialog();
        f.setArguments(args);
        return f;
    }

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_sort, container, false);

        ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_date)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                radioValueChecked = ""; //default
        });

        ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_download)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                radioValueChecked = "BY_DOWNLOAD";
        });

        ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_view)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                radioValueChecked = "BY_VIEW";
        });

        ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_size_asc)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                radioValueChecked = "BY_SIZE_ASC";
        });

        ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_size_desc)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                radioValueChecked = "BY_SIZE_DESC";
        });

        (root.findViewById(R.id.btn_sort)).setOnClickListener((View v) -> {
            dismiss();
            String orderBy = radioValueChecked;
            ((DisplayNewActivity) getActivity()).sort(orderBy); // pass to activity
        });

        String ORDER_DATA_RECEIVER = "";
        if(getArguments() != null)
            ORDER_DATA_RECEIVER = getArguments().getString("ORDER_DATA", "");
        switch (ORDER_DATA_RECEIVER) {
            default:
            case "BY_DATE":
                ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_date)).setChecked(true);
                break;
            case "BY_DOWNLOAD":
                ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_download)).setChecked(true);
                break;
            case "BY_VIEW":
                ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_view)).setChecked(true);
                break;
            case "BY_SIZE_ASC":
                ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_size_asc)).setChecked(true);
                break;
            case "BY_SIZE_DESC":
                ((RadioButton) root.findViewById(R.id.rdbtn_sort_by_size_desc)).setChecked(true);
                break;
        }

        return root;
    }

    private Fragment getForegroundFragment(){
        return mActivity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }
}
