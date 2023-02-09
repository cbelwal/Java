package com.dasmic.android.calllogbackup.Activity;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;

import com.dasmic.android.calllogbackup.R;
import com.dasmic.android.lib.calllog.Activity.ActivityAddLog;
import com.dasmic.android.lib.calllog.Activity.ActivityBackup;
import com.dasmic.android.lib.calllog.Activity.ActivityBaseMain;
import com.dasmic.android.lib.calllog.Activity.ActivityBluetooth;
import com.dasmic.android.lib.calllog.Activity.ActivityEditLog;
import com.dasmic.android.lib.calllog.Activity.ActivityExport;
import com.dasmic.android.lib.calllog.Activity.ActivityFilter;
import com.dasmic.android.lib.calllog.Activity.ActivityImport;
import com.dasmic.android.lib.calllog.Activity.ActivityRestore;
import com.dasmic.android.lib.calllog.Activity.ActivityShare;
import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Enum.AppOptions;
import com.dasmic.android.lib.calllog.Enum.DisplayCallLogCountEnum;
import com.dasmic.android.lib.calllog.Enum.DisplayCallLogEnum;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelCallLogDisplay;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Feedback.Feedback;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.Feedback.TellAFriend;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.google.firebase.messaging.FirebaseMessaging;

public class ActivityMainCL extends ActivityBaseMain
        implements NavigationView.OnNavigationItemSelectedListener{ //ActionBarActivity {


    final String helpURL =
            "http://www.coju.mobi/android/calllogbackup/faq/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CKIT", "ActivityMainCL is created");
        setContentView(R.layout.ui_main_cl);

        //----------------------------------------------
        _paid_version_sku_id =
                "com.dasmic.android.calllogbackup.proversion";
        _base64EncodedPublicKey =
                getContext().getText(R.string.license_one).toString() +
                        getContext().getText(
                                R.string.license_two).toString() +
                        getContext().getText(
                                R.string.license_three).toString() +
                        getContext().getText(
                                R.string.license_four).toString();

        if(AppOptions.IS_FOR_AMAZON) {
            AppOptions.isFreeVersion = false;
            setLocalVersionBasedUI();
        }
        else {
            setupForInAppPurchase();
        }
        //-----------------------------------------------------
        _ad_interstitial_id=getString(R.string.ad_main_is_id); //For Interstitial Ads

        setLocalVariablesAndEventHandlers();
        setDrawerMenu();
        setActionBarTitle();
        RefreshListView();
        AppOptions.FILE_PROVIDER_AUTHORITY=
                "com.dasmic.calllogbackup.FileProvider";
        demo_video_id = "4KOg6ZhygvQ";
        HelpMedia.ShowDemoVideoDialog(this,demo_video_id);
        setSortButton();
        //Subscribe to generic message to all apps
        FirebaseMessaging.getInstance().subscribeToTopic("Updates.General");
    }


    //This is very important since if Activity is restarted in
    //screen change, there is a major conflict with RefreshListView
    //This happens when RefreshListView is called along
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


    private void setDrawerMenu(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        _drawerToggle = new ActionBarDrawerToggle(
                 this,drawer ,R.string.menu_import_cl,R.string.menu_import_cl);
        drawer.setDrawerListener(_drawerToggle);
        _drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Set Navigation View listener
        NavigationView navigationView = (NavigationView)
                findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setActionBarTitle(){
       try {
           getSupportActionBar().setTitle(getContext().getText(
                       R.string.app_name).toString());
       }
       catch(Exception ex){ //If ActionBar is not found
           SupportFunctions.DebugLog("ActivityMainCL",
                   "SetLabelText", "Error:" + ex.getMessage());
       }
    }


    private void ShowCheckAll(){
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setVisibility(CheckBox.VISIBLE);
    }

    private void HideCheckAll(){
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setVisibility(CheckBox.INVISIBLE);
    }

    private void setButtonEventHandlers(){
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCheckUncheck();
            }
        });

    }

    private void setLocalVariablesAndEventHandlers() {
        _selectionCount=0;
        _listView = (ListView) findViewById(R.id.listView);
        _vmCallLog = ViewModelCallLogDisplay.getInstance(this);
        _pdSpin = new ProgressDialogSpinner(this,
                this.getResources().getText(R.string.progressbar_load_data_cl).toString());
        setButtonEventHandlers();
    }


    //Add Items to the action bar here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_cl, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        mDeleteMenuItem =menu.findItem(R.id.action_delete);

        final SearchView searchView = (SearchView) (menuItem.getActionView()).findViewById(R.id.textSearch);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search view is expanded
                ((SearchView) v).setMaxWidth(getSearchBoxWidth());

                Log.i("CKIT", "ActivityMainCL::SearchExpanded");
                HideCheckAll();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //Search View is collapsed
                Log.i("CKIT", "ActivityMainCL::SearchCollapsed");
                ShowCheckAll();
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                              @Override
                                              public boolean onQueryTextSubmit(String query) {
                                                  searchView.clearFocus();
                                                  Log.i("CKIT", "ActivityMainCL::setOnQueryTextListener");
                                                  return false;
                                              }

                                              @Override
                                              public boolean onQueryTextChange(String newText) {
                                                  // Do something while user is entering text
                                                  Log.i("CKIT", "ActivityMainCL::setOnQueryTextListener");
                                                  if(_listAdapter != null)
                                                        _listAdapter.getFilter().filter(newText);
                                                  return false;
                                              }
                                          }

        );

            return true;
        }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        boolean refresh=false;
        boolean invalidateAndRefresh=false;


        Intent myIntent;
        switch(id){
            case R.id.nav_view_all_cl:
                _vmCallLog.setCurrentDisplayOption(DisplayCallLogEnum.AllCalls);
                refresh=true;
                break;
            case R.id.nav_view_missedcalls_cl:
                 _vmCallLog.setCurrentDisplayOption(DisplayCallLogEnum.MissedCalls);
                refresh=true;
                break;
            case R.id.nav_view_incomingcalls_cl:
                _vmCallLog.setCurrentDisplayOption(DisplayCallLogEnum.IncomingCalls);
                refresh=true;
                break;
            case R.id.nav_view_outgoingcalls_cl:
                _vmCallLog.setCurrentDisplayOption(DisplayCallLogEnum.OutgoingCalls);
                refresh=true;
                break;
            case R.id.nav_view_call_duration_cl:
                _vmCallLog.setCurrentDisplayOption(
                        DisplayCallLogEnum.SortedByCallDuration);
                refresh=true;
                break;
            case R.id.nav_view_geocoded_location_cl:
                _vmCallLog.setCurrentDisplayOption(
                        DisplayCallLogEnum.SortedByGeocodedLocation);
                refresh=true;
                break;
            case R.id.nav_view_100_records_cl:
                _vmCallLog.setCurrentDisplayCount(
                        DisplayCallLogCountEnum.View100);
                invalidateAndRefresh=true;
                break;
            case R.id.nav_view_500_records_cl:
                _vmCallLog.setCurrentDisplayCount(
                        DisplayCallLogCountEnum.View500);
                invalidateAndRefresh=true;
                break;
            case R.id.nav_view_all_records_cl:
                _vmCallLog.setCurrentDisplayCount(
                        DisplayCallLogCountEnum.ViewAll);
                invalidateAndRefresh=true;
                break;
            case R.id.nav_view_rate_this_app_cl:
                ShowRateThisApp();
                break;
            case R.id.nav_view_backup_contacts_cl:
                myIntent = new Intent(this, ActivityBackup.class);
                this.startActivityForResult(myIntent,
                        AppOptions.CALLLOG_BACKUP_ACTIVITY_REQUEST);
                break;
            case R.id.nav_view_restore_contacts_cl:
                myIntent = new Intent(this, ActivityRestore.class);
                myIntent.putExtra(AppOptions.APP_NAME,
                        getResources().getString(R.string.app_folder));
                this.startActivityForResult(myIntent,
                        AppOptions.CALLLOG_IMPORT_ACTIVITY_REQUEST);
                break;

            case R.id.nav_view_upgrade_paid_version_cl:
                purchasePaidVersion();
                break;
            case R.id.nav_view_filter:
                myIntent = new Intent(this, ActivityFilter.class);
                this.startActivityForResult(myIntent, AppOptions.CALLLOG_FILTER_ACTIVITY_REQUEST);
                break;
            case R.id.nav_view_feedback_cl:
                Feedback.SendFeedbackByEmail(this);
                break;
            case R.id.nav_view_demo_video_cl:
                HelpMedia.ShowDemoVideo(this,
                        demo_video_id);
                break;
            case R.id.nav_view_tell_a_friend_cl:
                TellAFriend.TellAFriend(this,
                        getString(R.string.message_tellafriend));
                break;
            case R.id.nav_view_help_documentation_cl:
                HelpMedia.ShowWebURL(this,helpURL);
                break;
            case R.id.nav_view_refresh_cl:
                invalidateAndRefresh=true;
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout)
                findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(refresh) {
           RefreshListView();
        }
        else if(invalidateAndRefresh){
            InvalidateAndRefreshListView();
        }

        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            // Pass the event to ActionBarDrawerToggle, if it returns
            // true, then it has handled the app icon touch event
            if (_drawerToggle.onOptionsItemSelected(item)) {
                return true;
            }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        int size=0;
        Intent myIntent=null;
        ArrayList<DataCallLogDisplay> checkedItems= null;
        //noinspection SimplifiableIfStatement
        switch(id)
        {
            case R.id.action_delete:
                onButtonDelete();
                break;
            case R.id.action_add:
                //Launch Activity
                //No selection needed
                myIntent = new Intent(this, ActivityAddLog.class);
                setInternalDBChange();
                this.startActivityForResult(myIntent,
                        AppOptions.CALLLOG_ADD_ACTIVITY_REQUEST);
                break;
            case R.id.action_edit:
                //Launch Activity
                //Selection needed
                if(!checkSelection()) break;
                setInternalDBChange();
                myIntent = new Intent(this, ActivityEditLog.class);
                this.startActivityForResult(myIntent,
                        AppOptions.CALLLOG_EDIT_ACTIVITY_REQUEST);
                break;
            case R.id.action_share:
                if(!checkSelection()) break;
                myIntent = new Intent(this, ActivityShare.class);
                myIntent.putExtra(AppOptions.APP_NAME,
                        getResources().getString(R.string.app_name_small));
                this.startActivityForResult(myIntent, AppOptions.CALLLOG_EXPORT_ACTIVITY_REQUEST);
                break;
            case R.id.action_export:
                if(!checkSelection()) break;
                myIntent = new Intent(this, ActivityExport.class);
                myIntent.putExtra(AppOptions.APP_NAME,
                        getResources().getString(R.string.app_folder));
                this.startActivityForResult(myIntent, AppOptions.CALLLOG_EXPORT_ACTIVITY_REQUEST);
                break;
            case R.id.action_import:
                setInternalDBChange();
                myIntent = new Intent(this, ActivityImport.class);
                myIntent.putExtra(AppOptions.APP_NAME,
                        getResources().getString(R.string.app_name_small));
                this.startActivityForResult(myIntent,
                        AppOptions.CALLLOG_IMPORT_ACTIVITY_REQUEST);
                break;
            case R.id.action_bluetooth:
                if(!checkSelection(false)) break;
                setInternalDBChange();
                myIntent = new Intent(this, ActivityBluetooth.class);
                myIntent.putExtra(AppOptions.APP_NAME,
                        getResources().getString(R.string.app_folder));
                this.startActivityForResult(myIntent,
                        AppOptions.CALLLOG_BLUETOOTH_ACTIVITY_REQUEST);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        boolean bFlag=true;

        if(AppOptions.IS_FOR_AMAZON)
            bFlag=true;
        else
            bFlag=!_inAppPurchases.handleActivityResult(requestCode, resultCode, data);

        if (bFlag) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case (AppOptions.CALLLOG_ADD_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        InvalidateAndRefreshListView();
                    break;
                case (AppOptions.CALLLOG_EDIT_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        InvalidateAndRefreshListView();
                    break;
                case (AppOptions.CALLLOG_IMPORT_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        InvalidateAndRefreshListView(); //New contact have been added
                    break;
                case (AppOptions.CALLLOG_EXPORT_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        UncheckAll(); //Just UnselectAll
                    break;
                case (AppOptions.CALLLOG_RESTORE_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        InvalidateAndRefreshListView(); //New contact have been added
                    break;
                case (AppOptions.CALLLOG_BLUETOOTH_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        InvalidateAndRefreshListView(); //New contact have been added
                    break;
                case (AppOptions.CALLLOG_FILTER_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        InvalidateAndRefreshListView(); //New contact have been added
                    break;
            }
        }
    }


    //Color code based on Call Type
    @Override
    protected int getColor(DataCallLogDisplay contact){
            int bkColor=255;
            //Set all in Missed Call color
            bkColor= ContextCompat.getColor(this,
                R.color.MissedCall);

            if (contact.isIncomingCall())
                bkColor= ContextCompat.getColor(this,
                                R.color.IncomingCall);
            if (contact.isOutgoingCall())
                bkColor= ContextCompat.getColor(this,
                        R.color.OutgoingCall);

            return bkColor;

    }

    private void ShowRateThisApp(){
        RateThisApp rtp=new RateThisApp();
        rtp.ShowRateThisApp(this);

    }

}
