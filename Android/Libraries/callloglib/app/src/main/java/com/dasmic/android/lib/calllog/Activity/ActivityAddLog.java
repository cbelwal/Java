package com.dasmic.android.lib.calllog.Activity;

import android.content.res.Configuration;
import android.os.Bundle;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Model.ModelCallLogCreate;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 3/11/2017.
 */

public class ActivityAddLog extends ActivityEditLogBase {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    //This is very important since if Activity is restarted in
    //screen change, there is a major conflict with ReloadListView
    //This happens when ReloadListView is called along
    //with ActivityLoad and happens when screen orientation
    //is changed along with some update
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected DataCallLogDisplay getSelectedCallLog(){

        //ID of -1 means new call log
        return new DataCallLogDisplay(-1,1,DateOperations.getCurrentDate(),
                                            60,"","","",null);
    }

    @Override
    protected void doApplyAction(DataCallLogDisplay dcld){
        try {
            ModelCallLogCreate mclc =
                    new ModelCallLogCreate(this);
            mclc.createCallLog_Single(dcld);
            SupportFunctions.DisplayToastMessageLong(this,
                    getString(R.string.message_calllog_added));
        }
        catch(Exception ex){
            SupportFunctions.AsyncDisplayGenericDialog(this,
                    getString(R.string.message_calllog_added_error) + ":"
                    +ex.getMessage(),
                    getString(R.string.app_name));
        }
    }

}
