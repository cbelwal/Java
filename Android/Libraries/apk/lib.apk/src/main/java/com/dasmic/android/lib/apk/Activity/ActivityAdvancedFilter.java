package com.dasmic.android.lib.apk.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.dasmic.android.lib.apk.Enum.FilterAdvOptionsEnum;
import com.dasmic.android.lib.apk.R;
import com.dasmic.android.lib.apk.ViewModel.ViewModelAdvFilterOption;
import com.dasmic.android.lib.apk.ViewModel.ViewModelAppsDisplay;


/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ActivityAdvancedFilter extends ActivityBaseAd {
    private ViewModelAdvFilterOption _currentViewModelFilterOption;
    CheckBox[] _allCheckBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_adv_filter);
        createControls();
        setLocalVariablesAndEventHandlers();
        initializeDisplay();
    }


    private String getTextForFilterOption(FilterAdvOptionsEnum fo){
        String displayText;
        switch(fo){
            case ShowSecurityCamera:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_camera);
                break;
            case ShowInstalledLast30days:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_installed_last_30_days);
                break;
            case ShowSecurityMic:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_mic);
                break;
            case ShowSecurityInAppBilling:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_inappbilling);
                break;
            case ShowSecurityReadCalender:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_readcalender);
                break;
            case ShowSecurityReadCallLog:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_readcalllog);
                break;
            case ShowSecurityReadContacts:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_readcontacts);
                break;
            case ShowSecurityReadSMS:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_readsms);
                break;
            case ShowSecuritySendSMS:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_sendsms);
                break;
            case ShowSecurityWriteExternalStorage:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_writeexternalstorage);
                break;
            case ShowAccessLocation:
                displayText = (String) this.getResources().getText(
                        R.string.filter_adv_options_security_location);
                break;
            default:
                displayText=(String) this.getResources().getText(R.string.general_error);
                break;
        }
        return displayText;

    }

    private void createControls(){
        LinearLayout llCheckbox = (LinearLayout) findViewById(
                R.id.layoutCheckbox);
        float density = getResources().getDisplayMetrics().density;
        int idx=0;
        _allCheckBoxes = new CheckBox[FilterAdvOptionsEnum.values().length];
        LinearLayout.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);

        //Set device independent
        lp.setMargins((int) (2.0f * density),
                (int) (3.0f * density),
                (int) (2.0f * density),
                (int) (3.0f * density));

        for(FilterAdvOptionsEnum fo: FilterAdvOptionsEnum.values()){
            _allCheckBoxes[idx] = new CheckBox(getApplicationContext());
            _allCheckBoxes[idx].setTextAppearance(this, R.style.Base_ThemeOverlay_AppCompat_Dark);
            _allCheckBoxes[idx].setTextColor(Color.BLACK);
            _allCheckBoxes[idx].setId(fo.ordinal());
            _allCheckBoxes[idx].setLayoutParams(lp);
            _allCheckBoxes[idx].setText(getTextForFilterOption(fo));
            //rb[idx].setGravity(Gravity.RIGHT);
            llCheckbox.addView(_allCheckBoxes[idx++]);
        }
    }

    private void initializeDisplay(){
        //Display preselected values
        int idx=0;
        for(CheckBox cb:_allCheckBoxes) {
            cb.setChecked(_currentViewModelFilterOption.getSelectedOption(idx));
            idx++;
        }

        Switch af=(Switch) findViewById(R.id.switchActivateFilter);
        af.setChecked(_currentViewModelFilterOption.ActivateFilter);

        Switch dsa=(Switch) findViewById(R.id.switchDisplaySystemApps);
        dsa.setChecked(_currentViewModelFilterOption.DisplaySystemApps);
    }

    private void saveFilterValues() {
        int idx=0;
        for(CheckBox cb:_allCheckBoxes){
            if(cb.isChecked())
                _currentViewModelFilterOption.setSelectedOption(idx,true);
            else
                _currentViewModelFilterOption.setSelectedOption(idx,false);
            idx++;
        }

        Switch af=(Switch) findViewById(R.id.switchActivateFilter);
        _currentViewModelFilterOption.ActivateFilter=af.isChecked();

        Switch dsa=(Switch) findViewById(R.id.switchDisplaySystemApps);
        _currentViewModelFilterOption.DisplaySystemApps=dsa.isChecked();
    }

    private void onButtonCancel(){
        Intent resultData = new Intent();
        //resultData.putExtra("valueName", "valueData");
        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    private void onButtonApply()
    {
        saveFilterValues();
        Intent resultData = new Intent();
        //resultData.putExtra("valueName", "valueData");
        setResult(Activity.RESULT_OK, resultData);
        finish();
    }

    private void onButtonReset()
    {
        _currentViewModelFilterOption.ResetValues();
        initializeDisplay();
    }

    private void setLocalVariablesAndEventHandlers(){
        _currentViewModelFilterOption =
                ViewModelAppsDisplay.getInstance(this,_isMalwareProgram).getAdvFilterOption();


        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCancel();
            }
        });

        Button btnApply = (Button) findViewById(R.id.btnApply);
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonApply();
            }
        });

        Button btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonReset();
            }
        });
    }
}
