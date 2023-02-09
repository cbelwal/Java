package com.dasmic.android.lib.message.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.dasmic.android.lib.message.Data.DataMessageDisplay;
import com.dasmic.android.lib.message.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.support.Ad.Interstilial;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Feedback.ExternalAppInfo;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.message.Enum.AppOptions;
import com.dasmic.android.lib.message.Extension.ListViewAdapter;
import com.dasmic.android.lib.message.Interface.IGenericEvent;
import com.dasmic.android.lib.message.Interface.IGenericParameterLessEvent;

import com.dasmic.android.lib.message.ViewModel.ViewModelMessageDisplay;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/19/2017.
 */

public class ActivityBaseMain extends ActivityBaseAd  {
    protected int _selectionCount;
    protected MenuItem mDeleteMenuItem;
    //Controls
    protected ListViewAdapter _listAdapter;
    protected ListView _listView;
    protected ProgressDialogHorizontal _pdHori;
    protected ProgressDialogSpinner _pdSpin;
    protected ActionBarDrawerToggle _drawerToggle;

    //InApp purchase related
    protected String _base64EncodedPublicKey;
    protected String _paid_version_sku_id;

    //Classes
    protected ViewModelMessageDisplay _vmMessages;
    protected InAppPurchases _inAppPurchases;
    protected String demo_video_id;
    protected String _ad_interstitial_id;
    protected Interstilial _interstilial;
    protected DataMessageDisplay _dmdFirstSelection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected int getSearchBoxWidth(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width/2; //Do not let it take more than half
    }

    protected void singleCheckboxItemSelected(DataMessageDisplay dcd){
        CheckBox checkAll = (CheckBox) findViewById(
                R.id.checkSelectAll);
        if(checkAll.isChecked()) checkAll.setChecked(false);
        int value = dcd.isChecked ? 1 : 0;
        //if(_selectionCount==0 && value==1)
        //    _dmdFirstSelection=dcd;

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
        //_vmMessages.allChecked = !_vmMessages.allChecked;
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

    /*protected boolean checkIsUndeleted(){
        if(isUndeleteDisplay()) {
            SupportFunctions.DisplayToastMessageLong(this,
                    getString(R.string.message_undelete_option_invalid));
            return true;
        }
        return false;
    }*/


    protected boolean checkSelection(boolean checkMin){
        ArrayList<DataMessageDisplay> checkedItems= null;
        int size=0;
        checkedItems =
                _vmMessages.getCheckedItems();
        size=checkedItems.size();

        //dcdFirstSelection used by merge contacts
        //If it is null set it to first selection
        //if checkbox is set from code (not user selected)
        //dcdFirstSelection is null
        if(_dmdFirstSelection ==null && size > 0)
            _dmdFirstSelection =checkedItems.get(0);


        if(checkMin) //Dont do this check if checkMin is false, in BT Transfer
            if(size <= 0 || _dmdFirstSelection ==null){
                SupportFunctions.DisplayToastMessageShort(
                        this, (String) this.getResources().getText(
                                R.string.message_selectitems));
                SupportFunctions.vibrate(this,500);
                return false;
            }
        if(AppOptions.isFreeVersion){
            if(size > AppOptions.FREE_VERSION_CONTACT_LIMIT) {
                SupportFunctions.DisplayToastMessageLong(
                        this, (String) this.getResources().getString(
                                R.string.message_free_version_selection)+
                                String.valueOf(AppOptions.FREE_VERSION_CONTACT_LIMIT));
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
            /*if(isUndeleteDisplay()) {
                dlgAlert.setMessage(
                        String.valueOf(this.getResources().getText(
                                R.string.message_undelete_confirm)));
            }
            else*/
            dlgAlert.setMessage(
                        String.valueOf(this.getResources().getText(
                                R.string.message_delete_confirm)));

            dlgAlert.setCancelable(false);

            dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_Yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    /*if(isUndeleteDisplay())
                        unDeleteContacts();
                    else*/
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
                        _delCount += _vmMessages.DeleteContact(id);
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
                //if(_vmMessages.getCurrentDisplayOption() == DisplayOptionsEnum.Duplicates)
                //    _vmMessages.setCurrentDisplayOption(DisplayOptionsEnum.AllInformation);
            }


            @Override
            protected void onPreExecute() {
                _delCount=0;

                _allContactIds=
                        getUniqueRawContactIdList(
                                _vmMessages.getCheckedItems());
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


    protected Activity getActivity(){
        return this;
    }

    protected void RefreshListView(boolean value){
        _vmMessages.InvalidateData();//Will clean history of selection, be careful
        //Check for permissions
        if(checkPermissionsForAndroid6Plus())
            ReloadListView(value);
    }

    protected void RefreshListView(){
        _vmMessages.InvalidateData();//Will clean history of selection, be careful
        //Check for permissions
        if(checkPermissionsForAndroid6Plus())
            ReloadListView(false);
    }

    protected void ReloadListView() {
        //Check for permissions
        if(checkPermissionsForAndroid6Plus())
            ReloadListView(false);
    }

    //Check permission
    private boolean checkPermissionsForAndroid6Plus(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{
                            Manifest.permission.READ_CONTACTS},
                    AppOptions.PERMISSION_CONTACTS_REQUEST);
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
                    AppOptions.PERMISSION_STORAGE_REQUEST);
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
            case AppOptions.PERMISSION_CONTACTS_REQUEST:
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
            case AppOptions.PERMISSION_STORAGE_REQUEST:
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

    //Invalidate current data and reload listView
    protected void ReloadListView(boolean
                                          doDuplicatesOp) {
        if(!AppOptions.IS_FOR_AMAZON)
            RateThisApp.ShowRateAppDialog(this); //To show Rate this app

        UncheckAll();
        //if(doDuplicatesOp) {
        //    _vmMessages.setCurrentDisplayOption(
        //            DisplayOptionsEnum.Duplicates);
        //}

        //Empty the SearchBox ------------
        SearchView searchView = (SearchView) findViewById(R.id.textSearch);
        if(searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        //--------------------------------------- Ad
        //Show only for free version
        if(_ad_interstitial_id != null && AppOptions.isFreeVersion) { //If not even defined
            if (_interstilial == null && !_ad_interstitial_id.trim().equals(""))
                _interstilial = new Interstilial(_ad_interstitial_id, this);

            if (_interstilial != null)
                _interstilial.showAd(); //Show during load
        }
        //------------------
        showPreDisplayMessageAndPopulateListBox();
    }

    protected void showPreDisplayMessageAndPopulateListBox(){
        switch(_vmMessages.getCurrentDisplayOption()){
            default:
                PopulateListBoxCurrentDisplayOption();
                break;
        }

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
                    ArrayList<DataMessageDisplay> mainList =
                            _vmMessages.getCurrentDisplayOptionContacts();
                    _listAdapter = new ListViewAdapter(_context,
                            mainList,new ListViewAdapter.getColorBasedOnData() {
                        public int
                        onGetColorBasedOnData(DataMessageDisplay contact) {
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
        switch(_vmMessages.getCurrentDisplayOption()){
            //case Duplicates:
            //    doDuplicateOperation();
            //    break;
            /*case Deleted:
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

//    protected void showPreUndeleteMessageAndPopulateListBox() {
//        if(mShowUndeleteMessage) {
//            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
//            dlgAlert.setTitle(this.getResources().getText(
//                    R.string.title_disclaimer));
//            dlgAlert.setMessage(
//                    String.valueOf(this.getResources().getText(
//                            R.string.message_undelete_notice)));
//            dlgAlert.setCancelable(false);
//            dlgAlert.setPositiveButton(this.getResources().getText(
//                    R.string.button_understand), new DialogInterface.OnClickListener() {
//
//                public void onClick(DialogInterface dialog, int which) {
//                    //Show Google URL
//                    PopulateListBoxCurrentDisplayOption();
//                    mShowUndeleteMessage = false; //Show message once only
//                    dialog.dismiss();
//                }
//            });
//            dlgAlert.show();
//        }
//        else{
//            PopulateListBoxCurrentDisplayOption();
//        }
//    }



    /*
    protected void showOnlyDuplicates(){
        _listAdapter.getFilter().filter(
                AppOptions.FILTER_ON_DUPLICATES);
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
            public void onEvent(DataMessageDisplay dcd) {
                // do something with item
                singleCheckboxItemSelected(dcd);
            }
        });
    }

    protected int getColor(DataMessageDisplay contact){return 0;}

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
//    protected void RestoreContacts(){
//        if(checkExternalStoragePermissionsForAndroid6Plus()) {
//            ViewModelImport vmImport = new ViewModelImport(this, null);
//            if (!vmImport.RestoreAllContactsFromDefaultBackupFile()) {
//                SupportFunctions.DisplayToastMessageLong(this,
//                        getResources().getText(
//                                R.string.message_restore_fail).toString());
//            }
//        }
//    }

}
