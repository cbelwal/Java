package com.dasmic.android.lib.support.Feedback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.dasmic.android.lib.support.R;
import com.dasmic.android.lib.support.Static.PackageServices;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 1/18/2016.
 */
public class Feedback {

    public static void SendFeedbackByEmail(final Activity activity,boolean askForRating){
        if(askForRating) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
            dlgAlert.setTitle(activity.getString(R.string.title_send_feedback));
            dlgAlert.setMessage(activity.getString(R.string.message_check_for_rating));
            dlgAlert.setCancelable(false);

            dlgAlert.setPositiveButton(activity.getString(R.string.button_yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            RateThisApp.ShowRateThisApp(activity,false);
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
                            SendFeedback(activity);
                        }
                    });
            AlertDialog alert = dlgAlert.create();
            alert.show();
        }
        else{
            SendFeedback(activity);
        }
    }

    private static void SendFeedback(Activity activity){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "feedback@coju.mobi", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(
                R.string.message_feedback_subject)+ " " +
                PackageServices.getSimpleAppName(activity));
        try
        {
            activity.startActivity(Intent.createChooser(intent,
                    activity.getString(
                            R.string.title_feedback)));

        }
        catch (ActivityNotFoundException paramMenuItem)
        {
            SupportFunctions.DisplayToastMessageLong(activity,
                    activity.getString(
                            R.string.message_email_send_error).toString());

        }
    }

    public static void SendFeedbackByEmail(Activity activity){
        SendFeedbackByEmail(activity,true);
    }
}
