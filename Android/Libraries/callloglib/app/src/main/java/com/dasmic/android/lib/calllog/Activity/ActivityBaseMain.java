package com.dasmic.android.lib.calllog.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.telecom.TelecomManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Enum.AppOptions;
import com.dasmic.android.lib.calllog.Extension.CallLogContentObserver;
import com.dasmic.android.lib.calllog.Extension.ListViewAdapter;
import com.dasmic.android.lib.calllog.Interface.IGenericEvent;
import com.dasmic.android.lib.calllog.Interface.IGenericParameterLessEvent;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.StaticFunctions;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelCallLogDisplay;
import com.dasmic.android.lib.support.Ad.Interstilial;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Feedback.ExternalAppInfo;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.AppSettings;
import com.dasmic.android.lib.support.Static.SupportFunctions;


import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 1/28/2016.
 */
public class ActivityBaseMain extends ActivityBaseAd {
    protected int _selectionCount;
    protected boolean mShowUndeleteMessage;
    protected MenuItem mDeleteMenuItem;
    protected boolean _isDefaultCallHandler;

    //Controls
    protected ListViewAdapter _listAdapter;
    protected ListView _listView;
    protected ProgressDialogSpinner _pdSpin;
    protected ProgressDialogHorizontal _pdHori;
    protected ActionBarDrawerToggle _drawerToggle;

    //Classes
    protected ViewModelCallLogDisplay _vmCallLog;
    protected InAppPurchases _inAppPurchases;
    protected String demo_video_id;

    protected DataCallLogDisplay _dcdFirstSelection;
    protected CallLogContentObserver _contentObserver;

    //InApp purchase related
    protected String _base64EncodedPublicKey;
    protected String _paid_version_sku_id;
    protected String _ad_interstitial_id;
    protected Interstilial _interstilial;
    protected EditText _editTextPhone;
    protected boolean _isContentObserverSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowUndeleteMessage=true;
        _isDefaultCallHandler=false;
        _isContentObserverSet = false;
    }

    //Call this from RefreshListView() so that this is called only if permissions are set
    private void setContentObserver(){
        //Registering this content observer will need permissions for WRITE_CALL_LOG
        //This error started coming from 8.x
        if(!_isContentObserverSet) {
            _contentObserver = new CallLogContentObserver(mHandler);
                this.getApplicationContext()
                        .getContentResolver()
                        .registerContentObserver(
                                android.provider.CallLog.Calls.CONTENT_URI, false,
                                _contentObserver);
            _isContentObserverSet=true;
        }
    }

    //Request all call log and contacts permissions
    private boolean checkPermissionsForAndroid6Plus_CallLog_Contacts(){
        int hasReadCallLogPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG);
        int hasWriteCallLogPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CALL_LOG);
        int hasReadContactPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        int hasCallPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE);
        int hasWriteContactPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS);

        if (hasReadCallLogPermission != PackageManager.PERMISSION_GRANTED ||
                hasWriteCallLogPermission != PackageManager.PERMISSION_GRANTED ||
                hasReadContactPermission != PackageManager.PERMISSION_GRANTED ||
                hasWriteContactPermission != PackageManager.PERMISSION_GRANTED ||
                hasCallPermission != PackageManager.PERMISSION_GRANTED) {

                showPermissionsDisclaimerAndGetPermissions();
            return false;
        }
        return true;
    }

    private void showPermissionsDisclaimerAndGetPermissions() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setTitle(this.getResources().getText(R.string.title_ack_confirm));
        dlgAlert.setMessage(
                String.valueOf(this.getResources().getText(
                        R.string.message_permissions_disclaimer)));
        dlgAlert.setCancelable(false);
        dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_permissions_ack), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(getActivity(),new String[]{
                                Manifest.permission.READ_CALL_LOG,
                                Manifest.permission.WRITE_CALL_LOG,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS,
                                Manifest.permission.CALL_PHONE},
                        AppOptions.PERMISSION_ALL_REQUEST);

            }
        });
        dlgAlert.show();
    }


    @Override
    public void onDestroy() { //In App billing cleanup
        try { //This could have a null exception
            this.getApplicationContext()
                    .getContentResolver()
                    .unregisterContentObserver(_contentObserver);
        }
        catch(Exception ex){

        }

        super.onDestroy();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(_vmCallLog == null) return;//Safety check
            //Internal changes can be multiple changes so prevent
            //refresh on internal changes
           if(!_vmCallLog.getInternalDBChange()) //When on internal changes
               InvalidateAndRefreshListView();

        }
    };

    protected void setInternalDBChange(){
        _vmCallLog.setInternalDBChange(true);
    }

    protected void removeInternalDBChange(){
        _vmCallLog.setInternalDBChange(false);
    }

    protected void setSortButton(){
        ImageButton ib = (ImageButton) findViewById(
                R.id.buttonImageSort);
        ib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                _vmCallLog.changeSortOrder();
                InvalidateAndRefreshListView();
                updateSortedImage();
            }
        });
        updateSortedImage();
    }

    protected void updateSortedImage(){
        ImageButton ib = (ImageButton) findViewById(
                                R.id.buttonImageSort);

        if(_vmCallLog.getCurrentSortOrder()) //Then Descending
            ib.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_48dp);
        else
            ib.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_48dp);
    }

    protected int getSearchBoxWidth(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width/2;//Do not let it take more than half
    }

    protected ArrayList<Long> getUniqueRawContactIdList
            (ArrayList<DataCallLogDisplay> callLogs){
        ArrayList<Long> callLogIdList = new ArrayList<Long>();
        for(DataCallLogDisplay cld: callLogs){
            Long cid= new Long(cld.getId());
            if(!callLogIdList.contains(cid))
                callLogIdList.add(cid);
        }
        return callLogIdList;
    }

    protected void singleCheckboxItemSelected(DataCallLogDisplay dcd){

        CheckBox checkAll = (CheckBox) findViewById(
                                R.id.checkSelectAll);
        if(checkAll.isChecked()) checkAll.setChecked(false);
        int value = dcd.isChecked ? 1 : 0;
        if(_selectionCount==0 && value==1)
            _dcdFirstSelection=dcd;

        updateSelectionCountText(value);
    }

    protected void updateSelectionCountText(int value){
        if(value==1) _selectionCount++;
        else _selectionCount--;

        setSelectionCountText();
    }


    protected void UncheckAll(){
        Log.i("CKIT", "UncheckAll");
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setChecked(false);
        onButtonCheckUncheck();
    }

    protected void recomputeSelectionCountValue(){
        _selectionCount=0;
        for(int ii=0;ii<_listAdapter.getCount();ii++){
            if(_listAdapter.getItem(ii).isChecked)
                _selectionCount++;
        }
        setSelectionCountText();
    }


    protected void onButtonCheckUncheck(){
        Log.i("CKIT", "ActivityMain::onCheckUncheck");
        //Do not process _listAdapter is null
        if(_listAdapter == null) return;
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        //_vmCallLog.allChecked = !_vmCallLog.allChecked;
        for(int ii=0;ii<_listAdapter.getCount();ii++){
            _listAdapter.getItem(ii).isChecked=
                    checkAll.isChecked();
        }

        if(checkAll.isChecked()) _selectionCount=_listAdapter.getCount();
        else _selectionCount=0;

        setSelectionCountText();
        _listView.invalidateViews();
    }


    protected void setItemCountText(){
        TextView textDisplayCount = (TextView) findViewById(
                R.id.textDisplayCount);
        textDisplayCount.setText(String.valueOf(_listAdapter.getCount()).trim());
    }

    protected void setSelectionCountText(){
        TextView textDisplayCount = (TextView) findViewById(R.id.textSelectCount);
        textDisplayCount.setText(String.valueOf(_selectionCount).trim());
    }

    protected boolean checkSelection(){
        return checkSelection(true);
    }

    protected boolean checkSelection(boolean checkMin){
        ArrayList<DataCallLogDisplay> checkedItems= null;
        int size=0;
        checkedItems =
                _vmCallLog.getCheckedItems();
        size=checkedItems.size();

        //dcdFirstSelection used by merge contacts
        //If it is null set it to first selection
        //if checkbox is set from code (not user selected)
        //dcdFirstSelection is null
        if(_dcdFirstSelection==null && size > 0)
            _dcdFirstSelection=checkedItems.get(0);


        if(checkMin) //Dont do this check if checkMin is false, in BT Transfer
            if(size <= 0 || _dcdFirstSelection==null){
                SupportFunctions.DisplayToastMessageShort(
                        this, (String) this.getResources().getText(
                                R.string.message_selectitems));
                SupportFunctions.vibrate(this,500);
                return false;
            }
        if(AppOptions.isFreeVersion){
            if(size > AppOptions.FREE_VERSION_LIMIT) {
                SupportFunctions.DisplayToastMessageLong(
                        this, (String) this.getResources().getString(
                                R.string.message_free_version_selection)+
                                String.valueOf(AppOptions.FREE_VERSION_LIMIT));
                SupportFunctions.vibrate(this,500);
                return false;
            }
        }
        return true;
    }




    protected void onButtonDelete(){
        if(checkSelection()){
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle(this.getResources().getText(R.string.title_confirm));
            dlgAlert.setMessage(
                        String.valueOf(this.getResources().getText(
                                R.string.message_delete_confirm_cl)));
            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_Yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
            deleteContacts();
            dialog.dismiss();
                }
            });

            dlgAlert.setNegativeButton(
                    this.getResources().getText(R.string.button_No),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = dlgAlert.create();
            alert.show();
        }
    }


    protected void deleteContacts()
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            private int _totCount;
            private int _delCount;
            ArrayList<Long> _allIds;

            @Override
            protected Void doInBackground(Void...params) {
                for(Long id: _allIds){
                    try {
                        _delCount += _vmCallLog.deleteCallLog(id);
                        this.publishProgress();
                        Thread.sleep(50);
                    }
                    catch(Exception ex){
                        //Mainly for sleep
                    }
                }
                return null;
            }


            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                _pdHori.dismiss();
                InvalidateAndRefreshListView();


                SupportFunctions.DisplayToastMessageShort(getContext(),
                        String.valueOf(_delCount) + " " +
                                getContext().getText(
                                        R.string.message_delete_complete_cl).toString());
            }


            @Override
            protected void onPreExecute() {
                _delCount=0;
                _vmCallLog.setInternalDBChange(true);
                _allIds =
                        getUniqueRawContactIdList(
                                _vmCallLog.getCheckedItems());
                _totCount= _allIds.size();
                _pdHori = new ProgressDialogHorizontal(getActivity(),
                        getString(R.string.progressbar_delete_data)+
                                " "+
                                String.valueOf(_delCount) +
                                "/" +
                                String.valueOf(_totCount)+
                                "...");
                _pdHori.setMax(_totCount);
                _pdHori.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {
                _pdHori.setProgress(_delCount);
                _pdHori.setMessage( getString(R.string.progressbar_delete_data)+
                        " " +
                        String.valueOf(_delCount) +
                        "/" +
                        String.valueOf(_totCount)+
                        "...");
            }
        }
        Log.i("CKIT", "ActivityMain::onDeleteContacts");
        _pdSpin.setMessage
                (getString(R.string.progressbar_delete_data)+
                        " 0/0");
        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask();
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        }
        catch(Exception ex){
            Log.i("CKIT","Exception in Main::delete"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }


    protected Activity getActivity(){
        return this;
    }

    protected void InvalidateAndRefreshListView(boolean value){
        _vmCallLog.InvalidateData();//Will clean history of selection, be careful
        RefreshListView();
    }

    protected void InvalidateAndRefreshListView(){
        _vmCallLog.InvalidateData();//Will clean history of selection, be careful
        removeInternalDBChange();
        RefreshListView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        boolean failed=true; //Flag to denote failed
        switch (requestCode) {
            case AppOptions.PERMISSION_ALL_REQUEST:
                if(grantResults.length == 5)
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                        RefreshListView(); //Also refresh list view
                        failed=false;
                    }
                if(failed)
                {
                    SupportFunctions.DisplayToastMessageLong(this,
                            getString(R.string.message_contact_permissions_missing));
                    finish();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);
        }
    }

    //Invalidate current data and reload listView
    protected void RefreshListView() {
        //All contacts and call log permissions have been given by this stage
        if(checkPermissionsForAndroid6Plus_CallLog_Contacts()) {
            setContentObserver();
            if (!AppOptions.IS_FOR_AMAZON)
                RateThisApp.ShowRateAppDialog(this); //To show Rate this app

            UncheckAll();

            //Empty the SearchBox ------------
            SearchView searchView = (SearchView) findViewById(R.id.textSearch);
            if (searchView != null) {
                searchView.setQuery("", false);
                searchView.clearFocus();
            }

            //--------------------------------------- Ad
            if (AppOptions.isFreeVersion) { //Only show for free version
                if (_interstilial == null && !_ad_interstitial_id.trim().equals(""))
                    _interstilial = new Interstilial(_ad_interstitial_id, this);

                if (_interstilial != null)
                    _interstilial.showAd(); //Show during load
            }
            //------------------
            showPreDisplayMessageAndPopulateListBox();
            setNavigationMenuSelectedItems();
        }
    }

    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {

        if (true) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case (AppOptions.PERMISSION_DEFAULT_DIALER_REQUEST):
                    //if (resultCode == Activity.RESULT_OK)
                        RefreshListView();
                        break;
            }
        }
    }
    //Ask only once
    protected void askUserToSetAsDefaultDialer(){
        //Dont check for this in older version
        if(!_isDefaultCallHandler) return;
        if(android.os.Build.VERSION.SDK_INT < 23) return;

        //final String default_dialer="make_default_dialer";
        //int currentSetting = AppSettings.getPreferencesInt(this,default_dialer);
        final String packageName =  getActivity().getPackageName();

        // This needs min API 23
        // TelecomManager telecomManager  = getSystemService(Context.TELECOM_SERVICE);
        // if(telecomManager.getDefaultDialerPackage().equals(packageName))

        //Request to made the Default Call Handler
        //if(currentSetting < 2) {
            Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    packageName);
            startActivityForResult(intent, AppOptions.PERMISSION_DEFAULT_DIALER_REQUEST);
            //AppSettings.setPreferencesInt(this, default_dialer,currentSetting + 1);
        //}
        //Setting to only show once
    }

    protected void showPreDisplayMessageAndPopulateListBox(){
        switch(_vmCallLog.getCurrentDisplayOption()){

            default:
                populateListBoxCurrentDisplayOption();
                break;
        }

    }

    protected void setNavigationMenuSelectedItems(){
        //Unselect all
        NavigationView navigationView = (NavigationView)
                findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_view_all_cl).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_view_geocoded_location_cl).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_view_incomingcalls_cl).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_view_outgoingcalls_cl).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_view_missedcalls_cl).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_view_call_duration_cl).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_view_100_records_cl).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_view_500_records_cl).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_view_all_records_cl).setChecked(false);

        switch(_vmCallLog.getCurrentDisplayOption()){
            case AllCalls:
                navigationView.getMenu().findItem(R.id.nav_view_all_cl).setChecked(true);
                break;
            case MissedCalls:
                navigationView.getMenu().findItem(R.id.nav_view_missedcalls_cl).setChecked(true);
                break;

            case IncomingCalls:
                navigationView.getMenu().findItem(R.id.nav_view_incomingcalls_cl).setChecked(true);
                break;

            case OutgoingCalls:
                navigationView.getMenu().findItem(R.id.nav_view_outgoingcalls_cl).setChecked(true);
                break;

            case SortedByGeocodedLocation:
                navigationView.getMenu().findItem(R.id.nav_view_geocoded_location_cl).setChecked(true);
                break;

            case SortedByCallDuration:
                navigationView.getMenu().findItem(R.id.nav_view_call_duration_cl).setChecked(true);
                break;
        }

        switch(_vmCallLog.getCurrentDisplayCount()) {
            case View100:
                navigationView.getMenu().findItem(R.id.nav_view_100_records_cl).setChecked(true);
                break;

            case ViewAll:
                navigationView.getMenu().findItem(R.id.nav_view_all_records_cl).setChecked(true);
                break;

            case View500:
                navigationView.getMenu().findItem(R.id.nav_view_all_records_cl).setChecked(true);
                break;
        }
    }


    //Populate the listbox here
    protected void populateListBoxCurrentDisplayOption(){
        //Body of your click handler

        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;

            //Add Handler


            BasicAsyncTask(Activity context) {
                super();
                _context = context;
            }

            @Override
            protected Void doInBackground(Void...params) {
                try {
                    ArrayList<DataCallLogDisplay> mainList =
                            _vmCallLog.getCurrentDisplayOptionContacts();
                    _listAdapter = new ListViewAdapter(_context,
                            mainList,new ListViewAdapter.getColorBasedOnData() {

                        public int
                        onGetColorBasedOnData(DataCallLogDisplay contact) {
                            return getColor(contact);
                        }});
                } catch(Exception ex){
                    throw new RuntimeException(ex);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                resetListBox();
                _pdSpin.dismiss();

                postContactsLoadOperation();
            }

            @Override
            protected void onPreExecute() {
                _pdSpin.setMessage
                        (_context.getResources().getText(
                                R.string.progressbar_load_data_cl).toString());
                _pdSpin.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {
            }
        }
        new BasicAsyncTask(this).execute();
    }

    protected void customContactsLoadOperation(){

    }


    //Run after contacts have been reloaded
    protected void postContactsLoadOperation(){
        switch(_vmCallLog.getCurrentDisplayOption()){
            /*case Duplicates:
                doDuplicateOperation();
                break;
            case Deleted:
                //set icon
                if(mDeleteMenuItem != null)
                    mDeleteMenuItem.setIcon(R.drawable.ic_delete_forever_black_48dp);
                showPostUndeleteMessage(); //This will also set menu icon
                break;*/
            default:
                if(mDeleteMenuItem != null)
                    mDeleteMenuItem.setIcon(android.R.drawable.ic_menu_delete);
                break;
        }
        customContactsLoadOperation();//For any custom post load
    }

    protected void  resetListBox(){
        _listView.setAdapter(_listAdapter);
        _listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        _listView.setTextFilterEnabled(true);

        setItemCountText();
        setSelectionCountText();
        _listAdapter.setListener(new IGenericParameterLessEvent() {
            public void onEvent() {
                // do something with item
                setItemCountText();
            }
        });
        _listAdapter.setListener(new IGenericEvent() {
            public void onEvent(DataCallLogDisplay dcd) {
                // do something with item
                singleCheckboxItemSelected(dcd);
            }
        });
    }

    protected int getColor(DataCallLogDisplay contact){return 0;}

    protected Context getContext(){
        return this;
    }

    protected void openCojuLink(){
        ExternalAppInfo.ShowExternalAppDownloadDialog(this,
                getString(R.string.title_dialog_coju),
                getString(R.string.message_download_coju),
                "com.dasmic.android.coju");
    }

    //Contacts Backup and Restore
    protected void RestoreContacts(){
        /*iewModelImport vmImport = new ViewModelImport(this,null);
        if(!vmImport.RestoreAllContactsFromDefaultBackupFile()){
            SupportFunctions.DisplayToastMessageLong(this,
                    getResources().getText(
                            R.string.message_restore_fail).toString());
        }*/
    }

    protected void BackupContacts(){
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            String _folder;
            boolean _exceptionInBackup;

            BasicAsyncTask(Activity context) {
                super();
                _context = context;
            }

            @Override
            protected Void doInBackground(Void...params) {
                try {
                    _exceptionInBackup = false;
                    //ViewModelExport vmExport=new ViewModelExport(_context,
                    //        _context.getResources().getText(
                    //                R.string.app_name_small).toString());
                    //_folder=vmExport.StoreAllContactsInBackupFile_ExternalNoDate(
                    //        AppOptions.isFreeVersion);

                } catch(Exception ex){
                    _exceptionInBackup=true;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                _pdSpin.dismiss();

                if(!_exceptionInBackup) {
                    if (AppOptions.isFreeVersion)
                        SupportFunctions.DisplayToastMessageLong(_context,
                                _context.getResources().getString(
                                        R.string.message_backup_stored_freeversion)
                                        + _folder);
                    else
                        SupportFunctions.DisplayToastMessageLong(_context,
                                _context.getResources().getText(
                                        R.string.message_backup_stored).toString() +
                                        _folder);
                }
                else
                    SupportFunctions.AsyncDisplayGenericDialog(_context,
                            _context.getString(R.string.message_backup_failed),
                            _context.getString(R.string.app_name));
                _context=null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                _pdSpin.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {
            }

        }
        _pdSpin.setMessage
                (this.getResources().getText(
                        R.string.progressbar_backup).toString());
        new BasicAsyncTask(this).execute();

    }

    //InApp purchase related ---------------
    protected void setupForInAppPurchase() {
        try {

            _inAppPurchases = new InAppPurchases(this,
                    _paid_version_sku_id,
                    _base64EncodedPublicKey);

            _inAppPurchases.SetupInAppPurchase(
                    new InAppPurchases.OnIAPSetupFinishedListener() {
                        public void onIAPSetupFinished(
                                IabResult result,
                                boolean isPaidVersion) {
                            AppOptions.isFreeVersion = !isPaidVersion;
                            // update UI accordingly
                            setLocalVersionBasedUI(); //Don't move this as its called Async
                        }
                    });
        }
        catch(Exception ex)
        {
            //Setup failed, default to free
            AppOptions.isFreeVersion = true;
            // update UI accordingly
            setLocalVersionBasedUI(); //Dont move this
        }
    }

    protected void doSetupForDirectEditTextPhone(){
        _editTextPhone = (EditText) findViewById(R.id.editTextPhoneNumber);
        //Set event
        _editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //here is your code
                onPhoneNumberEntry();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        ImageButton phoneImage = (ImageButton) findViewById(R.id.buttonImagePhoneCall);
        phoneImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String number = _editTextPhone.getText().toString ();
                StaticFunctions.makeCall(getActivity(),number);
            }
        });

        ImageButton phoneSearch = (ImageButton) findViewById(R.id.buttonImageSearch);
        phoneSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String number = _editTextPhone.getText().toString ();
                StaticFunctions.searchNumber(getActivity() ,number);
            }
        });

        //Set Intent for phone
        _editTextPhone.setText(getPhoneNumberFromFromIntent());

    }

    //See if some intent called the dialer
    private String getPhoneNumberFromFromIntent(){
        Intent intent = getIntent();
        if(intent != null){
            Uri uri =intent.getData();
            if(uri != null) {
                String [] numbers = uri.toString().split(":");
                if(numbers.length != 2) return "";
                if(!numbers[0].trim().equals("tel")) return "";
                return numbers[1].trim();
            }
        }

        return "";
    }

    public void onPhoneNumberEntry(){
        String newText = _editTextPhone.getText().toString();
        if(_listAdapter != null)
            _listAdapter.getFilter().filter(newText);
    }


    protected void makeCall(String number)
    {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        //Ask for permission
        try {
            this.startActivity(intent);
        }
        catch(SecurityException ex)
        {
            //Throw message
            SupportFunctions.DisplayToastMessageLong(this,
                    this.getString(R.string.message_call_security_exception));
        }
    }


    protected void purchasePaidVersion(){
        try {
            _inAppPurchases.PurchasePaidVersion(new
                                                        InAppPurchases.OnIAPPurchaseFinishedListener() {
                                                            public void onIAPPurchaseFinished(
                                                                    IabResult result,
                                                                    boolean isPaidVersion) {
                                                                AppOptions.isFreeVersion = !isPaidVersion;
                                                                // update UI accordingly
                                                                setLocalVersionBasedUI();
                                                            }
                                                        });
        }
        catch(Exception ex){
            //Add message
            SupportFunctions.AsyncDisplayGenericDialog(this,
                    getString(R.string.message_purchase_error),
                    getString(R.string.app_name));
        }
    }

    protected void setLocalVersionBasedUI(){
        MenuItem item=null;
        Menu navMenu=null;
        NavigationView navView = (NavigationView)
                findViewById(R.id.nav_view);
        if (navView != null) {
            navMenu = navView.getMenu();
            if(navMenu != null)
                item = navMenu.findItem(
                        R.id.nav_view_upgrade_paid_version_cl);
            setVersionBasedUI(item, true);
        }
    }


    //-------------------------------------------------------------------


}
