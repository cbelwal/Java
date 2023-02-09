package com.dasmic.android.lib.support.Feedback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.dasmic.android.lib.support.R;
import com.dasmic.android.lib.support.Static.AppSettings;
import com.dasmic.android.lib.support.Static.PackageServices;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 1/18/2016.
 */
public class RateThisApp {
    private static final String rate_this_app="rate_this_app";

    public static void ShowRateThisApp(final Activity activity)
    {
        ShowRateThisApp(activity,true);
    }

    public static void ShowRateThisApp(final Activity activity,
                                       boolean askForFeedback){
        //Show Message is User wants to email in case to base feedback
        if(askForFeedback) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
            dlgAlert.setTitle(activity.getString(R.string.title_rate_app));
            dlgAlert.setMessage(activity.getString(R.string.message_check_for_feedback));
            dlgAlert.setCancelable(false);

            dlgAlert.setPositiveButton(activity.getString(R.string.button_yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Feedback.SendFeedbackByEmail(activity,false);
                            dialog.dismiss();
                            return;
                        }
                    });
            dlgAlert.setNegativeButton( //Never
                    activity.getResources().getText(R.string.button_no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ShowRateApp(activity);
                        }
                    });
            AlertDialog alert = dlgAlert.create();
            alert.show();
        }
        else{
            ShowRateApp(activity);
        }

    }

    private static void ShowRateApp(final Activity activity){
        //Setting to only show once
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //Try Google play
        intent.setData(Uri.parse(PackageServices.getGooglePlayLink(activity)));
        if (!StartActivity(activity,intent)) {
            //Market (Google play) app seems not installed, let's try to open a webbrowser
            intent.setData(Uri.parse(PackageServices.getGooglePlayWebLink(activity)));
            if (!StartActivity(activity,intent)) {
                //Well if this also fails, we have run out of options, inform the user.
                SupportFunctions.DisplayToastMessageLong(activity,
                        activity.getString(
                                R.string.message_rate_this_app_error).toString());
            }
        }
    }
    public static void ShowRateAppDialog(final Activity activity){
        int currentCount=AppSettings.getPreferencesInt(activity,rate_this_app);
        //AppSettings.setPreferencesInt(activity, rate_this_app,1);
        if(currentCount<0)
            return; //User has declined to review

        if(++currentCount%27==0){ //Do Every 27 times
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
            dlgAlert.setTitle(activity.getString(R.string.title_rate_this_app));
            dlgAlert.setMessage(activity.getString(R.string.message_rate_this_app));
            dlgAlert.setCancelable(false);

            dlgAlert.setPositiveButton(activity.getString(R.string.button_rate_now),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ShowRateThisApp(activity,false);
                        AppSettings.setPreferencesInt(activity, rate_this_app,-100);
                        dialog.dismiss();
                    }
                });

            dlgAlert.setNeutralButton(
                activity.getResources().getText(R.string.button_rate_later),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

            dlgAlert.setNegativeButton( //Never
                        activity.getResources().getText(R.string.button_rate_never),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppSettings.setPreferencesInt(activity, rate_this_app,-100);
                        dialog.dismiss();
                    }
                });

            AlertDialog alert = dlgAlert.create();
            try {
                alert.show();
            }
            catch(Exception ex){
                //In case dialog has error
            }
            //Setting to only show once
        }
        AppSettings.setPreferencesInt(activity, rate_this_app,currentCount);
    }

    private static boolean StartActivity(Context context,
                                         Intent aIntent) {
        try
        {
            context.startActivity(aIntent);
            return true;
        }
        catch (ActivityNotFoundException e)
        {
            return false;
        }
    }
}
