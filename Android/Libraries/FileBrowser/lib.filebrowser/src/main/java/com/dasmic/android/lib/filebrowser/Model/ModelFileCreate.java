package com.dasmic.android.lib.filebrowser.Model;

import android.content.Context;

import com.dasmic.android.lib.filebrowser.Data.DataFileDisplay;
import com.dasmic.android.lib.support.Static.FileOperations;

/**
 * Created by Chaitanya Belwal on 2/11/2018.
 */

public class ModelFileCreate {
    Context _context;

    public ModelFileCreate(Context context){
        _context=context;
    }

    public boolean copySingleFile(String sourceFilePath, String destFilePath){
        try {
            FileOperations.CopyFile(sourceFilePath, destFilePath);
            return true;
        }
        catch(Exception ex){
            return false;
        }
    }

    public void RenameFile(DataFileDisplay dfd, String newName){
        FileOperations.RenameFile(dfd.AbsoluteFilePath, newName);
    }

    public void CreateNewFolderInCurrentFolder(String parentFolder,String newFolderName){
        FileOperations.CreateFolder(parentFolder,newFolderName);
    }
}
