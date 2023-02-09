package com.dasmic.android.lib.calllog.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Enum.AppOptions;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelExport;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/18/2017.
 */

public class ActivityExport extends ActivityBaseExport {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_export_cl);
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

    protected void initializeDisplay(){
        //Set default
        RadioButton option=(RadioButton) findViewById(R.id.radioExternalStorage);
        option.setChecked(true);

        option = (RadioButton) findViewById(R.id.radioCombinedCLM);
        option.setChecked(true);
    }

    //Returns the option selected
    private int shareAs(){
        int value=0;
        RadioGroup opOptions=(RadioGroup) findViewById(R.id.radioShareAs);
        int id=opOptions.getCheckedRadioButtonId();

        if(id==R.id.radioCLM)
            value=2;
        else if(id==R.id.radioCombinedCLM)
            value=3;

        return  value;
    }


    // Do Export operations
    // Do not change name as function is inherited
    @Override
    protected void doSelectedOperation(ArrayList<DataCallLogDisplay> selContacts)
    {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            ArrayList<DataCallLogDisplay> _selContacts;
            String _combinedValue;
            String _exMessage;
            int mCount=0;
            int mTotalCount=0;
            int _flag;
            int _getAs;
            int _shareAs;
            ProgressDialogHorizontal _pdHori;

            BasicAsyncTask(Activity context,
                           ArrayList<DataCallLogDisplay> selContacts)
            {
                super();
                _context = context;
                _selContacts=selContacts;
                _flag=0;
                _getAs =0;
            }

            private final Handler mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //FragmentActivity activity = getActivity();
                    switch (msg.what) {
                        case ViewModelExport.HANDLER_DATAGEN_COUNT_UPDATE:
                            mCount = msg.arg1;
                            onProgressUpdate();
                            break;
                        case ViewModelExport.HANDLER_DATAGEN_TOTAL_COUNT:
                            mTotalCount = msg.arg1;
                            _pdHori.setMax(mTotalCount);
                            _pdHori.setMessage(getString(R.string.message_exporting_calllog));
                            break;
                        case ViewModelExport.HANDLER_FILEWRITE_TOTAL_COUNT:
                            mTotalCount = msg.arg1;
                            _pdHori.setMessage(getString(R.string.message_exporting_calllog_write_file));
                            _pdHori.setMax(mTotalCount);
                            break;
                        case ViewModelExport.HANDLER_FILEWRITE_COUNT_UPDATE:
                            mCount = msg.arg1;
                            onProgressUpdate();
                            break;
                        case ViewModelExport.HANDLER_CONTACTNAME_UPDATE:
                            String contactName = msg.obj.toString();
                            _pdHori.setMessage(getString(R.string.message_exporting_calllog_write_file) +
                                    "(" + contactName + ")");
                            onProgressUpdate();
                            break;
                        case ViewModelExport.HANDLER_FILEWRITE_COMBINED:
                            String fileName = msg.obj.toString();
                            _pdHori.setMessage(getString(R.string.message_exporting_calllog_write_file) +
                                    "(" + fileName + ")");
                            onProgressUpdate();
                            break;
                        default:
                    }
                }
            };

            @Override
            protected Void doInBackground(Void... params) {
                ViewModelExport vmExport =
                        new ViewModelExport(_context,
                                _appFolder,mHandler);

                try{
                    //Do actual Read Operations
                    switch(_shareAs){
                        case 2:
                            _combinedValue = //Folder + fileName
                                    vmExport.storeContactsInIndividualCLMFiles(_selContacts,
                                            _flag,getStorageFlag());
                            break;
                        case 3:
                            _combinedValue = //Folder name where all vCard files
                                    vmExport.storeContactsInSingleCLMFile(_selContacts,
                                            _flag, getStorageFlag());
                            break;
                    }
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
                    SupportFunctions.AsyncDisplayGenericDialog(
                            _context, "Error :" + _exMessage,
                            _context.getString(R.string.app_name));
                }
                _updateDone=true;
                _pdHori.dismiss();
            }

            @Override
            protected void onPreExecute() {
                Log.i("CKIT", "thread PreExecute");
                super.onPreExecute();

                _pdHori = new ProgressDialogHorizontal(_context,
                        getString(R.string.message_exporting_calllog_ready));
                _pdHori.show();
                _pdHori.setMax(1);

                //---- Set Field Share Flag
                _flag=getFlag();
                //Set shareAs Value
                _shareAs= shareAs();
            }

            @Override
            protected void onProgressUpdate(Void... params) {
                _pdHori.setProgress(mCount);

                try {
                    Thread.sleep(1);
                }
                catch(InterruptedException ex){

                }
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

    //Share via SMS Email
    //@Override
    protected void PerformOperation(String combinedString){
        if(getStorageFlag())
            shareExternalStorage(combinedString);
        else
            shareAllShare(combinedString);
    }

    //---------------------------
    private boolean getStorageFlag() {
        RadioGroup opOptions = (RadioGroup) findViewById(R.id.radioShareOptions);
        if(opOptions.getCheckedRadioButtonId()
                ==R.id.radioExternalStorage)
            return true;
        else if(opOptions.getCheckedRadioButtonId()
                == R.id.radioShareApplications)
            return false;
        else return false;
    }

    protected void shareAllShare(String combinedString){
        try {
            Intent intent=null;
            switch(shareAs()) {
                case 2: //Individual files
                    intent = getMultipleFileIntent("text/plain",
                        combinedString);
                    break;
                case 3: //Combined file
                    intent = getSingleFileIntent("text/plain",
                            combinedString);
                    break;
            }
            startActivity(Intent.createChooser(intent, "Export using"));
        }
        catch (Exception ex){
            SupportFunctions.DisplayToastMessageShort(this, ex.getMessage());
        }
    }


    private void shareExternalStorage(String combinedString){
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
