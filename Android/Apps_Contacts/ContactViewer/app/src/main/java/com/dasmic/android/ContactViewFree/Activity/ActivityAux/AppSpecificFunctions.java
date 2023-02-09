package com.dasmic.android.ContactViewFree.Activity.ActivityAux;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.dasmic.android.ContactViewFree.R;
import com.dasmic.android.lib.contacts.Data.DataContactDisplay;

/**
 * Created by Chaitanya Belwal on 2/21/2016.
 */
public class AppSpecificFunctions {
    int _lastIdx;
    public int getColor(Context context,
                        DataContactDisplay contact
                         ){
        //problems
        _lastIdx++;
        int color;
        if(_lastIdx%2==0)
            color = ContextCompat.getColor(context,
                    R.color.ContactAlternateBackgroundColor);
        else
            color = ContextCompat.getColor(context,
                    R.color.ContactMainBackgroundColor);

        return color;
    }
}
