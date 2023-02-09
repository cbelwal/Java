package com.dasmic.android.lib.support.Ad;

import android.app.Activity;

import com.dasmic.android.lib.support.Static.AppSettings;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by Chaitanya Belwal on 4/9/2017.
 */

public class Interstilial {
    private String _id; //Id of Add
    private InterstitialAd mInterstitialAd;
    private Activity _activity;
    private int _showOnceEveryCount=12;
    private static final String _ad_is_count="ad_is_count";

    public Interstilial(String adId, Activity activity){
        _id=adId;
        _activity = activity;
        loadAd();
    }

    public void SetShowOnceEveryCount(int newValue){
        _showOnceEveryCount = newValue;
    }

    private void loadAd(){
        if(_activity == null) return;
        mInterstitialAd = new InterstitialAd(_activity);
        mInterstitialAd.setAdUnitId(_id);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial(); //Request next one
            }

            @Override
            public void onAdLoaded(){

            }
        });
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        if(mInterstitialAd==null) return;

        AdRequest adRequest = new AdRequest.Builder()
        //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .build();

        mInterstitialAd.loadAd(adRequest);
    }

    public void showAd(){
        int currentCount= AppSettings.getPreferencesInt(_activity,_ad_is_count);

        if(++currentCount%_showOnceEveryCount==0) {
            if (mInterstitialAd == null) return;
            //Add logic to display only during 12 time
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                //Save setting only when ad shown
                AppSettings.setPreferencesInt(_activity, _ad_is_count,currentCount);
            }
        }
        else
            AppSettings.setPreferencesInt(_activity, _ad_is_count,currentCount);
    }


}
