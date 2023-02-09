package com.dasmic.android.lib.support.Feedback;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;

import com.dasmic.android.lib.support.R;
import com.dasmic.android.lib.support.Static.PackageServices;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 2/17/2016.
 */
public class TellAFriend {
    public static void TellAFriend(Activity activity,
                                    String appMessage){
        String message=activity.getString(
                R.string.message_tellafriend_body).toString()+"\n"+
                PackageServices.getGooglePlayWebLink(activity)+"\n\n";
        message = message + appMessage +"\n";
        SupportFunctions.ShareStringDataViaIntent(activity,
                message, activity.getString(
                        R.string.title_tellafriend));
    }


}
