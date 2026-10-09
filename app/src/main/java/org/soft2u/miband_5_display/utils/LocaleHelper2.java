package org.soft2u.miband_5_display.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper2 {

    private String SELECTED_LANGUAGE = "Locale.Helper.Selected.getLocale";

    public Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    public ContextWrapper onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    private String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    private ContextWrapper setLocale(Context context, String language) {
        persist(context, language);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            return updateResources(context, language);
//        }
//
//        return updateResourcesLegacy(context, language);
        return wrap(context, language);
    }

    private String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    public static ContextWrapper wrap(Context context, String language) {
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();
        Locale newLocale = new Locale(language);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            configuration.setLocale(newLocale);
//            LocaleList locale = new LocaleList(newLocale);
//            LocaleList.setDefault(locale);
//            configuration.setLocales(locale);
//            context = context.createConfigurationContext(configuration);
//
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            configuration.setLocale(newLocale);
//            context = context.createConfigurationContext(configuration);
//
//        } else {
//            configuration.locale = newLocale;
//            res.updateConfiguration(configuration, res.getDisplayMetrics());
//        }

        configuration.setLocale(newLocale);
        context = context.createConfigurationContext(configuration);
        configuration.locale = newLocale;
        res.updateConfiguration(configuration, res.getDisplayMetrics());
        return new ContextWrapper(context);
    }
}