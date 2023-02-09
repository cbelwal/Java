package com.dasmic.android.lib.contacts.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dasmic.android.lib.contacts.Enum.FilterOptionsEnum;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelContactsDisplay;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelFilterOption;


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
            case ShowOnlyWithContactedMoreThanOnce:
                displayText = (String) this.getResources().getText(R.string.filter_options_show_contacted_more_once);
                break;
            case ShowOnlyWithContactedNone:
                displayText = (String) this.getResources().getText(R.string.filter_options_show_contacted_less_once);
                break;
            case ShowOnlyWithLessThan7DayContact:
                displayText = (String) this.getResources().getText(R.string.filter_options_contacted_7_days);
                break;
            case ShowOnlyWithLessThan30DayContact:
                displayText = (String) this.getResources().getText(R.string.filter_options_contacted_31_days);
                break;
            case ShowOnlyWithMoreThan1YearContact:
                displayText = (String) this.getResources().getText(R.string.filter_options_contacted_more_1_year);
                break;
            case ShowOnlyWithLessThan1YearContact:
                displayText = (String) this.getResources().getText(R.string.filter_options_contacted_less_1_year);
                break;
            case ShowOnlySendToVoiceMailList:
                displayText = (String) this.getResources().getText(R.string.filter_options_send_to_voicemail);
                break;
            case ShowOnlyWithPictures:
                displayText = (String) this.getResources().getText(R.string.filter_options_with_pictures);
                break;
            case ShowOnlyStarred:
                displayText = (String) this.getResources().getText(R.string.filter_options_starred);
                break;
            case ShowOnlyInNonVisibleGroups:
                displayText = (String) this.getResources().getText(R.string.filter_options_non_visible_groups);
                break;
            case ShowOnlyDoNotSendToVoiceMailList:
                displayText = (String) this.getString(R.string.filter_options_donot_send_to_voicemail);
                break;

            case None:
                displayText = (String) this.getResources().getText(R.string.filter_options_no_filter);
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
    }

    private void saveFilterValues() {
        RadioGroup filterOptions=(RadioGroup) findViewById(R.id.radioFilterOptions);
        _currentViewModelFilterOption.setSelectedOption
                (FilterOptionsEnum.values()[filterOptions.getCheckedRadioButtonId()]);

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
        _currentViewModelFilterOption = ViewModelContactsDisplay.getInstance(this).getFilterOption();

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCancel();
            }
        });

        Button btnApply = (Button) findViewById(R.id.btnSend);
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonApply();
            }
        });
    }
}
