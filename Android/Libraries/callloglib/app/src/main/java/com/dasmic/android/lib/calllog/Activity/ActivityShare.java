package com.dasmic.android.lib.calllog.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Enum.AppOptions;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelExport;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/18/2017.
 */

public class ActivityShare extends ActivityBaseExport  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_share_cl);
        setLocalVariablesAndEventHandlers();
        initializeDisplay();
    }

    @Override
    protected void onDestroy() {
        Log.i("CKIT", "ActivityShare onDestroy");
        super.onDestroy();
    }


    //Need both Send_SMS and Read_External_Permissions
    //Based Export will only get Read_External_Permissions
    protected boolean checkPermissions(){
        int hasExternalStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    AppOptions.PERMISSION_SMS_STORAGE_REQUEST);
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
            case AppOptions.PERMISSION_SMS_STORAGE_REQUEST:
                if(grantResults.length == 1)
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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


    private void initializeDisplay(){
        //Set default
        RadioButton option=(RadioButton) findViewById(R.id.radioSMS);
        option.setChecked(true);
    }


    private int getAs(){
        int value=0;
        RadioGroup opOptions=(RadioGroup) findViewById(R.id.radioShareAs);
        int id=opOptions.getCheckedRadioButtonId();

        if(id==R.id.radioPlainText)
            value=1;
        else if(id==R.id.radioCLM)
            value=2;
        else if(id==R.id.radioCombinedCLM)
            value=3;

        return  value;
    }

    @Override
    protected void doSelectedOperation(ArrayList<DataCallLogDisplay> selContacts)
    {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            ArrayList<DataCallLogDisplay> _selContacts;
            String _combinedValue;
            String _exMessage;
            int _flag;
            int _shareAs;

            BasicAsyncTask(Activity context,
                           ArrayList<DataCallLogDisplay> selContacts)
            {
                super();
                _context = context;
                _selContacts=selContacts;
                _flag=0;
                _shareAs=0;
            }

            @Override
            protected Void doInBackground(Void... params) {
                ViewModelExport vmExport =
                        new ViewModelExport(_context,
                                _appFolder);

                try{
                    //Do actual Read Operations
                    switch(_shareAs){
                        case 1:
                            _combinedValue =
                                    vmExport.getContactsAsFormattedString(_selContacts,
                                            _flag);
                            break;
                        case 2:
                            _combinedValue = //Folder + fileName
                                    vmExport.storeContactsInIndividualCLMFiles(_selContacts,
                                            _flag,false);
                            break;
                        case 3:
                            _combinedValue = //Folder name where all vCard files
                                    vmExport.storeContactsInSingleCLMFile(_selContacts,
                                            _flag, false);
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
                //---- Set Field Share Flag
                _flag=getFlag();
                //Set shareAs Value
                _shareAs= getAs();

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
            Log.i("CKIT","Exception in Operations::doInBackground"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }

        Log.i("CKIT", "Thread finished execution");

    }

    //Share via SMS Email
    protected void PerformOperation(String combinedString){
        RadioGroup opOptions=(RadioGroup) findViewById(R.id.radioShareOptions);
        if(opOptions.getCheckedRadioButtonId()==
                R.id.radioSMS)
            ShareSMS(combinedString);
        else if(opOptions.getCheckedRadioButtonId()==
                R.id.radioClip)
            ShareClipboard(combinedString);
        else if(opOptions.getCheckedRadioButtonId()==
                R.id.radioShareApplications)
            ShareAllShare(combinedString);

    }


    private void ShareClipboard(String combinedString){
        try {

            switch(getAs()){
                case 1://Text
                    ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cbm.setPrimaryClip(ClipData.newPlainText(_appFolder,
                            combinedString));
                    SupportFunctions.DisplayToastMessageShort(this,
                                this.getString(R.string.message_clipboard_copy));
                    break;
                default://VCF
                    break;
            }

        }
        catch (Exception ex){
            SupportFunctions.DisplayToastMessageShort(this, ex.getMessage());
        }
    }

    private void ShareAllShare(String combinedString){

        try {
            Intent intent=null;
            switch(getAs()){
                case 1://Text
                    intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, combinedString);
                    break;
                case 2://Single Card
                    intent = getMultipleFileIntent("text/plain",
                            combinedString);
                    break;
                case 3://Multiple Card
                    intent =  getSingleFileIntent("text/plain",
                            combinedString);
                    break;
            }
            startActivity(Intent.createChooser(intent, "Share using"));
        }
        catch (Exception ex){
            SupportFunctions.DisplayToastMessageShort(this,
                    "Error:" + ex.getMessage());
        }
    }


    private void ShareSMS(String combinedString){
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:"));
            switch(getAs()){
                case 1://Text
                    intent.putExtra("sms_body", combinedString);
                    break;
                default://VCF
                    break;
            }
            startActivity(intent);

        }
        catch (Exception ex){
            SupportFunctions.DisplayToastMessageShort(this, ex.getMessage());
        }
    }

    private void onCLMSelected(){
        //Hide Messaging and Clipboard
        RadioButton rButton = (RadioButton) findViewById(R.id.radioSMS);
        rButton.setVisibility(View.INVISIBLE);

        rButton = (RadioButton) findViewById(R.id.radioClip);
        rButton.setVisibility(View.INVISIBLE);

        //Force selection of Share App
        rButton = (RadioButton) findViewById(R.id.radioShareApplications);
        rButton.setChecked(true);
    }

    private void onPlainTextSelected(){
        //Show Messaging and Clipboard
        RadioButton rButton = (RadioButton) findViewById(R.id.radioSMS);
        rButton.setVisibility(View.VISIBLE);

        rButton = (RadioButton) findViewById(R.id.radioClip);
        rButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setLocalVariablesAndEventHandlers(){
        checkPermissions();
        super.setLocalVariablesAndEventHandlers();
        RadioButton rButton = (RadioButton) findViewById(R.id.radioCLM);
        rButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCLMSelected();
            }
        });

        rButton = (RadioButton) findViewById(R.id.radioCombinedCLM);
        rButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCLMSelected();
            }
        });

        rButton = (RadioButton) findViewById(R.id.radioPlainText);
        rButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlainTextSelected();
            }
        });

    }

}
