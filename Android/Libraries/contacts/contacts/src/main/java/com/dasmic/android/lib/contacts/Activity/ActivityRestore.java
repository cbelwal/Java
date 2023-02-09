package com.dasmic.android.lib.contacts.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dasmic.android.lib.contacts.Data.DataValuePair;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelImport;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 5/24/2016.
 */
public class ActivityRestore extends ActivityImport {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SupportFunctions.DisplayToastMessageLong(this,
                getString(R.string.message_restore_legacy_name));
    }

    //Use multi select ListAdapter
    @Override
    protected int getLayoutResource(){
        return android.R.layout.simple_list_item_1;
    }

    @Override
    protected void importContactsFromFile() {
        SupportFunctions.DebugLog("Restore","importContactsFromFile",
                "selectedFileName:"+_selectedFileName);

        //Crash was happening at this stage
        try {
            if (!_vmImport.RestoreAllContactsFromSpecifiedBackupFile(
                    _selectedFileUri, _selectedFileName)) {
                SupportFunctions.DisplayToastMessageLong(this,
                        getResources().getText(
                                R.string.message_restore_fail).toString());
            }
        }
        catch(Exception ex){
            SupportFunctions.DisplayToastMessageLong(this,
                    getResources().getText(
                            R.string.message_restore_fail).toString());
        }

    }

    @Override
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
                            _vmImport.getListOfBackupFilesInPhone();
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
                if(_mainList != null) //Leading to a crash if _mainList was null
                    if(_mainList.size() >0) {
                        onListItemSelected(_mainList.get(0));
                    }

                Log.i("CKIT", "thread PostExecute");
                _progressDialog.dismiss();
                _vmImport.setExtensionToBackup(); //Very important
            }

            @Override
            protected void onPreExecute() {
                Log.i("CKIT", "thread PreExecute");
                _progressDialog = new ProgressDialogSpinner(_context,
                        _context.getResources().getText(R.string.message_loading_backup_files).toString());
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
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (ActivityOptions.CONTACTS_RESTORE_ACTIVITY_REQUEST):
                    _updateDone = true;
                if (resultCode != RESULT_CANCELED)
                    SupportFunctions.DisplayToastMessageLong(this,
                            getResources().getText(
                                    R.string.message_restore_selection_finish).toString());
                    break;
            default:
                break;
        }
    }

    @Override
    protected void setLocalVariablesAndEventHandlers() {
        super.setLocalVariablesAndEventHandlers();
        //Hide the Linear Layout
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rlMultiselection);
        rl.setVisibility(View. INVISIBLE);
    }
}
