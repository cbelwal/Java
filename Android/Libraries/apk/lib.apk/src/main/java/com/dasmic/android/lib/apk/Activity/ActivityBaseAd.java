package com.dasmic.android.lib.apk.Activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

//import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.dasmic.android.lib.apk.Enum.ActivityOptions;
import com.dasmic.android.lib.apk.R;
//import com.google.android.gms.ads.AdView;

/**
 * Created by Chaitanya Belwal on 12/6/2015.
 */
public class ActivityBaseAd extends AppCompatActivity {
    protected boolean _isMalwareProgram;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
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
            if (ActivityOptions.isFreeVersion) {
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
}
