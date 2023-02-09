package com.dasmic.android.lib.contacts.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelExport;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ActivityBackup extends ActivityBaseExport {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_backup);
        super.setLocalVariablesAndEventHandlers();
        initializeDisplay();
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
    protected void doSelectedOperation(ArrayList<Long> selContacts)
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
                            _context.getString(R.string.app_name_small).toString());
                    if(getStorageFlag()) //External storage
                        _combinedValue=vmExport.
                                StoreAllContactsInBackupFile_ExternalWithDate(
                            ActivityOptions.isFreeVersion);
                    else
                        _combinedValue=
                                vmExport.
                                        StoreAllContactsInBackupFile_InternalWithDate(
                                ActivityOptions.isFreeVersion);

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
        if(ActivityOptions.isFreeVersion)
            SupportFunctions.AsyncDisplayGenericDialog(this,
                    getString(R.string.message_backup_freeversion)
                            +  ActivityOptions.FREE_VERSION_CONTACT_LIMIT,
                    getString(R.string.app_name_small));
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
            intent=getMultipleFileIntent("text/x-vcard",
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
