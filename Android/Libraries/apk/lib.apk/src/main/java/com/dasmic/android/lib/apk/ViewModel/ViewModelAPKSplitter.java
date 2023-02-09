package com.dasmic.android.lib.apk.ViewModel;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Data.DataPackageSplit;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.ZipOperations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/2/2017.
 */

public class ViewModelAPKSplitter {
    Activity _context;

    public ViewModelAPKSplitter(Activity context){
        _context=context;
    }

    //Takes the original path of the APK and splits extracts the resources
    //Returns the path to folder where contents of APK are extracted
    public String SplitAPK(DataPackageDisplay dpd)
    {
        //Clean Internal folder for folder using unzipped files and APK files
        FileOperations.RemoveAllContentsInFolder(FileOperations.getInternalStorageFolder(_context).getAbsolutePath());
        //Create the folder to copy APK file
        File internalFolder  = FileOperations.CreateInternalFolder(_context,"Temp");
        try {
            //Copy APK file to temp folder
            FileOperations.CopyFileToFolder(dpd.getPackageDir(),
                            internalFolder.getAbsolutePath());
            //Extract zip contents to a new folder
            File f = new File(dpd.getPackageDir());
            String apkFileName = f.getName();
            File zipToFolder  = FileOperations.CreateInternalFolder(_context,dpd.getPackageName());


            ZipOperations.unZip(internalFolder.getAbsolutePath() + "/" + apkFileName,
                    zipToFolder.getAbsolutePath());
            return zipToFolder.getAbsolutePath();
        }
        catch(IOException ex){

        }
        return "";

    }


}
