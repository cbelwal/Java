package com.dasmic.android.vcardsplitter.ViewModel;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;

import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Chaitanya Belwal on 10/9/2016.
 */
public class ViewModelSplit {
    private Uri _fileUri;
    private Activity _activity;

    public static final int SPLIT_FILE_COUNT=0;
    public static final int SPLIT_TOTAL_COUNT=1;
    private final Handler mHandler;


    private void sendHandlerMessage(int messageIdx,
                                    int arg1){
        if(mHandler==null) return;//Dont do anything
        Message msg = mHandler.obtainMessage(
                messageIdx,arg1,-1);
        mHandler.sendMessage(msg);
    }

    public ViewModelSplit(Activity activity,
                          Uri uri,Handler handler) {
        _activity=activity;
        _fileUri=uri;
        mHandler=handler;
    }

    public Uri getFileUri(){
        return _fileUri;
    }

    //Number of contacts have to be in int range
    public int getNumberOfContacts()
    {
        int count=0;
        String line;
        File file=new File(_fileUri.getPath());
        //Open file

        if(!file.exists()) //Is from Browse button selection
            file = getFromGoogleDrive(_fileUri);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            while ((line = br.readLine()) != null) {
                if(line.toUpperCase().contains("BEGIN:")) count++;
            }
            br.close();
        }
        catch (IOException ex) {
            //You'll need to add proper error handling here
            throw new RuntimeException(ex);
        }
        return count;
    }

     public static void cleanUpInternalFolder(Activity activity){
        FileOperations.RemoveAllFilesInFolder(
                getInternalFolderPath(activity));
    }


    private void WriteToFile(String fileName,
                             boolean writeExternalStorage,
                             String text){
        String folderName = getFolderName();
        if(writeExternalStorage)
        {
            FileOperations.WriteFileToExternalStorage(_activity,
                                                    fileName,
                                                    folderName,
                                                    text);
        }
        else{
            FileOperations.WriteFileToInternalStorage(_activity,
                    fileName,
                    folderName,
                    text);
        }
    }

    public static String getFolderName(){
        //Should match that in xml/file_path
        return  "vCardSplitter";//_activity.getFilesDir().getName();
    }

    public String getExternalFolderPath(){
        return FileOperations.getExternalStorageFolderString() + "/" +
                getFolderName();
    }

    public static String getInternalFolderPath(Activity activity){
        return FileOperations.getInternalStorageFolderString(activity) + "/" +
                getFolderName();
    }

    //Returns the folder
    public String splitvCard(int numberOfFiles,
                           int totalContacts,
                           String origFileName,
                           boolean writeExternalStorage){
        int count=0;
        int fileCount=0;
        StringBuilder text = new StringBuilder();
        String line, contactName=null;
        File file=new File(_fileUri.getPath());

        if(!file.exists())
            file = getFromGoogleDrive(_fileUri);



        try {
            if(!writeExternalStorage)
                cleanUpInternalFolder(_activity);

            //Open file
            sendHandlerMessage(SPLIT_TOTAL_COUNT,
                    totalContacts);

            int numberOfContactsPerFile=totalContacts/numberOfFiles;

            BufferedReader br = new BufferedReader(new FileReader(file));

            while ((line = br.readLine()) != null) {
                if(line.toUpperCase().startsWith("N:"))
                    contactName=parseN(line);
                if(line.toUpperCase().startsWith("FN:") && contactName==null)
                    contactName=parseFN(line);
                if(line.toUpperCase().contains("END:")) {
                    count++;

                }

                text.append(line.trim());
                text.append("\r\n");
                //Do this check at the end so that string is ready
                //Account for situation whereE EOF reached but count is still low
                //Write in that situation too
                if(count==numberOfContactsPerFile || line == null){
                    SupportFunctions.DebugLog("ViewModelSplit",
                                "splitCard","Before write fileCount:"+fileCount);
                    count=0;
                    fileCount++;
                    String fileName;


                    if(numberOfContactsPerFile==1)
                        fileName=contactName + "_" + String.valueOf(fileCount)+ ".vcf"; //Will be last recorded name
                    else
                        fileName= origFileName + "_" +  String.valueOf(fileCount)+ ".vcf";
                    //Remove spaces from fileName
                    fileName = FileOperations.getValidFileName(fileName);


                    WriteToFile(fileName,
                                writeExternalStorage,text.toString());
                    text = new StringBuilder();
                    contactName=null; //Reset per contact value
                    sendHandlerMessage(SPLIT_FILE_COUNT,
                            fileCount * numberOfContactsPerFile);
                    SupportFunctions.DebugLog("ViewModelSplit",
                            "splitCard","After write fileCount:"+fileCount);
                }//if(count==numberOfContactsPerFile || line == null)
            }
            br.close();
        }
        catch (IOException ex) {
            //You'll need to add proper error handling here
            throw new RuntimeException(ex);
        }

        if(writeExternalStorage)
            return getExternalFolderPath();
        else
            return getInternalFolderPath(_activity);

    }

    //value should be N:Gump;Forrest;;;
    private String parseN(String value){
        if(value==null) return null;
        String finalValue="";
        String [] values = value.split(":");
        if(values.length > 1){
            values = values[1].split(";");
            if(values.length > 0) finalValue=values[0].trim();
            if(values.length > 1 )
                if(!values[1].trim().equals(""))
                    finalValue=values[1].trim() + "_" + finalValue;
            if(values.length > 2 )
                if(!values[2].trim().equals(""))
                    finalValue=values[2].trim() + "_" + finalValue;
        }
        return finalValue;
    }

    //value should be of FN:Forest Gump
    private String parseFN(String value){
        if(value==null) return null;
        String finalValue="";
        String [] values = value.split(":");
        if(values.length > 1) {
            finalValue = values[1].trim();
        }
        return finalValue;
    }

    //External sources like Google Drive
    private File getFromGoogleDrive(Uri uri){
        File f;
        String internalFolderName = getInternalFolderPath(_activity);
        File folder = FileOperations.CreateInternalFolder(_activity,
                internalFolderName);

        try {
            File tFile = new File(folder,"TmpFile.csv"); //Copy to tmpFile
            FileOperations.CopyFile(_activity.getContentResolver().openInputStream(uri),
                    tFile);
            if(tFile !=null)
                f = FileOperations.getFileObjectForRead(tFile.getPath());
            else
                f=null;
        }
        catch(Exception ex) {
            SupportFunctions.DebugLog("VMSplit", "ImportFromDrive", "Error:" + ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
        return f;
    }
}
