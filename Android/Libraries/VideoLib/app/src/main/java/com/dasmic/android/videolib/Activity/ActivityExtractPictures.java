package com.dasmic.android.videolib.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.videolib.Data.DataVideoDisplay;
import com.dasmic.android.videolib.R;
import com.dasmic.android.videolib.ViewModel.ViewModelExportSupport;
import com.dasmic.android.videolib.ViewModel.ViewModelExtractPicture;
import com.dasmic.android.videolib.ViewModel.ViewModelVideoBrowser;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/10/2018.
 */

public class ActivityExtractPictures extends AppCompatActivity {
    protected final String tagUpdateDone="Update Done";
    protected ProgressDialogHorizontal _pdHori;
    protected boolean _updateDone;
    protected ViewModelVideoBrowser _vmVideoBrowser;
    protected ViewModelExtractPicture _vmExtractPicture;
    protected DataVideoDisplay _dvd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            _updateDone = savedInstanceState.getBoolean(
                    tagUpdateDone);

        setContentView(R.layout.ui_extract_pictures);
        setLocalVariablesAndEventHandlers();

        //Ideally cleanup should be done in Export but its leading to a problem there
        cleanInternalFolder();//No Temporary files are shown
    }


    protected void cleanInternalFolder(){
        //Remove files from temp folder
        try{
        //    ViewModelExport vmExport =
        //            new ViewModelExport(this,
        //                    _appName);
        //    if(vmExport != null)
        //        vmExport.cleanUpInternalFolder();
        }
        catch(Exception ex){

        }
    }

    protected void onButtonCancel() {
        Intent resultData = new Intent();
        if (_updateDone)
            setResult(Activity.RESULT_OK, resultData);
        else
            setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    protected void onButtonApply() {
        ExtractImages();
    }

    protected void ExtractImages()
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            private int _totCount;
            int _noOfPictures;
            int _timeStart;
            int _timePeriod;
            boolean _isUseExternal;
            int _count;
            boolean _error;
            ArrayList<String> _allPictureFiles;


            public BasicAsyncTask(int noOfPictures,
                                    int timeStart, int timePeriod, boolean useExternal){
                _noOfPictures = noOfPictures;
                _timeStart = timeStart;
                _timePeriod = timePeriod;
                _isUseExternal = useExternal;
            }

            private final Handler mExtractHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //FragmentActivity activity = getActivity();
                    switch (msg.what) {
                        case ViewModelExtractPicture.EXTRACT_COUNT_UPDATE:
                           _count = msg.arg1;
                            publishProgress();
                            break;
                        default:
                    }
                }
            };

            @Override
            protected Void doInBackground(Void...params) {
                    try {
                        _allPictureFiles = _vmExtractPicture.ExtractPicturesFromVideos(_dvd,
                                    _noOfPictures,_timeStart,_timePeriod, _isUseExternal,mExtractHandler);
                    }
                    catch(Exception ex){
                        //Mainly for sleep
                        _error = true;
                    }

                return null;
            }


            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                _pdHori.dismiss();

                if(!_error) {
                    if(!_isUseExternal)
                        ShareMultipleFiles(_allPictureFiles);

                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            String.valueOf(_count) + " " +
                                    getActivity().getText(
                                            R.string.message_extract_complete).toString());
                }
                else
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            String.valueOf(_count) + " " +
                                    getActivity().getText(
                                            R.string.message_extract_error).toString());
            }


            @Override
            protected void onPreExecute() {
                _count=0;

                _totCount= _noOfPictures;
                _pdHori = new ProgressDialogHorizontal(getActivity(),
                        getString(R.string.progressbar_extracting_pictures)+
                                "...");
                _pdHori.setMax(_totCount);
                _pdHori.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {
                _pdHori.setProgress(_count);
                _pdHori.setMessage( getString(R.string.progressbar_extracting_pictures)+
                        "...");
            }
        }
        Log.i("CKIT", "ActivityMain::onDeleteContacts");
        //Get values
        NumberPicker np = (NumberPicker) findViewById(R.id.npNumberOfPictures);
        int noOfPictures = np.getValue();
        np = (NumberPicker) findViewById(R.id.npTimeStart);
        int timeStart = np.getValue();
        np = (NumberPicker) findViewById(R.id.npTimePeriod);
        int timePeriod = np.getValue();



        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(noOfPictures,
                                timeStart,timePeriod, getIsStoreInExternalStorage());
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        }
        catch(Exception ex){
            Log.i("CKIT","Exception in Main::delete"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    protected void ShareMultipleFiles(ArrayList<String> allPictureFiles){
        ViewModelExportSupport vmExportSupport = new
                ViewModelExportSupport(getActivity());
        ArrayList<File> allFiles = new ArrayList<>();

        for(String fp:allPictureFiles){
            File f= new File(fp);
            allFiles.add(f);
        }

        try {
            Intent intent=null;
            intent=vmExportSupport.getMultipleFileIntentFile("application/octet-stream",
                    allFiles);
            getActivity().startActivity(Intent.createChooser(intent,
                    getActivity().getString(R.string.title_share)));
        }
        catch (Exception ex){
            SupportFunctions.AsyncDisplayGenericDialog(getActivity(),
                    getActivity().getString(R.string.message_share_error)+":"+ex.getMessage(),"");
            SupportFunctions.vibrate(getActivity(),200);
        }
    }

    private boolean getIsStoreInExternalStorage(){
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioShareOptions);
        int selectedId = rg.getCheckedRadioButtonId();

        if(selectedId == R.id.radioShareApplications)
            return false;
        else
            return true;
    }

    private Activity getActivity(){
        return this;
    }

    protected void setLocalVariablesAndEventHandlers() {
        _vmVideoBrowser = ViewModelVideoBrowser.getInstance(getActivity());
        _vmExtractPicture = new ViewModelExtractPicture(getActivity());
        _dvd = _vmVideoBrowser.getCheckedItems().get(0);

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCancel();
            }
        });

        Button btnConfirm = (Button) findViewById(R.id.btnApply);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonApply();
            }
        });
        setFileSpecificValues();
    }


    protected void setFileSpecificValues(){
        if(_vmExtractPicture == null || _dvd == null) return;
        int duration = _vmExtractPicture.getLengthOfVideos(_dvd.AbsoluteFilePath);

        String[] nums = new String[duration-1];
        for(int ii=0; ii<duration-1; ii++)
            nums[ii] = String.valueOf(ii+1);

        final NumberPicker np1 = (NumberPicker) findViewById(R.id.npNumberOfPictures);
        np1.setMinValue(1);
        np1.setMaxValue(duration-1); //Based on values with each second
        np1.setWrapSelectorWheel(true);
        np1.setValue(1);
        np1.setDisplayedValues(nums);
        //np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
        //    @Override
        //    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //        EditText et = ((EditText) np1.getChildAt(0));
        //        et.setTextColor(100);
        //    }
        //});

        NumberPicker np = (NumberPicker) findViewById(R.id.npTimeStart);
        np.setMinValue(1);
        np.setMaxValue(duration - 1); //Based on values
        np.setWrapSelectorWheel(true);
        np.setValue(1);
        np.setDisplayedValues(nums);

        np = (NumberPicker) findViewById(R.id.npTimePeriod);
        np.setMinValue(1);
        np.setMaxValue(duration-1); //Based on values
        np.setWrapSelectorWheel(true);
        np.setValue(2);
        np.setDisplayedValues(nums);

        //Set text values
        TextView tv = (TextView) findViewById(R.id.tvFileName);
        tv.setText(_dvd.getName());

        tv = (TextView) findViewById(R.id.tvModifiedDate);
        tv.setText(_dvd.getModifiedDateFormatted());

        tv = (TextView) findViewById(R.id.tvFileSize);
        tv.setText(String.valueOf(_dvd.getSizeInKB()));

        tv = (TextView) findViewById(R.id.tvDuration);
        tv.setText(String.valueOf(duration) + "s");
    }
}
