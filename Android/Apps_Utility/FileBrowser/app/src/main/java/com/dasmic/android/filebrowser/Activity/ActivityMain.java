package com.dasmic.android.filebrowser.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dasmic.android.filebrowser.R;
import com.dasmic.android.lib.filebrowser.Data.ActivityStartupData;
import com.dasmic.android.lib.filebrowser.Enum.AppOptions;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.google.firebase.messaging.FirebaseMessaging;

//This ActivityMain mainly launched the ActivityMain inside lib.Browser
public class ActivityMain extends AppCompatActivity { //ActionBarActivity {
    protected String _demo_video_id;
    private final int MAIN_ACTIVITY=300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _demo_video_id ="xyJkdbt4Xo0";
        getActivityStartupData();
        launchFileBrowserActivity();
        HelpMedia.ShowDemoVideoDialog(this, _demo_video_id);

        //Subscribe to generic message to all apps
        FirebaseMessaging.getInstance().subscribeToTopic("Updates.General");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            trimCache();
            // Toast.makeText(this,"onDestroy " ,Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public   void trimCache() {
        try {
            FileOperations.DeleteFolder(FileOperations.getInternalCacheFolder(getActivity()));
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private Activity getActivity(){
        return this;
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

    //Launch the main activity
    private  void launchFileBrowserActivity(){
        ActivityStartupData asd = getActivityStartupData();
        Intent mIntent = new Intent(this, com.dasmic.android.lib.filebrowser.Activity.ActivityMain.class);
        //Pass Data
        mIntent.putExtra("asd", asd);
        this.startActivityForResult(mIntent, MAIN_ACTIVITY);
    }

    //ActivityData object is generated here
    private ActivityStartupData getActivityStartupData(){
        ActivityStartupData activityData = new ActivityStartupData();

        //Do this before calling Super
        activityData.Ad_interstitial_id=getString(R.string.ad_main_is_id);
        activityData.Demo_video_id=_demo_video_id ;
        activityData.HelpURL="http://www.coju.mobi/android/filebrowser/faq/index.html";
        activityData.Paid_version_sku_id="com.dasmic.android.filebrowser.paidversion";
        activityData.Base64EncodedPublicKey=this.getText(R.string.license_one).toString() +
                                                this.getText(R.string.license_two).toString() +
                                                this.getText(R.string.license_three).toString() +
                                                this.getText(R.string.license_four).toString();
        activityData.ForAmazon =0;
        activityData.TellAFriendText = getString(R.string.message_tellafriend);
        activityData.FileProviderAuthority = com.dasmic.android.filebrowser.Enum.AppOptions.FILE_PROVIDER_AUTHORITY;
        activityData.StartupFolderPath= FileOperations.getExternalStorageFolderString();//FileOperations.getInternalStorageFolderString(getApplicationContext())
        activityData.StartupFolderType=0;//External folder
        return activityData;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case (MAIN_ACTIVITY):
                    finish();
                    break;

                default:
                    break;
            }
    }
}
