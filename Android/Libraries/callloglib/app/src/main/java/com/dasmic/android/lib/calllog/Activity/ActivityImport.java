package com.dasmic.android.lib.calllog.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.dasmic.android.lib.calllog.Data.DataValuePair;
import com.dasmic.android.lib.calllog.Enum.AppOptions;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelImport;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/19/2017.
 */

public class ActivityImport extends ActivityBaseImport {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case AppOptions.PERMISSION_STORAGE_REQUEST:
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
        savedInstanceState.putBoolean(_tagUpdateDone,
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


    protected void onButtonHelp() {
        SupportFunctions.AsyncDisplayGenericDialog(this, String.valueOf(
                this.getResources().getText(R.string.message_import_help)),
                _appFolder);
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
                                    android.R.layout.simple_list_item_1,
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
                    else
                        SupportFunctions.AsyncDisplayGenericDialog(getContext(),
                                getString(R.string.message_clm_file_not_found),
                                getString(R.string.app_name));

                Log.i("CKIT", "thread PostExecute");
                _progressDialog.dismiss();
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


    @Override
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
                            R.string.message_importing_complete).toString());
        }

    }


    //Add Items to the action bar here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_import_cl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //Not menu used here
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

        if ((requestCode == AppOptions.CALLLOG_FILELOAD_ACTIVITY_REQUEST)
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
