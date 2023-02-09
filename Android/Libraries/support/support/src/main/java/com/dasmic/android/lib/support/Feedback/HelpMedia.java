package com.dasmic.android.lib.support.Feedback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.dasmic.android.lib.support.R;
import com.dasmic.android.lib.support.Static.AppSettings;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 3/17/2016.
 */
public class HelpMedia {
    private static final String video_setting="show_demo_video";
    private static final String help_message="show_help_message";

    public static void ShowDemoVideo(Activity activity,
                                     String videoID){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("vnd.youtube:" + videoID));
            intent.putExtra("force_fullscreen",true);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + videoID));
                activity.startActivity(intent);
            }
            catch(Exception newEx)
            {
                //Video cannot be shown at all
                //This catch is important so app does not crash
            }
        }

    }

    public static void ShowDemoVideoDialog(final Activity activity,
                                           final String videoID){
        ShowDemoVideoDialog(activity,videoID,0);
    }

    public static void ShowDemoVideoDialog(final Activity activity,
                                           final String videoID,
                                           final int minCount){
        int currentSetting = AppSettings.getPreferencesInt(activity,video_setting);

        if(currentSetting < minCount) { //Dont show but increment
            AppSettings.setPreferencesInt(activity, video_setting, currentSetting + 1);
            return;
        }

        if(currentSetting > minCount)
            return;

        //currentSetting == minCount
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
        dlgAlert.setTitle(activity.getString(R.string.title_demo_video));
        dlgAlert.setMessage(activity.getString(R.string.message_demo_video));
        dlgAlert.setCancelable(false);

        dlgAlert.setPositiveButton(activity.getString(R.string.button_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ShowDemoVideo(activity,videoID);
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
        //Setting to only show once
        AppSettings.setPreferencesInt(activity, video_setting,minCount+1);
    }

    public static void showHelpDialog(final Activity activity,
                                           final String helpMessage){
        if(AppSettings.getPreferencesInt(activity,help_message)>0)
            return;
        SupportFunctions.AsyncDisplayGenericDialog(activity,helpMessage,"");
        //Setting to only show once
        AppSettings.setPreferencesInt(activity, help_message,1);
    }


    public static void ShowWebURL(Activity activity,
                                     String URL){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(URL));
            activity.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            SupportFunctions.DisplayToastMessageShort(activity,
                    activity.getString(R.string.message_help_documentation_fail));
        }
        //Reset app settings
        //AppSettings.setPreferencesInt(activity, video_setting,0);
    }
}
