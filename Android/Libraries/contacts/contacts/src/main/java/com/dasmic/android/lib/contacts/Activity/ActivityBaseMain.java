package com.dasmic.android.lib.contacts.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.dasmic.android.lib.contacts.Data.DataContactDisplay;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.contacts.Enum.FilterOptionsEnum;
import com.dasmic.android.lib.contacts.Extension.ListViewAdapter;
import com.dasmic.android.lib.contacts.Interface.IGenericEvent;
import com.dasmic.android.lib.contacts.Interface.IGenericParameterLessEvent;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelContactsDisplay;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelDuplicates;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelExport;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelImport;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelMergeContacts;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelOperations;
import com.dasmic.android.lib.support.Ad.Interstilial;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Feedback.ExternalAppInfo;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 1/28/2016.
 */
public class ActivityBaseMain extends ActivityBaseAd {
    protected int _selectionCount;
    protected boolean mShowUndeleteMessage;
    protected MenuItem mDeleteMenuItem;
    //Controls
    protected ListViewAdapter _listAdapter;
    protected ListView _listView;
    protected ProgressDialogSpinner _pdSpin;
    protected ProgressDialogHorizontal _pdHori;
    protected ActionBarDrawerToggle _drawerToggle;
    protected DataContactDisplay _dcdFirstSelection;
    //InApp purchase related
    protected String _base64EncodedPublicKey;
    protected String _paid_version_sku_id;

    //Classes
    protected ViewModelContactsDisplay _vmContacts;
    protected InAppPurchases _inAppPurchases;
    protected String demo_video_id;
    protected String _ad_interstitial_id;
    protected Interstilial _interstilial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowUndeleteMessage=true;
    }

    protected int getSearchBoxWidth(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width*2/5; //Update ratio in new version, Oct. 13th
    }

    protected void singleCheckboxItemSelected(DataContactDisplay dcd){
        CheckBox checkAll = (CheckBox) findViewById(
                R.id.checkSelectAll);
        if(checkAll.isChecked()) checkAll.setChecked(false);
        int value = dcd.isChecked ? 1 : 0;
        if(_selectionCount==0 && value==1)
            _dcdFirstSelection=dcd;

        updateSelectionCountText(value);
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
                            ActivityOptions.isFreeVersion = !isPaidVersion;
                            // update UI accordingly
                            setLocalVersionBasedUI(); //Don't move this as its called Async
                        }
                    });
        }
        catch(Exception ex)
        {
            //Setup failed, default to free
            ActivityOptions.isFreeVersion = true;
            // update UI accordingly
            setLocalVersionBasedUI(); //Dont move this
        }
    }


    protected void purchasePaidVersion(){
        try {
            _inAppPurchases.PurchasePaidVersion(new
                                                        InAppPurchases.OnIAPPurchaseFinishedListener() {
                                                            public void onIAPPurchaseFinished(
                                                                    IabResult result,
                                                                    boolean isPaidVersion) {
                                                                ActivityOptions.isFreeVersion = !isPaidVersion;
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
                        R.id.nav_view_upgrade_paid_version);
            setVersionBasedUI(item, true);
        }
    }

    //-------------------------------------------------------------------
    protected void updateSelectionCountText(int value){
        if(value==1) _selectionCount++;
        else _selectionCount--;
        setSelectionCountText();
    }

    protected  boolean isForWhatsApp() {
        if(_vmContacts != null)
            return _vmContacts.getIsForWhatsApp();
        return false;
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
        //_vmContacts.allChecked = !_vmContacts.allChecked;
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
        textDisplayCount.setText(_listAdapter.getCount() +
                " " + getResources().getText(R.string.general_items));
    }


    protected void setSelectionCountText(){
        TextView textDisplayCount = (TextView) findViewById(R.id.textSelectCount);
        textDisplayCount.setText(_selectionCount +
                " " + getResources().getText(R.string.general_selected_items));
    }

    protected boolean checkSelection(){
        return checkSelection(true);
    }

    protected boolean checkIsUndeleted(){
        if(isUndeleteDisplay()) {
            SupportFunctions.DisplayToastMessageLong(this,
                    getString(R.string.message_undelete_option_invalid));
            return true;
        }
        return false;
    }


    protected boolean checkSelection(boolean checkMin){
        ArrayList<DataContactDisplay> checkedItems= null;
        int size=0;
        checkedItems =
                _vmContacts.getCheckedItems();
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
        if(ActivityOptions.isFreeVersion){
            if(size > ActivityOptions.FREE_VERSION_CONTACT_LIMIT) {
                SupportFunctions.DisplayToastMessageLong(
                        this, (String) this.getResources().getString(
                                R.string.message_free_version_selection)+
                                String.valueOf(ActivityOptions.FREE_VERSION_CONTACT_LIMIT));
                SupportFunctions.vibrate(this,500);
                return false;
            }
        }
        return true;
    }

    protected void onMergeContacts(){

        if(checkSelection()){
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle(this.getResources().getText(R.string.message_merge_title));

            String name=_dcdFirstSelection.getPrimaryValue();

            dlgAlert.setMessage(
                        String.valueOf(this.getResources().getText(
                                R.string.message_merge_confirm)+
                                " '" + name + "'" ));

            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_Yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    mergeContacts();
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

    protected void mergeContacts()
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            private int _totCount;
            private int _count;
            ArrayList<Long> _allContactIds;

            private final Handler mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //FragmentActivity activity = getActivity();
                    switch (msg.what) {
                        case ViewModelMergeContacts.MERGE_COUNT_UPDATE:
                            _count = msg.arg1;
                            onProgressUpdate();
                            break;
                        default:
                    }
                }
            };


            @Override
            protected Void doInBackground(Void...params) {
                    try {
                        //Remove First selection from allContacts If and put it at index 0
                        ViewModelMergeContacts vmmc=new ViewModelMergeContacts(
                                getActivity(),mHandler);
                        vmmc.mergeContacts(_allContactIds,_dcdFirstSelection.getRawContactId());
                    }
                    catch(Exception ex){
                        //Mainly for sleep
                    }
                return null;
            }


            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                _pdHori.dismiss();
                RefreshListView();
                SupportFunctions.DisplayToastMessageShort(getContext(),
                                getContext().getText(
                                        R.string.message_merge_complete).toString());
                showFullContactCard(_dcdFirstSelection.getContactId());
            }


            @Override
            protected void onPreExecute() {
                _count=0;
                _allContactIds=
                        getUniqueRawContactIdList(
                                _vmContacts.getCheckedItems());

                //Remove First Selection from Raw Contact operation
                //This is to prevent duplication of op.
                for(int ii=0;ii<_allContactIds.size();ii++)
                    if(_allContactIds.get(ii) == _dcdFirstSelection.getRawContactId()) {
                        _allContactIds.remove(ii);
                        break;
                    }

                _totCount=_allContactIds.size();
                _pdHori = new ProgressDialogHorizontal(getActivity(),
                        getString(R.string.progressbar_merge_data));
                _pdHori.setMax(_totCount);
                _pdHori.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {
                _pdHori.setProgress(_count);
            }
        }
        Log.i("CKIT", "ActivityMain::onMergeContacts");
        _pdSpin.setMessage
                (getString(R.string.progressbar_merge_data)+
                        " 0/0");
        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask();
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        }
        catch(Exception ex){
            Log.i("CKIT","Exception in Main::merge"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }


    protected void onButtonDelete(){
        if(checkSelection()){
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle(this.getResources().getText(R.string.title_confirm));
            if(isUndeleteDisplay()) {
                dlgAlert.setMessage(
                        String.valueOf(this.getResources().getText(
                                R.string.message_undelete_confirm)));
            }
            else
                dlgAlert.setMessage(
                    String.valueOf(this.getResources().getText(
                            R.string.message_delete_confirm)));

            dlgAlert.setCancelable(false);

            dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_Yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if(isUndeleteDisplay())
                        unDeleteContacts();
                    else
                        DeleteContacts();
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




    protected boolean isUndeleteDisplay(){
        if(_vmContacts.getCurrentDisplayOption()==DisplayOptionsEnum.Deleted)
            return true;
        else
            return false;
    }

    protected void DeleteContacts()
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            private int _totCount;
            private int _delCount;
            ArrayList<Long> _allContactIds;

            @Override
            protected Void doInBackground(Void...params) {
                for(Long id:_allContactIds){
                    try {
                        _delCount += _vmContacts.DeleteContact(id);
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
                RefreshListView();
                SupportFunctions.DisplayToastMessageShort(getContext(),
                        String.valueOf(_delCount) + " " +
                                getContext().getText(
                                        R.string.message_delete_complete).toString());
                //Revert back to default display every time after delete is done in duplicated
                //This will prevent duplicated ops from running again when auto-refresh happens
                //This line prevent contacts from being deleted in duplicated if placed elsewhere
                if(_vmContacts.getCurrentDisplayOption() == DisplayOptionsEnum.Duplicates)
                    _vmContacts.setCurrentDisplayOption(DisplayOptionsEnum.AllInformation);
            }


            @Override
            protected void onPreExecute() {
                    _delCount=0;


                    _allContactIds=
                        getUniqueRawContactIdList(
                                _vmContacts.getCheckedItems());
                    SupportFunctions.DebugLog("ActivityMain",
                            "Delete","Delete count is: " + _allContactIds.size());

                    _totCount=_allContactIds.size();
                    _pdHori = new ProgressDialogHorizontal(getActivity(),
                        getString(R.string.progressbar_delete_data)+
                                "...");
                    _pdHori.setMax(_totCount);
                    _pdHori.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {
                _pdHori.setProgress(_delCount);
                _pdHori.setMessage( getString(R.string.progressbar_delete_data)+
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

    protected void blockContacts(boolean blockFlag)
    {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            boolean _blockFlag;
            boolean _error;

            BasicAsyncTask(Activity context, boolean blockFlag)
            {
                super();
                _context = context;
                _blockFlag = blockFlag;
            }

            @Override
            protected Void doInBackground(Void... params) {
                ViewModelOperations vmOperations =
                        new ViewModelOperations(_context);

                ArrayList<Long> selContacts =
                        getUniqueContactIdList(_vmContacts.getCheckedItems());
                try {
                    if (_blockFlag) {
                        vmOperations.UpdateSendToVoiceMail(selContacts, true);
                    } else {
                        vmOperations.UpdateSendToVoiceMail(selContacts, false);
                    }
                    _error=false;
                }
                catch(Exception ex){
                    _error = true;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                super.onPostExecute(param);
                Log.i("CKIT", "thread PostExecute");

                _pdHori.dismiss();
                if(_error)
                    SupportFunctions.DisplayToastMessageLong(_context,
                            String.valueOf(
                                    _context.getString(R.string.message_block_unblock_error).toString()));
                else
                    SupportFunctions.DisplayToastMessageShort(_context,
                            String.valueOf(
                                    _context.getString(R.string.message_block_unblock_complete).toString()));
                RefreshListView();
            }

            @Override
            protected void onPreExecute() {
                Log.i("CKIT", "thread PreExecute");
                super.onPreExecute();
                if(_blockFlag)
                    _pdHori = new ProgressDialogHorizontal(getActivity(),
                        getString(R.string.message_blocking_contacts)+
                                "...");
                else
                    _pdHori = new ProgressDialogHorizontal(getActivity(),
                            getString(R.string.message_unblocking_contacts)+
                                    "...");
                _pdHori.show();
            }

            @Override
            protected void onProgressUpdate(Void... params) {
            }
        }

        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(this,blockFlag);
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


    protected void unDeleteContacts()
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            private int _totCount;
            private int _delCount;
            ArrayList<Long> _allContactIds;

            @Override
            protected Void doInBackground(Void...params) {
                for(Long id:_allContactIds){
                    try {
                        _delCount += _vmContacts.unDeleteContact(id);
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
                RefreshListView();
                SupportFunctions.DisplayToastMessageShort(getContext(),
                        String.valueOf(_delCount) + " " +
                                getContext().getText(
                                        R.string.message_undelete_complete).toString());
            }


            @Override
            protected void onPreExecute() {
                _delCount=0;


                _allContactIds=
                        getUniqueRawContactIdList(
                                _vmContacts.getCheckedItems());
                _totCount=_allContactIds.size();
                _pdHori = new ProgressDialogHorizontal(getActivity(),
                        getString(R.string.progressbar_undelete_data)+" "+
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
                _pdHori.setMessage( getString(R.string.progressbar_undelete_data)+
                        " "+
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

    protected void RefreshListView(boolean value){
        _vmContacts.InvalidateData();//Will clean history of selection, be careful
        //Check for permissions
        if(checkBasicContactPermissionsForAndroid6Plus())
            ReloadListView(value);
    }

    //Reload data and the listView
    protected void RefreshListView(){
        _vmContacts.InvalidateData();//Will clean history of selection, be careful
        //Check for permissions
        if(checkBasicContactPermissionsForAndroid6Plus())
            ReloadListView(false);
    }

    protected void ReloadListView() {
        //Check for permissions
        if(checkBasicContactPermissionsForAndroid6Plus())
            ReloadListView(false);
    }

    //In Adroid both read and write permission have to granted explicitly
    protected boolean checkBasicContactPermissionsForAndroid6Plus(){
        boolean flagRead= checkContactReadPermissionsForAndroid6Plus();
        boolean flagWrite=checkContactWritePermissionsForAndroid6Plus();

        return (flagRead & flagWrite);
    }

    protected boolean checkContactWritePermissionsForAndroid6Plus(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
                            Manifest.permission.WRITE_CONTACTS},
                    ActivityOptions.PERMISSION_CONTACTS_REQUEST);
            return false;
        }
        else
            return true;
    }

    //Check read permission
    protected boolean checkContactReadPermissionsForAndroid6Plus(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
                            Manifest.permission.READ_CONTACTS},
                    ActivityOptions.PERMISSION_CONTACTS_REQUEST);
            return false;
        }
        else
            return true;
    }

    private boolean checkExternalStoragePermissionsForAndroid6Plus(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
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
            case ActivityOptions.PERMISSION_CONTACTS_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    ReloadListView();
                } else {
                    // Permission Denied
                    SupportFunctions.DisplayToastMessageLong(this,
                            getString(R.string.message_contact_permissions_missing));
                    finish();
                }
                break;
            case ActivityOptions.PERMISSION_STORAGE_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                } else {
                    // Permission Denied
                    SupportFunctions.DisplayToastMessageLong(this,
                            getString(R.string.message_contact_permissions_missing));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //Reload listView with current data
    protected void ReloadListView(boolean
                                  doDuplicatesOp) {
        if(!ActivityOptions.IS_FOR_AMAZON)
            RateThisApp.ShowRateAppDialog(this); //To show Rate this app

        UncheckAll();
        if(doDuplicatesOp) {
                _vmContacts.setCurrentDisplayOption(
                    DisplayOptionsEnum.Duplicates);
        }

        //Empty the SearchBox ------------
        SearchView searchView = (SearchView) findViewById(R.id.textSearch);
        if(searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        //--------------------------------------- Ad
        //Show only for free version
        if(_ad_interstitial_id != null && ActivityOptions.isFreeVersion) { //If not even defined
            if (_interstilial == null && !_ad_interstitial_id.trim().equals(""))
                _interstilial = new Interstilial(_ad_interstitial_id, this);

            if (_interstilial != null)
                _interstilial.showAd(); //Show during load
        }
        //------------------
        showPreDisplayMessageAndPopulateListBox();
    }


    protected void showPreDisplayMessageAndPopulateListBox(){
        setNavigationMenuSelectedItems();
        switch(_vmContacts.getCurrentDisplayOption()){
            case Deleted:
                //set icon
                showPreUndeleteMessageAndPopulateListBox();
                break;
            default:
                PopulateListBoxCurrentDisplayOption();
                break;
        }

    }

    //In new thread due to progressbar
    protected void doDuplicateOperation(){
        //Body of your click handler
        class BasicAsyncTask extends AsyncTask<Void, Integer, Void> {
            Activity _context;
            ViewModelDuplicates _vmd;
            BasicAsyncTask(Activity context) {
                super();
                _context = context;
            }

            @Override
            protected Void doInBackground(Void...params) {
                _vmd=new ViewModelDuplicates();
                DataContactDisplay dcd;
                try {

                    for(int ii=0;ii<_listAdapter.getCount();ii++){
                        dcd=_listAdapter.getItem(ii);
                        _vmd.modifyForDuplicatesList(_listAdapter,
                                dcd,
                                _vmContacts.getDuplicateOption(),
                                ii+1);
                        this.publishProgress(ii);
                        Thread.sleep(50);
                    }

                } catch(Exception ex){
                    throw new RuntimeException(ex);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                _listAdapter=_vmd.RemoveNonDuplicatesFromAdapter(
                        _listAdapter);
                resetListBox();
                _pdHori.dismiss();
                SupportFunctions.DisplayToastMessageLong(
                        getActivity(),
                        getString(R.string.message_duplicates_selected));
                recomputeSelectionCountValue();

            }

            @Override
            protected void onPreExecute() {
                if(_pdHori==null)
                    _pdHori = new ProgressDialogHorizontal(getActivity(),
                            getString(R.string.progressbar_sel_duplicates));
                else //Reuse
                    _pdHori.setMessage(
                        _context.getResources().getText(
                                R.string.progressbar_sel_duplicates).toString());
                _pdHori.setMax(_listAdapter.getCount());
                _pdHori.show();
            }

            @Override
            protected void onProgressUpdate(Integer ... values) {
                super.onProgressUpdate(values);
                _pdHori.setProgress(values[0]);
            }

        }

        if(_listAdapter==null) return;//Dont do anything
        new BasicAsyncTask(this).execute();
    }


    //Populate the listbox here
    protected void PopulateListBoxCurrentDisplayOption(){
        //Body of your click handler

        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;


            BasicAsyncTask(Activity context) {
                super();
                _context = context;
            }

            @Override
            protected Void doInBackground(Void...params) {
                try {
                    ArrayList<DataContactDisplay> mainList =
                            _vmContacts.getCurrentDisplayOptionContacts();
                    _listAdapter = new ListViewAdapter(_context,
                            mainList,new ListViewAdapter.getColorBasedOnData() {
                        public int
                        onGetColorBasedOnData(DataContactDisplay contact) {
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
                                R.string.progressbar_load_data).toString());
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
        switch(_vmContacts.getCurrentDisplayOption()){
            case Duplicates:
                doDuplicateOperation();
                break;
            case Deleted:
                //set icon
                if(mDeleteMenuItem != null)
                    mDeleteMenuItem.setIcon(R.drawable.ic_delete_forever_black_48dp);
                showPostUndeleteMessage(); //This will also set menu icon
                break;
            default:
                if(mDeleteMenuItem != null)
                    mDeleteMenuItem.setIcon(android.R.drawable.ic_menu_delete);
                break;
        }
        customContactsLoadOperation();//For any custom post load
    }

    protected void showPreUndeleteMessageAndPopulateListBox() {
        if(mShowUndeleteMessage) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle(this.getResources().getText(
                    R.string.title_disclaimer));
            dlgAlert.setMessage(
                    String.valueOf(this.getResources().getText(
                            R.string.message_undelete_notice)));
            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton(this.getResources().getText(
                    R.string.button_understand), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    //Show Google URL
                    PopulateListBoxCurrentDisplayOption();
                    mShowUndeleteMessage = false; //Show message once only
                    dialog.dismiss();
                }
            });
            dlgAlert.show();
        }
        else{
            PopulateListBoxCurrentDisplayOption();
        }
    }


    protected void showPostUndeleteMessage() {
        if(_listAdapter == null) return;
        if(_listAdapter.getCount()==0) {
           AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
           dlgAlert.setTitle(this.getResources().getText(
                   R.string.title_no_undelete_found));
                   dlgAlert.setMessage(
                                String.valueOf(this.getResources().getText(
                                        R.string.message_no_undelete_found)));
                    dlgAlert.setCancelable(false);
                    dlgAlert.setPositiveButton(this.getResources().getText(
                            R.string.button_Yes), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            //Show Google URL
                            HelpMedia.ShowWebURL(getActivity(),
                                    "http://www.dasmic.com/android/contactsundelete/faq/google_recover_fwd.html");
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
        else{
            SupportFunctions.DisplayToastMessageLong(this,
                    getString(R.string.message_undelete_help));
        }
    }

    /*
    protected void showOnlyDuplicates(){
        _listAdapter.getFilter().filter(
                ActivityOptions.FILTER_ON_DUPLICATES);
    }*/

    protected void resetListBox(){
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
            public void onEvent(DataContactDisplay dcd) {
                // do something with item
                singleCheckboxItemSelected(dcd);
            }
        });
    }

    protected int getColor(DataContactDisplay contact){return 0;}

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
        if(checkExternalStoragePermissionsForAndroid6Plus()) {
            ViewModelImport vmImport = new ViewModelImport(this, null);
            if (!vmImport.RestoreAllContactsFromDefaultBackupFile()) {
                SupportFunctions.DisplayToastMessageLong(this,
                        getResources().getText(
                                R.string.message_restore_fail).toString());
            }
        }
    }

    //Backup not used by Coju
    protected void BackupContacts(){
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            String _folder;
            boolean _exceptionInBackup;
            String _exMessage;

            BasicAsyncTask(Activity context) {
                super();
                _context = context;
            }

            @Override
            protected Void doInBackground(Void...params) {
                try {
                    _exceptionInBackup = false;
                    ViewModelExport vmExport=new ViewModelExport(_context,
                            _context.getResources().getText(
                                    R.string.app_name_small).toString());
                    _folder=vmExport.StoreAllContactsInBackupFile_ExternalNoDate(
                            ActivityOptions.isFreeVersion);

                } catch(Exception ex){
                    _exceptionInBackup=true;
                    _exMessage = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                _pdSpin.dismiss();

                if(!_exceptionInBackup) {
                    if (ActivityOptions.isFreeVersion)
                        SupportFunctions.AsyncDisplayGenericDialog(_context,
                                _context.getResources().getString(
                                        R.string.message_backup_stored_freeversion)
                                        + _folder,getString(R.string.app_name));
                    else
                        SupportFunctions.DisplayToastMessageLong(_context,
                                _context.getResources().getText(
                                        R.string.message_backup_stored).toString() +
                                        _folder);
                }
                else
                    SupportFunctions.AsyncDisplayGenericDialog(_context,
                            _context.getString(R.string.message_backup_failed)
                            + ":" + _exMessage,
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

        if(checkExternalStoragePermissionsForAndroid6Plus())
            new BasicAsyncTask(this).execute();

    }


    protected boolean isMenuResourceValid(int resourceId ){
        return true;//The following code is not working as expected
        /* Add the menu to to the top level application*/
        /*try {
            int value = getResources().getInteger(resourceId);
            return value == 1 ? true : false;
        }
        catch(Exception ex){
            return false;
        }*/
    }

    //CAUTION: This routine will crash if the menu items in parent app do
    //not have any of these resource IDs declared. Declare those resource IDs and
    //set visibility to false in each app.
    protected void setNavigationMenuSelectedItems(){
        NavigationView navigationView = (NavigationView)
                findViewById(R.id.nav_view);

        if (isMenuResourceValid(R.id.nav_view_all))
                setNavigationCheckedValue(navigationView, R.id.nav_view_all, false);
        if (isMenuResourceValid(R.id.nav_view_deleted))
                setNavigationCheckedValue(navigationView, R.id.nav_view_deleted, false);
        if (isMenuResourceValid(R.id.nav_view_duplicates))
                setNavigationCheckedValue(navigationView, R.id.nav_view_duplicates, false);
        if (isMenuResourceValid(R.id.nav_view_email))
                setNavigationCheckedValue(navigationView, R.id.nav_view_email, false);
        if (isMenuResourceValid(R.id.nav_view_lastcontacttime))
                setNavigationCheckedValue(navigationView, R.id.nav_view_lastcontacttime, false);
        if (isMenuResourceValid(R.id.nav_view_phonenumber))
                setNavigationCheckedValue(navigationView, R.id.nav_view_phonenumber, false);
        if (isMenuResourceValid(R.id.nav_view_timescontacted))
                setNavigationCheckedValue(navigationView, R.id.nav_view_timescontacted, false);
        if (isMenuResourceValid(R.id.nav_view_blocked_contacts))
                setNavigationCheckedValue(navigationView, R.id.nav_view_blocked_contacts, false);
        if (isMenuResourceValid(R.id.nav_view_unblocked_contacts))
                setNavigationCheckedValue(navigationView, R.id.nav_view_unblocked_contacts, false);


        switch(_vmContacts.getCurrentDisplayOption()){
            case AllInformation:
                setNavigationCheckedValue(navigationView,R.id.nav_view_all,true);
                break;
            case AllWithContactCount:
                setNavigationCheckedValue(navigationView,R.id.nav_view_timescontacted,true);
                break;
            case AllWithLastContact:
                setNavigationCheckedValue(navigationView,R.id.nav_view_lastcontacttime,true);
                break;
            case PhoneNumber:
                setNavigationCheckedValue(navigationView,R.id.nav_view_phonenumber,true);
                break;
            case PhoneNumberWithName:
                if(_vmContacts.getFilterOption().getSelectedOption() ==
                        FilterOptionsEnum.ShowOnlySendToVoiceMailList)
                    setNavigationCheckedValue(navigationView,R.id.nav_view_blocked_contacts,true);
                else if(_vmContacts.getFilterOption().getSelectedOption() ==
                        FilterOptionsEnum.ShowOnlyDoNotSendToVoiceMailList)
                    setNavigationCheckedValue(navigationView,R.id.nav_view_unblocked_contacts,true);
                break;
            case Duplicates:
                setNavigationCheckedValue(navigationView,R.id.nav_view_duplicates,true);
                break;
            case Email:
                setNavigationCheckedValue(navigationView,R.id.nav_view_email,true);
                break;
            case Deleted:
                setNavigationCheckedValue(navigationView,R.id.nav_view_deleted,true);
                break;
        }
        //Blocked / Unblocked contacts are set on Filter options
        //Have them override previous selection
        switch(_vmContacts.getFilterOption().getSelectedOption()) {
            case ShowOnlyDoNotSendToVoiceMailList:
                setNavigationCheckedValue(navigationView,R.id.nav_view_unblocked_contacts,true);
                break;
            case ShowOnlySendToVoiceMailList:
                setNavigationCheckedValue(navigationView,R.id.nav_view_blocked_contacts,true);
                break;
        }

    }

    protected void setNavigationCheckedValue(NavigationView navigationView,
                                             int resourceId,
                                             boolean value){
        MenuItem item = navigationView.getMenu().findItem(resourceId);

        if(item != null)
            item.setChecked(value);
    }

    protected void showCojuForMicrosoftOutlook(){
        String url="http://www.coju.mobi/windows/license/CojuForOutlook/SendLinkByEmail";
        HelpMedia.ShowWebURL(this,url);
    }

}
