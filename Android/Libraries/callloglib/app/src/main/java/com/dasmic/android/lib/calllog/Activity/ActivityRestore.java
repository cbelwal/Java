package com.dasmic.android.lib.calllog.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

public class ActivityRestore extends ActivityBaseImport {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SupportFunctions.DisplayToastMessageLong(this,
        //        getString(R.string.message_restore_legacy_name));
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
                    else
                        SupportFunctions.AsyncDisplayGenericDialog(getContext(),
                                getString(R.string.message_backup_file_not_found),
                                getString(R.string.app_name));

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
    protected void onImportComplete(int count,
                                    String exceptionMessage){

        if(exceptionMessage!=""){
            SupportFunctions.DisplayToastMessageLong(this,
                    getResources().getText(
                            R.string.message_restore_fail).toString()+
                            ":" + exceptionMessage);
        }
        else {
            SupportFunctions.DisplayToastMessageShort(getActivity(),
                    String.valueOf(count));

            SupportFunctions.DisplayToastMessageLong(getActivity(),
                    getResources().getText(
                            R.string.message_restore_complete).toString());
        }

    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (AppOptions.CALLLOG_RESTORE_ACTIVITY_REQUEST):
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
}
