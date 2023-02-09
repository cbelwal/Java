package com.dasmic.android.lib.support.Static;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Chaitanya Belwal on 9/30/2017.
 */

public class ZipOperations {
    public static void unZip(String sourceZipFile, String targetFolder) throws IOException {
        try {
            //Start from clean slate
            FileOperations.DeleteFolder((targetFolder));
            //Make Target folder
            File f = new File(targetFolder);
            if(!f.isDirectory()) {
                f.mkdirs();
            }
            InputStream is;
            ZipInputStream zis;

            String filename;
            is = new FileInputStream(sourceZipFile);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // fileName
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if(filename.contains("/") || filename.contains("\\")) {
                    //if (ze.isDirectory()) {
                    //    File fmd = new File(targetLocation + "/" + filename);
                    createFoldersInPath(filename,targetFolder);
                    //fmd.mkdirs();
                        //continue;
                    //}
                }

                FileOutputStream fout = new FileOutputStream(targetFolder +
                                            "/" + filename);
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }
            zis.close();
        }
        catch (Exception e) {
            SupportFunctions.DebugLog("FileOperations","unZip","Error:"+e.getMessage());
        }
    }



    // Creates folders includes in FilePath
    // if FilePath is :/dir1/dir2/dir3/fileName
    // will create folders /dir1/dir2/dir3
    // also returns fileName
    public static String createFoldersInPath(String filePath,String basePath)
    {
        //Remove fileName
        String [] dirs = filePath.split("/");

        if(dirs==null) return null;
        if(dirs.length <=0) return null;
        String dirPath=dirs[0];
        for(int ii=1;ii<dirs.length-1;ii++)
        {
            dirPath = dirPath+"/"+dirs[ii];
        }

        if(basePath!=null)
            dirPath = basePath+"/"+dirPath;

        File fmd = new File(dirPath);
        fmd.mkdirs();

        //Return last element as dirName
        return dirs[dirs.length-1];
    }


    public static boolean zip(ArrayList<String> allSources, String targetLocationFilePath) {
        final int BUFFER = 2048;

        ZipOutputStream out=null;
        for(String sourcePath:allSources) {
            File sourceFile = new File(sourcePath);
            try {
                BufferedInputStream origin = null;
                FileOutputStream dest = new FileOutputStream(targetLocationFilePath);
                out = new ZipOutputStream(new BufferedOutputStream(
                        dest));
                if (sourceFile.isDirectory()) {
                    zipSubFolder(out, sourceFile, sourceFile.getParent().length());
                } else {
                    byte data[] = new byte[BUFFER];
                    FileInputStream fi = new FileInputStream(sourcePath);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }//AllSources
        try {
            out.close();
        }
        catch(IOException ex){
            return false;
        }
        return true;
    }


    //Zip a sub folder
    private static void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }
}
