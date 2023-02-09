package com.dasmic.android.lib.callrecorder.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.dasmic.android.lib.callrecorder.Enum.AppOptions;
import com.dasmic.android.lib.callrecorder.R;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

public class ActivityBaseMain extends ActivityBaseAd {
    private MediaRecorder _recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SupportFunctions.DebugLog("lib.callrecorder::ActivityBaseMain",
                "onCreate", "In function");
        setContentView(R.layout.ui_main_cr);
        setLocalVariablesAndEventHandlers();
    }

    protected void setLocalVariablesAndEventHandlers() {
        Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                _recorder.stop();
                _recorder.release();
            }
        });

        Button btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        });
    }

    protected void startRecording(){
        if(checkPermissionsForAndroid6Plus_CallRecord())
        {
            int audioSource = getAudioSource("VOICE_CALL");
            startMediaRecorder(audioSource);
        }
    }

    //Request all call log and contacts permissions
    private boolean checkPermissionsForAndroid6Plus_CallRecord(){
        int hasRecordPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int hasWriteExternalPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (hasRecordPermission != PackageManager.PERMISSION_GRANTED ||
                hasWriteExternalPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]
                            {Manifest.permission.RECORD_AUDIO,
                             Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    AppOptions.PERMISSION_CALL_RECORD_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        boolean failed=true; //Flag to denote failed
        switch (requestCode) {
            case AppOptions.PERMISSION_CALL_RECORD_REQUEST:
                if(grantResults.length == 1)
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED){
                        startRecording(); //Also refresh list view
                        failed=false;
                    }
                if(failed)
                {
                    //SupportFunctions.DisplayToastMessageLong(this,
                    //        getString(R.string.message_contact_permissions_missing));
                    finish();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);
        }
    }


    private boolean startMediaRecorder(final int audioSource){
        _recorder = new MediaRecorder();
        try{
            _recorder.reset();
            _recorder.setAudioSource(MediaRecorder.AudioSource.MIC);//audioSource);
            _recorder.setAudioSamplingRate(8000);
            _recorder.setAudioEncodingBitRate(12200);
            //_recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            _recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            _recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //_recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            String fileName = FileOperations.getExternalStorageFolderString() + "/Record.aac"; //audiofile.getAbsolutePath();
            //String fileName = "Record.aac"; //audiofile.getAbsolutePath();
            //String fileName = FileOperations.getInternalStorageFolder(this) + "/Record.aac"; //audiofile.getAbsolutePath();
            _recorder.setOutputFile(fileName);

            MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
                public void onError(MediaRecorder arg0, int arg1, int arg2) {
                    SupportFunctions.ErrorLog("lib.callrecorder::ActivityBaseMain",
                            "startMediaRecorder","In function");
                    //terminateAndEraseFile();
                }
            };
            _recorder.setOnErrorListener(errorListener);

            MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
                public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
                    SupportFunctions.ErrorLog("lib.callrecorder::ActivityBaseMain",
                            "startMediaRecorder","Error in OnInfoListner");
                }
            };
            _recorder.setOnInfoListener(infoListener);

            _recorder.prepare();
            // Sometimes prepare takes some time to complete
            Thread.sleep(2000);
            _recorder.start();

            //isRecordStarted = true;
            return true;
        }catch (Exception e){
            e.getMessage();
            return false;
        }
    }

    public static int getAudioSource(String str) {
        if (str.equals("MIC")) {
            return MediaRecorder.AudioSource.MIC;
        }
        else if (str.equals("VOICE_COMMUNICATION")) {
            return MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        }
        else if (str.equals("VOICE_CALL")) {
            return MediaRecorder.AudioSource.VOICE_CALL;
        }
        else if (str.equals("VOICE_DOWNLINK")) {
            return MediaRecorder.AudioSource.VOICE_DOWNLINK;
        }
        else if (str.equals("VOICE_UPLINK")) {
            return MediaRecorder.AudioSource.VOICE_UPLINK;
        }
        else if (str.equals("VOICE_RECOGNITION")) {
            return MediaRecorder.AudioSource.VOICE_RECOGNITION;
        }
        else if (str.equals("CAMCORDER")) {
            return MediaRecorder.AudioSource.CAMCORDER;
        }
        else {
            return MediaRecorder.AudioSource.DEFAULT;
        }
    }
}
