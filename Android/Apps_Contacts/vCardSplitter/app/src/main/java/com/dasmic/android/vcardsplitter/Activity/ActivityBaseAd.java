package com.dasmic.android.vcardsplitter.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dasmic.android.lib.support.Ad.Interstilial;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.vcardsplitter.Enum.ActivityOptions;
import com.dasmic.android.vcardsplitter.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by Chaitanya Belwal on 12/6/2015.
 */
public class ActivityBaseAd extends AppCompatActivity {
    protected String _ad_interstitial_id;
    protected Interstilial _interstilial;
    protected InAppPurchases _inAppPurchases;
    protected final String paid_version_sku_id =
            "com.dasmic.android.vcardsplitter.paidversion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() { //In App billing cleanup
        super.onDestroy();
        if (_inAppPurchases != null) _inAppPurchases.dispose();
        _inAppPurchases = null;

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

    protected Context getContext(){
        return this;
    }

    protected void PurchasePaidVersion(){
        _inAppPurchases.PurchasePaidVersion(new
                                                    InAppPurchases.OnIAPPurchaseFinishedListener() {
                                                        public void onIAPPurchaseFinished(
                                                                IabResult result,
                                                                boolean isPaidVersion) {
                                                            ActivityOptions.isFreeVersion = !isPaidVersion;
                                                            // update UI accordingly
                                                            setLocalVersionBasedUI();
                                                        }
                                                    });
    }

    //InApp Purchases
    protected void setupForInAppPurchase() {
        try {
            String base64EncodedPublicKey =
                    getContext().getText(R.string.license_one).toString() +
                            getContext().getText(
                                    R.string.license_two).toString() +
                            getContext().getText(
                                    R.string.license_three).toString() +
                            getContext().getText(
                                    R.string.license_four).toString();
            _inAppPurchases = new InAppPurchases(this,
                    paid_version_sku_id,
                    base64EncodedPublicKey);

            _inAppPurchases.SetupInAppPurchase(
                    new InAppPurchases.OnIAPSetupFinishedListener() {
                        public void onIAPSetupFinished(
                                IabResult result,
                                boolean isPaidVersion) {
                            ActivityOptions.isFreeVersion = false;//!isPaidVersion;
                            // update UI accordingly
                            setLocalVersionBasedUI();
                        }
                    });
        }
        catch(Exception ex){
            ActivityOptions.isFreeVersion = true;
            // update UI accordingly
            setLocalVersionBasedUI();
        }
    }

    private void setLocalVersionBasedUI(){
        MenuItem item=null;
        Menu navMenu=null;
        NavigationView navView = (NavigationView)
                findViewById(R.id.nav_view);
        if (navView != null) {
            navMenu = navView.getMenu();
            if(navMenu != null)
                item = navMenu.findItem(
                        R.id.nav_view_upgrade_paid_version);
            setVersionBasedUI(item, true);
        }
    }

    protected void displayInterstitialAd(){
        //--------------------------------------- Ad
        if(_interstilial==null && !_ad_interstitial_id.trim().equals(""))
            _interstilial=new Interstilial(_ad_interstitial_id,this);

        if(_interstilial!=null)
            _interstilial.showAd(); //Show during load
        //------------------
    }

}
