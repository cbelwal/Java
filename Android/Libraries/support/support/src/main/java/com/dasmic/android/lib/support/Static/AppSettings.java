package com.dasmic.android.lib.support.Static;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Chaitanya Belwal on 3/18/2016.
 */
public class AppSettings {
    public static int getPreferencesInt(final Activity activity,
                                 final String setting){
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(activity);
        return preferences.getInt(setting, 0);
    }

    public static long getPreferencesLong(final Activity activity,
                                        final String setting){
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(activity);
        return preferences.getLong(setting, 0);
    }

    public static void setPreferencesInt(final Activity activity,
                                     final String setting,
                                         int value){
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(setting, value); // value to store
        editor.commit();
    }

    public static void setPreferencesLong(final Activity activity,
                                         final String setting,
                                         long value){
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(setting, value); // value to store
        editor.commit();
    }


}
