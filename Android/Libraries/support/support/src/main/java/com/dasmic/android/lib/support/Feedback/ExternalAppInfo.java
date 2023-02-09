package com.dasmic.android.lib.support.Feedback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.dasmic.android.lib.support.R;
import com.dasmic.android.lib.support.Static.AppSettings;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 3/19/2016.
 */
public class ExternalAppInfo {
    public static void ShowExternalAppDownloadDialog(
                                           final Activity activity,
                                           final String title,
                                           final String message,
                                           final String fullPackageName){

        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
        dlgAlert.setTitle(title);
        dlgAlert.setMessage(message);
        dlgAlert.setCancelable(false);

        dlgAlert.setPositiveButton(activity.getString(R.string.button_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SupportFunctions.OpenGooglePlayLink(activity,
                                fullPackageName);
                        dialog.dismiss();
                    }
                });

        dlgAlert.setNegativeButton(
                activity.getResources().getText(R.string.button_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = dlgAlert.create();
        alert.show();
    }
}
