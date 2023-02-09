package com.dasmic.android.lib.calllog.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Model.ModelCallLogCreate;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelCallLogDisplay;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelFilterOption;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 3/11/2017.
 */

public class ActivityEditLogBase extends AppCompatActivity {

    protected DataCallLogDisplay _selectedCallLog;
    protected boolean _updateDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_single_log_edit_cl);
        setLocalVariablesAndEventHandlers();
        initializeDisplay();
    }


    protected void setLocalVariablesAndEventHandlers(){
        //super.setLocalVariablesAndEventHandlers();
        _selectedCallLog = getSelectedCallLog();
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

    protected void onButtonCancel(){
        Intent resultData = new Intent();
        if(_updateDone)
            setResult(Activity.RESULT_OK, resultData);
        else
            setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    protected void onButtonApply() {
        _updateDone=true;
        //Copy into new data DataCallLogDisplay
        DataCallLogDisplay dcld= getUpdatedCallLogObject();

        //Call function to do update
        doApplyAction(dcld);
    }

    protected DataCallLogDisplay getUpdatedCallLogObject(){
        long date,duration,type=0;
        String number;

        EditText textNumber=(EditText) findViewById(R.id.textNumber);
        number = textNumber.getText().toString();

        Spinner spinnerCallType=(Spinner) findViewById(
                                    R.id.spinnerCallLogType);
        //Now populate values
        //There ic coupling with array so be careful
        switch(spinnerCallType.getSelectedItemPosition()) {
            case 0:
                type= CallLog.Calls.INCOMING_TYPE;
                break;
            case 1:
                type = CallLog.Calls.OUTGOING_TYPE;
                break;
            case 2:
                type = CallLog.Calls.MISSED_TYPE;
                break;
        }

        //Type
        //Coupling to array definition, be careful
        //Date
        DatePicker dp=(DatePicker) findViewById(R.id.datePicker);
        //Time
        TimePicker tp=(TimePicker) findViewById(R.id.timePicker);

        if (Build.VERSION.SDK_INT >= 23 )
            date = DateOperations.getMilliseconds(dp.getYear(), dp.getMonth(),
                    dp.getDayOfMonth(),tp.getHour(),
                    tp.getMinute(),0);
        else
            date = DateOperations.getMilliseconds(dp.getYear(), dp.getMonth(),
                    dp.getDayOfMonth(),tp.getCurrentHour(),
                    tp.getCurrentMinute(),0);

        //Duration
        EditText textDuration=(EditText) findViewById(R.id.textDuration);
        duration = Long.parseLong(textDuration.getText().toString());

        return new DataCallLogDisplay(_selectedCallLog.getId(),type,date,
                                        duration,_selectedCallLog.getName(),
                                        number,_selectedCallLog.getGeoLocation(),
                                        _selectedCallLog.getPictureUri());

    }

    //Default action is to to Update, override this function for Add
    protected void doApplyAction(DataCallLogDisplay dcld){
       //Always overridden
        throw new RuntimeException("Not implemented");
    }

    protected DataCallLogDisplay getSelectedCallLog(){
        //Always overridden
        throw new RuntimeException("Not implemented");
    }

    protected void initializeDisplay(){
        //Set Spinner Choices
        Spinner spinnerCallType=(Spinner) findViewById(R.id.spinnerCallLogType);
        spinnerCallType.setAdapter(getSpinnerTypeAdapter());

        //Now populate values
        //Number
        if(_selectedCallLog == null) return;
        EditText number=(EditText) findViewById(R.id.textNumber);
        number.setText(_selectedCallLog.getNumber());

        //Type
        //Coupling to array definition, be careful
        if(_selectedCallLog.isIncomingCall())
            spinnerCallType.setSelection(0,true);
        if(_selectedCallLog.isOutgoingCall())
            spinnerCallType.setSelection(1,true);
        if(_selectedCallLog.isMissedCall())
            spinnerCallType.setSelection(2,true);

        //Date
        DatePicker dp=(DatePicker) findViewById(R.id.datePicker);
        dp.updateDate(DateOperations.getYear(_selectedCallLog.getDate()),
                DateOperations.getMonth(_selectedCallLog.getDate()),
                DateOperations.getDayOfMonth(_selectedCallLog.getDate()));
        //Time
        TimePicker tp=(TimePicker) findViewById(R.id.timePicker);
        tp.setIs24HourView(false);
        tp.setCurrentHour(DateOperations.getHour24Hr(_selectedCallLog.getDate()));
        tp.setCurrentMinute(DateOperations.getMinute(_selectedCallLog.getDate()));

        //Duration
        EditText textDuration=(EditText) findViewById(R.id.textDuration);
        textDuration.setText(String.valueOf(
                                _selectedCallLog.getDuration()));
    }


    protected ArrayAdapter<String> getSpinnerTypeAdapter(){
        String[] array = new String[3];
        array[0] = getString(R.string.spinner_calllog_type_incoming);
        array[1] = getString(R.string.spinner_calllog_type_outgoing);
        array[2] = getString(R.string.spinner_calllog_type_missed);

        return new ArrayAdapter<String>(this,
                R.layout.ui_spinner_item_cl, array);
    }



}
