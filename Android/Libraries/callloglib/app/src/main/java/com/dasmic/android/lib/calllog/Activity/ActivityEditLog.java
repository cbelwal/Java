package com.dasmic.android.lib.calllog.Activity;

import android.content.res.Configuration;
import android.os.Bundle;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Model.ModelCallLogCreate;
import com.dasmic.android.lib.calllog.Model.ModelCallLogUpdate;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelCallLogDisplay;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 3/11/2017.
 */

public class ActivityEditLog extends ActivityEditLogBase {

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
        ViewModelCallLogDisplay vmCallLog=
                ViewModelCallLogDisplay.getInstance(this);
        return vmCallLog.getCheckedItems().get(0);
    }

    @Override
    protected void doApplyAction(DataCallLogDisplay dcld){
        try {
            ModelCallLogUpdate mclu =
                    new ModelCallLogUpdate(this);
            mclu.updateCallLog_Single(_selectedCallLog,
                                                dcld);
            SupportFunctions.DisplayToastMessageLong(this,
                    getString(R.string.message_calllog_edited));
        }
        catch(Exception ex){
            SupportFunctions.AsyncDisplayGenericDialog(this,
                    getString(R.string.message_calllog_edited_error) + ":"
                    +ex.getMessage(),
                    getString(R.string.app_name));
        }
    }

}
