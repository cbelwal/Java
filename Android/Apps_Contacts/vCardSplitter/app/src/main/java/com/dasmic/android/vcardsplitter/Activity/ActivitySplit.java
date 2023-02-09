package com.dasmic.android.vcardsplitter.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.vcardsplitter.Enum.ActivityOptions;
import com.dasmic.android.vcardsplitter.R;
import com.dasmic.android.vcardsplitter.ViewModel.ViewModelDisplay;
import com.dasmic.android.vcardsplitter.ViewModel.ViewModelSplit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaitanya Belwal on 10/9/2016.
 */

public class ActivitySplit extends ActivityBaseAd {
    private ViewModelSplit _vmSplit;
    private int _totalNumberOfContacts;
    private String _fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_split);

        setLocalVariablesAndEventHandlers();
        initializeDisplay(); //Should be called last
    }

    private boolean checkSplitNumber()
    {
        if(getSplitNumber() > _totalNumberOfContacts){
            SupportFunctions.DisplayToastMessageLong(
                    this,getString(R.string.message_split_number_invalid));
            SupportFunctions.vibrate(this,1000);
            return false;
        }

        if(ActivityOptions.isFreeVersion &&
                getSplitNumber() > ActivityOptions.FREE_VERSION_CONTACT_LIMIT)
        {
            SupportFunctions.vibrate(this,1000);
            SupportFunctions.DisplayToastMessageLong(
                    this,getString(R.string.message_split_freeversion));
            return false;
        }
        return true;
    }

    private void onButtonApply(){
        //Do Background operations
        if(checkSplitNumber())
            splitvCardFile();
    }

    private void PostSplit(String folder)
    {
        Intent intent=null;
        //If External Storage display message
        if(getStoreInExternalStorage()){
            SupportFunctions.DisplayToastMessageLong(
                    this,getString(R.string.message_split_store_external));
            SupportFunctions.DisplayToastMessageLong(
                    this,folder);
        }
        else { //If internal store
            intent=getMultipleFileIntent("text/x-vcard",
                    folder);
            startActivity(Intent.createChooser(intent, "Send using"));
        }


    }

    private Activity getActivity(){
        return this;
    }

    private void onButtonCancel(){
        finish();
    }

    private int getSplitNumber(){
        RadioButton rb = (RadioButton)findViewById(R.id.radioSplitvCardCount);
        if(rb.isChecked())
                {
            EditText et =
                    (EditText)findViewById(R.id.textNumberSplit);
            return Integer.valueOf(et.getText().toString());
        }
        else
            return _totalNumberOfContacts;
    }

    private boolean getStoreInExternalStorage(){
        RadioGroup options = (RadioGroup)findViewById(R.id.radioSaveOptions);
        if(options.getCheckedRadioButtonId()
                ==R.id.radioExternalStorage){
            return true;
        }
        else
            return false;
    }

    protected void splitvCardFile() {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            ProgressDialogHorizontal _pdHori;
            Activity _context;
            int mImportCount=0;
            int mTotalCount=0;
            ViewModelSplit _vmSplit;
            Uri _fileUri;
            String _folder;

            BasicAsyncTask(Activity context, Uri fileUri)
            {
                _fileUri=fileUri;
                _context=context;
            }

            private final Handler mImportHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {

                    switch (msg.what) {
                        case ViewModelSplit.SPLIT_FILE_COUNT:
                            mImportCount = msg.arg1;
                            onProgressUpdate();
                            break;
                        case ViewModelSplit.SPLIT_TOTAL_COUNT:
                            mTotalCount = msg.arg1;
                            _pdHori.setMax(mTotalCount);
                            break;
                        default:
                    }
                }
            };

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    _vmSplit = new ViewModelSplit(getActivity(),
                                _fileUri, mImportHandler);
                    //Do actual Read Operations
                    String fileName; //Remove .vcf extension
                    fileName = _fileName.replace(".vcf","");

                   _folder= _vmSplit.splitvCard(getSplitNumber(),
                           _totalNumberOfContacts,
                           fileName,
                           getStoreInExternalStorage());
                }
                catch (Exception ex) {
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            getString(R.string.message_error_splitting));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                super.onPostExecute(param);
                _pdHori.dismiss();
                PostSplit(_folder);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                _pdHori = new ProgressDialogHorizontal(_context,
                        getString(R.string.message_split_ready));
                _pdHori.show();
                _pdHori.setMax(1);
            }

            @Override
            protected void onProgressUpdate(Void... params) {
                _pdHori.setMessage(getString(R.string.message_split_inprogress));
                _pdHori.setProgress(mImportCount);
                try {
                    Thread.sleep(25);
                }
                catch(InterruptedException ex){
                }
            }
        }

        AsyncTask<Void, Void, Void> aTask = new BasicAsyncTask(this,
                _vmSplit.getFileUri());
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        } catch (Exception ex) {
            Log.i("CKIT", "Exception in Operations::doInBackground"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
        Log.i("CKIT", "Thread finished execution");
    }

    private void onSplitRadioButton(int id){
        RadioButton rb;
        RadioButton rb1;
        if(id==R.id.radioSplitvCardCount) {
            rb = (RadioButton) findViewById(R.id.radioSplitPerContact);
            rb1 = (RadioButton)findViewById(R.id.radioSplitvCardCount);
        }
        else {
            rb = (RadioButton) findViewById(R.id.radioSplitvCardCount);
            rb1 = (RadioButton) findViewById(R.id.radioSplitPerContact);
        }
        rb.setChecked(false);
        rb1.setChecked(true);
    }

    private void onSplitNumberTextViewClick(){
        onSplitRadioButton(R.id.radioSplitvCardCount);
    }

    private void setLocalVariablesAndEventHandlers() {

        ViewModelDisplay vmDisplay=
                ViewModelDisplay.getInstance(this); //Will use previous activity
        _vmSplit=new ViewModelSplit(getActivity(),
                                vmDisplay.getSelectedFileUri(),null);

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCancel();
            }
        });

        Button btnConfirm = (Button) findViewById(R.id.btnApply);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonApply();
            }
        });


        //Manually handle radio buttons
        RadioButton rb = (RadioButton) findViewById(R.id.radioSplitvCardCount);
        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSplitRadioButton(view.getId());
            }
        });

        rb = (RadioButton) findViewById(R.id.radioSplitPerContact);
        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSplitRadioButton(view.getId());
            }
        });

        //Set this so that radio is automatically selected

        TextView tv = (TextView) findViewById(R.id.textViewNumberSplit);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSplitNumberTextViewClick();
            }
        });

        EditText et = (EditText) findViewById(R.id.textNumberSplit);
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSplitNumberTextViewClick();
            }
        });

    }

    private void initializeDisplay() {
        ViewModelDisplay vmDisplay=
                ViewModelDisplay.getInstance(this); //Will use previous activity

        TextView textFileSize = (TextView) findViewById(R.id.textFileSize);
        textFileSize.setText(String.valueOf(vmDisplay.getSelectedFileSize()));

        _fileName = vmDisplay.getSelectedFileName();
        TextView textFileName = (TextView) findViewById(R.id.textFileName);
        textFileName.setText(_fileName);

        //Set number of contacts in Async Op
        setContactCountFromvCardFile();

        //Set Other setting
        EditText et = (EditText) findViewById(R.id.textNumberSplit);
        et.setText(String.valueOf(vmDisplay.getNumberOfSplit()));

        RadioButton rb = (RadioButton) findViewById(vmDisplay.getRadioSplitOption());
        rb.setChecked(true);

        rb = (RadioButton) findViewById(vmDisplay.getRadioSaveAsOption());
        rb.setChecked(true);

    }


    protected void setContactCountFromvCardFile() {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {


            BasicAsyncTask() {

            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //Do actual Read Operations
                    _totalNumberOfContacts =
                            _vmSplit.getNumberOfContacts();

                }
                catch (Exception ex) {
                    SupportFunctions.DebugLog("ActivitySplit",
                                            "setContactCountFromvCardFile",
                                            ex.getMessage());
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            getString(R.string.message_error_contact_count));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                super.onPostExecute(param);
                TextView textFileNumberOfContacts = (TextView) findViewById(
                         R.id.textFileNumberOfContacts);
                textFileNumberOfContacts.setText(String.valueOf(_totalNumberOfContacts));
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onProgressUpdate(Void... params) {
            }
        }

        AsyncTask<Void, Void, Void> aTask = new BasicAsyncTask();
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        } catch (Exception ex) {
            Log.i("CKIT", "Exception in Operations::doInBackground"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
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


}
