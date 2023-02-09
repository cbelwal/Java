package com.dasmic.android.apkviewer.Activity;


import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;


import com.dasmic.android.lib.apk.Activity.ActivityBaseMain;
import com.dasmic.android.lib.apk.Activity.ActivityExport;
import com.dasmic.android.lib.apk.Activity.ActivityFilter;
import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Enum.ActivityOptions;
import com.dasmic.android.lib.apk.Enum.DisplayOptionsEnum;
import com.dasmic.android.apkviewer.R;
import com.dasmic.android.lib.apk.ViewModel.ViewModelLinksReport;
import com.dasmic.android.lib.support.Feedback.Feedback;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Feedback.TellAFriend;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.google.firebase.messaging.FirebaseMessaging;


public class ActivityMain extends ActivityBaseMain
        implements NavigationView.OnNavigationItemSelectedListener { //ActionBarActivity {

    protected String demo_video_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CKIT", "ActivityMain is created");
        setContentView(R.layout.ui_main);
        _paid_version_sku_id =
                "com.dasmic.android.apkviewer.paidversion"; //Dont change location


        helpURL =
                "http://www.coju.mobi/android/apkit/faq/index.html";
        demo_video_id = "LAKqZtkaqTk";
        _base64EncodedPublicKey =
                getContext().getText(R.string.license_one).toString() +
                        getContext().getText(
                                R.string.license_two).toString() +
                        getContext().getText(
                                R.string.license_three).toString() +
                        getContext().getText(
                                R.string.license_four).toString();

        //Do this before calling Super
        _ad_interstitial_id=getString(R.string.ad_main_is_id); //For Interstitial Ads

        if(ActivityOptions.IS_FOR_AMAZON) {
            ActivityOptions.isFreeVersion = false;
            setVersionBasedUI();
        }
        else {
            setupForInAppPurchase();
        }
        //----------------------------
        setLocalVariablesAndEventHandlers();
        setDrawerMenu();
        ReloadListView();
        HelpMedia.ShowDemoVideoDialog(this, demo_video_id);
        //Subscribe to generic message to all apps
        FirebaseMessaging.getInstance().subscribeToTopic("Updates.General");
    }

    @Override
    protected void setLocalVariablesAndEventHandlers() {
        super.setLocalVariablesAndEventHandlers();
        _vmApps.setCurrentDisplayOption(
                DisplayOptionsEnum.defaultView);
    }

    //This is very important since if Activity is restarted in
    //screen change, there is a major conflict with ReloadListView
    //This happens when ReloadListView is called along
    //with ActivityLoad and happens when screen orientation
    //is changed along with some update
    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }*/

    @Override
    public void onDestroy() { //In App billing cleanup
        super.onDestroy();
        if (_inAppPurchases != null) _inAppPurchases.dispose();
        _inAppPurchases = null;
    }



    private void setDrawerMenu(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        _drawerToggle = new ActionBarDrawerToggle(
                this,drawer,R.string.nav_menu_group_viewoptions,
                R.string.nav_menu_group_viewoptions);
        drawer.addDrawerListener(_drawerToggle);
        _drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Set Navigation View listener
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }




    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        boolean refresh=false;
        switch(id){
            case R.id.nav_view_default:
                refresh=true;
                _vmApps.setCurrentDisplayOption(
                        DisplayOptionsEnum.defaultView);
                break;
            case R.id.nav_view_size:
                refresh=true;
                _vmApps.setCurrentDisplayOption(
                        DisplayOptionsEnum.PackageSize);
                break;

            case R.id.nav_view_sourcedir:
                refresh=true;
                _vmApps.setCurrentDisplayOption(
                        DisplayOptionsEnum.PackageSourceName);
                break;
            case R.id.nav_view_updateDate:
                refresh=true;
                _vmApps.setCurrentDisplayOption(
                        DisplayOptionsEnum.UpdatedOnDate);
                break;
            case R.id.nav_view_installedDate:
                refresh=true;
                _vmApps.setCurrentDisplayOption(
                        DisplayOptionsEnum.InstalledOnDate);
                break;
            case R.id.nav_view_launchIntent:
                refresh=true;
                _vmApps.setCurrentDisplayOption(
                        DisplayOptionsEnum.LaunchIntent);
                break;
            case R.id.nav_view_securityPermissions:
                if(checkAllowedOption()) {
                    refresh=true;
                    _vmApps.setCurrentDisplayOption(
                            DisplayOptionsEnum.SecurityPermissions);
                }
                break;
            case R.id.nav_view_upgrade_paid_version:
                purchasePaidVersion();
                break;
            case R.id.nav_view_rate_this_app:
                ShowRateThisApp();
                break;
            case R.id.nav_view_feedback:
                Feedback.SendFeedbackByEmail(this);
                break;
            case R.id.nav_view_tell_a_friend:
                TellAFriend.TellAFriend(this,
                        getString(R.string.message_tellafriend));
                break;
            case R.id.nav_view_action_refresh:
                ReloadListView();
                break;
            case R.id.nav_view_demo_video:
                HelpMedia.ShowDemoVideo(this,
                        demo_video_id);
                break;
            case R.id.nav_view_help_documentation:
                HelpMedia.ShowWebURL(this, helpURL);
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout)
                findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(refresh) {
            UncheckAll();
            PopulateListBoxCurrentDisplayOption();
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
            ViewModelLinksReport vml=null;
        ArrayList<DataPackageDisplay> checkedItems= null;
        //noinspection SimplifiableIfStatement
        switch(id)
        {
            case R.id.action_filter:
                myIntent = new Intent(this, ActivityFilter.class);
                this.startActivityForResult(myIntent, ActivityOptions.FILTER_ACTIVITY_REQUEST);
                break;
            case R.id.action_delete:
                onButtonDelete();
                break;
            case R.id.action_share_apk:
                if(!checkSelection()) break;
                myIntent = new Intent(this, ActivityExport.class);
                this.startActivityForResult(myIntent,
                        ActivityOptions.EXPORT_ACTIVITY_REQUEST);
                break;
            case R.id.action_share_link:
                if(!checkSelection()) break;
                 vml=new ViewModelLinksReport(this,false);
                vml.SendPackageLinks(_vmApps.getCheckedItems());
                break;
            case R.id.action_share_report:
                if(!checkSelection()) break;
                vml=new ViewModelLinksReport(this,false);
                vml.SendPackageReports(_vmApps.getCheckedItems());
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        // This is very important
        boolean bFlag=true;

        if(ActivityOptions.IS_FOR_AMAZON)
            bFlag=true;
        else
            bFlag=!_inAppPurchases.handleActivityResult(requestCode, resultCode, data);

        if (bFlag) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case (ActivityOptions.FILTER_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        ReloadListView();
                    break;
                case (ActivityOptions.OPERATION_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        ReloadListView();
                    break;
                case (ActivityOptions.EXPORT_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        UncheckAll(); //Just UnselectAll
                    break;
                case (ActivityOptions.IMPORT_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        ReloadListView(); //New contact have been added
                    break;
                case (ActivityOptions.DELETE_ACTIVITY_REQUEST): //This will be triggered from Model Delete
                    _deleteCount--;
                    if(_deleteCount<=0)
                        ReloadListView();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
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

}
