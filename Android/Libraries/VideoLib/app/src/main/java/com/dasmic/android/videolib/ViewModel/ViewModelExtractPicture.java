package com.dasmic.android.videolib.ViewModel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.videolib.Data.DataVideoDisplay;
import com.dasmic.android.videolib.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/10/2018.
 */

public class ViewModelExtractPicture {
    Activity _activity;
    private Handler mHandler;
    public static final int EXTRACT_COUNT_UPDATE=0;

    public ViewModelExtractPicture(Activity activity){
        _activity = activity;
        mHandler=null;
    }

    public ArrayList<String> ExtractPicturesFromVideos(DataVideoDisplay dvd, int noOfPictures,
                                          int timeStart,
                                          int timePeriod, boolean useExternalStorage,
                                          Handler handler){

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(dvd.AbsoluteFilePath);
        mHandler = handler;
        ArrayList<String> allPictureFiles = new ArrayList<>();

        String saveFolder;
        if(useExternalStorage) {
            File f = FileOperations.CreateExternalFolder(_activity, _activity.getString(R.string.app_name_small));
            saveFolder = f.getAbsolutePath();
        }
        else{
            File f = FileOperations.CreateInternalFolder(_activity, _activity.getString(R.string.app_name_small));
            saveFolder = f.getAbsolutePath();
        }

        String baseFileName = dvd.getNameWithoutExtension();

        final long microSConv = 1000000;//Convert seconds to microseconds
        long timeMs = timeStart * microSConv; //timeStart is in seconds, convert to micro-seconds
        int timeS = timeStart; //Used for naming files only
        for(int idx=0;idx<noOfPictures;idx++) {

            Bitmap bmp = retriever
                    .getFrameAtTime(timeMs, MediaMetadataRetriever.OPTION_CLOSEST);
            timeMs+=timePeriod*microSConv;
            timeS+=timePeriod;
            String fileName = baseFileName + "_" + String.valueOf(idx+1) + "_" +String.valueOf(timeS) + ".png";
            fileName = saveFolder + "/" + fileName;
            allPictureFiles.add(fileName);
            //Save into internal folder
            saveBitMap(bmp,fileName);
            sendHandlerMessage(EXTRACT_COUNT_UPDATE,idx+1);
        }
        return allPictureFiles;
    }


    private void saveBitMap(Bitmap bmp, String fullFileName){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fullFileName);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
               throw new RuntimeException(ex);
            }
        }
    }

    public int getLengthOfVideos(String fullFilePath){
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
