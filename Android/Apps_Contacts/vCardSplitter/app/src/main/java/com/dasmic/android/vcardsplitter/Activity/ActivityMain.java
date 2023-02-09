package com.dasmic.android.vcardsplitter.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Feedback.Feedback;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.Feedback.TellAFriend;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.vcardsplitter.Data.DataValuePair;
import com.dasmic.android.vcardsplitter.Enum.ActivityOptions;
import com.dasmic.android.vcardsplitter.R;
import com.dasmic.android.vcardsplitter.ViewModel.ViewModelDisplay;
import com.dasmic.android.vcardsplitter.ViewModel.ViewModelSplit;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ActivityMain extends ActivityBaseAd
        implements NavigationView.OnNavigationItemSelectedListener{
    protected final String tagUpdateDone="Update Done";
    protected ViewModelDisplay _vmDisplay;
    protected ListView _listView;
    protected Uri _selectedFileUri;
    protected String _selectedFileName;
    protected long _selectedFileSize;

    protected ActionBarDrawerToggle _drawerToggle;
    protected ProgressDialogSpinner _progressDialog;

    final String helpURL =
            "http://www.coju.mobi/android/vcardsplitter/faq/index.html";
    final String  demo_video_id = "O2iRMuIPbwI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main);
        if(checkPermissions()) {
            setLocalVariablesAndEventHandlers();
            initializeDisplay();
        }

        setupForInAppPurchase();
        _ad_interstitial_id=getString(R.string.ad_main_is_id); //For Interstitial Ads
        setDrawerMenu();
        HelpMedia.ShowDemoVideoDialog(this,demo_video_id);

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
            ViewModelSplit.cleanUpInternalFolder(this);

        }
        catch(Exception ex){

        }
    }

    //Only ask for permission to write in External Storage
    protected boolean checkPermissions(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        boolean reload=false;
        boolean refresh=false;
        Intent myIntent;
        switch(id){
            case R.id.nav_view_rate_this_app:
                RateThisApp rtp=new RateThisApp();
                rtp.ShowRateThisApp(this);
                break;

            case R.id.nav_view_upgrade_paid_version:
                PurchasePaidVersion();
                break;
            case R.id.nav_view_feedback:
                Feedback.SendFeedbackByEmail(this);
                break;
            case R.id.nav_view_demo_video:
                HelpMedia.ShowDemoVideo(this,
                        demo_video_id);
                break;
            case R.id.nav_view_tell_a_friend:
                TellAFriend.TellAFriend(this,
                        getString(R.string.message_tellafriend));
                break;
            case R.id.nav_view_help_documentation:
                HelpMedia.ShowWebURL(this,helpURL);
                break;
            case R.id.nav_view_refresh:
                refresh=true;
                break;
            default:
                break;
        }

        //Do Refresh

        DrawerLayout drawer = (DrawerLayout)
                findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        if(refresh)
            initializeDisplay();
        return true;
    }

    @Override
    public void onDestroy() { //In App billing cleanup
        super.onDestroy();
        if(_progressDialog != null) //Make sure no process dialog is running
            _progressDialog.dismiss();
    }


    protected void onButtonCancel() {
        finish();
    }

    protected void onButtonApply() {

        if(_selectedFileUri == null){
            SupportFunctions.DisplayToastMessageLong(this,
                    getString(R.string.message_select_file_first));
            SupportFunctions.vibrate(this,200);

        }
        _vmDisplay.setSelectedFile(_selectedFileUri,
                                _selectedFileName,
                                _selectedFileSize);
        Intent myIntent = new Intent(this, ActivitySplit.class);
        this.startActivityForResult(myIntent,
                ActivityOptions.SPLIT_ACTIVITY_REQUEST);
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
        //Ideally cleanup should be done in splitting but its leading to a problem there
        cleanInternalFolder();//No Temporary files are shown
        searchFilesInLocalStorage();
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

        if(_vmDisplay.isValidFileName(fileName)) {
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
        _selectedFileSize=Long.valueOf(sFileSize);
        displayInterstitialAd(); //Display ad when updating info
    }

    protected void setSelectedFileUri(Uri uri, String fileName){
            _selectedFileUri =uri;
    }

    protected void searchFilesInLocalStorage() {
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
                            _vmDisplay.getListofVCFFilesInPhone();
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
                if(_mainList.size() >0) {
                    onListItemSelected(_mainList.get(0));
                }
                else
                { //Let User know
                    SupportFunctions.AsyncDisplayGenericDialog(_context,
                            _context.getString(R.string.message_no_vcard_files),
                            _context.getString(R.string.app_name));
                }

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


    protected void setLocalVariablesAndEventHandlers() {
        _vmDisplay = ViewModelDisplay.getInstance(this);
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

    //Add Items to the action bar here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

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

    private void setDrawerMenu(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        _drawerToggle = new ActionBarDrawerToggle(
                this,drawer ,R.string.app_name,R.string.app_name);
        drawer.setDrawerListener(_drawerToggle);
        _drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Set Navigation View listener
        NavigationView navigationView = (NavigationView)

                findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


}
