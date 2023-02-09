package com.dasmic.android.lib.apk.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Enum.ActivityOptions;
import com.dasmic.android.lib.apk.R;
import com.dasmic.android.lib.apk.ViewModel.ViewModelAppsDisplay;
import com.dasmic.android.lib.apk.ViewModel.ViewModelStopProcess;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 4/16/2017.
 */

public class ActivityStopProcesses extends Activity {
    private  boolean _updateDone;
    ProgressDialogHorizontal _pdHori;

    //Will not have a UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        _updateDone=false;
        ViewModelAppsDisplay vmad = ViewModelAppsDisplay.getInstance();
        if(vmad==null) onButtonCancel();
        stopProcesses(vmad.getCheckedItems());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(_pdHori != null)
            _pdHori.dismiss();
    }

    protected boolean checkPermissions(){
        int hasKillProcessPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.KILL_BACKGROUND_PROCESSES);
        if (hasKillProcessPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.KILL_BACKGROUND_PROCESSES},
                    ActivityOptions.PERMISSION_KILLPROCESS_REQUEST);
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
            case ActivityOptions.PERMISSION_KILLPROCESS_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    //Do nothing
                } else {
                    // Permission Denied
                    SupportFunctions.DisplayToastMessageLong(this,
                            getString(R.string.message_other_permissions_missing));
                    finish();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    protected void onButtonCancel(){
        Intent resultData = new Intent();
        if(_updateDone)
            setResult(Activity.RESULT_OK, resultData);
        else
            setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }


    private void stopProcesses(ArrayList<DataPackageDisplay>
                                                 selContacts){
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            ArrayList<DataPackageDisplay> _selected;
            String _exMessage;

            int mCount;
            int mTotalClosedCount;

            BasicAsyncTask(Activity context,
                           ArrayList<DataPackageDisplay> selected)
            {
                super();
                _context = context;
                _selected =selected;
                _exMessage="";
                mCount=0;
            }

            @Override
            protected Void doInBackground(Void... params) {
                ViewModelStopProcess vmbs = new ViewModelStopProcess(_context);
                int count=0;
                for (DataPackageDisplay dpd : _selected) {
                    try {
                        count = vmbs.stopUserTask(dpd);
                    }
                    catch(Exception ex){
                        _exMessage = ex.getMessage();
                    }
                    mTotalClosedCount += count;
                    mCount++;
                    onProgressUpdate();
                }
                //_pdHori.setMessage(getString(R.string.message_stopping_running_tasks));
                vmbs.stopSystemHardware();
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                super.onPostExecute(param);
                Log.i("CKIT", "thread PostExecute");

                if(!_exMessage.equals(""))
                    SupportFunctions.DisplayToastMessageShort(
                            _context, "Error :" + _exMessage);

                _updateDone=true;
                _pdHori.dismiss();
                onButtonCancel();
            }

            @Override
            protected void onPreExecute() {
                Log.i("CKIT", "thread PreExecute");
                super.onPreExecute();

                _pdHori = new ProgressDialogHorizontal(_context,
                        getString(R.string.message_stop_tasks));
                _pdHori.show();
                _pdHori.setMax(_selected.size());
            }

            @Override
            protected void onProgressUpdate(Void... params) {
                if(mCount==_selected.size())
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






}
