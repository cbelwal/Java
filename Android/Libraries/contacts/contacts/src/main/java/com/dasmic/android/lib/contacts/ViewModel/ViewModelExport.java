package com.dasmic.android.lib.contacts.ViewModel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.Model.ModelContactsRead;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.FileOperations;


import java.util.ArrayList;


/**
 * Created by Chaitanya Belwal on 9/20/2015.
 */
public class ViewModelExport {
    private Context _context;
    private ModelContactsRead _mRead;
    //private ArrayList<String> _writtenFiles;
    private String _fileNamePrefix;
    private String _externalFolderName;
    private final String BACKUP_FILE_NAME_WITHDATE=
            ActivityOptions.BACKUP_FILE_NAME_PREFIX;

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

    private void WriteFileInExternalStorage(String fileName, String contents){
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

    private void WriteToFile(String fileName,
                             String combinedValue,
                             boolean externalStorage){
        if(externalStorage)
            WriteFileInExternalStorage(fileName, combinedValue);
        else
            WriteFileInInternalStorage(fileName, combinedValue);
    }

    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String StoreContactsInSingleVCard40File(ArrayList<Long> contactIds,
                                                   int valueFlag,
                                                   boolean externalStorage){
        cleanUpInternalFolder();
        StringBuilder combinedValue =
                new StringBuilder("");
        String fileName;

        ArrayList<DataContactTransfer> dcTransfers =
                getDataContactTransfers(contactIds,valueFlag);

        sendHandlerMessage(HANDLER_FILEWRITE_TOTAL_COUNT,
                dcTransfers.size());
        int idx=1;
        for(DataContactTransfer dct:dcTransfers){
            sendHandlerMessage(HANDLER_CONTACTNAME_UPDATE,
                    dct.getVCardName());
            combinedValue.append(dct.getVCardString());
            sendHandlerMessage(HANDLER_FILEWRITE_COUNT_UPDATE,
                    idx++);
        }

        //Add new event

        //Now store Combined Value in File
        fileName = _fileNamePrefix + String.valueOf(dcTransfers.size()) +  "_Contacts_v40.vcf";
        sendHandlerMessage(HANDLER_FILEWRITE_COMBINED,
                fileName);
        WriteToFile(fileName, combinedValue.toString(), externalStorage);
        Log.i("CKIT", "Export::getContactsAsVCard");
        return getCombinedFolderName(externalStorage) + "/" + fileName;
    }

    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String StoreContactsInSingleVCard21File(ArrayList<Long> contactIds,
                                                 int valueFlag,
                                                 boolean externalStorage){
        cleanUpInternalFolder();
        StringBuilder combinedValue = new StringBuilder("");
        String fileName;

        ArrayList<DataContactTransfer> dcTransfers =
                getDataContactTransfers(contactIds,valueFlag);

        ModelContactsRead mcr = new ModelContactsRead(_context);

        sendHandlerMessage(HANDLER_FILEWRITE_TOTAL_COUNT,
                dcTransfers.size());
        int idx=1;
        for(DataContactTransfer dct:dcTransfers){
            sendHandlerMessage(HANDLER_CONTACTNAME_UPDATE,
                    dct.getVCardName());
            combinedValue.append(mcr.getVCFDataSingleContact(dct.getContactId()));
            sendHandlerMessage(HANDLER_FILEWRITE_COUNT_UPDATE,
                    idx++);
        }

        //Now store Combined Value in File
        fileName = _fileNamePrefix + String.valueOf(dcTransfers.size()) +  "_Contacts_v21.vcf";
        sendHandlerMessage(HANDLER_FILEWRITE_COMBINED,
                fileName);
        WriteToFile(fileName, combinedValue.toString(), externalStorage);
        Log.i("CKIT", "Export::getContactsAsVCard");
        return getCombinedFolderName(externalStorage) + "/" + fileName;
    }

    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String StoreContactsInSingleCSVFile(ArrayList<Long> contactIds,
                                                              int valueFlag,
                                                              boolean externalStorage){
        cleanUpInternalFolder();
        StringBuilder combinedValue = new StringBuilder("");
        String fileName;

        ArrayList<DataContactTransfer> dcTransfers =
                getDataContactTransfers(contactIds,valueFlag);

        if(dcTransfers.size()==0) return combinedValue.toString();
        combinedValue.append(dcTransfers.get(0).getCSVHeader(_context)); //Add CSV Header Once

        sendHandlerMessage(HANDLER_FILEWRITE_TOTAL_COUNT,
                dcTransfers.size());
        int idx=1;

        for(DataContactTransfer dct:dcTransfers){
            sendHandlerMessage(HANDLER_CONTACTNAME_UPDATE,
                    dct.getVCardName());
            combinedValue.append(dct.getCSVString());
            sendHandlerMessage(HANDLER_FILEWRITE_COUNT_UPDATE,
                    idx++);
        }

        //Now store Combined Value in File
        fileName = _fileNamePrefix + String.valueOf(dcTransfers.size())
                                    +  "_Contacts.csv";
        sendHandlerMessage(HANDLER_FILEWRITE_COMBINED,
                fileName);
        WriteToFile(fileName,
                combinedValue.toString(),externalStorage);

        Log.i("CKIT", "Export::getContactsAsCSV");
        return getCombinedFolderName(externalStorage) + "/" + fileName;
    }

    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String StoreContactsAsFormattedStringFile(ArrayList<Long> contactIds,
                                                     int valueFlag,
                                                      boolean externalStorage){

        String content = getContactsAsFormattedString(contactIds,valueFlag);

        //Now store Combined Value in File
        String fileName = _fileNamePrefix + contactIds.size() +
                "_Contacts.txt";

        //External Storage
        //WriteFileInExternalStorage(fileName, content);
        WriteToFile(fileName, content, externalStorage);
        return getCombinedFolderName(externalStorage) + "/" + fileName;
    }


    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String getContactsAsFormattedString(ArrayList<Long> contactIds,
                                               int valueFlag){
        String combinedValue = "";

        ArrayList<DataContactTransfer> dcTransfers =
                getDataContactTransfers(contactIds,valueFlag);

        sendHandlerMessage(HANDLER_FILEWRITE_TOTAL_COUNT,
                dcTransfers.size());

        int idx=1;
        for(DataContactTransfer dct:dcTransfers){
            combinedValue=combinedValue + dct.getFormattedString(_context) + "\n";
            sendHandlerMessage(HANDLER_FILEWRITE_COUNT_UPDATE,
                    idx++);
        }

        Log.i("CKIT", "Export::getContactsAsFormattedString");
        return combinedValue;
    }


    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String StoreContactsInIndividualVCard40Files(ArrayList<Long> contactIds,
                                                        int valueFlag,
                                                        boolean externalStorage){
        cleanUpInternalFolder();
        String fileName = "";
        ArrayList<DataContactTransfer> dcTransfers =
                getDataContactTransfers(contactIds,valueFlag);

        sendHandlerMessage(HANDLER_FILEWRITE_TOTAL_COUNT,
                dcTransfers.size());

        int idx=1;
        for(DataContactTransfer dct:dcTransfers){
            sendHandlerMessage(HANDLER_CONTACTNAME_UPDATE,
                    dct.getVCardName());
            fileName=dct.getVCardName();
            //Remove Spaces And Special Characters
            fileName=fileName.replaceAll("[^a-zA-Z]+", " ");
            fileName=fileName.replaceAll("\\s", "");
            if(fileName=="")
                fileName=String.valueOf(dct.getContactId());
            fileName = fileName + "_v40.vcf";

            WriteToFile(fileName, dct.getVCardString(), externalStorage);
            sendHandlerMessage(HANDLER_FILEWRITE_COUNT_UPDATE,
                    idx++);
        }

        Log.i("CKIT", "Export::getContactsAsFormattedString");
        return getCombinedFolderName(externalStorage);
    }

    //valueFlag = 001 = 1 =>  Phone Numbers
    //valueFlag = 010 = 2 =>  Email
    //valueFlag = 100 = 4 =>  Address
    public String StoreContactsInIndividualVCard21Files(ArrayList<Long> contactIds,
                                                        int valueFlag,
                                                        boolean externalStorage){
        cleanUpInternalFolder();
        String fileName = "";

        ArrayList<DataContactTransfer> dcTransfers =
                getDataContactTransfers(contactIds,valueFlag);

        ModelContactsRead mcr = new ModelContactsRead(_context);
        String vCardString="";

        sendHandlerMessage(HANDLER_FILEWRITE_TOTAL_COUNT,
                dcTransfers.size());

        int idx=1;
        for(DataContactTransfer dct:dcTransfers){
            sendHandlerMessage(HANDLER_CONTACTNAME_UPDATE,
                    dct.getVCardName());
            fileName=dct.getVCardName();
            //Remove Spaces And Special Characters
            fileName=fileName.replaceAll("[^a-zA-Z]+", " ");
            fileName=fileName.replaceAll("\\s", "");
            if(fileName=="")
                fileName=String.valueOf(dct.getContactId());
            fileName = fileName + "_v21.vcf";

            vCardString = mcr.getVCFDataSingleContact(dct.getContactId());
            //Get vCard String
            WriteToFile(fileName, vCardString, externalStorage);
            sendHandlerMessage(HANDLER_FILEWRITE_COUNT_UPDATE,
                    idx++);
        }

        Log.i("CKIT", "Export::getContactsAsFormattedString");
        return getCombinedFolderName(externalStorage);
    }

    public  ArrayList<DataContactTransfer>
        getDataContactTransfers(ArrayList<Long> contactIds,
                            int valueFlag){

        ArrayList<DataContactTransfer> values = new ArrayList<>();

        sendHandlerMessage(HANDLER_DATAGEN_TOTAL_COUNT,
                contactIds.size());

        for(int idx=0;idx<contactIds.size();idx++){
            DataContactTransfer singleContact =
                    _mRead.getTransferData_Single(contactIds.get(idx),
                    valueFlag,false);
            values.add(singleContact);
            sendHandlerMessage(HANDLER_DATAGEN_COUNT_UPDATE,idx);
        }
        return values;
    }

    public  ArrayList<DataContactTransfer>
    getDataContactTransfers_WhatsApp(ArrayList<Long> contactIds,
                            int valueFlag){
        return _mRead.getContactsForTransfer_WhatsApp(
                contactIds,valueFlag);
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

    private void initialize(Context context,
                            String appName){
        _context = context;
        _mRead = new ModelContactsRead(_context);
        //_writtenFiles = new ArrayList<>();
        InternalFolderName =appName+"_"+ _context.getResources().getText(
                R.string.folder_name_internal).toString();
        _externalFolderName = appName +
                "_" +
                _context.getResources().getText(R.string.folder_name_external).toString();
        _fileNamePrefix=appName;
    }

    //Ctor
    public ViewModelExport(Context context,
                           String appName){
        initialize(context,appName);
        mHandler=null;
    }

    public ViewModelExport(Context context,
                           String appName,
                           Handler handler){
        initialize(context,appName);
        mHandler=handler;
    }

    //Will store in external folder name
    public String StoreAllContactsInBackupFile_InternalWithDate(
            boolean isFreeVersion){
        cleanUpInternalFolder();
        StoreAllContactsInBackupFile_Folder(isFreeVersion,
                InternalFolderName,false,true);
        //return full folder name
        return (getCombinedFolderName(false)); //Includes internal folder
    }

    //Store in external storage, no date -> For use when single file is uses
    //and no file selection is offered
    public String StoreAllContactsInBackupFile_ExternalNoDate(
            boolean isFreeVersion){
        String folderName=ActivityOptions.BACKUP_FOLDER_NAME;
        String fileName= StoreAllContactsInBackupFile_Folder(
                isFreeVersion,folderName,
                true,false);
        return (folderName+"/"+fileName);
    }

    public String StoreAllContactsInBackupFile_ExternalWithDate(boolean isFreeVersion){
        String folderName=ActivityOptions.BACKUP_FOLDER_NAME;
        String fileName = StoreAllContactsInBackupFile_Folder(isFreeVersion,
                folderName,true,true);

        return (folderName+"/"+fileName);
    }

    private String StoreAllContactsInBackupFile_Folder(boolean isFreeVersion,
                                                       String folderName, boolean externalStorage,
                                                       boolean useDate){
        String combinedValue;
        if(isFreeVersion)
            combinedValue=_mRead.getVCFDataAllContacts(
                    ActivityOptions.FREE_VERSION_CONTACT_LIMIT);
        else
            combinedValue=_mRead.getVCFDataAllContacts(0);

        String fileName;
        if(useDate)
            fileName = BACKUP_FILE_NAME_WITHDATE +"_"+
                    DateOperations.getFormattedDateForFileName(
                            DateOperations.getCurrentDate()) + ".vcf";
        else
            fileName = ActivityOptions.BACKUP_FILE_NAME;

        if(externalStorage)
            FileOperations.WriteFileToExternalStorage(_context, fileName,
                folderName,combinedValue);
        else
            FileOperations.WriteFileToInternalStorage(_context, fileName,
                    folderName,combinedValue);

        return fileName;
    }
}
