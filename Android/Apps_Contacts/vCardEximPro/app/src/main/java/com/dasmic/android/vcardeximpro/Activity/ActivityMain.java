package com.dasmic.android.vcardeximpro.Activity;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.dasmic.android.lib.contacts.Activity.ActivityBackup;
import com.dasmic.android.lib.contacts.Activity.ActivityBaseMain;
import com.dasmic.android.lib.contacts.Activity.ActivityBluetoothExim;
import com.dasmic.android.lib.contacts.Activity.ActivityDuplicates;
import com.dasmic.android.lib.contacts.Activity.ActivityExport;
import com.dasmic.android.lib.contacts.Activity.ActivityFilter;
import com.dasmic.android.lib.contacts.Activity.ActivityImport;
import com.dasmic.android.lib.contacts.Activity.ActivityOperations;
import com.dasmic.android.lib.contacts.Activity.ActivityRestore;
import com.dasmic.android.lib.contacts.Activity.ActivityShare;
import com.dasmic.android.lib.contacts.Data.DataContactDisplay;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.Enum.DisplayOptionsEnum;
import com.dasmic.android.vcardeximpro.R;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Feedback.Feedback;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.Feedback.TellAFriend;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelContactsDisplay;

public class ActivityMain extends ActivityBaseMain
        implements NavigationView.OnNavigationItemSelectedListener{ //ActionBarActivity {


    final String helpURL =
            "http://www.coju.mobi/android/vcardexim/faq/index.html";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CKIT", "ActivityMain is created");
        setContentView(R.layout.ui_main);


        ActivityOptions.IS_FOR_AMAZON=true; //Full Pro version
        if(ActivityOptions.IS_FOR_AMAZON) {
            ActivityOptions.isFreeVersion = false;
            setLocalVersionBasedUI();
        }
        else {
            _paid_version_sku_id =
                    "com.dasmic.android.vcardexim.paidversion";
            _base64EncodedPublicKey =
                    getContext().getText(R.string.license_one).toString() +
                            getContext().getText(
                                    R.string.license_two).toString() +
                            getContext().getText(
                                    R.string.license_three).toString() +
                            getContext().getText(
                                    R.string.license_four).toString();
            setupForInAppPurchase();
        }

        setLocalVariablesAndEventHandlers();
        setDrawerMenu();
        setActionBarTitle();
        ReloadListView();
        ActivityOptions.FILE_PROVIDER_AUTHORITY=
                "com.dasmic.vcardeximpro.FileProvider";

        demo_video_id = "Yttrh6WGloE";
        HelpMedia.ShowDemoVideoDialog(this,demo_video_id);
    }

    //Display this message here as otherwise it leads to Screen Overlay error
    @Override
    protected void customContactsLoadOperation()
    {
        //SupportFunctions.DisplayToastMessageLong(getActivity(),
        //        getString(R.string.startup_message));
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


    private void setDrawerMenu(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        _drawerToggle = new ActionBarDrawerToggle(
                this,drawer ,R.string.action_Import,R.string.action_Import);
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
           SupportFunctions.DebugLog("ActivityMain",
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
        _vmContacts = ViewModelContactsDisplay.getInstance(this);
        _vmContacts.setCurrentDisplayOption(DisplayOptionsEnum.AllInformation);
        _pdSpin = new ProgressDialogSpinner(this,
                this.getResources().getText(R.string.progressbar_load_data).toString());

        setButtonEventHandlers();
    }


    //Add Items to the action bar here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        mDeleteMenuItem =menu.findItem(R.id.action_delete);

        final SearchView searchView = (SearchView) (menuItem.getActionView()).findViewById(R.id.textSearch);

        Log.d("CKIT", "ActivityMain::ExpandingSearchBox");
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
        boolean reload=false;
        boolean refresh=false;
        Intent myIntent;
        switch(id){
            case R.id.nav_view_timescontacted:
                _vmContacts.setCurrentDisplayOption(DisplayOptionsEnum.AllWithContactCount);
                reload=true;
                break;
            case R.id.nav_view_all:
                _vmContacts.setCurrentDisplayOption(DisplayOptionsEnum.AllInformation);
                reload=true;
                break;
            case R.id.nav_view_lastcontacttime:
                _vmContacts.setCurrentDisplayOption(DisplayOptionsEnum.AllWithLastContact);
                reload=true;
                break;
            case R.id.nav_view_email:
                _vmContacts.setCurrentDisplayOption(DisplayOptionsEnum.Email);
                reload=true;
                break;
            case R.id.nav_view_phonenumber:
                _vmContacts.setCurrentDisplayOption(
                        DisplayOptionsEnum.PhoneNumber);
                reload=true;
                break;
            case R.id.nav_view_deleted:
                _vmContacts.setCurrentDisplayOption(
                        DisplayOptionsEnum.Deleted);
                reload=true;
                break;

            case R.id.nav_view_rate_this_app:
                ShowRateThisApp();
                break;
            case R.id.nav_view_backup_contacts:
                myIntent = new Intent(this, ActivityBackup.class);
                this.startActivityForResult(myIntent,
                        ActivityOptions.BACKUP_ACTIVITY_REQUEST);
                break;
            case R.id.nav_view_restore_contacts:
                myIntent = new Intent(this, ActivityRestore.class);
                myIntent.putExtra(ActivityOptions.APP_NAME,
                        getResources().getString(R.string.app_name_small));
                this.startActivityForResult(myIntent,
                        ActivityOptions.IMPORT_ACTIVITY_REQUEST);
                break;
            case R.id.nav_view_upgrade_paid_version:
                purchasePaidVersion();
                break;
            case R.id.nav_view_filter:
                myIntent = new Intent(this, ActivityFilter.class);
                this.startActivityForResult(myIntent, ActivityOptions.FILTER_ACTIVITY_REQUEST);
                break;
            case R.id.nav_view_feedback:
                Feedback.SendFeedbackByEmail(this);
                break;
            case R.id.nav_view_demo_video:
                HelpMedia.ShowDemoVideo(this,
                        demo_video_id);
                break;
            case R.id.nav_view_coju_outlook:
                showCojuForMicrosoftOutlook();
                break;
            case R.id.nav_view_tell_a_friend:
                TellAFriend.TellAFriend(this,
                        getString(R.string.message_tellafriend));
                break;
            case R.id.nav_view_duplicates:
                myIntent = new Intent(this, ActivityDuplicates.class);
                this.startActivityForResult(myIntent,
                        ActivityOptions.DUPLICATES_ACTIVITY_REQUEST);
                break;
            case R.id.nav_view_help_documentation:
                HelpMedia.ShowWebURL(this,helpURL);
                break;
            case R.id.nav_view_refresh:
                refresh=true;
                break;
            case R.id.nav_view_coju:
                openCojuLink();
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout)
                findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(reload) {
           ReloadListView();
        }
        else if(refresh){
            RefreshListView();
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
        ArrayList<DataContactDisplay> checkedItems= null;
        //noinspection SimplifiableIfStatement
        switch(id)
        {
            case R.id.action_delete:
                onButtonDelete();
                break;
            case R.id.action_operations:
                if(!checkSelection() || checkIsUndeleted()) break;

                myIntent = new Intent(this, ActivityOperations.class);
                this.startActivityForResult(myIntent, ActivityOptions.OPERATION_ACTIVITY_REQUEST);
                break;
            case R.id.action_share:
                if(!checkSelection() || checkIsUndeleted()) break;
                myIntent = new Intent(this, ActivityShare.class);
                myIntent.putExtra(ActivityOptions.APP_NAME,
                        getResources().getString(R.string.app_name_small));
                this.startActivityForResult(myIntent, ActivityOptions.EXPORT_ACTIVITY_REQUEST);
                break;
            case R.id.action_bluetooth:
                if(!checkSelection(false) || checkIsUndeleted()) break;
                myIntent = new Intent(this, ActivityBluetoothExim.class);
                myIntent.putExtra(ActivityOptions.APP_NAME,
                        getResources().getString(R.string.app_name_small));
                this.startActivityForResult(myIntent,
                        ActivityOptions.BLUETOOTH_ACTIVITY_REQUEST);
                break;
            case R.id.action_export:
                if(!checkSelection() || checkIsUndeleted()) break;
                myIntent = new Intent(this, ActivityExport.class);
                myIntent.putExtra(ActivityOptions.APP_NAME,
                        getResources().getString(R.string.app_name_small));
                this.startActivityForResult(myIntent, ActivityOptions.EXPORT_ACTIVITY_REQUEST);
                break;
            case R.id.action_edit:
                if(!checkSelection()) break;
                if(_vmContacts.getCheckedItems().size() > 1){
                    SupportFunctions.DisplayToastMessageLong(
                            this, (String) this.getResources().getText(R.string.message_edit));
                    break;
                }
                //Launch Activity
                myIntent = new Intent(Intent.ACTION_EDIT);
                myIntent.setDataAndType(_vmContacts.getCheckedItems().get(0).getContactUri(),
                        ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                SupportFunctions.StartActivityForResult(
                        this,
                        myIntent,
                        ActivityOptions.EDIT_ACTIVITY_REQUEST);
                //startActivityForResult(myIntent, ActivityOptions.EDIT_ACTIVITY_REQUEST);
                break;
            case R.id.action_add:
                //Launch Activity
                myIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
                // Sets the MIME type to match the Contacts Provider
                myIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                SupportFunctions.StartActivityForResult(
                                this,
                                myIntent,
                                ActivityOptions.EDIT_ACTIVITY_REQUEST);
                //startActivityForResult(myIntent,
                //        ActivityOptions.EDIT_ACTIVITY_REQUEST);
                break;
            case R.id.action_import:
                myIntent = new Intent(this, ActivityImport.class);
                myIntent.putExtra(ActivityOptions.APP_NAME,
                        getResources().getString(R.string.app_name_small));
                this.startActivityForResult(myIntent,
                        ActivityOptions.IMPORT_ACTIVITY_REQUEST);
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
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
                        RefreshListView();
                    break;
                case (ActivityOptions.DUPLICATES_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        RefreshListView(true);
                    break;
                case (ActivityOptions.OPERATION_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        RefreshListView();
                    break;
                case (ActivityOptions.EXPORT_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        UncheckAll(); //Just UnselectAll
                    break;
                case (ActivityOptions.IMPORT_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        RefreshListView(); //New contact have been added
                    break;
                case (ActivityOptions.EDIT_ACTIVITY_REQUEST):
                    RefreshListView();
                    break;
                case (ActivityOptions.BLUETOOTH_ACTIVITY_REQUEST):
                    if (resultCode == Activity.RESULT_OK)
                        RefreshListView(); //New contact have been added
                    break;
                case (ActivityOptions.CONTACTS_RESTORE_ACTIVITY_REQUEST):
                    SupportFunctions.AsyncDisplayGenericDialog(this,
                            getResources().getString(R.string.message_restore_refresh),
                            getResources().getString(R.string.app_name_small));
            }
        }
    }


    //Construct color based on data
    @Override
    protected int getColor(DataContactDisplay contact){

        if(contact.colorDuplicates<0) {

            int red = Color.red(ContextCompat.getColor(this, R.color.ContactListRed));
            int green = Color.green(ContextCompat.getColor(this, R.color.ContactListGreen));
            int blue = Color.blue(ContextCompat.getColor(this, R.color.ContactListBlue));
            int alpha = Color.alpha(ContextCompat.getColor(this, R.color.ContactListAlpha));//Do not change this as in Android 5.0 this can lead to

            //problems
            if (contact.getHasPhoneNumbers())
                blue = blue + 30;
            if (contact.getInFavorites())
                green = green + 30;
            if (contact.getLastContactTime() != 0)
                red = red + 30;


            int color = Color.argb(alpha, red, green, blue);
            return color;
        }
        else
        {
            return contact.colorDuplicates;
        }
    }


    private void ShowRateThisApp(){
        RateThisApp rtp=new RateThisApp();
        rtp.ShowRateThisApp(this);

    }

}
