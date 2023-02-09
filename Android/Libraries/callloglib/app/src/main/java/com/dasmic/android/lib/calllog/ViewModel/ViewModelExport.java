package com.dasmic.android.lib.calllog.ViewModel;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Enum.AppOptions;
import com.dasmic.android.lib.calllog.Model.ModelCallLogRead;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.FileOperations;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/18/2017.
 */

public class ViewModelExport {
    private Activity _context;
    private String _fileNamePrefix;
    private String _externalFolderName;
    private final String BACKUP_FILE_NAME_WITHDATE=
            AppOptions.BACKUP_FILE_NAME_PREFIX;

    public String InternalFolderName;

    public static final int HANDLER_DATAGEN_TOTAL_COUNT =0;
    public static final int HANDLER_DATAGEN_COUNT_UPDATE =1;
    public static final int HANDLER_FILEWRITE_TOTAL_COUNT =3;
    public static final int HANDLER_FILEWRITE_COUNT_UPDATE =4;
    public static final int HANDLER_CONTACTNAME_UPDATE =5;
    public static final int HANDLER_FILEWRITE_COMBINED =7;
    private final Handler mHandler;


    public void cleanUpInternalFolder(){
        FileOperations.RemoveAllFilesInFolder(
                getCombinedFolderName(false));
    }

    private void WriteFileInInternalStorage(String fileName, String contents){
        FileOperations.WriteFileToInternalStorage(_context, fileName,
                InternalFolderName, contents);
    }

    private void writeFileInExternalStorage(String fileName, String contents){
        FileOperations.WriteFileToExternalStorage(_context, fileName,
                _externalFolderName, contents);
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

    private void writeToFile(String fileName,
                             String combinedValue,
                             boolean externalStorage){
        if(externalStorage)
            writeFileInExternalStorage(fileName, combinedValue);
        else
            WriteFileInInternalStorage(fileName, combinedValue);
    }

    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String storeContactsInSingleCLMFile(ArrayList<DataCallLogDisplay>
                                                       clTransfer,
                                               int valueFlag,
                                               boolean externalStorage){
        cleanUpInternalFolder();
        StringBuilder combinedValue =
                new StringBuilder("");
        String fileName;

        sendHandlerMessage(HANDLER_FILEWRITE_TOTAL_COUNT,
                clTransfer.size());
        int idx=1;
        for(DataCallLogDisplay dct:clTransfer){
            sendHandlerMessage(HANDLER_CONTACTNAME_UPDATE,
                    dct.getUniqueName());
            combinedValue.append(dct.getCLMString());
            sendHandlerMessage(HANDLER_FILEWRITE_COUNT_UPDATE,
                    idx++);
        }

        //Add new event

        //Now store Combined Value in File
        fileName = _fileNamePrefix + "_" + String.valueOf(clTransfer.size()) +  "_v10.clm";
        sendHandlerMessage(HANDLER_FILEWRITE_COMBINED,
                fileName);
        writeToFile(fileName, combinedValue.toString(), externalStorage);
        Log.i("CKIT", "Export::getContactsAsVCard");
        return getCombinedFolderName(externalStorage) + "/" + fileName;
    }


    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String storeContactsAsFormattedStringFile(ArrayList<DataCallLogDisplay> clTransfer,
                                                     int valueFlag,
                                                     boolean externalStorage){

        String content = getContactsAsFormattedString(clTransfer,valueFlag);

        //Now store Combined Value in File
        String fileName = _fileNamePrefix + clTransfer.size() +
                "_Contacts.txt";

        //External Storage
        //writeFileInExternalStorage(fileName, content);
        writeToFile(fileName, content, externalStorage);
        return getCombinedFolderName(externalStorage) + "/" + fileName;
    }


    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String getContactsAsFormattedString(ArrayList<DataCallLogDisplay>
                                                       clTransfer, int valueFlag){
        String combinedValue = "";
        sendHandlerMessage(HANDLER_FILEWRITE_TOTAL_COUNT,
                clTransfer.size());

        int idx=1;
        for(DataCallLogDisplay dct:clTransfer){
            combinedValue=combinedValue + dct.getFormattedString(_context) + "\n";
            sendHandlerMessage(HANDLER_FILEWRITE_COUNT_UPDATE,
                    idx++);
        }

        //Add Header
        combinedValue = _context.getString(R.string.export_generated_header) +"\r\n\r\n" +
                            combinedValue;
        //Add footer
        combinedValue = combinedValue + "\r\n" +
                           _context.getString(R.string.export_generated_footer);
        Log.i("CKIT", "Export::getContactsAsFormattedString");
        return combinedValue;
    }

    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String storeContactsInIndividualCLMFiles(ArrayList<DataCallLogDisplay> clTransfer,
                                                    int valueFlag,
                                                    boolean externalStorage){
        cleanUpInternalFolder();
        String fileName = "";

        sendHandlerMessage(HANDLER_FILEWRITE_TOTAL_COUNT,
                clTransfer.size());

        int idx=1;
        for(DataCallLogDisplay dct:clTransfer){
            sendHandlerMessage(HANDLER_CONTACTNAME_UPDATE,
                    dct.getUniqueName());
            fileName=dct.getUniqueName();
            //Remove Spaces And Special Characters
            //fileName=fileName.replaceAll("[^a-zA-Z]+", " ");
            fileName=fileName.replaceAll(":", "");
            fileName=fileName.replaceAll("\\s", "");
            if(fileName=="")
                fileName=String.valueOf(dct.getId());
            fileName = fileName + "_v10.clm";

            writeToFile(fileName, dct.getCLMString(), externalStorage);
            sendHandlerMessage(HANDLER_FILEWRITE_COUNT_UPDATE,
                    idx++);
        }

        Log.i("CKIT", "Export::getContactsAsFormattedString");
        return getCombinedFolderName(externalStorage);
    }

    private void sendHandlerMessage(int messageIdx,
                                    int arg1){
        if(mHandler==null) return;//Dont do anything
        Message msg = mHandler.obtainMessage(
                messageIdx,arg1,-1);
        mHandler.sendMessage(msg);
    }

    private void sendHandlerMessage(int messageIdx,
                                    String arg1){
        if(mHandler==null) return;//Dont do anything
        Message msg = mHandler.obtainMessage(
                messageIdx,arg1);
        mHandler.sendMessage(msg);
    }

    private void initialize(Activity activity,
                            String appFolder){
        _context = activity;
        //_writtenFiles = new ArrayList<>();
        InternalFolderName =appFolder;//+"_"+ _context.getResources().getText(
                //R.string.folder_name_internal).toString();
        _externalFolderName = appFolder; //+
                //"_" +
                //_context.getResources().getText(R.string.folder_name_external).toString();
        _fileNamePrefix=appFolder;
    }

    //Ctor
    public ViewModelExport(Activity  activity,
                           String appFolder){
        initialize(activity,appFolder);
        mHandler=null;
    }

    public ViewModelExport(Activity activity,
                           String appName,
                           Handler handler){
        initialize(activity,appName);
        mHandler=handler;
    }

    //Will store in external folder name
    public String storeAllContactsInBackupFile_InternalWithDate(
            boolean isFreeVersion){
        cleanUpInternalFolder();
        storeAllContactsInBackupFile_Folder(isFreeVersion,
                InternalFolderName,false,true);
        //return full folder name
        return (getCombinedFolderName(false)); //Includes internal folder
    }

    //Store in external storage, no date -> For use when single file is used
    //and no file selection is offered
    public String storeAllContactsInBackupFile_ExternalNoDate(
            boolean isFreeVersion){
        String folderName=AppOptions.BACKUP_FOLDER_NAME;
        String fileName= storeAllContactsInBackupFile_Folder(
                isFreeVersion,folderName,
                true,false);
        return (folderName+"/"+fileName);
    }

    public String storeAllContactsInBackupFile_ExternalWithDate(boolean isFreeVersion){
        String folderName=AppOptions.BACKUP_FOLDER_NAME;
        String fileName = storeAllContactsInBackupFile_Folder(isFreeVersion,
                folderName,true,true);

        return (folderName+"/"+fileName);
    }

    private String storeAllContactsInBackupFile_Folder(boolean isFreeVersion,
                                                       String folderName,
                                                       boolean externalStorage,
                                                       boolean useDate){
        ModelCallLogRead mcRead = new ModelCallLogRead(_context,null);
        String combinedValue;
        if(isFreeVersion)
            combinedValue=mcRead.getCLMDataAllLogs(
                    AppOptions.FREE_VERSION_LIMIT);
        else
            combinedValue=mcRead.getCLMDataAllLogs(0);

        String fileName;
        if(useDate)
            fileName = BACKUP_FILE_NAME_WITHDATE +"_"+
                    DateOperations.getFormattedDateForFileName(
                            DateOperations.getCurrentDate()) + ".clm";
        else
            fileName = AppOptions.BACKUP_FILE_NAME;

        if(externalStorage)
            FileOperations.WriteFileToExternalStorage(_context, fileName,
                    folderName,combinedValue);
        else
            FileOperations.WriteFileToInternalStorage(_context, fileName,
                    folderName,combinedValue);

        return "";//fileName;
    }
}
