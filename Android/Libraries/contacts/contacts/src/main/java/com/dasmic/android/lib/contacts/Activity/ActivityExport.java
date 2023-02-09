package com.dasmic.android.lib.contacts.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.dasmic.android.lib.contacts.Enum.ExportAsEnum;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelExport;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ActivityExport extends ActivityBaseExport {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_export);
        setLocalVariablesAndEventHandlers();
        initializeDisplay();
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
    protected void onDestroy() {
        Log.i("CKIT", "ActivityExport onDestroy");
        super.onDestroy();
    }


    protected void initializeDisplay(){
        //Set default
        RadioButton option=(RadioButton) findViewById(R.id.radioExternalStorage);
        option.setChecked(true);
        Spinner spinner = (Spinner)findViewById(R.id.spinExportAs);
        spinner.setSelection(0,true);
    }


    @Override
    protected void setLocalVariablesAndEventHandlers(){
        super.setLocalVariablesAndEventHandlers();
    }

    //This is mapping between UI and Data
    //Make sure UI is correct
    protected int getAs(){
        ExportAsEnum value;
        Spinner spinner = (Spinner)findViewById(R.id.spinExportAs);
        int pos = spinner.getSelectedItemPosition();

        switch(pos)
        {
            case 0:
                value=ExportAsEnum.vCard21Combined;
                break;
            case 1:
                value=ExportAsEnum.vCard21Individual;
                break;
            case 2:
                value=ExportAsEnum.vCard40Combined;
                break;
            case 3:
                value=ExportAsEnum.vCard40Individual;
                break;
            case 4:
                value=ExportAsEnum.Text;
                break;
            case 5:
                value=ExportAsEnum.CSV;
                break;
            default:
                value=ExportAsEnum.vCard21Combined;
                break;
        }

        return  value.ordinal();
    }

    // Do Export operations
    // Do not change name as function is inherited
    @Override
    protected void doSelectedOperation(ArrayList<Long> selContacts)
    {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            ArrayList<Long> _selContacts;
            String _combinedValue;
            String _exMessage;
            int mCount=0;
            int mTotalCount=0;
            int _flag;
            int _getAs;
            ProgressDialogHorizontal _pdHori;

            BasicAsyncTask(Activity context,
                           ArrayList<Long> selContacts)
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
                            _pdHori.setMessage(getString(R.string.message_exporting_contacts));
                            break;
                        case ViewModelExport.HANDLER_FILEWRITE_TOTAL_COUNT:
                            mTotalCount = msg.arg1;
                            _pdHori.setMessage(getString(R.string.message_exporting_contacts_write_file));
                            _pdHori.setMax(mTotalCount);
                            break;
                        case ViewModelExport.HANDLER_FILEWRITE_COUNT_UPDATE:
                            mCount = msg.arg1;
                            onProgressUpdate();
                            break;
                        case ViewModelExport.HANDLER_CONTACTNAME_UPDATE:
                            String contactName = msg.obj.toString();
                            _pdHori.setMessage(getString(R.string.message_exporting_contacts_write_file) +
                                            "(" + contactName + ")");
                            onProgressUpdate();
                            break;
                        case ViewModelExport.HANDLER_FILEWRITE_COMBINED:
                            String fileName = msg.obj.toString();
                            _pdHori.setMessage(getString(R.string.message_exporting_contacts_write_file) +
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
                                _appName,mHandler);

                try{
                    //Do actual Read Operations
                    switch(ExportAsEnum.values()[_getAs]){
                        case Text:
                            _combinedValue =
                                    vmExport.StoreContactsAsFormattedStringFile(_selContacts,
                                            _flag,getStorageFlag());
                            break;
                        case vCard40Individual:
                            _combinedValue =
                                    vmExport.StoreContactsInIndividualVCard40Files(_selContacts,
                                            _flag,getStorageFlag());
                            break;

                        case vCard40Combined:
                            _combinedValue =
                                    vmExport.StoreContactsInSingleVCard40File(_selContacts,
                                            _flag,getStorageFlag());
                            break;
                        case vCard21Individual:
                            _combinedValue =
                                    vmExport.StoreContactsInIndividualVCard21Files(_selContacts,
                                            _flag,getStorageFlag());
                            break;

                        case vCard21Combined:
                            _combinedValue =
                                    vmExport.StoreContactsInSingleVCard21File(_selContacts,
                                            _flag,getStorageFlag());
                            break;

                        case CSV:
                            _combinedValue =
                                    vmExport.StoreContactsInSingleCSVFile(_selContacts,
                                            _flag,getStorageFlag());
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
                        getString(R.string.message_exporting_contacts_ready));
                _pdHori.show();
                _pdHori.setMax(1);

                //---- Set Field Share Flag
                _flag=getFlag();
                //Set shareAs Value
                _getAs = getAs();
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
                ShareExternalStorage(combinedString);
        else
                ShareAllShare(combinedString);
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

    protected void ShareAllShare(String combinedString){
        try {
            Intent intent=null;
            switch(ExportAsEnum.values()[getAs()]){ //Set Intent Types
                case Text://Text
                    intent=getSingleFileIntent("text/plain",
                            combinedString);
                    break;
                case vCard21Individual:
                case vCard40Individual:
                    intent=getMultipleFileIntent("text/x-vcard",
                            combinedString);
                    break;

                 //VCF file
                case vCard40Combined:
                case vCard21Combined:
                    intent=getSingleFileIntent("text/x-vcard",
                            combinedString);
                    break;

                case CSV://CSV file
                    intent=getSingleFileIntent("text/csv",
                            combinedString);
                    break;
            }
            startActivity(Intent.createChooser(intent, "Export using"));
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
