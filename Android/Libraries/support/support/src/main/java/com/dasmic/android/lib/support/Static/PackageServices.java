package com.dasmic.android.lib.support.Static;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Chaitanya Belwal on 2/18/2016.
 */
public class PackageServices {
    public static String getGooglePlayWebLink(final Activity activity){
        String value= getGooglePlayWebLink(getFullPackageName(activity));
        return value;
    }

    public static String getGooglePlayWebLink(final String fullPackageName){
        String value= "https://play.google.com/store/apps/details?id="+
                fullPackageName;
        return value;
    }

    public static String getGooglePlayLink(final Activity activity){
        String value= getGooglePlayLink(getFullPackageName(activity));
        return value;
    }

    public static String getGooglePlayLink(final String fullPackageName){
        String value= "market://details?id=" +
                fullPackageName;
        return value;
    }

    public static String getFullPackageName(Activity activity){
        String value= activity.getApplicationContext().getPackageName();
        return value;
    }

    public static String getSimpleAppName(Activity activity){
        String value="";
        try {
            PackageManager pm = activity.
                    getApplicationContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getFullPackageName(activity), 0);
            value = pi.applicationInfo.loadLabel(pm).toString();
        }
        catch(PackageManager.NameNotFoundException ex){

        }
        catch(Exception ex){

        }
        return value;
    }


}
