package com.dasmic.android.lib.calllog.ViewModel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 7/1/2017.
 */

public  class StaticFunctions {

    public static void searchNumber(Context context, String number)
    {
        Uri uri = Uri.parse("http://www.google.com/#q="+number);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        //Ask for permission
        try {
            context.startActivity(intent);
        }
        catch(SecurityException ex)
        {
            //Throw message
            SupportFunctions.DisplayToastMessageLong(context,
                    context.getString(R.string.message_search_number_security_exception));
        }
    }

    public static void makeCall(Context context, String number)
    {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        //Ask for permission
        try {
            context.startActivity(intent);
        }
        catch(SecurityException ex)
        {
            //Throw message
            SupportFunctions.DisplayToastMessageLong(context,
                    context.getString(R.string.message_call_security_exception));
        }
    }
}
