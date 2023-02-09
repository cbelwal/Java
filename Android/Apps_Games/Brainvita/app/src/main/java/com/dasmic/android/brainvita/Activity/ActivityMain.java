package com.dasmic.android.brainvita.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dasmic.android.brainvita.Data.HighScores;
import com.dasmic.android.brainvita.Enum.AppOptions;
import com.dasmic.android.brainvita.Enum.GameOptions;
import com.dasmic.android.brainvita.R;
import com.dasmic.android.brainvita.ViewModel.VMMain;
import com.dasmic.android.lib.support.Feedback.HelpMedia;
import com.dasmic.android.lib.support.Feedback.RateThisApp;
import com.dasmic.android.lib.support.Feedback.TellAFriend;
import com.dasmic.android.lib.support.IAPUtil.IabResult;
import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ActivityMain extends AppCompatActivity {
    VMMain _vmMain;
    private InAppPurchases _inAppPurchases;
    protected String _ad_interstitial_id;
    private final String demo_video_id = "d3bE9KlU0ak";
    final String paid_version_sku_id =
            "com.dasmic.android.brainvita.paid";
    final String helpURL =
            "http://www.coju.mobi/android/brainvita/faq/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main);

        if(AppOptions.IS_FOR_AMAZON) {
            AppOptions.isFreeVersion = false;
            setLocalVersionBasedUI();
        }
        else {
            SetupForInAppPurchase();
        }

        _ad_interstitial_id=getString(R.string.ad_main_is_id);
        setLocalVariablesAndEventHandlers();
        HelpMedia.ShowDemoVideoDialog(this, demo_video_id);
        autoRateThisApp();
    }


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

    private void autoRateThisApp(){
        if(!AppOptions.IS_FOR_AMAZON)
            RateThisApp.ShowRateAppDialog(this); //To show Rate this app
    }

    private Activity getContext() {
        return this;
    }

    private void onButtonForward(){
        _vmMain.goForward();
    }

    private void onButtonBack(){
        _vmMain.goBack();
    }

    private void onButtonOptions(){
        Intent myIntent;
        myIntent = new Intent(this, ActivityOptions.class);
        _vmMain.chronoPause();
        this.startActivityForResult(myIntent,
                AppOptions.OPTIONS_ACTIVITY_REQUEST);
    }

    private void onButtonSolver(){
        if(_vmMain.getIsCustomBoardSetup()) {
            setCustomBoard();
        }
        else {
            confirmSolverMessage();
        }
    }

     private void confirmSolverMessage(){
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        //dlgAlert.setTitle(this.getResources().getText(R.string.title_confirm));
        dlgAlert.setMessage(
                String.valueOf(this.getResources().getText(
                        R.string.message_solver_start_confirm)));
        dlgAlert.setCancelable(false);

        dlgAlert.setPositiveButton(this.getResources().getText(
                R.string.button_start), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                _vmMain.startSolver();
                dialog.dismiss();
            }
        });

        dlgAlert.setNegativeButton(
                this.getResources().getText(R.string.button_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = dlgAlert.create();
        alert.show();
    }

    private final VMMain.OnSolverComplete
            mSolverComplete = new
            VMMain.OnSolverComplete() {
                public void onSolverComplete() {
                    //Allow phone to sleep
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            };

    private final VMMain.OnBoardStale
            mBoardStale = new
            VMMain.OnBoardStale(){
                public void onBoardStale(int count,
                                         long time, boolean recordHighScore) {

                    SupportFunctions.DisplayToastMessageLong(getContext(),
                            getString(R.string.message_game_over));
                    if(recordHighScore){
                        HighScores hs=new HighScores(getContext());
                        if(hs.addScore(count, time)  ) { //Score added to high score
                            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(getContext());
                            dlgAlert.setMessage(getString(R.string.message_high_score));
                            dlgAlert.setTitle(getString(R.string.app_name));
                            dlgAlert.setCancelable(true);
                            dlgAlert.setPositiveButton(getString(R.string.button_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        newGameConfirm();
                                        //dismiss the dialog
                                    }
                                });
                            dlgAlert.create().show();
                            }
                        else {//High score not recorded
                            newGameConfirm();
                        }
                }
                else { //No High Score required
                    newGameConfirm();
                }
            }
    };

    private void newGameConfirm(){
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(
                String.valueOf(this.getResources().getText(
                        R.string.message_game_end_new_game)));
        dlgAlert.setCancelable(false);

        dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                _vmMain.newGame();
                autoRateThisApp();
                dialog.dismiss();
            }
        });

        dlgAlert.setNegativeButton(
                this.getResources().getText(R.string.button_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = dlgAlert.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


    private void setLocalVariablesAndEventHandlers() {
       Chronometer chrono = (Chronometer) findViewById(
               R.id.chronoMeter);
        LinearLayout boardLayout = (LinearLayout) findViewById(
                R.id.layoutBoard);

        if(_vmMain==null)
            _vmMain=new VMMain(this,boardLayout,chrono,_ad_interstitial_id,
                            mBoardStale,mSolverComplete);

        ImageView imgBack = (ImageView) findViewById(
                R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonBack();
            }
        });

        ImageView imgForward = (ImageView) findViewById(
                R.id.imgForward);
        imgForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonForward();
            }
        });

        ImageView imgOptions = (ImageView) findViewById(
                R.id.imgOptions);
        imgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonOptions();
            }
        });

        ImageView imgSolver = (ImageView) findViewById(
                R.id.imgSolver);
        imgSolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonSolver();
            }
        });

    }



    private void SetupForInAppPurchase() {
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
                        AppOptions.isFreeVersion = !isPaidVersion;
                        // update UI accordingly
                        setLocalVersionBasedUI();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {

        boolean bFlag=true;

        if(AppOptions.IS_FOR_AMAZON)
            bFlag=true;
        else
            bFlag=!_inAppPurchases.handleActivityResult(requestCode, resultCode, data);

        if (bFlag) {

            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case (AppOptions.OPTIONS_ACTIVITY_REQUEST):
                    int selectedIdx = GameOptions.None.ordinal();
                    if (data != null)
                        selectedIdx = data.getIntExtra(AppOptions.SELECTED_OPTION_IDENTIFIER,
                                GameOptions.None.ordinal());
                    workOnSelectedOption(selectedIdx);
                    break;
                case (AppOptions.HIGHSCORE_ACTIVITY_REQUEST):
                    _vmMain.chronoResume();
                    break;
            }
        }
    }


    //All
    private void workOnSelectedOption(int ordinal){

        GameOptions go=GameOptions.values()[ordinal];
        switch(go){
            case NewGame:
                _vmMain.newGame();
                break;
            case SwitchBoardType:
                _vmMain.switchBoardStyle();
                break;
            case SetCustomBoard:
                setCustomBoard();
                break;
            case HighScores:
                ShowHighScores();
                break;
            case RateThisApp:
                ShowRateThisApp();
                break;
            case Help:
                HelpMedia.ShowWebURL(this, helpURL);
                break;
            case DemoVideo:
                HelpMedia.ShowDemoVideo(this,
                        demo_video_id);
                break;
            case TellAFriend:
                TellAFriend.TellAFriend(this,
                        getString(R.string.message_tellafriend));
                break;
            case PurchasePaidVersion:
                PurchasePaidVersion();
                break;
            default:
                    //Do nothing
        }
        _vmMain.chronoResume();
    }

    private void ShowHighScores() {
        Intent myIntent;
        myIntent = new Intent(this, ActivityHighScores.class);
        _vmMain.chronoPause();
        this.startActivityForResult(myIntent,
                AppOptions.OPTIONS_ACTIVITY_REQUEST);
    }

    private void ShowRateThisApp() {
        RateThisApp rtp = new RateThisApp();
        rtp.ShowRateThisApp(this);
    }

    private void setCustomBoard() {
        if (!_vmMain.getIsCustomBoardSetup()) {
            SupportFunctions.DisplayToastMessageLong(this,
                    this.getString(R.string.message_custom_board));

            ImageView btnSolver = (ImageView) findViewById(
                    R.id.imgSolver);
            btnSolver.setImageResource(R.drawable.lock_256);
            _vmMain.setIsCustomBoard(true);
        }
        else{ //Change button{
            SupportFunctions.DisplayToastMessageLong(this,
                    this.getString(R.string.message_custom_board_done));
            ImageView btnSolver = (ImageView) findViewById(
                    R.id.imgSolver);
            btnSolver.setImageResource(R.drawable.ok_256);
            _vmMain.setIsCustomBoard(false);
            }
    }

    private void setLocalVersionBasedUI() {
        AdView mAdView = (AdView) findViewById(R.id.adView);

        if (AppOptions.isFreeVersion) {
            //Ads
            if (mAdView != null) {
                mAdView.setVisibility(View.VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
        } else { //Paid version
            //Ads
            if (mAdView != null) {
                mAdView.setVisibility(View.GONE);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
            AppOptions.MinimumSolverPegCount =1;
        }
    }

    private void PurchasePaidVersion() {
        _inAppPurchases.PurchasePaidVersion(new
                                                    InAppPurchases.OnIAPPurchaseFinishedListener() {
                                                        public void onIAPPurchaseFinished(
                                                                IabResult result,
                                                                boolean isPaidVersion) {
                                                            AppOptions.isFreeVersion = !isPaidVersion;
                                                            // update UI accordingly
                                                            setLocalVersionBasedUI();
                                                        }
                                                    });
    }


}
