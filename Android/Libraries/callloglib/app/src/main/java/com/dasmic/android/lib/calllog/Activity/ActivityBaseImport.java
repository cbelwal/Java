package com.dasmic.android.lib.calllog.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dasmic.android.lib.calllog.Data.DataValuePair;
import com.dasmic.android.lib.calllog.Enum.AppOptions;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelExport;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelImport;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.io.File;

/**
 * Created by Chaitanya Belwal on 3/19/2017.
 */

public class ActivityBaseImport extends AppCompatActivity {
    protected final String _tagUpdateDone ="Update Done";
    //protected ArrayList<Long> _selContacts;
    protected ViewModelImport _vmImport;
    protected ListView _listView;
    protected Uri _selectedFileUri;
    protected String _selectedFileName;
    protected boolean _updateDone;
    protected ProgressDialogSpinner _progressDialog;
    protected String _appFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _appFolder = getString(R.string.app_folder);

        if(savedInstanceState != null)
            _updateDone = savedInstanceState.getBoolean(
                    _tagUpdateDone);
        setContentView(R.layout.ui_import_cl);
        if(checkPermissions()) {
            setLocalVariablesAndEventHandlers();
            initializeDisplay();
        }

        //Ideally cleanup should be done in Export but its leading to a problem there
        cleanInternalFolder();//No Temporary files are shown
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

    protected void cleanInternalFolder(){
        //Remove files from temp folder
        try{
            ViewModelExport vmExport =
                    new ViewModelExport(this,
                            _appFolder);
            if(vmExport != null)
                vmExport.cleanUpInternalFolder();
        }
        catch(Exception ex){

        }
    }

    protected boolean checkPermissions(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    AppOptions.PERMISSION_STORAGE_REQUEST);
            return false;
        }
        else
            return true;
    }

    protected void onListItemSelected(DataValuePair<String, String> dvp) {
        File f = FileOperations.getFileObjectForRead(dvp.Key);
        if(f==null) return;
        updateFileInfo(Uri.fromFile(f),f.getName(),
                String.valueOf(f.length()));
    }


    protected void updateFileInfo(Uri uri,
                                  String fileName,
                                  String sFileSize) {
        if(fileName.trim().equals("")) return; //Invalid selection

        if(_vmImport.isValidFileName(fileName)) {
            setSelectedFileUri(uri, fileName);
            _selectedFileName = fileName;
        }
        else {
            SupportFunctions.DisplayToastMessageLong(this,
                    getString(R.string.message_import_invalid_file));
            return;
        }

        TextView tv;
        tv = (TextView) findViewById(R.id.textFileName);
        tv.setText(fileName);

        //& (sFileSize.trim() == "" ||
        //sFileSize.trim() == ""))
        if(sFileSize == null)
            sFileSize = this.getResources().getText(
                    R.string.text_unknown_file_size).toString();
        tv = (TextView) findViewById(R.id.textFileSize);
        tv.setText(sFileSize);
    }

    protected void setLocalVariablesAndEventHandlers() {
        _vmImport = new ViewModelImport(this,_appFolder, null);
        _listView = (ListView) findViewById(R.id.listViewFile);
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position,
                                    long id) {
                DataValuePair<String, String> dvp = (DataValuePair<String, String>)
                        parentView.getItemAtPosition(position);
                onListItemSelected(dvp);
            }
        });

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

        Button btnBrowse = (Button) findViewById(R.id.btnScan);
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonBrowse();
            }
        });
    }

    protected void onButtonCancel() {
        Intent resultData = new Intent();
        if (_updateDone)
            setResult(Activity.RESULT_OK, resultData);
        else
            setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    protected void onButtonApply() {
        importContactsFromFile();
    }


    protected void onButtonBrowse() {
        LaunchFileSelector();
    }

    protected void LaunchFileSelector() {
        Intent mRequestFileIntent;
        mRequestFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        //mRequestFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        mRequestFileIntent.setType("*/*");
        startActivityForResult(mRequestFileIntent,
                AppOptions.CALLLOG_FILELOAD_ACTIVITY_REQUEST);
    }




    //Set defaults
    protected void initializeDisplay() {
        loadFileFromLocalStorage();
    }

    protected Activity getContext(){
        return this;
    }

    protected void setSelectedFileUri(Uri uri, String fileName){
        _selectedFileUri =uri;
    }

    protected Activity getActivity(){
        return this;
    }

    protected void importContactsFromFile() {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            int _count=0;
            String _exceptionMessage;
            int mImportCount=0;
            int mTotalCount=0;
            ViewModelImport mVmImport;
            ProgressDialogHorizontal _pdHori;

            private final Handler mImportHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //FragmentActivity activity = getActivity();
                    switch (msg.what) {
                        case ViewModelImport.IMPORT_COUNT_UPDATE:
                            mImportCount = msg.arg1;
                            onProgressUpdate();
                            break;
                        case ViewModelImport.IMPORT_TOTAL_COUNT:
                            mTotalCount = msg.arg1;
                            _pdHori.setMax(mTotalCount);
                            break;
                        default:
                    }

                }
            };

            BasicAsyncTask(Activity context) {
                super();
                _context = context;
                _exceptionMessage="";
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mVmImport=new ViewModelImport(_context, _appFolder, mImportHandler);
                    _count=mVmImport.ImportFrom(
                            _selectedFileUri,_selectedFileName);
                }
                catch (com.dasmic.android.lib.calllog.Extension.ImportException ex)
                {
                    _exceptionMessage=ex.getMessage();
                }
                catch (Exception ex) {
                    SupportFunctions.DebugLog(
                            "Import",
                            "ImportData",
                            ex.getMessage());
                    _exceptionMessage=ex.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                super.onPostExecute(param);
                Log.i("CKIT", "thread PostExecute");

                _updateDone = true;
                _pdHori.dismiss();
                onImportComplete(_count,_exceptionMessage);
            }

            @Override
            protected void onPreExecute() {
                Log.i("CKIT", "thread PreExecute");
                super.onPreExecute();

                _pdHori = new ProgressDialogHorizontal(_context,
                        getString(R.string.message_reading_import_file));
                _pdHori.show();
                _pdHori.setMax(1);
                _updateDone=true; //This will force a refresh when used changes orientation
            }

            @Override
            protected void onProgressUpdate(Void... params) {
                _pdHori.setMessage(getString(R.string.message_importing_contacts));
                _pdHori.setProgress(mImportCount);

                try {
                    Thread.sleep(25);
                }
                catch(InterruptedException ex){

                }

            }
        }

        AsyncTask<Void, Void, Void> aTask = new BasicAsyncTask(this);
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

    protected void loadFileFromLocalStorage(){}
    protected void onImportComplete(int count,
                                    String exceptionMessage){}
}
