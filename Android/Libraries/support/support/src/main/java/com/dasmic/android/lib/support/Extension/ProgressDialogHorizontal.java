package com.dasmic.android.lib.support.Extension;

import android.app.Activity;
import android.app.ProgressDialog;

/**
 * Created by Chaitanya Belwal on 9/13/2015.
 */
public class ProgressDialogHorizontal extends ProgressDialog {
    public ProgressDialogHorizontal(Activity context, String message){
        super(context);
        setMessage(message);
        setCancelable(false);
        setIndeterminate(false);
        setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }
}
