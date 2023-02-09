package com.dasmic.android.kiduninstall.Activity;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;


import com.dasmic.android.lib.apk.Activity.ActivityBaseMain;
import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Enum.ActivityOptions;
import com.dasmic.android.lib.apk.Enum.DisplayOptionsEnum;
import com.dasmic.android.kiduninstall.R;
import com.dasmic.android.lib.apk.ViewModel.ViewModelLinksReport;
import com.dasmic.android.lib.support.Feedback.Feedback;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Feedback.TellAFriend;
import com.dasmic.android.lib.support.Static.AppSettings;
import com.dasmic.android.lib.support.Static.DateOperations;


public class ActivityMain extends ActivityBaseMain
        implements NavigationView.OnNavigationItemSelectedListener { //ActionBarActivity {
    protected String demo_video_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CKIT", "ActivityMain is created");
        setContentView(R.layout.ui_main);
        //---------------------------------------
        _paid_version_sku_id =
                "com.dasmic.android.kiduninstall.paidversion"; //Dont change location
        helpURL =
                "http://www.coju.mobi/android/kidouninstall/faq/index.html";
        demo_video_id = "8uKfNzDwANU";
        _base64EncodedPublicKey =
                getContext().getText(R.string.license_one).toString() +
                        getContext().getText(
                                R.string.license_two).toString() +
                        getContext().getText(
                                R.string.license_three).toString() +
                        getContext().getText(
                                R.string.license_four).toString();

        _ad_interstitial_id=getString(R.string.ad_main_is_id); //For Interstitial Ads
        setupForInAppPurchase();
        //----------------------------
        setLocalVariablesAndEventHandlers();
        setDrawerMenu();
        ReloadListView();

        HelpMedia.ShowDemoVideoDialog(this, demo_video_id);
    }

    @Override
    protected void setLocalVariablesAndEventHandlers() {
        super.setLocalVariablesAndEventHandlers();
        _vmApps.setCurrentDisplayOption(
                DisplayOptionsEnum.InstalledOnDate);
    }

    //Executes after listbox is displayed
    @Override
    protected void additionalPostDisplayFunction()
    {
        autoSelectNewInstalls();
    }

    //Auto select new installs if they are beyond a certain date
    private void autoSelectNewInstalls(){
        final String prefId="TIME_LAST_RUN";
        //Store current date
        long lastTimeChecked=AppSettings.getPreferencesLong(this, prefId);
        AppSettings.setPreferencesLong(this, prefId, DateOperations.getCurrentDate());
        if(lastTimeChecked <=0) return; //Dont do anything
        //Else check the date
        if(_listAdapter == null) return;

        //lastTimeChecked=1;
        for(int ii=0;ii<_listAdapter.getCount();ii++){
            if(_listAdapter.getItem(ii).getInstalledOnDateLong()
                    > lastTimeChecked)
            setCheckValue(_listAdapter.getItem(ii),true);
        }
        setSelectionCountText();
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
        drawer.setDrawerListener(_drawerToggle);
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
            case R.id.action_delete:
                onButtonDelete();
                break;
            case R.id.action_share_link:
                if(!checkSelection()) break;
                vml=new ViewModelLinksReport(this,false);
                vml.SendPackageLinks(_vmApps.getCheckedItems());
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        // This is very important
        if (!_inAppPurchases.handleActivityResult(requestCode, resultCode, data)) {

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
}
