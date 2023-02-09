package com.dasmic.android.videolib.Data;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;

import com.dasmic.android.lib.support.Static.DateOperations;

import java.io.File;

/**
 * Created by Chaitanya Belwal on 2/25/2018.
 */

public class DataVideoDisplay {
    public long Size; //in Bytes
    public long LastModifiedDate;
    public String AbsoluteFilePath;
    public boolean IsChecked;
    private String mName;
    public boolean HasReadPermission;
    public boolean HasWritePermission;
    private Drawable mThumbNail;

    public DataVideoDisplay(String name){
        mName = name;
        IsChecked=false;
    }

    public String getVideoFileFolder(){
        File f = new File(AbsoluteFilePath);
        return f.getParent(); //Returns containing folder of file
    }


    public String getName(){
        return mName;
    }

    public String getNameWithoutExtension(){
        String filenameArray[] = getName().split("\\.");
        String extension = filenameArray[0];
        return extension;
    }

    public String getExtension(){
        String filenameArray[] = getName().split("\\.");
        String extension="";
        if(filenameArray.length > 1)
            extension = filenameArray[filenameArray.length-1];
        return "." + extension;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getSecondaryValue() {
        return DateOperations.getFormattedDate(LastModifiedDate);
    }

    public String getTertiaryValue() {
        return GetSizeInKB();
    }

    private String GetSizeInKB()
    {
        double sizeKb = Size/1024;
        return String.format("%.2f", sizeKb) + " KB";
    }




    public static boolean isVideoFile(String fullFileName){
        String fileName = fullFileName.trim().toUpperCase();
        if(     fileName.endsWith("3GP") ||
                fileName.endsWith("MP4") ||
                fileName.endsWith("WEBM"))
            return true;
        else
            return false;
    }


    public boolean isGotoParentFolder(){
        try {
            if (mName.trim().equals(".."))
                return true;
            else
                return false;
        }
        catch(Exception ex){
            return false;
        }
    }


    public Drawable getThumbnail(Activity activity){
        if(mThumbNail != null) return mThumbNail;

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(AbsoluteFilePath);
            Bitmap bitmap = retriever
                    .getFrameAtTime(1, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
            mThumbNail = new BitmapDrawable(activity.getResources(), bitmap);
        }
        catch(Exception ex){
            return null;
        }
        return mThumbNail;
    }

    public String getModifiedDateFormatted(){
        return DateOperations.getFormattedDate(LastModifiedDate);
    }

    public String getSizeInKB(){
        return String.valueOf(Size/1024) + " KB";
    }
}
