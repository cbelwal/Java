package com.dasmic.android.audiograter.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
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
import android.widget.CheckBox;
import android.widget.SearchView;

import com.dasmic.android.audiograter.R;
import com.dasmic.android.lib.support.Feedback.Feedback;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Feedback.TellAFriend;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.audio.Activity.ActivityBaseMain;
import com.dasmic.android.lib.audio.Enum.AppOptions;
import com.dasmic.android.lib.audio.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.audio.Enum.SearchOptionsEnum;
import com.google.firebase.messaging.FirebaseMessaging;


/**
 * Created by Chaitanya Belwal on 10/14/2017.
 */
/**
 Activity is launched with root level Physical Path from a different activity
 Then the activity takes over. The close event of this activity should be handled
 by the calling activity so that it can figure out what to do
 */
public class ActivityMain extends ActivityBaseMain
        implements NavigationView.OnNavigationItemSelectedListener {

    final String helpURL =
            "http://www.coju.mobi/android/audiograter/faq/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppOptions.IS_FOR_AMAZON = true;

        //if(savedInstanceState != null)
        //    _updateDone = savedInstanceState.getBoolean(
        //            tagUpdateDone);
        //Set  values from parent Activity

        //Do this before calling Super
        setContentView(R.layout.ui_main_ag);

        _paid_version_sku_id =
                    "com.dasmic.android.audiograter.paidversion";
        _base64EncodedPublicKey =
                    getActivity().getText(R.string.license_one).toString() +
                            getActivity().getText(
                                    R.string.license_two).toString() +
                            getActivity().getText(
                                    R.string.license_three).toString() +
                            getActivity().getText(
                                    R.string.license_four).toString();

        _ad_interstitial_id=getString(R.string.ad_main_is_id); //For Interstitial Ads
        AppOptions.FILE_PROVIDER_AUTHORITY=
                "com.dasmic.audiograter.FileProvider";

        _demo_video_id = "H9HLSgV5rqE ";
        _helpURL=helpURL;

        super.onCreate(savedInstanceState); //CAUTION: Do not move this location
        setDrawerMenu();
        HelpMedia.ShowDemoVideoDialog(this,_demo_video_id);
        setLocalVariablesAndEventHandlers();
        ReloadListView();
        //Subscribe to generic message to all apps
        FirebaseMessaging.getInstance().subscribeToTopic("Updates.General");
    }


    @Override
    public void onDestroy() { //In App billing cleanup
        super.onDestroy();
        if (_inAppPurchases != null) _inAppPurchases.dispose();
        _inAppPurchases = null;
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

    //Display this message here as otherwise it leads to Screen Overlay error
    @Override
    protected void customContactsLoadOperation()
    {
        SupportFunctions.DisplayToastMessageLong(getActivity(),
                getString(R.string.startup_message));
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
    protected void setLocalVariablesAndEventHandlers() {
        super.setLocalVariablesAndEventHandlers();
    }

    //TODO
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        boolean refresh=false;
        if(id == R.id.nav_view_default)
        {
            refresh=true;
            _vmVideoBrowser.setCurrentDisplayOption(
                    DisplayOptionsEnum.DefaultView);
        }
        else if(id == R.id.nav_view_size)
        {
            refresh=true;
            _vmVideoBrowser.setCurrentDisplayOption(
                    DisplayOptionsEnum.SortedBySize);
        }
        else if(id == R.id.nav_view_last_modified)
        {
            refresh=true;
            _vmVideoBrowser.setCurrentDisplayOption(
                    DisplayOptionsEnum.SortedByModifiedDate);
        }
        else if(id == R.id.nav_view_search_entire_phone)
        {
            _vmVideoBrowser.setCurrentSearchOption(SearchOptionsEnum.EntirePhone);
            onSearchEntirePhone();
        }
        else if(id == R.id.nav_view_search_media_store_default)
        {
            _vmVideoBrowser.setCurrentSearchOption(SearchOptionsEnum.MediaStore);
            ReloadListView();
        }
        else if(id == R.id.nav_view_search_external_folders)
        {
            _vmVideoBrowser.setCurrentSearchOption(SearchOptionsEnum.ExternalFolder);
            ReloadListView();
        }
        else if(id == R.id.nav_view_search_media_folders)
        {
            _vmVideoBrowser.setCurrentSearchOption(SearchOptionsEnum.MediaFolder);
            ReloadListView();
        }
        else if(id == R.id.nav_view_action_refresh)
        {
            ReloadListView();
        }
        else if(id == R.id.nav_view_demo_video)
        {
            HelpMedia.ShowDemoVideo(this,
                    _demo_video_id);
        }
        else if(id == R.id.nav_view_help_documentation)
        {
            HelpMedia.ShowWebURL(this, _helpURL);
        }
        else if(id == R.id.nav_view_upgrade_paid_version)
        {
            purchasePaidVersion();
        }
        else if(id == R.id.nav_view_rate_this_app)
        {
            ShowRateThisApp();
        }
        else if(id == R.id.nav_view_feedback)
        {
            Feedback.SendFeedbackByEmail(this);
        }
        else if(id == R.id.nav_view_tell_a_friend)
        {
            TellAFriend.TellAFriend(this,
                    _tellAFriend );
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
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Use if-then-else since switch statements cant be used in resource IDs
        if(id ==R.id.action_delete)
            onButtonDelete();
        else if(id== R.id.action_copy){
            onFileCopy();
        }
        else if(id== R.id.action_extract_pictures){
            onExtractAudio();
        }
        else if(id== R.id.action_rename){
            onRename();
        }
        else if(id== R.id.action_create_folder){
            onCreateFolder();
        }
        else if(id== R.id.action_share){
            onFilesShare();
        }
        else if(id== R.id.action_share_zip){
            onFilesShareZip();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case AppOptions.PERMISSION_STORAGE_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    //Very important to set startUpFolderPath as it cannot be assigned if
                    //permissions are not there
                    setCurrentBrowseFolder(_startupFolderPath);
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

    @Override
    protected boolean checkPermissionsForAndroid6Plus(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    AppOptions.PERMISSION_STORAGE_REQUEST);
            return false;
        }
        else
            return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_fb, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) (menuItem.getActionView()).findViewById(R.id.textSearch);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search view is expanded
                ((SearchView) v).setMaxWidth(getSearchBoxWidth());

                Log.i("CKIT", "ActivityMain::SearchExpanded");
                _isSearching=true;
                HideCheckAll();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //Search View is collapsed
                Log.i("CKIT", "ActivityMain::SearchCollapsed");
                _isSearching=false;//Is used to display count
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
                                                  if(_listAdapter != null)
                                                      _listAdapter.getFilter().filter(newText);
                                                  return false;
                                              }
                                          }

        );

        return true;
    }

    private void ShowCheckAll(){
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setVisibility(CheckBox.VISIBLE);
    }

    private void HideCheckAll(){
        CheckBox checkAll = (CheckBox) findViewById(R.id.checkSelectAll);
        checkAll.setVisibility(CheckBox.INVISIBLE);
    }

}
