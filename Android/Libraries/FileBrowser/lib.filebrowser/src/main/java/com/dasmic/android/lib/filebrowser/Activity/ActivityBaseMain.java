package com.dasmic.android.lib.filebrowser.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.dasmic.android.lib.filebrowser.Data.ActivityStartupData;
import com.dasmic.android.lib.filebrowser.Data.DataFileDisplay;
import com.dasmic.android.lib.filebrowser.Enum.AppOptions;

import com.dasmic.android.lib.filebrowser.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.filebrowser.Extension.ListViewAdapter;
import com.dasmic.android.lib.filebrowser.Extension.ProgressDialogCustom;
import com.dasmic.android.lib.filebrowser.Interface.IGenericEvent;
import com.dasmic.android.lib.filebrowser.Interface.IGenericParameterLessEvent;

import com.dasmic.android.lib.filebrowser.R;
import com.dasmic.android.lib.filebrowser.ViewModel.ViewModelExportSupport;
import com.dasmic.android.lib.filebrowser.ViewModel.ViewModelFileBrowser;
import com.dasmic.android.lib.support.Ad.Interstilial;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/30/2017.
 */

public class ActivityBaseMain extends AppCompatActivity {
    protected boolean _updateDone;
    protected ActionBarDrawerToggle _drawerToggle;
    protected String _helpURL;
    protected String _demo_video_id;
    protected String _base64EncodedPublicKey;
    protected String _paid_version_sku_id;
    protected String _ad_interstitial_id;
    protected Interstilial _interstilial;
    protected InAppPurchases _inAppPurchases;
    protected  String _startupFolderPath;
    protected int _startupFolderType;
    protected  String _tellAFriend;
    protected ListViewAdapter _listAdapter;
    protected ListView _listView;
    protected ProgressDialogCustom _progressDialog;
    protected int _selectionCount;
    protected ViewModelFileBrowser _vmFileBrowser;
    protected ProgressDialogHorizontal _pdHori;
    protected boolean _isSearching;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() { //In App billing cleanup
        super.onDestroy();
        if (_inAppPurchases != null) _inAppPurchases.dispose();
        _inAppPurchases = null;
    }

    protected void setupForInAppPurchase() {
        try {
            String base64EncodedPublicKey =
                    _base64EncodedPublicKey;
            _inAppPurchases = new InAppPurchases(this,
                    _paid_version_sku_id,
                    base64EncodedPublicKey);

            _inAppPurchases.SetupInAppPurchase(
                    new InAppPurchases.OnIAPSetupFinishedListener() {
                        public void onIAPSetupFinished(
                                IabResult result,
                                boolean isPaidVersion) {
                            AppOptions.isFreeVersion = !isPaidVersion;
                            // update UI accordingly
                            setVersionBasedUI();
                        }
                    });
        }
        catch(Exception ex) { //Exception Handling
            AppOptions.isFreeVersion = true;
            // update UI accordingly
            setVersionBasedUI();
        }
    }

    protected void updateFreeSpaceDisplay(){
        try{
        double freeSpace = (FileOperations.getFreeSpaceExternal(getApplicationContext())
                            + FileOperations.getFreeSpaceInternal(getApplicationContext()))/(1024.0*1024.0);

        long totalSpace = (FileOperations.getTotalSpaceExternal(getApplicationContext())
                + FileOperations.getTotalSpaceInternal(getApplicationContext()))/(1024*1024);

        String display = getString(R.string.free_space) + " " + String.format("%.2f", freeSpace) +
                                                            "/" + String.valueOf(totalSpace) + " MB";

        TextView tv = (TextView) findViewById(R.id.textFreeSpace);
        tv.setText(display);
        }
        catch(Exception ex){
            SupportFunctions.DebugLog("ActivityBaseMain",
                    "updateFreeSpaceDisplay",
                    ex.getMessage());
        }
    }

    protected  void setValuesFromActivityData(ActivityStartupData ad){
        _paid_version_sku_id = ad.Paid_version_sku_id;
        _helpURL = ad.HelpURL;
        _demo_video_id = ad.Demo_video_id;
        _base64EncodedPublicKey = ad.Base64EncodedPublicKey;
        _ad_interstitial_id=ad.Ad_interstitial_id;
        _startupFolderPath = ad.StartupFolderPath;
        _startupFolderType = ad.StartupFolderType;
        AppOptions.IS_FOR_AMAZON = ad.ForAmazon==1?true:false;
        AppOptions.FILE_PROVIDER_AUTHORITY = ad.FileProviderAuthority;
        SupportFunctions.DebugLog("ActivityBaseMain","setValuesFromActivityData",
                "FileProvider:"+AppOptions.FILE_PROVIDER_AUTHORITY);
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
                                                                setVersionBasedUI();
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

    protected void setVersionBasedUI() {
        MenuItem item = null;
        Menu navMenu = null;
        AdView mAdView = (AdView) findViewById(R.id.adView);
        NavigationView navView = (NavigationView)
                findViewById(R.id.nav_view);
        if (navView != null) {
            navMenu = navView.getMenu();
            if (navMenu != null) {
                item = navMenu.findItem(
                        R.id.nav_view_upgrade_paid_version);
            }
            if (AppOptions.isFreeVersion) {
                //Ads
                if (mAdView != null) {
                    mAdView.setVisibility(View.VISIBLE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);
                }
                if (item != null) //Purchase Menu
                    item.setVisible(true);
            } else { //Paid version
                //Ads
                if (mAdView != null) {
                    mAdView.setVisibility(View.GONE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);
                }
                if (item != null) //Purchase Menu
                    item.setVisible(false);
            }
        }
    }

    protected void ShowRateThisApp(){
        RateThisApp rtp=new RateThisApp();
        rtp.ShowRateThisApp(this);
    }

    public void onClickItem(DataFileDisplay dfd){
        try {
            if (dfd.IsFolder) {
                //GoInsideFolder
                changeFolder(_vmFileBrowser.getCurrentFolder(), dfd);
            } else { //Auto launch intent faced on file extension
                File file = new File(dfd.AbsoluteFilePath);
                MimeTypeMap map = MimeTypeMap.getSingleton();
                String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
                String type = map.getMimeTypeFromExtension(ext);

                if (type == null)
                    type = "*/*";

                //This is DEAD Code as is doesnt wor in Android 8+
                //Intent intent = new Intent(Intent.ACTION_VIEW);
                //Uri data = Uri.fromFile(file); //Doesn't work in Android 8
                //intent.setDataAndType(data, type);
                //startActivity(intent);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Required for Android 8+
                Uri data = FileProvider.getUriForFile(this, AppOptions.FILE_PROVIDER_AUTHORITY, file);
                intent.setDataAndType(data, type);
                startActivity(intent);

            }
        }
        catch(Exception ex){
            SupportFunctions.AsyncDisplayGenericDialog(this,
                    getString(R.string.message_file_open_error) + ":" + ex.getMessage(),
                    "");
        }
    }

    public void changeFolder(String currentFolder, DataFileDisplay dfd){
        if(!dfd.HasReadPermission){
            SupportFunctions.DisplayToastMessageLong(this,getString(R.string.message_no_read_permission                                             ));
            return;
        }

        if (dfd.getName().equals("..")) {
            //Goto new top level folder
            setCurrentBrowseFolder(FileOperations.getParentFolder(currentFolder));
        } else
            setCurrentBrowseFolder(dfd.AbsoluteFilePath);

        ReloadListView();
    }

    //Central place to set Current Browse Folder
    protected void setCurrentBrowseFolder(String currentBrowseFolder){
        if(currentBrowseFolder != null) {
            try {
                _vmFileBrowser.setCurrentBrowseFolder(currentBrowseFolder);
                return;
            } catch (Exception ex) {
                SupportFunctions.DisplayToastMessageLong(this,
                        getString(R.string.message_cannot_go_in));
                SupportFunctions.vibrate(this,500);
            }
        }


    }



    //Populate the listbox here
    protected void PopulateListBoxCurrentDisplayOption(){
        //Body of your click handler
        setNavigationMenuSelectedItems();

        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;

            BasicAsyncTask(Activity context) {
                super();
                _context = context;
            }

            @Override
            protected Void doInBackground(Void...params) {
                try {
                    ArrayList<DataFileDisplay> mainList =
                            _vmFileBrowser.getCurrentDisplayOptionValues();
                    _listAdapter = new ListViewAdapter(getActivity(),mainList);
                    //_listAdapter.setActivityBaseMain(getActivity());
                } catch(Exception ex){
                    throw new RuntimeException(ex);
                }
                //SupportFunctions.addDelay(3000);
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                _listView.setAdapter(_listAdapter);
                _listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                _listView.setTextFilterEnabled(true);

                setSelectionCountText();
                additionalPostDisplayFunction();
                _listAdapter.setListener(new IGenericParameterLessEvent() {
                    public void onEvent() {
                        // do something with item
                        setSelectionCountText();
                    }
                });
                //Very important to add this event
                _listAdapter.setListener(new IGenericEvent() {
                    public void onEvent(int value) {
                        updateSelectionCountText(value);
                    }
                });

                //Some exception were reported in catching this
                try {
                    _progressDialog.dismiss();
                }
                catch(Exception ex){

                }
            }

            @Override
            protected void onPreExecute() {
                _progressDialog.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {
            }

        }
        _progressDialog.setMessage
                (this.getResources().getText(R.string.progressbar_load_data).toString());
        new BasicAsyncTask(this).execute();
    }

    protected void ReloadListView(){
        if(checkPermissionsForAndroid6Plus())
            ReloadListView(true);
    }

    protected boolean checkPermissionsForAndroid6Plus(){
        return true;
    }



    protected void setSelectionCountText(){
        TextView textDisplayCount = (TextView) findViewById(R.id.textSelectCount);
        int totalCount = _listAdapter.getCount() ;

        if(!_isSearching) totalCount = totalCount-1;//1 less for up folder except when searching

        textDisplayCount.setText(_selectionCount + "/" + totalCount +
                " " + getResources().getText(R.string.general_selected_items));
    }

    //Set a single checkbox through code
    protected void setCheckValue(DataFileDisplay dpd, boolean value){
        if(dpd.isGotoParentFolder()) return; //Never check ifGotFolder
        if(dpd.IsChecked != value) { //If value has to be updated
            dpd.IsChecked = value; //When Listview populates this value will be shown
            if (value) _selectionCount++;
            else if (_selectionCount > 0) _selectionCount--;
        }
    }

    //Update when a single item is checked or unchecked
    protected void updateSelectionCountText(int value){
        if(value==1) _selectionCount++;
        else _selectionCount--;
        setSelectionCountText();
    }

    protected boolean checkAllowedOption(){
        return true;
    }

    protected boolean checkSelectionMinimum(){
        int size = _vmFileBrowser.getCheckedItems().size();
        if(size <= 0){
            SupportFunctions.DisplayToastMessageShort(
                    this, (String) this.getResources().getString(
                            R.string.message_selectitems));
            SupportFunctions.vibrate(this,300);
            return false;
        }
        return true;
    }

    protected void additionalPostDisplayFunction(){

    }


    protected void setNavigationMenuSelectedItems(){
        NavigationView navigationView = (NavigationView)
                findViewById(R.id.nav_view);

        setNavigationCheckedValue(navigationView,R.id.nav_view_default,false);
        setNavigationCheckedValue(navigationView,R.id.nav_view_size,false);
        setNavigationCheckedValue(navigationView,R.id.nav_view_last_modified,false);

        switch(_vmFileBrowser.getCurrentDisplayOption()){
            case DefaultView:
                setNavigationCheckedValue(navigationView,R.id.nav_view_default,true);
                break;
            case SortedBySize:
                setNavigationCheckedValue(navigationView,R.id.nav_view_size,true);
                break;
            case SortedByModifiedDate:
                setNavigationCheckedValue(navigationView,R.id.nav_view_last_modified,true);
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

    //Invalidate current data and reload listView
    //flag is dummy, for overloading purposes
    protected void ReloadListView(boolean flag) {
        if(!AppOptions.IS_FOR_AMAZON)
            RateThisApp.ShowRateAppDialog(this); //To show Rate this app

        UncheckAll();
        _vmFileBrowser.InvalidateData();

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
        //Update folder path display
        updateFolderPathDisplay();
        updateFreeSpaceDisplay();
        //------------------
        PopulateListBoxCurrentDisplayOption();
    }

    private void updateFolderPathDisplay(){
        TextView tv = (TextView) findViewById(R.id.textFolderPath);
        tv.setText(_vmFileBrowser.getCurrentFolder());

    }

    protected ActivityBaseMain getActivity(){
        return this;
    }

    protected void UncheckAll(){
        Log.i("CKIT", "UncheckAll");
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setChecked(false);
        onButtonCheckUncheck();
    }

    //On select all button check/uncheck
    protected void onButtonCheckUncheck(){
        Log.i("CKIT", "ActivityMain::onCheckUncheck");
        //Do not process if _listAdapter is null
        if(_listAdapter == null) return;
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);

        for(int ii=0;ii<_listAdapter.getCount();ii++){
            setCheckValue(_listAdapter.getItem(ii),
                    checkAll.isChecked());
        }
        setSelectionCountText();
        _listView.invalidateViews(); //force a refresh
    }

    protected void setLocalVariablesAndEventHandlers() {
        _selectionCount=0;
        _listView = (ListView) findViewById(R.id.listView);
        _vmFileBrowser = ViewModelFileBrowser.getInstance(this);
        _vmFileBrowser.setCurrentDisplayOption(
                DisplayOptionsEnum.DefaultView);
        setCurrentBrowseFolder(_startupFolderPath); //Only set initial folder

        //then it will be taken care of when user click on folder name
        //so the setCurrentBrowseFolder function will be called then
        _progressDialog = new ProgressDialogCustom(this,
                this.getResources().getText(R.string.progressbar_load_data).toString());

        setButtonEventHandlers();
    }

    protected void setButtonEventHandlers(){
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCheckUncheck();
            }
        });
        ImageButton ibUp = (ImageButton) findViewById(
                R.id.buttonUpFolder);

        ibUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DataFileDisplay dfd = new DataFileDisplay("..");
                dfd.HasReadPermission=true; //Top folder has read permission
                changeFolder(_vmFileBrowser.getCurrentFolder(),dfd);
            }
        });

        setSortButton();
    }


    protected void onFileCopy(){
        if(checkSelection(true)) {
            if (checkSelectionMax(1)) {
                ArrayList<DataFileDisplay> checkedItems =
                        _vmFileBrowser.getCheckedItems();
                DataFileDisplay dfd = checkedItems.get(0);
                if(!dfd.IsFolder){
                    //Do copy
                    CopyFile();
                }
                else {
                    ShowMessageOnlyFile();
                    return;
                }
            }
        }
    }

    protected void onRename(){
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(checkSelection(true)) {
            if (checkSelectionMax(1)) {
                ArrayList<DataFileDisplay> checkedItems =
                        _vmFileBrowser.getCheckedItems();
                final DataFileDisplay dfd = checkedItems.get(0);
                //Ask for new name - Dialog
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(getActivity().getString(
                        R.string.title_rename));
                // Set up the input
                final EditText editText = new EditText(getActivity());

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                dialog.setView(editText);

                // Set up the buttons
                dialog.setPositiveButton(getActivity().getString(
                        R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String newName = editText.getText().toString();
                            _vmFileBrowser.RenameFile(dfd,newName);
                            RefreshListView();
                            SupportFunctions.DisplayToastMessageShort(getActivity(),(String) getActivity().getString(
                                    R.string.message_rename_complete));
                        }
                        catch(Exception ex){
                            SupportFunctions.DisplayErrorToast(getActivity(),(String) getActivity().getString(
                                    R.string.message_rename_error));
                        }
                    }
                });

                dialog.setNegativeButton(getActivity().getString(
                        R.string.button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();

                    }
                });

                dialog.show();
                //Force keyboard to show up
                //editText.requestFocus();
                //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            } //checkSelectionMax
        }//checkSelection
    }

    protected void onCreateFolder(){
                //Ask for new name - Dialog
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(getActivity().getString(
                        R.string.title_create_folder));
                // Set up the input
                final EditText editText = new EditText(getActivity());

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                dialog.setView(editText);

                // Set up the buttons
                dialog.setPositiveButton(getActivity().getString(
                        R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String newName = editText.getText().toString();
                            _vmFileBrowser.CreateNewFolderInCurrentFolder(newName);
                            RefreshListView();
                            SupportFunctions.DisplayToastMessageShort(getActivity(),(String) getActivity().getString(
                                    R.string.message_create_folder_complete));
                        }
                        catch(Exception ex){
                            SupportFunctions.DisplayErrorToast(getActivity(),(String) getActivity().getString(
                                    R.string.message_create_folder_error));
                        }
                    }
                });

                dialog.setNegativeButton(getActivity().getString(
                        R.string.button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog.show();
                //Force keyboard to show up
                editText.requestFocus();
                //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }



    private void ShowMessageOnlyFile(){
        SupportFunctions.DisplayErrorToast(getActivity(),(String) this.getResources().getString(
                R.string.message_only_file));

    }

    //Share one or more files
    protected void onFilesShare(){
        if(checkSelection(true)) {
            ArrayList<DataFileDisplay> allFiles
                    =_vmFileBrowser.getCheckedItems();

            //Check if there is no folder
            for(DataFileDisplay dfd:allFiles){
                if(dfd.IsFolder){
                   onFilesShareZip(); //Use Zip is has folder
                    return;
                }
            }
            ShareMultipleFiles(allFiles);
        }
    }

    protected void onFilesShareZip(){
        if(checkSelection(true)) {
            ArrayList<DataFileDisplay> allFiles = _vmFileBrowser.getCheckedItems();
            String dateTime=DateOperations.getCurrentFormattedDateForFileName();
            String fileName = dateTime + "_" + String.valueOf(allFiles.size())  + "_files.zip";
            String targetFilePath
                    = FileOperations.getInternalCacheFolder(getActivity())
                    + "/" + fileName;
            //Delete file since its temporary
            FileOperations.DeleteFile(targetFilePath);

            ZipSelectedFiles(targetFilePath,true);
        }
    }

    protected void ShareSingleFile(String filePath){
        ViewModelExportSupport vmExportSupport = new
                ViewModelExportSupport(getActivity());
        try {
            Intent intent=null;
            intent=vmExportSupport.getSingleFileIntent("application/octet-stream",
                    filePath);
            startActivity(Intent.createChooser(intent, getString(R.string.title_share)));
        }
        catch (Exception ex){
            SupportFunctions.AsyncDisplayGenericDialog(getActivity(),
                    getString(R.string.message_share_error)+":"+ex.getMessage(),"");
            SupportFunctions.vibrate(getActivity(),200);
        }
    }

    protected void ShareMultipleFiles(ArrayList<DataFileDisplay> allFiles){
        ViewModelExportSupport vmExportSupport = new
                ViewModelExportSupport(getActivity());
        try {
            Intent intent=null;
            intent=vmExportSupport.getMultipleFileIntent("application/octet-stream",
                    allFiles);
            startActivity(Intent.createChooser(intent, getString(R.string.title_share)));
        }
        catch (Exception ex){
            SupportFunctions.AsyncDisplayGenericDialog(getActivity(),
                            getString(R.string.message_share_error)+":"+ex.getMessage(),"");
            SupportFunctions.vibrate(getActivity(),200);
        }
    }

    protected void onButtonDelete(){
        if(checkSelection(true)){
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setTitle(this.getResources().getText(R.string.title_confirm));

            dlgAlert.setMessage(
                        String.valueOf(this.getResources().getText(
                                R.string.message_delete_confirm)));

            dlgAlert.setCancelable(false);

            dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                        DeleteFiles();
                    dialog.dismiss();
                }
            });

            dlgAlert.setNegativeButton(
                    this.getResources().getText(R.string.button_no),
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

    protected void DeleteFiles()
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            private int _totCount;
            private int _delCount;
            ArrayList<DataFileDisplay> _allFileDfd;

            @Override
            protected Void doInBackground(Void...params) {
                for(DataFileDisplay dfd: _allFileDfd){
                    try {
                        _delCount += _vmFileBrowser.DeleteFile(dfd);
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
                SupportFunctions.DisplayToastMessageShort(getActivity(),
                        String.valueOf(_delCount) + " " +
                                getActivity().getText(
                                        R.string.message_delete_complete).toString());
            }


            @Override
            protected void onPreExecute() {
                _delCount=0;

                _allFileDfd =  _vmFileBrowser.getCheckedItems();
                SupportFunctions.DebugLog("ActivityMain",
                        "Delete","Delete count is: " + _allFileDfd.size());

                _totCount= _allFileDfd.size();
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
        //_pdHori.setMessage
        //        (getString(R.string.progressbar_delete_data)+
        //                " 0/0");
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

    protected void CopyFile()
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            ArrayList<DataFileDisplay> _allFileDfd;
            private boolean _error;

            @Override
            protected Void doInBackground(Void...params) {
                _error=false;
                for(DataFileDisplay dfd: _allFileDfd){
                    try {
                        _vmFileBrowser.CopyFile(dfd);
                    }
                    catch(Exception ex){
                        //Mainly for sleep
                        _error = true;
                    }
                }
                return null;
            }


            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                _progressDialog.dismiss();
                RefreshListView();
                if(!_error)
                    SupportFunctions.DisplayToastMessageShort(getActivity(),
                                getActivity().getString(
                                        R.string.message_copy_complete));
                else {
                    SupportFunctions.DisplayToastMessageShort(getActivity(),
                            getActivity().getString(
                                    R.string.message_copy_error));
                    SupportFunctions.vibrate(getActivity(),200);
                }

            }


            @Override
            protected void onPreExecute() {
                _allFileDfd =  _vmFileBrowser.getCheckedItems();

                _progressDialog = new ProgressDialogCustom(getActivity(),
                        getActivity().getResources().getString(R.string.message_copying_file));
                super.onPreExecute();
                _progressDialog.show();

            }

            @Override
            protected void onProgressUpdate(Void...params) {

            }
        }
        Log.i("CKIT", "ActivityMain::onDeleteContacts");
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


    protected void ZipSelectedFiles(final String targetFilePath,
                                    boolean openShareFile)
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            String _targetFolder;
            ArrayList<String> _allFilePaths;
            boolean _openShareFile;
            private boolean _error;
            ViewModelExportSupport _vmExportSupport
                    =new ViewModelExportSupport(getActivity());

            public BasicAsyncTask( String targetFolder,
                                            boolean openShareFile) {
                _targetFolder = targetFolder;
                _openShareFile = openShareFile;
            }

            @Override
            protected Void doInBackground(Void...params) {
                    try {
                        ArrayList<DataFileDisplay> allSelected =
                                _vmFileBrowser.getCheckedItems();
                        _vmExportSupport.CreateZipFile(allSelected,targetFilePath);
                    }
                    catch(Exception ex){
                        //Mainly for sleep
                        _error=true;
                    }
                return null;
            }


            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                _progressDialog.dismiss();


                if(!_error) {
                    SupportFunctions.DisplayToastMessageShort(getActivity(),
                            getActivity().getString(
                                    R.string.message_zipping_completed));
                }
                else{
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            getActivity().getString(
                                    R.string.message_zipping_error));
                    SupportFunctions.vibrate(getActivity(),500);
                    return; //Dont proceed on error
                }

                if(_openShareFile)
                    ShareSingleFile(targetFilePath);
                else
                    RefreshListView();

            }


            @Override
            protected void onPreExecute() {
                _progressDialog = new ProgressDialogCustom(getActivity(),
                        getActivity().getResources().getString(R.string.message_zipping_files));
                super.onPreExecute();
                _progressDialog.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {

            }
        }
        Log.i("CKIT", "ActivityMain::onDeleteContacts");
        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(targetFilePath,
                                openShareFile);
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

    protected boolean checkSelectionMax(int maxAllowed){
        ArrayList<DataFileDisplay> checkedItems= null;
        checkedItems =
                _vmFileBrowser.getCheckedItems();
        int size=checkedItems.size();

        if(size > maxAllowed) {
                SupportFunctions.DisplayToastMessageLong(
                        this, (String) this.getResources().getString(
                                R.string.message_max_allowed_one));
                SupportFunctions.vibrate(this,200);
                return false;
            }
        return true;
    }

    protected void RefreshListView(){
        _vmFileBrowser.InvalidateData();//Will clean history of selection, be careful
        //Check for permissions
        if(checkPermissionsForAndroid6Plus())
            ReloadListView(false);
    }

    protected boolean checkSelection(boolean checkMin){
        ArrayList<DataFileDisplay> checkedItems= null;
        checkedItems =
                _vmFileBrowser.getCheckedItems();
        int size=checkedItems.size();


        if(checkMin) //Dont do this check if checkMin is false, in BT Transfer
            if(size <= 0){
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

    protected int getSearchBoxWidth(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width/2; //Do not let it take more than half
    }


    protected void setSortButton(){
        ImageButton ib = (ImageButton) findViewById(
                R.id.buttonImageSort);
        ib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                _vmFileBrowser.changeSortOrder();
                RefreshListView();
                updateSortedImage();
            }
        });
        updateSortedImage();
    }

    protected void updateSortedImage(){
        ImageButton ib = (ImageButton) findViewById(
                R.id.buttonImageSort);

        if(_vmFileBrowser.getCurrentSortOrder()) //Then Descending
            ib.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_48dp);
        else
            ib.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_48dp);
    }
}
