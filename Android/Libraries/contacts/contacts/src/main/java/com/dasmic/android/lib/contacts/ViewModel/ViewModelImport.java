package com.dasmic.android.lib.contacts.ViewModel;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;
import com.dasmic.android.lib.contacts.Data.DataValuePair;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.Extension.ImportException;
import com.dasmic.android.lib.contacts.Model.ModelContactsCreate;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaitanya Belwal on 9/20/2015.
 */
public class ViewModelImport {
    Activity _activity;
    ArrayList<String> mExtensions;
    ModelContactsCreate mContactsCreate;
    public static final int IMPORT_TOTAL_COUNT=0;
    public static final int IMPORT_COUNT_UPDATE=1;
    private final Handler mHandler;

    //Need to grant permission if it needs to be shared with external applications
    private void GrantPermissionToAll(Uri uri, Intent intent){
        List<ResolveInfo> resInfoList = _activity.getPackageManager()
                .queryIntentActivities(intent,
                PackageManager.MATCH_ALL);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            _activity.grantUriPermission(packageName, uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    //File contents have '\r\n' at End of line irrespective
    //of whether it Unix/Windows
    private int ImportFromCSV(String fileContents){
        int count=0,ii=0;
        String sContact ="";

        //Split based in '\r\n'
        String [] sAllContacts=fileContents.split("\r\n");
        sendHandlerMessage(IMPORT_TOTAL_COUNT,
                sAllContacts.length);
        //Check if Valid
        if(sAllContacts.length>=1){
            if(!sAllContacts[0].toLowerCase().startsWith("name"))
                return 0;
        }
        else
            return 0;

        if(!checkFreeVersion(sAllContacts.length - 1)) return count;

        DataContactTransfer dct;
        SupportFunctions.DebugLog("Import", "VCF", "Contacts #" + sAllContacts.length);
        for(ii=1;ii<sAllContacts.length;ii++){
            sendHandlerMessage(IMPORT_COUNT_UPDATE,
                    ii);
            //SupportFunctions.DebugLog("Import","VCF","Contacts Data:"+sContact);
            try {
                sContact = sAllContacts[ii];
                if(!sContact.trim().equals("")) {
                    dct = new DataContactTransfer(0);
                    dct.setFromCSVString(sContact);
                    mContactsCreate.CreateContact_New(dct);
                    count++;
                }
            }
            catch(Exception ex){
                throw new RuntimeException(ex);
            }
        }
        return count;
    }

    private boolean checkFreeVersion(int contactCount){
        if(contactCount > ActivityOptions.FREE_VERSION_CONTACT_LIMIT &&
                ActivityOptions.isFreeVersion){
            throw new ImportException(
                    (String) _activity.getResources().getText(
                    R.string.message_free_version_selection) + " " +
                    String.valueOf(ActivityOptions.FREE_VERSION_CONTACT_LIMIT));
        }
        return true;
    }



    //File contents have '\r\n' at End of line irrespective
    //of whether it Unix/Windows
    private int ImportFromVCF(String fileContents){
        int count=0;
        //If Lower case 'begin' is present, make sure to make it upper case
        //Do not replace whole string is photo strings may also be present
        fileContents = fileContents.replace("begin:","BEGIN:");

        //Split based in BEGIN
        String [] sAllContacts=fileContents.split("BEGIN");
        sendHandlerMessage(IMPORT_TOTAL_COUNT,
                sAllContacts.length);
        //Update event
        if(!checkFreeVersion(sAllContacts.length - 1)) return count;
        SupportFunctions.DebugLog("Import", "VCF", "Contacts #" + sAllContacts.length);
        for(String sContact:sAllContacts){
            //Update Event
            sendHandlerMessage(IMPORT_COUNT_UPDATE  ,
                    count);
            SupportFunctions.DebugLog("Import", "VCF", "Contacts Data:" + sContact);
            try {
                if(!sContact.trim().equals("")) {
                    createContactFromvCard(sContact);
                    count++;
                }
            }
            catch(Exception ex){
                throw new RuntimeException(ex);
            }
        }
        return count;
    }


    public void createContactFromvCard(String vCardData)
    {
        createContactFromvCard(vCardData,false);
    }

    public void createContactFromvCard_WhatsApp(String vCardData)
    {
        createContactFromvCard(vCardData,true);
    }

    public void createContactFromvCard(String vCardData,
                                       boolean isForWhatsApp)
    {
        try {
            if(vCardData.trim().equals("")) return; //Do nothing
            DataContactTransfer dct = new DataContactTransfer(0);
            dct.setFromVCardString(vCardData);


            if(isForWhatsApp)
                mContactsCreate.CreateContact_New_WhatsApp(dct);
            else
                mContactsCreate.CreateContact_New(dct);

        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private void sendHandlerMessage(int messageIdx,
                                    int arg1){
        if(mHandler==null) return;//Dont do anything
        Message msg = mHandler.obtainMessage(
                messageIdx,arg1,-1);
        mHandler.sendMessage(msg);
    }

    //Ctor
    public ViewModelImport(Activity activity,
                           Handler handler) {
        _activity = activity;
        mHandler=handler;
        mExtensions = new ArrayList<>();
        mExtensions.add(".csv");
        mExtensions.add(".vcf");
        mContactsCreate = new ModelContactsCreate(_activity);
    }

    public void setExtensionToBackup(){
        mExtensions= new ArrayList<>();
        mExtensions.add(".vcf");
    }

    public ArrayList<DataValuePair<String,String>>
        getListofFilesInPhone(){
            return getListofSpecificFilesInPhone(mExtensions);
    }

    public ArrayList<DataValuePair<String,String>>
        getListOfBackupFilesInPhone(){
            ArrayList<DataValuePair<String,String>> allFiles
                =getListofSpecificFilesInPhone(mExtensions);
            ArrayList<DataValuePair<String,String>> newAllFiles
                =new ArrayList<>();
        //Look only for vcf files name with term back in it
        for(DataValuePair<String, String> dvp:allFiles)
        {
            String name=dvp.Value;

            if(name.contains(ActivityOptions.BACKUP_FILE_NAME_PREFIX)) {
                DataValuePair<String, String> newDvp=
                        new DataValuePair<>(dvp.Key,dvp.Value);
                newAllFiles.add(newDvp);
            }

        }

        return newAllFiles;
    }

    public ArrayList<DataValuePair<String,String>>
        getListofSpecificFilesInPhone(ArrayList<String> extensions) {
            ArrayList<DataValuePair<String,String>> allMaps =
                new ArrayList<DataValuePair<String,String>>();
        DataValuePair<String,String> dvPair;

        ArrayList<File> allFiles =  //Do internal storage folder
                FileOperations.getAllFileObjectsInFolder(
                        FileOperations.getInternalStorageFolder(_activity),
                        extensions);
        allFiles.addAll( //Add external storage folder
                FileOperations.getAllFileObjectsInFolder(
                        FileOperations.getExternalStorageFolder(),
                        extensions));
        for(File f:allFiles){
            if(f!=null) {
                dvPair = new DataValuePair<String, String>(f.getPath(), f.getName());
                allMaps.add(dvPair);
            }
        }
        return allMaps;
    }

    public boolean isValidFileName(String fileName){
        if(fileName==null) return false;
        for(String ex: mExtensions) {
            if(fileName.endsWith(ex))
                return true;
        }
        return false;
    }

    //External sources like Google Drive
    private File getFromGoogleDrive(Uri uri, String fileName){
        File f;
        String internalFolderName =_activity.getString(
                R.string.app_name_small).toString() + "_"+ _activity.getString(
                R.string.folder_name_internal).toString();
        File folder = FileOperations.CreateInternalFolder(_activity,
                internalFolderName);

        try {
            File tFile = new File(folder,fileName);
            FileOperations.CopyFile(_activity.getContentResolver().openInputStream(uri),
                    tFile);
            if(tFile !=null)
                f = FileOperations.getFileObjectForRead(tFile.getPath());
            else
                f=null;
        }
        catch(Exception ex) {
            SupportFunctions.DebugLog("VMImport", "ImportFrom", "Error:" + ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
        return f;
    }

    //Returns count
    public int ImportFrom(Uri uri, String fileName){
        if(uri==null) return 0;
        File f=null;
        try {
            f = FileOperations.getFileObjectForRead(uri.getPath());
        }
        catch(Exception ex)
        {
            //Some exception happened keep f as null
        }
        if(f==null){ //Try using alternate way. Needed if from Google Drive
            f=getFromGoogleDrive(uri,fileName);
        }
        if(f==null) return 0;

        String fileContents = FileOperations.ReadFromFileWindowsFormat(f);
        //Dont do this as all files whether UNIX or windows have
        //\r\n at EOL in the Read file command
        // Replace all '\r' with '' to make is compatible
        // with unix systems
        //fileContents=fileContents.replace("\r","").trim();
        Log.i("CKIT","File loaded:"+fileContents);

        if(fileName.endsWith(mExtensions.get(0)))
        {
            //CSV Import
            return ImportFromCSV(fileContents);
        }
        else
        {   //VCARD Input
            return ImportFromVCF(fileContents);
        }
    }

    //Backward compatibility
    //Change to backup file
    public boolean RestoreAllContactsFromDefaultBackupFile(){
        String fileName = ActivityOptions.BACKUP_FILE_NAME;
        String folderName = ActivityOptions.BACKUP_FOLDER_NAME;
        File fileBackup = new File(FileOperations.getExternalStorageFolder() + "/" +
                folderName + "/" + fileName);
        return RestoreAllContactsFromBackupFile(fileBackup);
    }

    public boolean
        RestoreAllContactsFromSpecifiedBackupFile(Uri uri, String fileName) {
            File fileBackup =
                    FileOperations.getFileObjectForRead(uri.getPath());
            if(fileBackup==null){
                fileBackup=getFromGoogleDrive(uri,fileName);
            }
        return RestoreAllContactsFromBackupFile(fileBackup);
    }

    public boolean RestoreAllContactsFromBackupFile(File fileBackup) {
        if(fileBackup==null) return false;
        try {
            if (fileBackup.exists()) {
                Intent paramView = new Intent();

                paramView.setAction("android.intent.action.VIEW");
                String str = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        ((File) fileBackup).getName().
                                substring(((File) fileBackup).getName().indexOf(".") + 1));
                Uri contentUri = FileProvider.getUriForFile(_activity,
                        ActivityOptions.FILE_PROVIDER_AUTHORITY,
                        fileBackup);
                //paramView.setDataAndType(Uri.fromFile((File) fileBackup), str);
                paramView.setDataAndType(contentUri, str);

                //Insure read permission to External intent
                GrantPermissionToAll(contentUri,paramView);
                _activity.startActivityForResult(paramView,
                        ActivityOptions.CONTACTS_RESTORE_ACTIVITY_REQUEST);
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }
}
