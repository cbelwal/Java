package com.dasmic.android.lib.contacts.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.dasmic.android.lib.contacts.Data.DataValuePair;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.Extension.ImportException;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelExport;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelImport;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ActivityImport extends ActivityBaseAd {
    protected final String tagUpdateDone="Update Done";
    //protected ArrayList<Long> _selContacts;
    protected ViewModelImport _vmImport;
    protected ListView _listView;
    protected Uri _selectedFileUri;
    protected String _selectedFileName;
    protected boolean _updateDone;
    protected ProgressDialogSpinner _progressDialog;
    protected TextView _tvFileCount;
    protected int _totalFileCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            _updateDone = savedInstanceState.getBoolean(
                    tagUpdateDone);
        setContentView(R.layout.ui_import);
        if(checkPermissions()) {
            setLocalVariablesAndEventHandlers();
            initializeDisplay();
        }
        //Ideally cleanup should be done in Export but its leading to a problem there
        cleanInternalFolder();//No Temporary files are shown
    }


    protected void cleanInternalFolder(){
        //Remove files from temp folder
        try{
            ViewModelExport vmExport =
                    new ViewModelExport(this,
                            _appName);
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
                    ActivityOptions.PERMISSION_STORAGE_REQUEST);
            return false;
        }
        else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case ActivityOptions.PERMISSION_STORAGE_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    setLocalVariablesAndEventHandlers();
                    initializeDisplay();
                } else {
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean(tagUpdateDone,
                _updateDone);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Log.i("CKIT", "ActivityImport onDestroy");
        if(_progressDialog != null) //Make sure no process dialog is running
            _progressDialog.dismiss();
        super.onDestroy();
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
        if(getNumberOfCheckedItems() > 1) //Show confirmation dialog
        {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle(this.getResources().getText(R.string.app_name));

            dlgAlert.setMessage(this.getResources().getText(
                    R.string.message_confirm_multiple_import));

            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton(
                    this.getResources().getText(R.string.button_Yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    importContactsFromFile();
                }
            });

            dlgAlert.setNegativeButton(
                    this.getResources().getText(R.string.button_No),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            return;
                        }
                    });
            AlertDialog alert = dlgAlert.create();
            alert.show();
        }
        else
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
                ActivityOptions.FILELOAD_ACTIVITY_REQUEST);
    }

    //Set defaults
    protected void initializeDisplay() {
        loadFileFromLocalStorage();
    }

    protected void onButtonHelp() {
        SupportFunctions.AsyncDisplayGenericDialog(this, String.valueOf(
                        this.getResources().getText(R.string.message_import)),
                        _appName);
    }

    protected void onListItemSelected(DataValuePair<String, String> dvp) {
        File f = FileOperations.getFileObjectForRead(dvp.Key);
        if(f==null) return;
        updateFileInfo(Uri.fromFile(f),f.getName(),
                String.valueOf(f.length()));
        updateSelectedFileCount();
    }

    protected Uri getFileUri(String fileName){
        File f = FileOperations.getFileObjectForRead(fileName);
        return Uri.fromFile(f);
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

    protected void setSelectedFileUri(Uri uri, String fileName){
            _selectedFileUri =uri;
    }


    //Use multi select ListAdapter
    protected int getLayoutResource(){
        return android.R.layout.simple_list_item_multiple_choice;
    }

    protected int getNumberOfCheckedItems(){
        return getCheckedFileNames().size();
    }

    protected ArrayList<DataValuePair<String, String>> getCheckedFileNames(){
        ArrayList<DataValuePair<String, String>> checkedItems = new ArrayList<>();
        SparseBooleanArray checked = _listView.getCheckedItemPositions();
        int size = checked.size(); // number of name-value pairs in the array
        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value) {
                DataValuePair<String, String> dvPair =
                        (DataValuePair<String, String>)_listView.getItemAtPosition(key);
                checkedItems.add(dvPair);
            }
        }
        return checkedItems;
    }

    protected void loadFileFromLocalStorage() {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            ArrayList<DataValuePair<String, String>> _mainList;
            ArrayAdapter<DataValuePair<String, String>> _listAdapter;
            Activity _context;

            BasicAsyncTask(Activity context) {
                _context=context;
            }
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //Do actual Read Operations
                    _mainList =
                            _vmImport.getListofFilesInPhone();
                    _listAdapter =
                            new ArrayAdapter<DataValuePair<String, String>>(
                                    _context,
                                    getLayoutResource(),
                                    _mainList);

                }
                catch (Exception ex) {
                    Log.i("CKIT", "Import::Error::doInBackground::" + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                super.onPostExecute(param);

                _listView.setAdapter(_listAdapter);

                //Make first file the selection
                if(_mainList != null)
                    if(_mainList.size() >0) {
                        onListItemSelected(_mainList.get(0));
                    }

                Log.i("CKIT", "thread PostExecute");
                _progressDialog.dismiss();
                _totalFileCount=_mainList.size();
                updateSelectedFileCount();
            }

            @Override
            protected void onPreExecute() {
                Log.i("CKIT", "thread PreExecute");
                _progressDialog = new ProgressDialogSpinner(_context,
                        _context.getResources().getText(R.string.message_loading_files).toString());
                super.onPreExecute();
                _progressDialog.show();
            }

            @Override
            protected void onProgressUpdate(Void... params) {
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

    protected Activity getActivity(){
        return this;
    }

    //Updates the file count
    protected void updateSelectedFileCount(){
        if(_tvFileCount == null)
            _tvFileCount =
                    (TextView) findViewById(R.id.tvFileCount);
        _tvFileCount.setText(getNumberOfCheckedItems() +
                "/" + _totalFileCount);
    }

    protected void importContactsFromFile() {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            int _count=0;
            String _exceptionMessage;
            String _currentFileName;
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
                Uri fUri;
                try {
                    mVmImport=new ViewModelImport(_context,mImportHandler);
                    ArrayList<DataValuePair<String, String>> allFileNames =
                            getCheckedFileNames();

                    for(DataValuePair<String, String> dvp:allFileNames) {
                        _currentFileName = dvp.Value;
                        fUri = getFileUri(dvp.Key);
                        _count += mVmImport.ImportFrom(
                                fUri, dvp.Value);
                    }
                }
                catch (ImportException ex)
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
                _pdHori.setMessage(_currentFileName);
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

    protected void onImportComplete(int count,
                                  String exceptionMessage){

        if(exceptionMessage!=""){
            SupportFunctions.AsyncDisplayGenericDialog(
                    this,
                    exceptionMessage,"Error");
        }
        else {
            SupportFunctions.DisplayToastMessageShort(this,
                    String.valueOf(count) + " " + this.getResources().getText(
                            R.string.message_importing_contacts_complete).toString());
        }
    }

    protected void setLocalVariablesAndEventHandlers() {
        _vmImport = new ViewModelImport(this,null);
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

        CheckBox cb = (CheckBox) findViewById(R.id.cbFileCount);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAllSelectCheckBox();
            }
        });
    }

    protected void onClickAllSelectCheckBox(){
        CheckBox cb = (CheckBox) findViewById(R.id.cbFileCount);
        setCheckedAll(cb.isChecked());
    }

    protected void setCheckedAll(boolean flag){
        for (int ii=0; ii < _listView.getAdapter().getCount(); ii++) {
            _listView.setItemChecked(ii, flag);
        }
        updateSelectedFileCount();
    }

    //Add Items to the action bar here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_import, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.action_import_help)
                onButtonHelp();

        return super.onOptionsItemSelected(item);
    }

    //Load files
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String fileName = "";
        String sFileSize="";
        Uri uri=null;

        if ((requestCode == ActivityOptions.FILELOAD_ACTIVITY_REQUEST)
                && (resultCode == Activity.RESULT_OK)) {
            uri = data.getData();

            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);



            if (uri != null) {
                if (uri.toString().startsWith("file:")) {
                    fileName = uri.getPath();
                } else { // uri.startsWith("content:")

                    Cursor c = getContentResolver().query(uri,
                            null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        int id = c.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME);
                        if (id != -1) {
                            fileName = c.getString(id);
                        }
                        id = c.getColumnIndex(
                                OpenableColumns.SIZE);
                        if (id != -1) {
                            sFileSize = c.getString(id);
                        }
                    }
                }
            }
        }
        updateFileInfo(uri,fileName,sFileSize);
    }
}
