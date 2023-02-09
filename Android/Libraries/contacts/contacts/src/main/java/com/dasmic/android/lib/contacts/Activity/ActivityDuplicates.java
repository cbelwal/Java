package com.dasmic.android.lib.contacts.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dasmic.android.lib.contacts.Enum.DuplicateOptionsEnum;
import com.dasmic.android.lib.contacts.Enum.DuplicateOptionsEnum;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelContactsDisplay;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelDuplicates;
import com.dasmic.android.lib.support.Static.SupportFunctions;


/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ActivityDuplicates extends ActivityBaseAd {
    protected DuplicateOptionsEnum _selDoe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_duplicates);
        createRadioControls();
        setLocalVariablesAndEventHandlers();
        initializeDisplay();
        SupportFunctions.DisplayToastMessageLong(this,getString(R.string.message_duplicates_backup));
    }


    protected String getTextForDuplicateOption(DuplicateOptionsEnum doe){
        String displayText;
        switch(doe){
            case NameMatchWithEmailPhone:
                displayText = (String)
                        this.getResources().getText(
                                R.string.duplicate_options_name_matches_email_phone);
                break;
            case NameAndPhoneMatch:
                displayText = (String) this.getResources().getText(
                        R.string.duplicate_options_name_matches_phone);
                break;
            case NameAndEmailMatch:
                displayText = (String) this.getResources().getText(
                        R.string.duplicate_options_name_matches_email);
                break;
            case SameNumberMultipleNames:
                displayText = (String) this.getResources().getText(
                        R.string.duplicate_options_same_number_multi_name);
                break;
            case SameEmailMultipleNames:
                displayText = (String) this.getResources().getText(
                        R.string.duplicate_options_same_email_multi_name);
                break;
            case None:
                displayText = (String) this.getResources().getText(
                        R.string.duplicate_options_none);
                break;
            default:
                displayText=(String) this.getResources().getText(
                        R.string.duplicate_options_none);
                break;
        }
        return displayText;

    }

    protected void createRadioControls(){
        RadioGroup radioDuplicateOptions = (RadioGroup) findViewById(R.id.radioDuplicateOptions);
        float density = getResources().getDisplayMetrics().density;
        int idx=0;
        RadioButton[] rb= new RadioButton[DuplicateOptionsEnum.values().length];
        RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);
        //Set device independent
        lp.setMargins((int) (2.0f * density),
                (int) (3.0f * density),
                (int) (2.0f * density),
                (int) (3.0f * density));

        for(DuplicateOptionsEnum doe: DuplicateOptionsEnum.values()){
            rb[idx] = new RadioButton(getApplicationContext());
            rb[idx].setTextAppearance(this,R.style.Base_ThemeOverlay_AppCompat_Dark);
            rb[idx].setTextColor(Color.BLACK);
            rb[idx].setId(doe.ordinal());
            rb[idx].setLayoutParams(lp);
            String displayText=getTextForDuplicateOption(doe);
            rb[idx].setText(displayText);
            //Do not show if no text
            if(displayText.equals(""))rb[idx].setVisibility(View.GONE);
            radioDuplicateOptions.addView(rb[idx++]);
        }
    }

    protected void initializeDisplay(){
        RadioButton DuplicateOption=(RadioButton)
                findViewById(_selDoe.ordinal());
        DuplicateOption.setChecked(true);
    }

    protected void saveDuplicateValues() {
        RadioGroup DuplicateOptions=(RadioGroup) findViewById(R.id.radioDuplicateOptions);
        _selDoe=
                (DuplicateOptionsEnum.values()[DuplicateOptions.getCheckedRadioButtonId()]);
        ViewModelContactsDisplay.getInstance(this).setDuplicateOption(_selDoe);
    }

    protected void onButtonCancel(){
        Intent resultData = new Intent();
        //resultData.putExtra("valueName", "valueData");
        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    protected void onButtonApply()
    {
        saveDuplicateValues();
        Intent resultData = new Intent();
        //resultData.putExtra("valueName", "valueData");
        setResult(Activity.RESULT_OK, resultData);
        finish();
    }

    protected void setLocalVariablesAndEventHandlers(){
        _selDoe=ViewModelContactsDisplay.getInstance(this).getDuplicateOption();

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

