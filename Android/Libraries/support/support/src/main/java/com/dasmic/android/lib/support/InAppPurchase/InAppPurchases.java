package com.dasmic.android.lib.support.InAppPurchase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.IAPUtil.Inventory;
import com.dasmic.android.lib.support.IAPUtil.Purchase;
import com.dasmic.android.lib.support.IAPUtil.IabHelper;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 1/14/2016.
 */
public class InAppPurchases {
    IabHelper mHelper;
    String paid_version_sku_id ="";
    boolean _isPaidVersion=false;
    String _base64EncodedPublicKey;
    Context _context;
    Activity _activity;

    public InAppPurchases(
            Activity activity,
            String sku_paid_version,
            String base64EncodedPublicKey){
        paid_version_sku_id=sku_paid_version;
        _base64EncodedPublicKey=base64EncodedPublicKey;
        _isPaidVersion=false;
        _activity=activity;
    }


    //Changed on 6/10 after getting new library from github
    public void finalize(){
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    public void dispose(){
        finalize();
    }

    public interface OnIAPSetupFinishedListener {
        void onIAPSetupFinished(IabResult result,
                                boolean isPaidVersion);
    }

    public interface OnIAPPurchaseFinishedListener {
        void onIAPPurchaseFinished(IabResult result,
                                boolean isPaidVersion);
    }


    public void SetupInAppPurchase(final OnIAPSetupFinishedListener listener){
        BindInAppPurchase(listener);
    }

    private void BindInAppPurchase(final OnIAPSetupFinishedListener listener){
        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(_activity, _base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    //Log.d(TAG, "Problem setting up In-app Billing: " + result);
                    SupportFunctions.DebugLog("InAppPurchases",
                            "Bind", result.getMessage());
                    if (listener != null) {
                        listener.onIAPSetupFinished(result,_isPaidVersion);
                    }
                }
                // Hooray, IAB is fully set up!
                SupportFunctions.DebugLog("InAppPurchases",
                        "Bind", "IABHelper is initialized");
                SetFreeOrPaidVersion(listener);
            }
        });

    }

    /*private void QueryPurchaseItems(){
        IabHelper.QueryInventoryFinishedListener
                mQueryFinishedListener = new
                IabHelper.QueryInventoryFinishedListener() {
                    public void onQueryInventoryFinished(IabResult result,
                                                         Inventory inventory)
                    {
                        if (result.isFailure()) {
                            // handle error
                            return;
                        }
                        String paid_version =
                                inventory.getSkuDetails(
                                        paid_version_sku_id).getPrice();
                        // update the UI
                    }
                };
        ArrayList additionalSkuList = new ArrayList();
        additionalSkuList.add(paid_version_sku_id);
        mHelper.queryInventoryAsync(true, additionalSkuList,
                mQueryFinishedListener);
    }*/

    private void SetFreeOrPaidVersion(final
                                      OnIAPSetupFinishedListener listener){
        IabHelper.QueryInventoryFinishedListener mGotInventoryListener
                = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result,
                                                 Inventory inventory) {
                if (result.isFailure()) {
                    SupportFunctions.DebugLog("InAppPurchases",
                            "FreeOrPaidVersion", result.getMessage());
                    if (listener != null) {
                        listener.onIAPSetupFinished(result,_isPaidVersion);
                    }
                }
                else {
                    // does the user have the premium upgrade?
                    _isPaidVersion =
                            inventory.hasPurchase(paid_version_sku_id);
                    SupportFunctions.DebugLog("InAppPurchases",
                            "FreeOrPaidVersion","FreeOrPaidVersion - Complete");
                    if (listener != null) {
                        listener.onIAPSetupFinished(result,_isPaidVersion);
                    }
                }
            }
        };
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        }
        catch (IabHelper.IabAsyncInProgressException e) {
            SupportFunctions.AsyncDisplayGenericDialog(_context, "Error launching purchase flow. Another async operation in progress.","");
        }
    }

    public void PurchasePaidVersion(final OnIAPPurchaseFinishedListener listener){
        IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
                = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result,
                                              Purchase purchase)
            {
                if (result.isFailure()) {
                    SupportFunctions.DebugLog("InAppPurchases",
                            "PurchasePaidVersion",
                            result.getMessage());
                    if (listener != null) {
                        listener.onIAPPurchaseFinished(result, false);
                    }
                    return;
                }
                else if (purchase.getSku().equals(paid_version_sku_id)) {
                    // consume the gas and update the UI
                    _isPaidVersion=true;
                    if (listener != null) {
                        listener.onIAPPurchaseFinished(result,_isPaidVersion);
                    }
                    SupportFunctions.DebugLog("InAppPurchases",
                            "PurchasePaidVersion",
                            "Purchase Complete");
                }
            }
        };

        try {
            mHelper.launchPurchaseFlow(_activity,
                    paid_version_sku_id,
                    10001,
                    mPurchaseFinishedListener);
        }
        catch (IabHelper.IabAsyncInProgressException e) {
            SupportFunctions.AsyncDisplayGenericDialog(_context, "Error launching purchase flow. Another async operation in progress.","");
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data){
        // Pass on the activity result to the helper for handling
        return mHelper.handleActivityResult(requestCode, resultCode, data);
    }
}
