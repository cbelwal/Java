package com.dasmic.android.lib.apk.ViewModel;

import android.content.Context;
import android.util.Log;


import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.R;
import com.dasmic.android.lib.support.Static.FileOperations;


import java.util.ArrayList;


/**
 * Created by Chaitanya Belwal on 9/20/2015.
 */
public class ViewModelExport {
    private Context _context;
    private String _externalFolderName;
    public String InternalFolderName;

    private void DeleteWrittenFiles(){
        FileOperations.RemoveAllFilesInFolder(
                getCombinedFolderName(false));
    }

    private void CopyFileToInternalStorage(String fileName){
        FileOperations.CopyFileToInternalStorage(_context, fileName,
                InternalFolderName);
    }

    private void CopyFileToExternalStorage(String sourceFileName,
                                           String targetFileName){
        FileOperations.CopyFileToExternalStorage(_context,
                                                    sourceFileName,
                                                    targetFileName,
                                            _externalFolderName);
    }

    private void CopyFileToInternalStorage(String sourceFileName,
                                           String targetFileName){
        FileOperations.CopyFileToInternalStorage(_context,
                                sourceFileName, targetFileName,
                InternalFolderName);
    }

    private void CopyFileToExternalStorage(String fileName){
        FileOperations.CopyFileToExternalStorage(_context, fileName,
                _externalFolderName);
    }

    private String getCombinedFolderName(boolean externalStorage){
        if(!externalStorage) {
            return FileOperations.getInternalStorageFolderString(
                    _context) + "/" + InternalFolderName;
        }
        else {
            return  _externalFolderName; //Only return folder name
        }
    }

    private void CopyFile(String sourceFileName,
                          String targetFileName,
                          boolean externalStorage){
        if(externalStorage)
            CopyFileToExternalStorage(sourceFileName,targetFileName);
        else
            CopyFileToInternalStorage(sourceFileName,targetFileName);
    }



    public String StorePackagesInSingleFolder(
            ArrayList<DataPackageDisplay> allDpd,
                        boolean externalStorage){
        DeleteWrittenFiles();
        String fileName = "";

        for(DataPackageDisplay dpd:allDpd){
            String targetFileName = dpd.getPackageName() + ".apk";
            CopyFile(dpd.getPackageDir(),targetFileName,
                     externalStorage);
        }

        Log.i("CKIT", "Export::getContactsAsFormattedString");
        return getCombinedFolderName(externalStorage);
    }

    //Ctor
    public ViewModelExport(Context context){
        _context = context;

        //_writtenFiles = new ArrayList<>();
        InternalFolderName =_context.getResources().getText(
                R.string.folder_name_internal).toString();
        _externalFolderName = _context.getResources().getText(
                R.string.folder_name_external).toString();
    }

}
