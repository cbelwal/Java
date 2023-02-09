package com.dasmic.android.lib.calllog.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Enum.AppOptions;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelExport;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/19/2017.
 */

public class ActivityBackup extends ActivityBaseExport {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_backup_cl);
        setLocalVariablesAndEventHandlers();
        initializeDisplay();
    }

    @Override
    protected void setLocalVariablesAndEventHandlers(){
        checkPermissions();
        super.setLocalVariablesAndEventHandlers();
    }

    protected boolean checkPermissions(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    AppOptions.PERMISSION_STORAGE_REQUEST);
            return false;
        }
        else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        boolean failed=false; //Flag to denote failed
        switch (requestCode) {
            case AppOptions.PERMISSION_STORAGE_REQUEST:
                if(grantResults.length > 0)
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission Granted
                        setLocalVariablesAndEventHandlers();
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
        Log.i("CKIT", "ActivityExport onDestroy");
        super.onDestroy();
    }

    //@Override
    protected void initializeDisplay(){
        //Set default
        RadioButton option=(RadioButton) findViewById(R.id.radioExternalStorage);
        option.setChecked(true);
    }


    @Override
    protected void doSelectedOperation(ArrayList<DataCallLogDisplay> 
                                                        selContacts)
    {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            String _combinedValue;
            String _exMessage;


            BasicAsyncTask(Activity context)
            {
                super();
                _context = context;
            }

            @Override
            protected Void doInBackground(Void... params) {

                try{
                    ViewModelExport vmExport=new ViewModelExport(_context,
                            _context.getString(R.string.app_folder).toString());
                    if(getStorageFlag()) //External storage
                        _combinedValue=vmExport.
                                storeAllContactsInBackupFile_ExternalWithDate(
                                        AppOptions.isFreeVersion);
                    else
                        _combinedValue=
                                vmExport.
                                        storeAllContactsInBackupFile_InternalWithDate(
                                                AppOptions.isFreeVersion);

                }
                catch(Exception ex){
                    _combinedValue =null;
                    _exMessage=ex.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                super.onPostExecute(param);
                Log.i("CKIT", "thread PostExecute");
                if(_combinedValue != null)
                    PerformOperation(_combinedValue);
                else{
                    SupportFunctions.DisplayToastMessageShort(
                            _context, "Error :" + _exMessage);
                }
                _updateDone=true;
                _progressDialog.dismiss();
                _context=null;
            }

            @Override
            protected void onPreExecute() {
                Log.i("CKIT", "thread PreExecute");
                super.onPreExecute();
                _progressDialog.setMessage
                        (_context.getString(
                                R.string.progressbar_backup));
                _progressDialog.show();

            }

            @Override
            protected void onProgressUpdate(Void... params) {
            }
        }

        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(this);
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        }
        catch(Exception ex){
            Log.i("CKIT", "Exception in Backup::doInBackground"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    //Called after file is ready
    protected void PerformOperation(String combinedString){
        //Display free message flag
        if(AppOptions.isFreeVersion)
            SupportFunctions.AsyncDisplayGenericDialog(this,
                    getString(R.string.message_backup_freeversion)
                            +  AppOptions.FREE_VERSION_LIMIT,
                    getString(R.string.app_name));
        else
            SupportFunctions.DisplayToastMessageLong(this,
                    getString(
                            R.string.message_backup_paidversion));

        if(getStorageFlag())
            ShareExternalStorage(combinedString);
        else
            ShareAllShare(combinedString);
    }

    private boolean getStorageFlag() {
        RadioGroup opOptions = (RadioGroup) findViewById(R.id.radioShareOptions);
        if (opOptions.getCheckedRadioButtonId()== R.id.radioExternalStorage)
            return true;
        else if (opOptions.getCheckedRadioButtonId()==
                R.id.radioShareApplications)
            return false;
        return false;
    }

    //@Override
    protected void ShareAllShare(String combinedString){
        try {
            Intent intent=null;
            intent=getMultipleFileIntent("text/plain",
                    combinedString);
            startActivity(Intent.createChooser(intent,
                    getString(R.string.backup_options_backupusing)));
        }
        catch (Exception ex){
            SupportFunctions.DisplayToastMessageShort(this, ex.getMessage());
        }
    }


    private void ShareExternalStorage(String combinedString){
        try {//Only display message as file is already saved
            SupportFunctions.DisplayToastMessageLong(
                    this, this.getResources().getText(R.string.message_save_external).toString() +
                            combinedString);
        }
        catch (Exception ex){
            SupportFunctions.DisplayToastMessageLong(this,
                    ex.getMessage());
        }
    }
}