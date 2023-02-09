package com.dasmic.android.lib.audio.Activity;

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
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dasmic.android.lib.audio.Interface.IGenericEvent;
import com.dasmic.android.lib.audio.Model.SoundFile;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Extension.ProgressDialogSpinner;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.audio.Data.DataAudioDisplay;

import com.dasmic.android.lib.audio.ViewModel.ViewModelExportSupport;
import com.dasmic.android.lib.audio.ViewModel.ViewModelExtractAudio;
import com.dasmic.android.lib.audio.ViewModel.ViewModelAudioBrowser;
import com.dasmic.android.libaudio.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/10/2018.
 */

public class ActivityExtractAudio extends AppCompatActivity {
    protected final String tagUpdateDone="Update Done";
    protected ProgressDialogHorizontal _pdHori;
    protected ProgressDialogSpinner _pdSpinner;
    protected boolean _updateDone;
    protected ViewModelAudioBrowser _vmVideoBrowser;
    protected ViewModelExtractAudio _vmExtractAudio;
    protected DataAudioDisplay _dad;
    protected int _audioTotalDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            _updateDone = savedInstanceState.getBoolean(
                    tagUpdateDone);
        setContentView(R.layout.ui_extract_audio);
        loadFileDetails();
    }


    @Override
    public void onDestroy() { //In App billing cleanup
        super.onDestroy();
        if(_vmVideoBrowser != null)
            _vmVideoBrowser.stopCurrentAudio();

        if(_pdHori != null)
            _pdHori.dismiss();

        if(_pdSpinner != null)
            _pdSpinner.dismiss();
    }

    protected void loadFileDetails(){
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            private String _errorString;

            public BasicAsyncTask(){
                _errorString="";
            }

            @Override
            protected Void doInBackground(Void...params) {

                try {
                    setLocalVariablesAndEventHandlers();
                    setFileSpecificValues();
                    //Ideally cleanup should be done in Export but its leading to a problem there
                    cleanInternalFolder();//No Temporary files are shown
                }
                catch(Exception ex){
                    //Mainly for sleep
                    _errorString = ex.getMessage();
                }

                return null;
            }


            @Override
            protected void onPostExecute(Void param) {
                // _listAdapter.registerDataSetObserver();
                _pdSpinner.dismiss();
                if(!_errorString.equals("")) {
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                                    getString(
                                            R.string.message_extract_error) + ":" + _errorString);
                }
                else
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            getActivity().getText(
                                    R.string.message_load_complete).toString());

                //Set the values of the UI
                String[] nums = new String[_audioTotalDuration];
                for(int ii = 0; ii< _audioTotalDuration; ii++)
                    nums[ii] = String.valueOf(ii+1);

                NumberPicker np = (NumberPicker) findViewById(R.id.npTimeStart);
                np.setMinValue(1);
                np.setMaxValue(_audioTotalDuration - 1); //Based on values
                np.setWrapSelectorWheel(true);
                np.setValue(1);
                np.setDisplayedValues(nums);

                np = (NumberPicker) findViewById(R.id.npTimeDuration);
                np.setMinValue(1);
                np.setMaxValue(_audioTotalDuration); //Based on values
                np.setWrapSelectorWheel(true);
                np.setValue(2);
                np.setDisplayedValues(nums);

                //Set text values
                TextView tv = (TextView) findViewById(R.id.tvFileName);
                tv.setText(_dad.getName());

                tv = (TextView) findViewById(R.id.tvModifiedDate);
                tv.setText(_dad.getModifiedDateFormatted());

                tv = (TextView) findViewById(R.id.tvFileSize);
                tv.setText(String.valueOf(_dad.getSizeInKB()));

                tv = (TextView) findViewById(R.id.tvDuration);
                tv.setText(String.valueOf(_audioTotalDuration) + "s");
            }


            @Override
            protected void onPreExecute() {
                _pdSpinner = new ProgressDialogSpinner(getActivity(),
                        getString(R.string.progressbar_loading_audio));
                _pdSpinner.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {

            }
        }
        Log.i("CKIT", "ActivityMain::onDeleteContacts");
        //Get values
        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask();
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
        _updateDone=true;
        ExtractAudio();
    }

    protected void onButtonPlay() {
        Button btnPlay = (Button) findViewById(R.id.btnPlay);

        if(btnPlay.getText().equals(getString(R.string.button_stop)))
            _vmVideoBrowser.stopCurrentAudio();
        else
            _vmVideoBrowser.playAudio(_dad.AbsoluteFilePath,  new IGenericEvent() { //Specify action when music stops playing
                @Override
                public void onEvent(int value) {
                // do something with item
                    changePlayText();
                }});
        changePlayText();
    }

    protected  void changePlayText(){
        Button btnPlay = (Button) findViewById(R.id.btnPlay);
        if(btnPlay.getText().equals(getString(R.string.button_play)))
            btnPlay.setText(R.string.button_stop);
        else
            btnPlay.setText(R.string.button_play);
    }

    protected void ExtractAudio()
    {
        //Show Progress Dialog
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            private int _totCount;
            int _noOfAudioFiles;
            int _timeStart;
            int _timeDuration;
            boolean _isUseExternal;
            int _count;
            int _currentTime;
            boolean _error;
            boolean mDurationReduced;
            int _extractErrorCount;

            ArrayList<String> _allAudioFiles;


            public BasicAsyncTask(int timeStart,
                                  int timeDuration, boolean useExternal){
                _timeStart = timeStart;
                _timeDuration = timeDuration;
                _isUseExternal = useExternal;
                _extractErrorCount = 0;
            }

            private final Handler mExtractHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //FragmentActivity activity = getActivity();
                    switch (msg.what) {
                        case ViewModelExtractAudio.EXTRACT_COUNT_UPDATE:
                           _count++;
                           _currentTime = msg.arg1;
                            publishProgress();
                            break;
                        case ViewModelExtractAudio.EXTRACT_COUNT_ERROR:
                            _extractErrorCount++;
                        case ViewModelExtractAudio.EXTRACT_COUNT_REDUCED_DURATION:
                            mDurationReduced=true;
                        default:
                    }
                }
            };

            @Override
            protected Void doInBackground(Void...params) {
                    try {
                        _allAudioFiles = _vmExtractAudio.ExtractSmallAudioFromLargeAudio(_dad,
                                _audioTotalDuration,
                                _timeStart, _timeDuration,
                                _isUseExternal
                                ,mExtractHandler);
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
                        ShareMultipleFiles(_allAudioFiles);

                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            String.valueOf(_count) + " " +
                                    getActivity().getText(
                                            R.string.message_extract_complete).toString());
                }
                else
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                                    getActivity().getText(
                                            R.string.message_extract_error).toString());

                if(_extractErrorCount > 0)
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                                    getString(R.string.message_extract_incomplete,
                                            _extractErrorCount));

                if(mDurationReduced)
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                                    getActivity().getText(
                                            R.string.message_extract_duration_reduced).toString());
            }

            @Override
            protected void onPreExecute() {
                _count=0;

                _totCount= _audioTotalDuration;
                _pdHori = new ProgressDialogHorizontal(getActivity(),
                        getString(R.string.progressbar_extracting_audio)+
                                "...");
                _pdHori.setMax(_totCount);
                _pdHori.show();
            }

            @Override
            protected void onProgressUpdate(Void...params) {
                _pdHori.setProgress(_currentTime);
                _pdHori.setMessage( getString(R.string.progressbar_extracting_audio)+
                        "...");
            }
        }
        Log.i("CKIT", "ActivityMain::onDeleteContacts");
        //Get values
        NumberPicker np = (NumberPicker) findViewById(R.id.npTimeStart);
        int timeStart = np.getValue();
        np = (NumberPicker) findViewById(R.id.npTimeDuration);
        int timeDuration = np.getValue();


        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(timeStart,timeDuration, getIsStoreInExternalStorage());
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
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message_extract_signature));
            getActivity().startActivity(Intent.createChooser(intent,
                    getString(R.string.title_share)));
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
        _vmVideoBrowser = ViewModelAudioBrowser.getInstance(getActivity());
        _vmExtractAudio = new ViewModelExtractAudio(getActivity());
        _dad = _vmVideoBrowser.getCheckedItems().get(0);

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

        Button btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPlay();
            }
        });
    }


    protected void setFileSpecificValues(){
        if(_vmExtractAudio == null || _dad == null) return;
        _audioTotalDuration = _vmExtractAudio.getLengthOfAudio(_dad.AbsoluteFilePath);

    }
}
