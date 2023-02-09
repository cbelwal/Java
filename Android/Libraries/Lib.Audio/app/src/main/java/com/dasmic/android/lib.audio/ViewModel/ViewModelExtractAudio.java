package com.dasmic.android.lib.audio.ViewModel;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.dasmic.android.lib.audio.Model.SoundFile;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.audio.Data.DataAudioDisplay;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.libaudio.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Chaitanya Belwal on 3/10/2018.
 */

public class ViewModelExtractAudio {
    Activity _activity;
    private Handler mHandler;
    public static final int EXTRACT_COUNT_UPDATE=0;
    public static final int EXTRACT_COUNT_ERROR=1;
    public static final int EXTRACT_COUNT_REDUCED_DURATION=2;

    public ViewModelExtractAudio(Activity activity){
        _activity = activity;
        mHandler=null;
    }

    public ArrayList<String> ExtractSmallAudioFromLargeAudio(DataAudioDisplay dad,
                                                             int totalAudioDurationSec,
                                                             int timeStart,
                                                             int timeAudioDuration,
                                                             boolean useExternalStorage,
                                                             Handler handler){

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(dad.AbsoluteFilePath);
        mHandler = handler;
        ArrayList<String> allAudioFiles = new ArrayList<>();

        String saveFolder;
        if(useExternalStorage) {
            File f = FileOperations.CreateExternalFolder(_activity, _activity.getString(R.string.app_name_small));
            saveFolder = f.getAbsolutePath();
        }
        else{
            File f = FileOperations.CreateInternalFolder(_activity, _activity.getString(R.string.app_name_small));
            saveFolder = f.getAbsolutePath();
        }

        String baseFileName = dad.getNameWithoutExtension();

        int timeS = timeStart; //Used for naming files only

        //Init the Sound object
        SoundFile soundFile=null;
        try {
            soundFile = SoundFile.create(dad.AbsoluteFilePath, null);
        }
        catch(Exception ex){
            SupportFunctions.DebugLog("SplitSampleMPAFileText",
                    "splitSampleM4a","Error");
        }


        for(int timeInSec=timeStart;
            timeInSec<=totalAudioDurationSec;timeInSec+=timeAudioDuration) {
            String destFileName = baseFileName + "_" + String.valueOf(timeInSec) +
                    "_" +String.valueOf(timeS) + ".M4A"; //Output is always a M4A file
            destFileName = saveFolder + "/" + destFileName;

            try {
                float startTime=timeInSec;
                float endTime = startTime+timeAudioDuration;
                if(endTime > totalAudioDurationSec) {
                    endTime = startTime + (totalAudioDurationSec - startTime);
                    sendHandlerMessage(EXTRACT_COUNT_REDUCED_DURATION,timeInSec);
                }
                File fOut = new File(destFileName);
                soundFile.WriteFile(fOut, startTime, endTime);
                sendHandlerMessage(EXTRACT_COUNT_UPDATE,timeInSec);
            }
            catch(Exception ex){
                sendHandlerMessage(EXTRACT_COUNT_ERROR,timeInSec);
            }
            allAudioFiles.add(destFileName);

        }
        return allAudioFiles;
    }

    public int getLengthOfAudio(String fullFilePath){
        File f = new File(fullFilePath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(_activity, Uri.fromFile(f));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time );
        retriever.release();
        return (int)timeInMillisec/1000;
    }

    private void sendHandlerMessage(int messageIdx,
                                    int arg1){
        if(mHandler==null) return;
        Message msg = mHandler.obtainMessage(
                messageIdx,arg1,-1);

        mHandler.sendMessage(msg);
    }
}
