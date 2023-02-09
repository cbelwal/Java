package com.dasmic.android.lib.contacts.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelExport;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelContactsDisplay;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaitanya Belwal on 11/8/2015.
 */
public class ActivityBaseExport extends ActivityBaseAd {
    protected boolean  _updateDone;
    protected ProgressDialogSpinner _progressDialog;
    private ArrayList<Long> _selContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    protected boolean checkPermissions(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ActivityOptions.PERMISSION_STORAGE_REQUEST);
            return false;
        }
        else
            return true;
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
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        boolean failed=false; //Flag to denote failed
        switch (requestCode) {
            case ActivityOptions.PERMISSION_STORAGE_REQUEST:
                if(grantResults.length > 0)
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    }
                    else {
                        failed = true;
                    }
                else
                    failed=true;

                if(failed) {
                    // Permission Denied
                    SupportFunctions.DisplayToastMessageLong(this,
                            getString(R.string.message_other_permissions_missing));
                    finish();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        ViewModelContactsDisplay vmContactsDisplay=
                ViewModelContactsDisplay.getInstance(this);
        _selContacts =
                getUniqueContactIdList(vmContactsDisplay.getCheckedItems());

        doSelectedOperation(_selContacts);
    }




    protected void doSelectedOperation(ArrayList<Long> selContacts){}

    protected int getFlag(){
        int flag=0;
        CheckBox cb = (CheckBox) findViewById(R.id.checkPhone);
        if(cb.isChecked()) flag = flag | 1;
        cb = (CheckBox) findViewById(R.id.checkEmail);
        if(cb.isChecked()) flag = flag | 2;
        cb = (CheckBox) findViewById(R.id.checkPostalAddress);
        if(cb.isChecked()) flag = flag | 4;
        cb = (CheckBox) findViewById(R.id.checkTimesContacted);
        if(cb.isChecked()) flag = flag | 8;
        cb = (CheckBox) findViewById(R.id.checkLastContact);
        if(cb.isChecked()) flag = flag | 16;
        cb = (CheckBox) findViewById(R.id.checkIsFavorite);
        if(cb.isChecked()) flag = flag | 32;
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
        intent.setType("text/x-vcard");
        //GetAll files
        ArrayList<File> allFiles = FileOperations.getOnlyFileObjectsInFolder(
                combinedString);
        ArrayList<Uri> uris = new ArrayList<>();
        for(File f:allFiles) {
            Uri contentUri = FileProvider.getUriForFile(this,
                    ActivityOptions.FILE_PROVIDER_AUTHORITY,
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
        intent.setType("text/x-vcard");
        Uri contentUri=FileProvider.getUriForFile(this,
                ActivityOptions.FILE_PROVIDER_AUTHORITY,
                FileOperations.getFileObjectForRead(combinedString));
        GrantPermissionToAll(
                contentUri,
                intent);
        intent.putExtra(Intent.EXTRA_STREAM,
                contentUri);
        return intent;
    }
}