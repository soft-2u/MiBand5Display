package org.soft2u.miband_5_display.ui.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.activity.SettingsActivity;
import org.soft2u.miband_5_display.utils.General;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class LanguageAppDialog extends DialogFragment {

    private Context mContext;
    private FragmentActivity mActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity){
            mContext = context;
            mActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    public LanguageAppDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_language_app, container, false);
        ListView lvLanguage = root.findViewById(R.id.lv_language);
        String[] values = new String[] {
                getResources().getString(R.string.app_lang_en),
                getResources().getString(R.string.app_lang_vi),
                getResources().getString(R.string.app_lang_ja),
                getResources().getString(R.string.app_lang_ko),
                getResources().getString(R.string.app_lang_zh),
                getResources().getString(R.string.app_lang_ru),
                getResources().getString(R.string.app_lang_de),
                getResources().getString(R.string.app_lang_es),
                getResources().getString(R.string.app_lang_pt),
                getResources().getString(R.string.app_lang_pl),
                getResources().getString(R.string.app_lang_tr),
                getResources().getString(R.string.app_lang_it)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        lvLanguage.setAdapter(adapter);

        lvLanguage.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            String value = adapter.getItem(position);
            if(value != null) {
                new PrefManager(mContext).setLocale(new General().langCode(value));
                // Toast.makeText(mContext, R.string.toast_success_with_language_change, Toast.LENGTH_SHORT).show();
                ((SettingsActivity) mActivity).localeChanged();
//                requireActivity().recreate();
                dismiss();
            }
        });

        (root.findViewById(R.id.rl_bkg_trans)).setOnClickListener((View v) ->
                dismiss()
        );

        (root.findViewById(R.id.btn_cancel)).setOnClickListener((View v) ->
                dismiss()
        );

        return root;
    }
}
