package com.dasmic.android.backupcontacts.Activity;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
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
import com.dasmic.android.lib.contacts.Activity.ActivityRestore;
import com.dasmic.android.lib.contacts.Data.DataContactDisplay;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.Enum.DisplayOptionsEnum;
import com.dasmic.android.backupcontacts.R;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Feedback.Feedback;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.Feedback.TellAFriend;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.AppSettings;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelContactsDisplay;
import com.google.firebase.messaging.FirebaseMessaging;

public class ActivityMain extends ActivityBaseMain
        implements NavigationView.OnNavigationItemSelectedListener{ //ActionBarActivity {


    final String helpURL =
            "http://www.coju.mobi/android/backupcontacts/faq/index.html";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CKIT", "ActivityMain is created");
        setContentView(R.layout.ui_main);
        //---------------- In app purchase
        _base64EncodedPublicKey =
                getContext().getText(R.string.license_one).toString() +
                        getContext().getText(
                                R.string.license_two).toString() +
                        getContext().getText(
                                R.string.license_three).toString() +
                        getContext().getText(
                                R.string.license_four).toString();
        _paid_version_sku_id =
                "com.dasmic.android.backupcontacts.paidversion";
        setupForInAppPurchase();
        //---------------------------------
        _ad_interstitial_id=getString(R.string.ad_main_is_id); //For Interstitial Ads
        setLocalVariablesAndEventHandlers();
        setDrawerMenu();
        setActionBarTitle();
        ReloadListView();
        ActivityOptions.FILE_PROVIDER_AUTHORITY=
                "com.dasmic.backupcontacts.FileProvider";
        ActivityOptions.FREE_VERSION_CONTACT_LIMIT=90;
        demo_video_id = "cmo6ORYBFUY";
        HelpMedia.ShowDemoVideoDialog(this,demo_video_id);

        //Subscribe to generic message to all apps
        FirebaseMessaging.getInstance().subscribeToTopic("Updates.General");
        ShowBackupDialog();
    }

    //Asks if the user wants to backup contacts
    //If yes, then select all and show up backup screen
    private void ShowBackupDialog() {
        Activity activity = this;
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
        dlgAlert.setTitle(activity.getString(R.string.app_name));
        dlgAlert.setMessage(activity.getString(R.string.message_backup_contacts));
        dlgAlert.setCancelable(false);

        dlgAlert.setPositiveButton(activity.getString(com.dasmic.android.lib.support.R.string.button_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CheckBox checkAll = (CheckBox) findViewById(com.dasmic.android.lib.contacts.R.id.checkSelectAll);
                        checkAll.setChecked(true);
                        onButtonCheckUncheck();
                        showBackupOptions();
                        dialog.dismiss();
                    }
                });

        dlgAlert.setNegativeButton(
                activity.getResources().getText(com.dasmic.android.lib.support.R.string.button_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = dlgAlert.create();
        alert.show();
        //Setting to only show once


    }

    //Display this message here as otherwise it leads to Screen Overlay error
    @Override
    protected void customContactsLoadOperation()
    {
        SupportFunctions.DisplayToastMessageLong(this,
                getString(R.string.message_help_startup));
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
        //_displayOptionsSpinner = (Spinner) findViewById(R.id.spinnerViewData);
        _vmContacts = ViewModelContactsDisplay.getInstance(this);
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
            case R.id.nav_view_all:
                _vmContacts.setCurrentDisplayOption(DisplayOptionsEnum.AllInformation);
                reload=true;
                break;
            case R.id.nav_view_lastcontacttime:
                _vmContacts.setCurrentDisplayOption(DisplayOptionsEnum.AllWithLastContact);
                reload=true;
                break;
            case R.id.nav_view_action_edit:
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
                SupportFunctions.StartActivityForResult(this,
                        myIntent,
                        ActivityOptions.EDIT_ACTIVITY_REQUEST);
                break;
            case R.id.nav_view_action_add:
                //Launch Activity
                myIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
                // Sets the MIME type to match the Contacts Provider
                myIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                SupportFunctions.StartActivityForResult(this,
                        myIntent,
                        ActivityOptions.EDIT_ACTIVITY_REQUEST);
                break;
            case R.id.nav_view_rate_this_app:
                ShowRateThisApp();
                break;
            case R.id.nav_view_backup_contacts:
                showBackupOptions();
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
            case R.id.nav_view_coju:
                openCojuLink();
                break;
            case R.id.nav_view_feedback:
                Feedback.SendFeedbackByEmail(this);
                break;
            case R.id.nav_view_demo_video:
                HelpMedia.ShowDemoVideo(this,
                        demo_video_id);
                break;
            case R.id.nav_view_tell_a_friend:
                TellAFriend.TellAFriend(this,
                        getString(R.string.message_tellafriend));
                break;
            case R.id.nav_view_help_documentation:
                HelpMedia.ShowWebURL(this,helpURL);
                break;
            case R.id.nav_view_refresh:
                refresh=true;
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

    private void showBackupOptions(){
        Intent myIntent = new Intent(this, ActivityBackup.class);
        this.startActivityForResult(myIntent,
                ActivityOptions.BACKUP_ACTIVITY_REQUEST);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {

        if (!_inAppPurchases.handleActivityResult(requestCode, resultCode, data)) {
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
            int red = Color.red(getResources().getColor(com.dasmic.android.lib.contacts.R.color.ContactListRed));
            int green = Color.green(getResources().getColor(com.dasmic.android.lib.contacts.R.color.ContactListGreen));
            int blue = Color.blue(getResources().getColor(com.dasmic.android.lib.contacts.R.color.ContactListBlue));
            int alpha = Color.alpha(getResources().getColor(com.dasmic.android.lib.contacts.R.color.ContactListAlpha)); //Do not change this as in Adrnoid 5.0 this can lead to
            //problems

            if (contact.getHasPhoneNumbers())
                red = red + 40;
            if (contact.getInFavorites())
                green = green + 30;
            if (contact.getLastContactTime() != 0)
                blue = blue + 20;

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
