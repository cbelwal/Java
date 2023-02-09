package com.dasmic.android.lib.calllog.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dasmic.android.lib.calllog.ViewModel.ViewModelFilterOption;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelCallLogDisplay;

/**
 * Created by Chaitanya Belwal on 3/25/2017.
 */

public class ActivityFilter extends AppCompatActivity {
    ViewModelFilterOption _vmFilterOption;
    boolean _updateDone=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_filter_cl);
        setLocalVariablesAndEventHandlers();
        initializeDisplay();
    }

    protected void onButtonCancel(){
        Intent resultData = new Intent();
        if(_updateDone)
            setResult(Activity.RESULT_OK, resultData);
        else
            setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    protected void onButtonApply() {
        //Remember values
        Spinner spinner= (Spinner) findViewById(R.id.spinnerFilterOptions);
        EditText editText = (EditText) findViewById(R.id.textFilterOptionValue);
        _vmFilterOption.setCurrentOption(spinner.getSelectedItemPosition());
        _vmFilterOption.setOptionValue(editText.getText().toString(),
                _vmFilterOption.getCurrentOption());

        spinner= (Spinner) findViewById(R.id.spinnerFilterShow);
        if(spinner.getSelectedItemPosition()==0)
            _vmFilterOption.setShowSelection(true);
        else
            _vmFilterOption.setShowSelection(false);

        //---------------
        _updateDone=true;
        onButtonCancel();
    }

    protected void onFilterOptionChanged() {
        Spinner spinner= (Spinner) findViewById(R.id.spinnerFilterOptions);
        TextView textView = (TextView) findViewById(R.id.tvFilterOption);



        //Change Text
        switch(spinner.getSelectedItemPosition()){
            case ViewModelFilterOption.OptionShowFromLocation:
                textView.setText(getString(R.string.filter_text_value_location));
                break;
            case ViewModelFilterOption.OptionShowLessThanDays:
            case ViewModelFilterOption.OptionShowMoreThanDays:
                textView.setText(getString(R.string.filter_text_value_days));
                break;
            case ViewModelFilterOption.OptionShowLessThanDuration:
            case ViewModelFilterOption.OptionShowMoreThanDuration:
                textView.setText(getString(R.string.filter_text_value_duration));
                break;
            default:
                textView.setText("");
                break;
        }

        EditText editText = (EditText) findViewById(R.id.textFilterOptionValue);
        if(textView.getText().toString().equals(""))
            editText.setVisibility(View.INVISIBLE);
        else
            editText.setVisibility(View.VISIBLE);

    }

    protected void onFilterShowChanged() {
        //Don't to anything
    }

    private String[] assignValuesToFilterOptions(){
        String [] allOptions = new String[_vmFilterOption.getOptionsCount()];
        allOptions[ViewModelFilterOption.OptionShowFromLocation]=
                getString(R.string.filter_options_from_entered_location);
        allOptions[ViewModelFilterOption.OptionShowLessThanDuration]=
                getString(R.string.filter_options_less_than_duration);
        allOptions[ViewModelFilterOption.OptionShowLessThanDays]=
                getString(R.string.filter_options_less_than_entered_days);
        allOptions[ViewModelFilterOption.OptionShowMoreThanDuration]=
                getString(R.string.filter_options_more_than_duration);
        allOptions[ViewModelFilterOption.OptionShowMoreThanDays]=
                getString(R.string.filter_options_more_than_entered_days);
        allOptions[ViewModelFilterOption.OptionShowNone]=
                getString(R.string.filter_options_none);
        allOptions[ViewModelFilterOption.OptionShowWithStoredContacts]=
                getString(R.string.filter_options_from_stored_contacts);
        return allOptions;
    }

    private String[] assignValuesToFilterShow(){
        String [] allOptions = new String[2];
        allOptions[0]=
                getString(R.string.filter_show_show);
        allOptions[1]=
                getString(R.string.filter_show_noshow);
        return allOptions;
    }

    protected void initializeDisplay(){
        //Populate Both Spinners
        Spinner spinner= (Spinner) findViewById(R.id.spinnerFilterOptions);
        String []allOptions= assignValuesToFilterOptions();
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(this,
                R.layout.ui_spinner_item_cl, allOptions);
        spinner.setAdapter(spinAdapter);
        //Assign values based on current selection
        spinner.setSelection(_vmFilterOption.getCurrentOption());
        EditText editText = (EditText) findViewById(R.id.textFilterOptionValue);
        editText.setText(_vmFilterOption.getOptionValue(_vmFilterOption.getCurrentOption()));


        spinner= (Spinner) findViewById(R.id.spinnerFilterShow);
        String [] allShow=assignValuesToFilterShow();
        ArrayAdapter<String> spinAdapterShow = new ArrayAdapter<String>(this,
                R.layout.ui_spinner_item_cl, allShow);//android.R.layout.simple_spinner_dropdown_item
        spinner.setAdapter(spinAdapterShow);
        if(_vmFilterOption.getShowSelection())
            spinner.setSelection(0);
        else
            spinner.setSelection(1);


    }

    protected void setLocalVariablesAndEventHandlers(){
        ViewModelCallLogDisplay vmcld  =
                ViewModelCallLogDisplay.getInstance();
        if(vmcld == null) return; //vmcld is required
        _vmFilterOption = vmcld.getFilterData();

        //Add event handler for Spinners
        Spinner spinner= (Spinner) findViewById(R.id.spinnerFilterOptions);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                onFilterOptionChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });



        spinner= (Spinner) findViewById(R.id.spinnerFilterShow);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                onFilterShowChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCancel();
            }
        });

        Button btnConfirm = (Button) findViewById(R.id.btnSend);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonApply();
            }
        });
    }

}
