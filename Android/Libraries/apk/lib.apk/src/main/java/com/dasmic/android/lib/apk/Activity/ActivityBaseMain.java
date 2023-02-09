package com.dasmic.android.lib.apk.Activity;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Enum.ActivityOptions;
import com.dasmic.android.lib.apk.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.apk.Extension.ListViewAdapter;
import com.dasmic.android.lib.apk.Extension.ProgressDialogCustom;
import com.dasmic.android.lib.apk.Interface.IGenericEvent;
import com.dasmic.android.lib.apk.Interface.IGenericParameterLessEvent;
import com.dasmic.android.lib.apk.R;
import com.dasmic.android.lib.apk.ViewModel.ViewModelAPKSplitter;
import com.dasmic.android.lib.apk.ViewModel.ViewModelAppsDisplay;
import com.dasmic.android.lib.apk.ViewModel.ViewModelStopProcess;
import com.dasmic.android.lib.filebrowser.Data.ActivityStartupData;
import com.dasmic.android.lib.filebrowser.Data.DataFileDisplay;
import com.dasmic.android.lib.filebrowser.Enum.AppOptions;
import com.dasmic.android.lib.support.Ad.Interstilial;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.SupportFunctions;


public class ActivityBaseMain extends ActivityBaseAd
        { //ActionBarActivity {
        protected int _selectionCount;
        //Controls
        protected ListViewAdapter _listAdapter;
        protected ListView _listView;
        protected ProgressDialogCustom _progressDialog;
        protected ActionBarDrawerToggle _drawerToggle;
        protected ViewModelAppsDisplay _vmApps;
        protected int _deleteCount;
        protected InAppPurchases _inAppPurchases;
        protected String helpURL;
        protected String demo_video_id;
        protected String _base64EncodedPublicKey;
        protected String _paid_version_sku_id;
        protected String _ad_interstitial_id;
        protected Interstilial _interstilial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                            ActivityOptions.isFreeVersion = !isPaidVersion;
                            // update UI accordingly
                            setVersionBasedUI();
                        }
                    });
        }
        catch(Exception ex) { //Exception Handling
            ActivityOptions.isFreeVersion = true;
            // update UI accordingly
            setVersionBasedUI();
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

    protected Context getContext(){
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
        //_vmApps.allChecked = !_vmApps.allChecked;
        for(int ii=0;ii<_listAdapter.getCount();ii++){
            setCheckValue(_listAdapter.getItem(ii),
                    checkAll.isChecked());
        }
        setSelectionCountText();
        _listView.invalidateViews();
    }

    protected Activity getActivity(){
        return this;
    }

    protected void showDialogStopAllRunningTasks(){
        //Wait till list populated
        while(_listAdapter == null);

        autoSelectRunningProcesses();

        if(_vmApps.getCheckedItems().size() <= 0)
        {
            SupportFunctions.DisplayToastMessageLong(this,
                    getString(R.string.message_no_user_process));
            //Dont do anything
            return;
        }

        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setTitle(this.getResources().getText(
                R.string.app_name));
        dlgAlert.setMessage(
                String.valueOf(this.getResources().getText(
                        R.string.message_stop_running_tasks)));
        dlgAlert.setCancelable(false);
        dlgAlert.setPositiveButton(this.getResources().getText(
                R.string.button_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //Show Google URL
                launchStopProcesses();
                dialog.dismiss();
            }
        });

        dlgAlert.setNegativeButton(
                this.getResources().getText(R.string.button_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UncheckAll();
                        dialog.dismiss();//Do nothing
                    }
                });
        AlertDialog alert = dlgAlert.create();
        alert.show();

    }

    protected void autoSelectRunningProcesses(){
        ArrayList<DataPackageDisplay> mainList =
                _vmApps.getCurrentDisplayOptionContacts();

        for(DataPackageDisplay dpd:mainList){
            if(!dpd.getIsSystemApp() &&
                    !dpd.getPackageName().equals(this.getPackageName()))
                dpd.isChecked=true;
        }
        PopulateListBoxCurrentDisplayOption();
        recomputeSelectionCountValue();
        //Will be auto reloaded once Activity ends
    }

    //Recompute selection count value
    //Used only when selection is changed from
    protected void recomputeSelectionCountValue(){
                _selectionCount=0;
                if(_listAdapter == null) return; //Protection
                for(int ii=0;ii<_listAdapter.getCount();ii++){
                    if(_listAdapter.getItem(ii).isChecked)
                        _selectionCount++;
                }
                setSelectionCountText();
        }

    protected void launchStopProcesses(){
        Intent myIntent;
        if(checkSelectionMinimum()) {
            myIntent = new Intent(this, ActivityStopProcesses.class);
            this.startActivityForResult(myIntent,
                    ActivityOptions.STOP_PROCESES_ACTIVITY_REQUEST);
        }
    }

    protected int getCurrentBatterState(){
        return ViewModelStopProcess.getBatteryPercentage(getActivity());
    }

    protected void RemovePackages()
    {
        try {
            _deleteCount = _vmApps.getCheckedItems().size();
            SupportFunctions.DisplayToastMessageLong(getContext(),
                    getContext().getText(
                            R.string.message_deleting_files).toString());
            _vmApps.Delete(
                    _vmApps.getCheckedItems());
        }
        catch(Exception ex){
            Log.i("CKIT","Exception in Main::delete"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    protected void onButtonDelete(){
        if(checkSelection()) {
            RemovePackages();
        }
    }

    protected void ShowCheckAll(){
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setVisibility(CheckBox.VISIBLE);
    }

    protected void HideCheckAll(){
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setVisibility(CheckBox.INVISIBLE);
    }

    protected void setButtonEventHandlers(){
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCheckUncheck();
            }
        });
    }

    protected void setLocalVariablesAndEventHandlers() {
        _selectionCount=0;

        _listView = (ListView) findViewById(R.id.listView);
        _vmApps = ViewModelAppsDisplay.getInstance(this,_isMalwareProgram);
        _vmApps.setCurrentDisplayOption(
                DisplayOptionsEnum.SecurityPermissions);

        _progressDialog = new ProgressDialogCustom(this,
                this.getResources().getText(R.string.progressbar_load_data).toString());

        setButtonEventHandlers();
    }

    protected void setItemCountText(){
        TextView textDisplayCount = (TextView) findViewById(R.id.textDisplayCount);
        textDisplayCount.setText(_listAdapter.getCount() +
                " " + getResources().getText(R.string.general_items));
    }


    protected void setSelectionCountText(){
        TextView textDisplayCount = (TextView) findViewById(R.id.textSelectCount);
        textDisplayCount.setText(_selectionCount +
                " " + getResources().getText(R.string.general_selected_items));
    }

    protected void setCheckValue(DataPackageDisplay dpd, boolean value){
        dpd.isChecked=
                value;
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
        int size = _vmApps.getCheckedItems().size();
        if(size <= 0){
            SupportFunctions.DisplayToastMessageShort(
                    this, (String) this.getResources().getString(
                            R.string.message_selectitems));
            SupportFunctions.vibrate(this,300);
            return false;
        }
        return true;
    }

    //Returns false if more than 1 is selected
    protected boolean checkSingleSelection(){
        ArrayList<DataPackageDisplay> checkedItems= null;
        int size=0;
        checkedItems =
                _vmApps.getCheckedItems();
        size=checkedItems.size();
        checkSelectionMinimum();

        if(size > 1) {
                SupportFunctions.DisplayToastMessageLong(
                        this,
                        (String) this.getResources().getString(
                                R.string.message_single_selection));
                SupportFunctions.vibrate(this,300);

                return false;
        }

        return true;
    }


    protected boolean checkSelection(){
        ArrayList<DataPackageDisplay> checkedItems= null;
        int size=0;
        checkedItems =
                _vmApps.getCheckedItems();
        size=checkedItems.size();
        checkSelectionMinimum();
        if(ActivityOptions.isFreeVersion){
            if(size > ActivityOptions.FREE_VERSION_CONTACT_LIMIT) {
                SupportFunctions.DisplayToastMessageLong(
                        this,
                        (String) this.getResources().getString(
                                R.string.message_free_version_selection));
                SupportFunctions.vibrate(this,300);

                return false;
            }
        }
        return true;
    }

    //Add Items to the action bar here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) (menuItem.getActionView()).findViewById(R.id.textSearch);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search view is expanded
                ((SearchView) v).setMaxWidth(getSearchBoxWidth());

                Log.i("CKIT", "ActivityMain::SearchExpanded");
                HideCheckAll();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //Search View is collapsed
                Log.i("CKIT", "ActivityMain::SearchCollapsed");
                ShowCheckAll();
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                              @Override
                                              public boolean onQueryTextSubmit(String query) {
                                                  searchView.clearFocus();
                                                  Log.i("CKIT", "ActivityMain::setOnQueryTextListener");
                                                  return false;
                                              }

                                              @Override
                                              public boolean onQueryTextChange(String newText) {
                                                  // Do something while user is entering text
                                                  Log.i("CKIT", "ActivityMain::setOnQueryTextListener");
                                                  if(_listAdapter!=null)
                                                      _listAdapter.getFilter().filter(newText);
                                                  return false;
                                              }
                                          }

        );

        return true;
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
                    ArrayList<DataPackageDisplay> mainList =
                            _vmApps.getCurrentDisplayOptionContacts();
                    _listAdapter = new ListViewAdapter(_context,mainList);
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
                setItemCountText();
                setSelectionCountText();
                additionalPostDisplayFunction();
                _listAdapter.setListener(new IGenericParameterLessEvent() {
                    public void onEvent() {
                        // do something with item
                        setItemCountText();
                    }
                });
                _listAdapter.setListener(new IGenericEvent() {
                    public void onEvent(int value) {
                        // do something with item
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
        if(checkPermissions())
            ReloadListView(true);
    }

    protected void additionalPostDisplayFunction(){

    }

    protected boolean checkPermissions(){
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
                    ReloadListView();
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

     protected int getSearchBoxWidth(){
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                return width/2;//Do not let it take more than half
            }

    //Invalidate current data and reload listView
    //flag is dummy, for overloading purposes
    protected void ReloadListView(boolean flag) {
        if(!ActivityOptions.IS_FOR_AMAZON)
            RateThisApp.ShowRateAppDialog(this); //To show Rate this app

        UncheckAll();
        _vmApps.InvalidateData();

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
        PopulateListBoxCurrentDisplayOption();

    }

    protected void ShowRateThisApp(){
        RateThisApp rtp=new RateThisApp();
        rtp.ShowRateThisApp(this);
    }

    protected void setNavigationMenuSelectedItems(){
        NavigationView navigationView = (NavigationView)
                        findViewById(R.id.nav_view);

        setNavigationCheckedValue(navigationView,R.id.nav_view_default,false);
        setNavigationCheckedValue(navigationView,R.id.nav_view_installedDate,false);
        setNavigationCheckedValue(navigationView,R.id.nav_view_launchIntent,false);
        setNavigationCheckedValue(navigationView,R.id.nav_view_securityPermissions,false);
        setNavigationCheckedValue(navigationView,R.id.nav_view_size,false);
        setNavigationCheckedValue(navigationView,R.id.nav_view_updateDate,false);
        setNavigationCheckedValue(navigationView,R.id.nav_view_currently_running,false);


        switch(_vmApps.getCurrentDisplayOption()){
            case CurrentlyRunning:
                setNavigationCheckedValue(navigationView,R.id.nav_view_currently_running,true);
                break;
            case defaultView:
                setNavigationCheckedValue(navigationView,R.id.nav_view_default,true);
                break;

            case InstalledOnDate:
                setNavigationCheckedValue(navigationView,R.id.nav_view_installedDate,true);
                break;
            case LaunchIntent:
                setNavigationCheckedValue(navigationView,R.id.nav_view_launchIntent,true);
                break;
            case SecurityPermissions:
                setNavigationCheckedValue(navigationView,R.id.nav_view_securityPermissions,true);
                break;
            case PackageSize:
                setNavigationCheckedValue(navigationView,R.id.nav_view_size,true);
                break;
            case UpdatedOnDate:
                setNavigationCheckedValue(navigationView,R.id.nav_view_updateDate,true);
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

    protected void onSplitAPK(String fileProviderAuthority, String msgTellaFriend){
        if(!checkSelection()) return;
        if(!checkSingleSelection()) return;
        ActivityStartupData ad = getBasicActivityData();
        ad.TellAFriendText = msgTellaFriend;
        ad.FileProviderAuthority = fileProviderAuthority;
        ad.StartupFolderType=1;//External folder
        doSplitAPK(ad);
    }

    protected void launchFileBrowser(ActivityStartupData ad){
        Intent myIntent = new Intent(this, com.dasmic.android.lib.filebrowser.Activity.ActivityMain.class);
        myIntent.putExtra("asd",ad); //Name has to be "asd"
        this.startActivityForResult(myIntent, ActivityOptions.SPLIT_ACTIVITY_REQUEST);
    }

    protected ActivityStartupData getBasicActivityData(){
        ActivityStartupData activityData = new ActivityStartupData();
        activityData.Ad_interstitial_id=_ad_interstitial_id;
        activityData.Base64EncodedPublicKey=_base64EncodedPublicKey;
        activityData.Demo_video_id=demo_video_id;
        activityData.HelpURL=helpURL;
        activityData.Paid_version_sku_id=_paid_version_sku_id;
        activityData.Ad_interstitial_id=getString(R.string.ad_main_is_id);
        activityData.ForAmazon = ActivityOptions.IS_FOR_AMAZON?1:0;
        return activityData;
    }

    protected void doSplitAPK(ActivityStartupData activityData)
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
                ActivityStartupData _activityData;
                boolean _error;

                public BasicAsyncTask(ActivityStartupData activityData){
                    _activityData = activityData;
                    _error=false;
                }
                    @Override
                    protected Void doInBackground(Void...params) {
                        try {
                            ViewModelAPKSplitter vmAPKContents =
                                    new ViewModelAPKSplitter(getActivity());
                            ArrayList<DataPackageDisplay> checkedItems =
                                    _vmApps.getCheckedItems();
                            String zipToFolder = vmAPKContents.SplitAPK(checkedItems.get(0));
                            _activityData.StartupFolderPath = zipToFolder;
                        }
                        catch(Exception ex){
                            _error = true;
                        }
                        return null;
                    }


                    @Override
                    protected void onPostExecute(Void param) {
                        // _listAdapter.registerDataSetObserver();
                        _progressDialog.dismiss();

                        if(_error) {
                            SupportFunctions.DisplayToastMessageLong(getActivity(),
                                    getActivity().getString(R.string.message_extract_contents_error));
                        }
                        else{
                            SupportFunctions.DisplayToastMessageLong(getActivity(),
                                    getActivity().getString(R.string.message_extract_contents_complete));
                            launchFileBrowser(_activityData);
                        }
                    }


                    @Override
                    protected void onPreExecute() {
                        _progressDialog = new ProgressDialogCustom(getActivity(),
                                getActivity().getString(R.string.message_extract_contents_progress));
                        _progressDialog.show();
                    }

                    @Override
                    protected void onProgressUpdate(Void...params) {

                    }
                }
                Log.i("CKIT", "ActivityMain::onDeleteContacts");

                AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(activityData);
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
}
