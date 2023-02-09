package com.dasmic.android.lib.contacts.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dasmic.android.lib.contacts.Enum.FilterOptionsEnum;
import com.dasmic.android.lib.contacts.Enum.OperationOptionsEnum;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelContactsDisplay;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelOperations;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ActivityOperations extends ActivityBaseAd {
    ProgressDialogSpinner _progressDialog;
    boolean  _updateDone;
    ArrayList<Long> _selContacts;
    OperationOptionsEnum _selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_operations);

        Intent intent = getIntent();
        //String value = intent.getStringExtra("key"); //if it's a string you stored.
        setLocalVariablesAndEventHandlers();
        createRadioControls();
        initializeDisplay();
    }

    @Override
    protected void onDestroy() {
        Log.i("CKIT", "ActivityOperation onDestroy");
        super.onDestroy();
    }

    private String getTextForOption(OperationOptionsEnum fo){
        String displayText;
        switch(fo){
            case AddSendToVoiceMail:
                displayText = (String) this.getResources().getText(R.string.operation_options_setsendtoVM);
                break;
            case RemoveSendToVoicemail:
                displayText = (String) this.getResources().getText(R.string.operation_options_removesendtoVM);
                break;
            case AddStarred:
                displayText = (String) this.getResources().getText(R.string.operation_options_setstarred);
                break;
            case RemoveStarred:
                displayText = (String) this.getResources().getText(R.string.operation_options_removestarred);
                break;
            case ResetTimesCalledCount:
                displayText = (String) this.getResources().getText(R.string.operation_options_resettimescalled);
                break;
            case SetLastContactToNow:
                displayText = (String) this.getResources().getText(R.string.operation_options_settimecallednow);
                break;
            default:
                displayText=(String) this.getResources().getText(R.string.general_error);
                break;
        }
        return displayText;

    }

    private void createRadioControls(){
        RadioGroup radioFilterOptions = (RadioGroup) findViewById(R.id.radioOperationOptions);
        float density = getResources().getDisplayMetrics().density;
        int idx=0;
        RadioButton[] rb= new RadioButton[FilterOptionsEnum.values().length];
        RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins((int) (2.0f * density),
                (int) (3.0f * density),
                (int) (2.0f * density),
                (int) (3.0f * density));

        for(OperationOptionsEnum fo: OperationOptionsEnum.values()){
            rb[idx] = new RadioButton(getApplicationContext());
            rb[idx].setTextAppearance(this, R.style.Base_ThemeOverlay_AppCompat_Dark);
            rb[idx].setTextColor(Color.BLACK);
            rb[idx].setId(fo.ordinal());
            rb[idx].setLayoutParams(lp);
            rb[idx].setText(getTextForOption(fo));
            radioFilterOptions.addView(rb[idx++]);
        }
    }

    private void initializeDisplay(){
        //Set default
        RadioButton option=(RadioButton) findViewById(OperationOptionsEnum.AddStarred.ordinal());
        option.setChecked(true);
    }


    private void onButtonCancel(){
        Intent resultData = new Intent();
        if(_updateDone)
            setResult(Activity.RESULT_OK, resultData);
        else
            setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    private void onButtonApply()
    {
        ViewModelContactsDisplay vmContactsDisplay=
                ViewModelContactsDisplay.getInstance(this);
        _selContacts =
                getUniqueContactIdList(vmContactsDisplay.getCheckedItems());

        RadioGroup opOptions=(RadioGroup) findViewById(R.id.radioOperationOptions);
        _selected = OperationOptionsEnum.values()
                [opOptions.getCheckedRadioButtonId()];

        //Show MessageBox for these
        if(_selected==OperationOptionsEnum.ResetTimesCalledCount ||
                _selected==OperationOptionsEnum.SetLastContactToNow){
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

            dlgAlert.setTitle(this.getResources().getText(R.string.title_confirm));
            dlgAlert.setMessage(String.valueOf(_selContacts.size()) + " " +
                    this.getResources().getText(R.string.message_update_confirm));
            dlgAlert.setCancelable(false);

            dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_Yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing but close the dialog
                    doSelectedOperation(_selContacts, _selected);
                    dialog.dismiss();
                }

            });

            dlgAlert.setNegativeButton(this.getResources().getText(R.string.button_No), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = dlgAlert.create();
            alert.show();
        }
        else {
            doSelectedOperation(_selContacts, _selected);
        }

    }

    private void doSelectedOperation(ArrayList<Long> selContacts,
                                     OperationOptionsEnum selection)
    {
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            ArrayList<Long> _selContacts;
            OperationOptionsEnum _selection;


            BasicAsyncTask(Activity context,
                           ArrayList<Long> selContacts,
                           OperationOptionsEnum selection)
            {
                super();
                _context = context;
                _selContacts =selContacts;
                _selection = selection;

            }

            @Override
            protected Void doInBackground(Void... params) {
                    ViewModelOperations vmOperations =
                            new ViewModelOperations(_context);

                    switch(_selection){
                        case AddSendToVoiceMail:
                            vmOperations.UpdateSendToVoiceMail(_selContacts,true);
                            break;
                        case RemoveSendToVoicemail:
                            vmOperations.UpdateSendToVoiceMail(_selContacts, false);
                            break;
                        case AddStarred:
                            vmOperations.UpdateStarred(_selContacts, true);
                            break;
                        case RemoveStarred:
                            vmOperations.UpdateStarred(_selContacts, false);
                            break;
                        case ResetTimesCalledCount:
                            vmOperations.resetTimesCalled(_selContacts);
                            break;
                        case SetLastContactToNow:
                            vmOperations.setLastContactToNow(_selContacts);
                            break;
                    }

                //SupportFunctions.addDelay(30000);
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                super.onPostExecute(param);
                Log.i("CKIT", "thread PostExecute");
                _updateDone=true;
                _progressDialog.dismiss();
                SupportFunctions.DisplayToastMessageShort(_context,
                        String.valueOf(_selContacts.size()) + " " +
                                _context.getResources().getText(R.string.message_update_complete).toString()
                );
            }

            @Override
            protected void onPreExecute() {
                Log.i("CKIT", "thread PreExecute");
                super.onPreExecute();
                _progressDialog.show();
            }

            @Override
            protected void onProgressUpdate(Void... params) {
            }
        }

        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(this,selContacts,selection);
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        }
        catch(Exception ex){
            Log.i("CKIT","Exception in Operations::doInBackground"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }

        Log.i("CKIT", "Thread finished execution");

    }


    private void setLocalVariablesAndEventHandlers(){
        _progressDialog = new ProgressDialogSpinner(this,
                this.getResources().getText(R.string.progressbar_update_data).toString());

        _updateDone = false;

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
