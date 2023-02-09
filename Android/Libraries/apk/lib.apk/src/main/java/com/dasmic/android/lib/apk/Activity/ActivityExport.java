package com.dasmic.android.lib.apk.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.R;
import com.dasmic.android.lib.apk.ViewModel.ViewModelExport;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ActivityExport extends ActivityBaseExport {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_export);
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
    protected void doSelectedOperation(ArrayList<DataPackageDisplay>
                                                   selContacts)
    {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            ArrayList<DataPackageDisplay> _selected;
            String _combinedValue;
            String _exMessage;


            BasicAsyncTask(Activity context,
                           ArrayList<DataPackageDisplay> selected)
            {
                super();
                _context = context;
                _selected =selected;
            }

            @Override
            protected Void doInBackground(Void... params) {
                ViewModelExport vmExport =
                        new ViewModelExport(_context);

                try{
                    //Do actual Read Operations
                    _combinedValue =
                            vmExport.StorePackagesInSingleFolder(_selected,
                                            getStorageFlag());

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
            }

            @Override
            protected void onPreExecute() {
                Log.i("CKIT", "thread PreExecute");
                super.onPreExecute();

                _progressDialog.show();

            }

            @Override
            protected void onProgressUpdate(Void... params) {
            }
        }

        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(this,selContacts);
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        }
        catch(Exception ex){
            Log.i("CKIT", "Exception in Export::doInBackground"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
        Log.i("CKIT", "Thread finished execution");
    }

    protected void PerformOperation(String combinedString){
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
            intent=getMultipleFileIntent("application/octet-stream",
                            combinedString);
            startActivity(Intent.createChooser(intent, "Share using"));
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
