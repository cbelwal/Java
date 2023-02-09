

package com.dasmic.android.lib.apk.Model;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Enum.ActivityOptions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/15/2015.
 */
public class ModelPackageDelete {
   Activity _activity;
    public ModelPackageDelete(Activity context){
        _activity =context;
    }

    public int DeleteContacts(ArrayList<DataPackageDisplay> allDpd){
        int retVal=0;
        //Main query
        try {
            for(DataPackageDisplay dpd:allDpd){
                Intent intent = new Intent(Intent.ACTION_DELETE,
                        Uri.fromParts("package", dpd.getPackageName(),null));
                _activity.startActivityForResult(intent,
                        ActivityOptions.DELETE_ACTIVITY_REQUEST);
            }
        }
        catch(Exception ex){
            Log.i("CKIT", "Delete Operation Error: " + ex.getMessage());

        }

        return retVal;
    }
}
