package com.dasmic.android.lib.apk.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dasmic.android.lib.apk.Enum.FilterOptionsEnum;
import com.dasmic.android.lib.apk.R;
import com.dasmic.android.lib.apk.ViewModel.ViewModelAppsDisplay;
import com.dasmic.android.lib.apk.ViewModel.ViewModelFilterOption;


/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ActivityFilter extends ActivityBaseAd {
    private ViewModelFilterOption _currentViewModelFilterOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_filter);
        createRadioControls();
        setLocalVariablesAndEventHandlers();
        initializeDisplay();
    }


    private String getTextForFilterOption(FilterOptionsEnum fo){
        String displayText;
        switch(fo){


            case ShowOnlyInstalledLast1Year:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_installed_last_1_year);
                break;
            case ShowOnlyInstalledLast30days:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_installed_last_30_days);
                break;
            case ShowOnlyLessThan20KB:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_less_than_20_mb);
                break;
            case ShowOnlyMoreThan20KB:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_more_than_20_mb);
                break;
            case NoFilter:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_nofilter);
                break;
            case ShowOnlySecurityInAppBilling:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_security_inappbilling);
                break;
            case ShowOnlySecurityReadCalender:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_security_readcalender);
                break;
            case ShowOnlySecurityReadCallLog:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_security_readcalllog);
                break;
            case ShowOnlySecurityReadContacts:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_security_readcontacts);
                break;
            case ShowOnlySecurityReadSMS:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_security_readsms);
                break;
            case ShowOnlySecurityWriteExternalStorage:
                displayText = (String) this.getResources().getText(
                        R.string.filter_options_security_writeexternalstorage);
                break;
            default:
                displayText=(String) this.getResources().getText(R.string.general_error);
                break;
        }
        return displayText;

    }

    private void createRadioControls(){
        RadioGroup radioFilterOptions = (RadioGroup) findViewById(R.id.radioFilterOptions);
        float density = getResources().getDisplayMetrics().density;
        int idx=0;
        RadioButton[] rb= new RadioButton[FilterOptionsEnum.values().length];
        RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);
        //Set device independent
        lp.setMargins((int) (2.0f * density),
                (int) (3.0f * density),
                (int) (2.0f * density),
                (int) (3.0f * density));

        for(FilterOptionsEnum fo: FilterOptionsEnum.values()){
            rb[idx] = new RadioButton(getApplicationContext());
            rb[idx].setTextAppearance(this, R.style.Base_ThemeOverlay_AppCompat_Dark);
            rb[idx].setTextColor(Color.BLACK);
            rb[idx].setId(fo.ordinal());
            rb[idx].setLayoutParams(lp);
            rb[idx].setText(getTextForFilterOption(fo));
            //rb[idx].setGravity(Gravity.RIGHT);
            radioFilterOptions.addView(rb[idx++]);
        }
    }

    private void initializeDisplay(){
       RadioButton filterOption=(RadioButton) findViewById(_currentViewModelFilterOption.getSelectedOption().ordinal());
       filterOption.setChecked(true);
        CheckBox ctv=(CheckBox) findViewById(R.id.checkedDisplaySystemApps);
        ctv.setChecked(_currentViewModelFilterOption.DisplaySystemApps);
    }

    private void saveFilterValues() {
        RadioGroup filterOptions=(RadioGroup) findViewById(R.id.radioFilterOptions);
        _currentViewModelFilterOption.setSelectedOption
                (FilterOptionsEnum.values()[filterOptions.getCheckedRadioButtonId()]);
        CheckBox ctv=(CheckBox) findViewById(R.id.checkedDisplaySystemApps);
        _currentViewModelFilterOption.DisplaySystemApps=ctv.isChecked();

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

    private void setLocalVariablesAndEventHandlers(){
        _currentViewModelFilterOption = ViewModelAppsDisplay.getInstance(this,
                _isMalwareProgram).getFilterOption();

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
    }
}
