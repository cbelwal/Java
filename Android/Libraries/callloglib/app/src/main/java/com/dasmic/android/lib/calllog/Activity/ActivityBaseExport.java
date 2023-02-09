package com.dasmic.android.lib.calllog.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Enum.AppOptions;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelCallLogDisplay;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaitanya Belwal on 3/18/2017.
 */

public abstract class ActivityBaseExport extends AppCompatActivity {
    protected boolean  _updateDone;
    protected ProgressDialogSpinner _progressDialog;
    protected String _appFolder;
    private ArrayList<DataCallLogDisplay> _selContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _appFolder = getString(R.string.app_folder);
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



    protected void GrantPermissionToAll(Uri uri, Intent intent){
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            Log.i("CKIT","Package Name:"+packageName);
            grantUriPermission(packageName, uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }


    @Override
    protected void onDestroy() {
        Log.i("CKIT", "ActivityBaseExport onDestroy");
        if(_progressDialog != null) //Make sure no process dialog is running
            _progressDialog.dismiss();

        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    protected void onButtonCancel(){
        Intent resultData = new Intent();
        if(_updateDone)
            setResult(Activity.RESULT_OK, resultData);
        else
            setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    protected void onButtonApply() {
        ViewModelCallLogDisplay vmCallLog=
                ViewModelCallLogDisplay.getInstance(this);

        _selContacts =
                vmCallLog.getCheckedItems();

        doSelectedOperation(_selContacts);
    }


    protected abstract void doSelectedOperation(ArrayList<DataCallLogDisplay>
                                                        selContacts);

    protected int getFlag(){
        int flag=0;
        return flag;
    }

    /*protected void GrantPermissionToAll(Uri uri, Intent intent){
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            Log.i("CKIT","Package Name:"+packageName);
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }*/

    protected void setLocalVariablesAndEventHandlers() {
        _progressDialog = new ProgressDialogSpinner(this,
                this.getResources().getText(R.string.progressbar_prepare_data).toString());

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCancel();
            }
        });

        Button btnConfirm = (Button) findViewById(R.id.btnSend);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonApply();
            }
        });
    }

    protected Intent getMultipleFileIntent(String intentType,
                                           String combinedString  ){
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType(intentType);
        //GetAll files
        ArrayList<File> allFiles = FileOperations.getOnlyFileObjectsInFolder(
                combinedString);
        ArrayList<Uri> uris = new ArrayList<>();
        for(File f:allFiles) {
            Uri contentUri = FileProvider.getUriForFile(this,
                    AppOptions.FILE_PROVIDER_AUTHORITY,
                    f);
            GrantPermissionToAll(
                    contentUri,
                    intent);
            uris.add(contentUri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                uris);
        return intent;
    }

    protected Intent getSingleFileIntent(String intentType,
                                         String combinedString){
        Intent intent = new Intent(
                android.content.Intent.ACTION_SEND);
        intent.setType(intentType);
        Uri contentUri=FileProvider.getUriForFile(this,
                AppOptions.FILE_PROVIDER_AUTHORITY,
                FileOperations.getFileObjectForRead(combinedString));
        GrantPermissionToAll(
                contentUri,
                intent);
        intent.putExtra(Intent.EXTRA_STREAM,
                contentUri);
        return intent;
    }
}
