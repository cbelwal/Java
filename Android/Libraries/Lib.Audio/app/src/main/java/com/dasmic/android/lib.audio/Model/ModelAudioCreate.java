package com.dasmic.android.lib.audio.Model;

import android.app.Activity;

import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.audio.Data.DataAudioDisplay;

/**
 * Created by Chaitanya Belwal on 2/25/2018.
 */

public class ModelAudioCreate {
    Activity _activity;

    public ModelAudioCreate(Activity activity){
        _activity = activity;
    }

    public void copySingleVideoFile(String sourceFilePath, String destFilePath){
        try {
            FileOperations.CopyFile(sourceFilePath, destFilePath);

        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void CreateNewFolderInCurrentFolder(String parentFolder,String newFolderName){
        FileOperations.CreateFolder(parentFolder,newFolderName);
    }

    public void RenameFile(DataAudioDisplay dvd, String newName){
        FileOperations.RenameFile(dvd.AbsoluteFilePath, newName);
    }

}
