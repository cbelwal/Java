package com.dasmic.android.videolib.ViewModel;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.dasmic.android.lib.support.Static.ZipOperations;
import com.dasmic.android.videolib.Data.DataVideoDisplay;
import com.dasmic.android.videolib.Enum.AppOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaitanya Belwal on 3/10/2018.
 */

public class ViewModelExportSupport {
    Context _context;

    public ViewModelExportSupport(Context context){
        _context = context;
    }

    //Will put all contents of allSelected in a zip files created in folder targetFolder
    public void CreateZipFile(ArrayList<DataVideoDisplay> allSelected,
                              String targetFilePath){
        ArrayList<String> allFiles = new ArrayList<>();
        for(DataVideoDisplay dfd:allSelected){
            allFiles.add(dfd.AbsoluteFilePath);
        }
        if(!ZipOperations.zip(allFiles,targetFilePath))
            throw new RuntimeException("Error in zipping contents");
    }

    public Intent getSingleFileIntent(String intentType,
                                      String filePath){
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType(intentType);
        //GetAll files

        File f = new File(filePath);
        Uri contentUri = FileProvider.getUriForFile(_context,
                AppOptions.FILE_PROVIDER_AUTHORITY,
                f);
        GrantPermissionToAll(
                contentUri,
                intent);
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(contentUri);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                uris);
        return intent;
    }


    public Intent getMultipleFileIntent(String intentType,
                                        ArrayList<DataVideoDisplay> allSelected) {
        //GetAll files
        ArrayList<File> allFiles = new ArrayList<>();

        for (DataVideoDisplay dvd : allSelected) {
            File f = new File(dvd.AbsoluteFilePath);
            allFiles.add(f);
        }
        return getMultipleFileIntentFile(intentType,allFiles);
    }

    public Intent getMultipleFileIntentFile(String intentType,
                                        ArrayList<File> allFiles) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType(intentType);
        ArrayList<Uri> uris = new ArrayList<>();
        for(File f:allFiles) {
            Uri contentUri = FileProvider.getUriForFile(_context,
                    AppOptions.FILE_PROVIDER_AUTHORITY,
                    f);
            GrantPermissionToAll(
                    contentUri,
                    intent);
            uris.add(contentUri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                uris);
        return intent;
    }

    public void GrantPermissionToAll(Uri uri, Intent intent){
        List<ResolveInfo> resInfoList = _context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            Log.i("CKIT","Package Name:"+packageName);
            _context.grantUriPermission(packageName, uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }
}
