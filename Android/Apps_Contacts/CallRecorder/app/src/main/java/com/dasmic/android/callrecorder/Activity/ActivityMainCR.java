package com.dasmic.android.callrecorder.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dasmic.android.callrecorder.R;
import com.dasmic.android.lib.callrecorder.Activity.ActivityBaseMain;
import com.dasmic.android.lib.support.Feedback.HelpMedia;

public class ActivityMainCR extends ActivityBaseMain {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CKIT", "ActivityMainCL is created");
        //setContentView(R.layout.ui_main_cl);

    }


}
