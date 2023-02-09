package com.dasmic.android.lib.contacts.Activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by Chaitanya Belwal on 12/6/2015.
 */
public class ActivityBaseAd extends ActivityBaseContact {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    protected void setVersionBasedUI(MenuItem item,
                                     boolean showAds) {
        AdView mAdView = (AdView) findViewById(R.id.adView);

            if (ActivityOptions.isFreeVersion) {
                //Ads
                if (mAdView != null && showAds) {
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
