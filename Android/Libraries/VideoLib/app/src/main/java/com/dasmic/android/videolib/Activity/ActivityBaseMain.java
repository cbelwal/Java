package com.dasmic.android.videolib.Activity;

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

import com.dasmic.android.lib.support.Ad.Interstilial;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.videolib.Data.DataVideoDisplay;
import com.dasmic.android.videolib.Enum.AppOptions;
import com.dasmic.android.videolib.Enum.DisplayOptionsEnum;
import com.dasmic.android.videolib.Enum.SearchOptionsEnum;
import com.dasmic.android.videolib.Extension.ListViewAdapter;
import com.dasmic.android.videolib.Interface.IGenericEvent;
import com.dasmic.android.videolib.Interface.IGenericParameterLessEvent;
import com.dasmic.android.videolib.R;
import com.dasmic.android.videolib.ViewModel.ViewModelExportSupport;
import com.dasmic.android.videolib.ViewModel.ViewModelVideoBrowser;
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
    protected ProgressDialogSpinner _progressDialog;
    protected int _selectionCount;
    protected ViewModelVideoBrowser _vmVideoBrowser;
    protected ProgressDialogHorizontal _pdHori;
    protected boolean _isSearching;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(AppOptions.IS_FOR_AMAZON==true){
            AppOptions.isFreeVersion=false;
            setVersionBasedUI();
        }
        else
            setupForInAppPurchase();
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
        double freeSpace = (FileOperations.getFreeSpaceExternal(getApplicationContext())
                + FileOperations.getFreeSpaceInternal(getApplicationContext()))/(1024.0*1024.0);

        long totalSpace = (FileOperations.getTotalSpaceExternal(getApplicationContext())
                + FileOperations.getTotalSpaceInternal(getApplicationContext()))/(1024*1024);

        String display = getString(R.string.free_space) + " " + String.format("%.2f", freeSpace) +
                "/" + String.valueOf(totalSpace) + " MB";

        TextView tv = (TextView) findViewById(R.id.textFreeSpace);
        tv.setText(display);
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

    public void onClickItem(DataVideoDisplay dfd){

        try {
            File file = new File(dfd.AbsoluteFilePath);
            MimeTypeMap map = MimeTypeMap.getSingleton();
            String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
            String type = map.getMimeTypeFromExtension(ext);

            if (type == null)
                type = "*/*";

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Requires for Android 8+
            //Uri data = Uri.fromFile(file);
            //Change the hard coded file provider to one taken from variable
            Uri data = FileProvider.getUriForFile(this, "com.dasmic.videograter.FileProvider", file);
            intent.setDataAndType(data, type);
            startActivity(intent);
        }
        catch(Exception ex) {
            SupportFunctions.DisplayToastMessageLong(this,
                    getResources().getString(
                            R.string.message_activity_load_fail)+ " Error:" +ex.getMessage());
        }

    }

    public void changeFolder(String currentFolder, DataVideoDisplay dvd){
        if(!dvd.HasReadPermission){
            SupportFunctions.DisplayToastMessageLong(this,getString(R.string.message_no_read_permission                                             ));
            return;
        }

        if (dvd.getName().equals("..")) {
            //Goto new top level folder
            setCurrentBrowseFolder(FileOperations.getParentFolder(currentFolder));
        } else
            setCurrentBrowseFolder(dvd.AbsoluteFilePath);

        ReloadListView();
    }

    //Central place to set Current Browse Folder
    protected void setCurrentBrowseFolder(String currentBrowseFolder){
        if(currentBrowseFolder != null) {
            try {
                _vmVideoBrowser.setCurrentBrowseFolder(currentBrowseFolder);
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
                    ArrayList<DataVideoDisplay> mainList =
                            _vmVideoBrowser.getCurrentDisplayOptionValues();
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
                _listAdapter.setListener(new IGenericEvent() {
                    public void onEvent(int value) {
                        // do something with item
                        updateSelectionCountText(value);
                    }
                });
                customContactsLoadOperation();

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

        textDisplayCount.setText(_selectionCount + "/" + totalCount +
                " " + getResources().getText(R.string.general_selected_items));
    }

    protected void setCheckValue(DataVideoDisplay dvd, boolean value){
        if(dvd.isGotoParentFolder()) return; //Never check ifGotFolder
        dvd.IsChecked = value;
        if(value)_selectionCount++;
        else
        if(_selectionCount > 0) _selectionCount--;
    }

    protected void updateSelectionCountText(int value){
        if(value==1) _selectionCount++;
        else _selectionCount--;
        setSelectionCountText();
    }

    protected boolean checkAllowedOption(){
        return true;
    }

    protected boolean checkSelectionMinimum(){
        int size = _vmVideoBrowser.getCheckedItems().size();
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

        switch(_vmVideoBrowser.getCurrentDisplayOption()){
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
    protected void  ReloadListView(boolean flag) {
        if(!AppOptions.IS_FOR_AMAZON)
            RateThisApp.ShowRateAppDialog(this); //To show Rate this app

        UncheckAll();
        _vmVideoBrowser.InvalidateData();

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
        //updateFolderPathDisplay();
        updateFreeSpaceDisplay();
        //------------------
        PopulateListBoxCurrentDisplayOption();
    }


    private void updateFolderPathDisplay(){
        //TextView tv = (TextView) findViewById(R.id.textFolderPath);
        //tv.setText(_vmVideoBrowser.getCurrentFolder());
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

    protected void onButtonCheckUncheck(){
        Log.i("CKIT", "ActivityMain::onCheckUncheck");
        //Do not process of _listAdapter is null
        if(_listAdapter == null) return;
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        //_vmVideoBrowser.allChecked = !_vmVideoBrowser.allChecked;
        for(int ii=0;ii<_listAdapter.getCount();ii++){
            setCheckValue(_listAdapter.getItem(ii),
                    checkAll.isChecked());
        }
        setSelectionCountText();
        _listView.invalidateViews();
    }

    protected void setLocalVariablesAndEventHandlers() {
        _selectionCount=0;
        _listView = (ListView) findViewById(R.id.listView);
        _vmVideoBrowser = ViewModelVideoBrowser.getInstance(this);
        _vmVideoBrowser.setCurrentDisplayOption(
                DisplayOptionsEnum.DefaultView);
        _vmVideoBrowser.setCurrentSearchOption(
                SearchOptionsEnum.ExternalFolder);
        setCurrentBrowseFolder(_startupFolderPath); //Only set initial folder

        //then it will be taken care of when user click on folder name
        //so the setCurrentBrowseFolder function will be called then
        _progressDialog = new ProgressDialogSpinner(this,
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
        setSortButton();
    }


    protected void onFileCopy(){
        if(checkSelection(true)) {
            if (checkSelectionMax(1)) {
                ArrayList<DataVideoDisplay> checkedItems =
                        _vmVideoBrowser.getCheckedItems();
                DataVideoDisplay dfd = checkedItems.get(0);
                //Do copy
                CopyFile();
            }
        }
    }

    protected void onRename(){
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(checkSelection(true)) {
            if (checkSelectionMax(1)) {
                ArrayList<DataVideoDisplay> checkedItems =
                        _vmVideoBrowser.getCheckedItems();
                final DataVideoDisplay dfd = checkedItems.get(0);
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
                            _vmVideoBrowser.RenameVideoFile(dfd,newName);
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
                    _vmVideoBrowser.CreateNewFolderInCurrentFolder(newName);
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
            ArrayList<DataVideoDisplay> allFiles
                    = _vmVideoBrowser.getCheckedItems();

            //Check if there is no folder
            ShareMultipleFiles(allFiles);
        }
    }

    protected void onFilesShareZip(){
        if(checkSelection(true)) {
            ArrayList<DataVideoDisplay> allFiles = _vmVideoBrowser.getCheckedItems();
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

    protected void ShareMultipleFiles(ArrayList<DataVideoDisplay> allFiles){
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
            ArrayList<DataVideoDisplay> _allFileDfd;

            @Override
            protected Void doInBackground(Void...params) {
                for(DataVideoDisplay dfd: _allFileDfd){
                    try {
                        _delCount += _vmVideoBrowser.DeleteVideoFile(dfd);
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
                SupportFunctions.DisplayToastMessageLong(getActivity(),
                        String.valueOf(_delCount) + " " +
                                getActivity().getText(
                                        R.string.message_delete_complete).toString());
            }


            @Override
            protected void onPreExecute() {
                _delCount=0;

                _allFileDfd =  _vmVideoBrowser.getCheckedItems();
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
            ArrayList<DataVideoDisplay> _allFileDfd;
            private boolean _error;

            @Override
            protected Void doInBackground(Void...params) {
                _error=false;
                for(DataVideoDisplay dfd: _allFileDfd){
                    try {
                        _vmVideoBrowser.CopyVideoFile(dfd);
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
                    SupportFunctions.AsyncDisplayGenericDialog(getActivity(),
                            getActivity().getString(
                                    R.string.message_copy_error),"");
                    SupportFunctions.vibrate(getActivity(),200);
                }

            }


            @Override
            protected void onPreExecute() {
                _allFileDfd =  _vmVideoBrowser.getCheckedItems();

                _progressDialog = new ProgressDialogSpinner(getActivity(),
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
                    ArrayList<DataVideoDisplay> allSelected =
                            _vmVideoBrowser.getCheckedItems();
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
                _progressDialog = new ProgressDialogSpinner(getActivity(),
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
        ArrayList<DataVideoDisplay> checkedItems= null;
        checkedItems =
                _vmVideoBrowser.getCheckedItems();
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
        _vmVideoBrowser.InvalidateData();//Will clean history of selection, be careful
        //Check for permissions
        if(checkPermissionsForAndroid6Plus())
            ReloadListView(false);
    }

    protected boolean checkSelection(boolean checkMin){
        ArrayList<DataVideoDisplay> checkedItems= null;
        checkedItems =
                _vmVideoBrowser.getCheckedItems();
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
                _vmVideoBrowser.changeSortOrder();
                RefreshListView();
                updateSortedImage();
            }
        });
        updateSortedImage();
    }

    protected void updateSortedImage(){
        ImageButton ib = (ImageButton) findViewById(
                R.id.buttonImageSort);

        if(_vmVideoBrowser.getCurrentSortOrder()) //Then Descending
            ib.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_48dp);
        else
            ib.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_48dp);
    }


    protected void onExtractPictures(){
        if(checkSelection(true))
            if (checkSelectionMax(1)) {
                Intent myIntent = new Intent(this, ActivityExtractPictures.class);
                this.startActivityForResult(myIntent,
                        AppOptions.EXTRACT_PICTURES_ACTIVITY_REQUEST);
            }
    }

    protected void onSearchEntirePhone(){
        //Ask for new name - Dialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(getActivity().getString(
                R.string.title_search_depth));
        // Set up the input
        final EditText editText = new EditText(getActivity());

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setText(String.valueOf(_vmVideoBrowser.getCurrentSearchDepth()));
        dialog.setView(editText);

        // Set up the buttons
        dialog.setPositiveButton(getActivity().getString(
                R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int newValue = Integer.valueOf(editText.getText().toString());
                    _vmVideoBrowser.setCurrentSearchDepth(newValue);
                    ReloadListView();
                    SupportFunctions.DisplayToastMessageShort(getActivity(),(String) getActivity().getString(
                            R.string.message_search_depth_updated));
                }
                catch(Exception ex){
                    SupportFunctions.DisplayErrorToast(getActivity(),(String) getActivity().getString(
                            R.string.message_search_depth_error));
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

    }

    protected void customContactsLoadOperation(){

    }

}
