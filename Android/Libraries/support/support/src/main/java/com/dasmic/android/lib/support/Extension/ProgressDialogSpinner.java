package com.dasmic.android.lib.support.Extension;

import android.app.Activity;
import android.app.ProgressDialog;

/**
 * Created by Chaitanya Belwal on 9/13/2015.
 */
public class ProgressDialogSpinner extends ProgressDialog {
    public ProgressDialogSpinner(Activity context, String message){
        super(context);
        setMessage(message);
        setCancelable(false);
        setIndeterminate(true);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
}
