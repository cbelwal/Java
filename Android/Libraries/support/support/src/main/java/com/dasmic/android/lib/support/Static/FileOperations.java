package com.dasmic.android.lib.support.Static;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * Created by Chaitanya Belwal on 10/4/2015.
 */
public class FileOperations {
    public static void WriteFileToInternalStorage(Context context,
                                                  String fileName,
                                                  String folderName,
                                                  String contents){

            File folder = CreateInternalFolder(context, folderName);
            Log.d("CKIT", "InternalStorageFolder:" +
                    getInternalStorageFolderString(context));
            WriteFileToFolder( fileName
                    , folder, contents);
    }

    public static String getFileSizeString(String folder){
        return
                String.format("%.2f",
                        getFileSizeDoubleMB(folder)) + " MB";
    }

    public static double getFileSizeDoubleMB(String folder){
        double size=0;
        try{
            File file = new File(folder);
            size = (file.length()/1024.0)/1024.0;  // size in MByte
        } catch( Exception e ) {
            //e.printStackTrace();
        }
        return size;
    }

    public static void CopyFileToExternalStorage(Context context,
                                                 String fileName,
                                                  String folderName){

        File folder = CreateExternalFolder(context, folderName);
        Log.d("CKIT", "ExternalStorageFolder:" + getExternalStorageFolderString());
        CopyFileToFolder(fileName, folder.getAbsolutePath());
    }

    public static void CopyFileToExternalStorage(Context context,
                                                 String sourceFileName,
                                                 String targetFileName,
                                                 String targetFolderName){

        File folder = CreateExternalFolder(context, targetFolderName);
        Log.d("CKIT", "ExternalStorageFolder:" + getExternalStorageFolderString());
        CopyFileToFolder(sourceFileName, targetFileName, folder.getAbsolutePath());
    }

    public static void CopyFileToInternalStorage(Context context,
                                                 String sourceFileName,
                                                 String targetFileName,
                                                 String targetFolderName){
        File folder = CreateInternalFolder(context,targetFolderName);
        Log.d("CKIT", "ExternalStorageFolder:" + getExternalStorageFolderString());
        CopyFileToFolder(sourceFileName, targetFileName, folder.getAbsolutePath());
    }

    public static void CopyFileToInternalStorage(Context context,
                                                 String fileName,
                                                 String folderName){
        File folder = CreateInternalFolder(context,folderName);
        Log.d("CKIT", "ExternalStorageFolder:" + getExternalStorageFolderString());
        CopyFileToFolder(fileName, folder.getAbsolutePath());
    }

    public static String getParentFolder(String currentPath){
        File parentFile = new File(currentPath).getParentFile();
        if(parentFile != null)
            return parentFile.getAbsolutePath();
        else
            return null;
    }

    public static boolean canReadFolder(String currentPath){
        File currentFile = new File(currentPath);
        return currentFile.canRead();
    }

    public static void WriteFileToExternalStorage(Context context,
                                                  String fileName,
                                                  String folderName,
                                                  String contents){
        File folder = CreateExternalFolder(context,folderName);
        Log.d("CKIT", "ExternalStorageFolder:" + getExternalStorageFolderString());
        WriteFileToFolder(fileName
                , folder, contents);
    }

    public static String getInternalStorageFolderString(Context context){
            return context.getFilesDir().getAbsolutePath();
    }

    public static String getExternalStorageFolderString(){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getInternalCacheFolder(Context context){
        return context.getCacheDir().toString();
    }


    public static File getTopLevelFolder(String fullFolderPath){
        File f = new File(fullFolderPath);
        File parent = f;
        while(f != null){
            parent =f;
            f = f.getParentFile();
        }
        return parent;
    }


    //get File Dir is coming from xml/file_path
    public static File getInternalStorageFolder(Context context){
        return context.getFilesDir();
    }

    public static File getExternalStorageFolder(){
        return Environment.getExternalStorageDirectory();
    }

    public static File getMediaStorageFolder(){
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
    }


    public static void CopyFileToFolder(String sourceFileFullPath,
                                String destFolderName) {
         File sourceLocation = new File(sourceFileFullPath);
         File targetLocation = new File(destFolderName,
                sourceLocation.getName());
        CopyFile(sourceLocation,targetLocation);
    }

    public static void CopyFileToFolder(String sourceFileFullPath,
                                        String targetFileName,
                                        String destFolderName) {
        File sourceLocation = new File(sourceFileFullPath);
        File targetLocation = new File(destFolderName,
                targetFileName);
        CopyFile(sourceLocation,targetLocation);
    }

    public static void CopyFile(String sourceFilePath, String targetFilePath){
        File sourceLocation = new File(sourceFilePath);
        File targetLocation = new File(targetFilePath);
        CopyFile(sourceLocation,targetLocation);
    }

    //Changed for File Browser
    public static void CopyFile(File sourceLocation, File targetLocation){
        InputStream in=null;
        OutputStream out=null;
        try {
            in = new FileInputStream(sourceLocation);
            out = new FileOutputStream(targetLocation);
        }
        catch(FileNotFoundException ex){
            Log.d("APKit","CopyFile Error: " + ex.getMessage());
        }

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
        }
        catch(IOException ex){
            throw new RuntimeException(ex);
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    //Older default function
    public static ArrayList<File> getAllFileObjectsInFolder(File parentDir,
                                                            ArrayList<String> extensions){
        return getAllFileObjectsInFolder(parentDir,extensions,0,9999);
    }


                                                            //Returns a File object for every item in folder
    public static ArrayList<File> getAllFileObjectsInFolder(File parentDir,
                                                      ArrayList<String> extensions,
                                                      int level,
                                                      int maxLevel) {

        ArrayList<File> inFiles = new ArrayList<File>();
        if(level > maxLevel) return inFiles;

        if(parentDir==null) {
            Log.i("CKIT", "getAllFileObjectsInFolder::parentDir is null");
            return inFiles;
        }
        File[] files = parentDir.listFiles();
        if(files == null) return inFiles;
        for (File file : files) {
            if(file != null) {
                if (file.isDirectory() && file.canRead()) {
                    inFiles.addAll(getAllFileObjectsInFolder(file, extensions, level++, maxLevel));
                } else { //Check if File matches Extension
                    for (String extension : extensions) {
                        if (file.getName().toUpperCase().endsWith(extension.toUpperCase())) {
                            inFiles.add(file);
                        }
                    }
                }
            }//file != null
        }
        return inFiles;
    }


    public static void RenameFile(String fullFilePath, String newName){
        try {
            File from = new File(fullFilePath);
            File to = new File(from.getParent() + "/" + newName);
            from.renameTo(to);
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static File CreateInternalFolder(Context context,
                                            String folderName)
    {
        //File folder = context.getDir(folderName, Context.MODE_PRIVATE); //Creating an internal dir;
        File folder = new File(getInternalStorageFolder(context),
                                    folderName);
        if(!folder.exists())
            folder.mkdirs();
        return folder;
    }

    public static File CreateExternalFolder(Context context,
                                            String folderName)
    {
        //File folder = context.getDir(folderName, Context.MODE_PRIVATE); //Creating an internal dir;
        File folder = new File(getExternalStorageFolder(),
                folderName);
        if(!folder.exists())
            folder.mkdirs();
        return folder;
    }

    public static File CreateFolder(String parentFolder,
                                            String newFolderName)
    {
        File folder = new File(parentFolder,newFolderName);
        if(!folder.exists())
            folder.mkdirs();
        return folder;
    }

    public static void WriteFileToFolder(
                                   String fileName,
                                   File folder,
                                   String contents){
        try {

            File file = new File(folder,fileName);
            FileWriter writer = new FileWriter(file);
            writer.append(contents);
            writer.flush();
            writer.close();
        }
        catch(Exception ex){
            Log.i("CKIT", "WriteFileInFolder Exception:" + ex.getMessage());
            throw new RuntimeException("WriteFileToFolderException:" + ex);
        }
    }

    public static void WriteBytestoFile(String fileName, byte[] bytes){
        try{
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(bytes);
            fos.close();
        }
        catch(FileNotFoundException ex){
            return;
        }
        catch(IOException ex){
            return;
        }


    }

    public static long getLastModifiedData(String fileName){
        try {
            File file = new File(fileName);
            if(file.exists()){
                return file.lastModified();
            }
            return 0;
        }
        catch(Exception ex){
            Log.i("CKIT", "FileOperations::getFileObjectForRead Exception:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static File getFileObjectForRead(String fileName){
        try {
            //Create Folder
            File file = new File(fileName);

            if(file.exists()){
                return file;
            }
            return null;
        }
        catch(Exception ex){
            Log.i("CKIT", "FileOperations::getFileObjectForRead Exception:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static Uri getFileURI(String fileName){
        try {
            //Create Folder
            File file = new File(fileName);
            if(file.exists()){
                return Uri.fromFile(file);
            }
            return null;
        }
        catch(Exception ex){
            Log.i("CKIT", "FileOperations::DoesFileExist Exception:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static boolean DoesFileExist(String fileName){
        try {
            //Create Folder
            File file = new File(fileName);
            if(file.exists())
                return true;
            else
                return false;

        }
        catch(Exception ex){
            Log.i("CKIT", "FileOperations::DoesFileExist Exception:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static void RemoveAllFilesInFolder(String folderName){
        try {
            ArrayList<File> allFiles =
                    getOnlyFileObjectsInFolder(folderName);
            if(allFiles == null) return; //No files exist
            for(File f:allFiles)
                 f.delete();
        }
        catch(Exception ex){
            Log.i("CKIT", "FileOperations::DoesFileExist Exception:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static void RemoveAllContentsInFolder(String folderName){
        try {
            ArrayList<File> allFiles =
                    getAllObjectsInFolder(folderName);
            if(allFiles == null) return; //No files exist
            for(File f:allFiles) {
                if(f.isDirectory())
                    DeleteFolder(f);
                else
                    f.delete();
            }
        }
        catch(Exception ex){
            Log.i("CKIT", "FileOperations::DoesFileExist Exception:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static ArrayList<File> getOnlyFileObjectsInFolder(String folderName){
        ArrayList<File> files=new ArrayList<>();
        try {
            //Create Folder
            File folder = new File(folderName);
            File[] listOfFiles = folder.listFiles();

            if(listOfFiles==null) return null;
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    files.add(listOfFiles[i]);
                }
            }
        }
        catch(Exception ex){
            Log.i("CKIT", "FileOperations::getAllFileObjectsInFolder Exception:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
        return files;
    }

    public static ArrayList<File> getAllObjectsInFolder(String folderName){
        ArrayList<File> files=new ArrayList<>();
        try {
            //Create Folder
            File folder = new File(folderName);
            File[] listOfFiles = folder.listFiles();

            if(listOfFiles==null) return null;
            for (int i = 0; i < listOfFiles.length; i++) {
                    files.add(listOfFiles[i]);
            }
        }
        catch(Exception ex){
            Log.i("CKIT", "FileOperations::getAllFileObjectsInFolder Exception:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
        return files;
    }

    public static String ReadFromFileWindowsFormat(File f){
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line.trim());
                text.append("\r\n");
            }
            br.close();
        }
        catch (IOException ex) {
            //You'll need to add proper error handling here
            throw new RuntimeException(ex);
        }
        return text.toString();
    }

    public static void CopyFile(InputStream inputStream,
                                   File destination)  {
        //InputStream in = new FileInputStream(src);
        try {
            OutputStream outStream =  new
                    FileOutputStream(destination,false);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outStream.write(buf, 0, len);
            }
            inputStream.close();
            outStream.close();
        }
        catch(IOException ex){
            SupportFunctions.DebugLog("FileOperations", "Copy", "Error:" + ex.getMessage());
        }
    }

    //Can delete a File or folder
    public static void DeleteFile(String fileName){
        File f= new File(fileName);
        if(f.isDirectory())
            DeleteFolder(fileName);
        if(f!=null) f.delete();
    }

    public static void DeleteFolder(String folderPath){
          File folder = new File(folderPath);
          DeleteFolder(folder);
    }

    public static void DeleteFolder(File folder){
        if(!folder.exists()) return;
        File[] contents = folder.listFiles();
        if (contents != null) {
            for (File f : contents) {
                DeleteFolder(f);
            }}
        folder.delete();
    }

    public static long getTotalSpaceInternal(Context _context){
        long bytes = new File(_context.getFilesDir().getAbsoluteFile().toString()).getTotalSpace();
        return bytes;
    }

    public static long getTotalSpaceExternal(Context _context){
        long bytes = new File(_context.getExternalFilesDir(null).toString()).getTotalSpace();
        return bytes;
    }

    public static long getFreeSpaceInternal(Context _context){
        try {
            long bytes = new File(_context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
            return bytes;
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static long getFreeSpaceExternal(Context _context){
        try {
            long bytes = new File(_context.getExternalFilesDir(null).toString()).getFreeSpace();
            return bytes;
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    //Removed spaces and other characters to make valid file name
    public static String getValidFileName(String fileName){
        String newFileName = fileName.replace(",","");
        newFileName = newFileName.replace("\"","");
        newFileName = newFileName.replace("\\","");
        newFileName = newFileName.replace("/","");
        newFileName = newFileName.replace("-","_");
        newFileName = newFileName.replace("(","");
        newFileName = newFileName.replace(")","");
        newFileName = newFileName.replace(":","");
        //Space should be last
        newFileName = newFileName.replace(" ","");

        return newFileName;
    }


}
