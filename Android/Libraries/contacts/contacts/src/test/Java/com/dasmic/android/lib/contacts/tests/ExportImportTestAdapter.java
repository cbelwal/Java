package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;
import com.dasmic.android.lib.support.Static.FileOperations;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 12/26/2015.
 */
public class  ExportImportTestAdapter {
    public static final String CrLf ="\r\n";
    private final String vCard40=
            "BEGIN:VCARD" + CrLf +
                    "VERSION:4.0" + CrLf +
                    "N:Gump;Forrest;;;" + CrLf +
                    "FN:Forrest Gump" + CrLf +
                    "ORG:Bubba Gump Shrimp Co." + CrLf +
                    "TITLE:Shrimp Man" + CrLf +
                    "PHOTO;MEDIATYPE=image/gif:http://www.example.com/dir_photos/my_photo.gif" + CrLf +
                    "TEL;TYPE=work,voice;VALUE=uri:tel:+11115551212" + CrLf +
                    "TEL;TYPE=home,voice;VALUE=uri:tel:+14045551212" + CrLf +
                    "ADR;TYPE=work;LABEL=\"100 Waters Edge\nBaytown, LA 30314\nUnited States of America\":;;100 Waters Edge;Baytown;LA;30314;United States of America" + CrLf +
                    "ADR;TYPE=home;LABEL=\"42 Plantation St.\nBaytown, LA 30314\nUnited States of America\":;;42 Plantation St.;Baytown;LA;30314;United States of America" + CrLf +
                    "EMAIL:forrestgump@example.com" + CrLf +
                    "NOTE:This is a test note" + CrLf +
                    "REV:20080424T195243Z" + CrLf +
                    "END:VCARD";

    public DataContactTransfer getSampleTransferObject(){
        DataContactTransfer dct = new DataContactTransfer(0);
        dct.setFromVCardString(vCard40);
        return dct;
    }

    public String getTestdataFolder(){
        String dir = System.getProperty("user.dir");
        dir = dir + "\\..\\TestData";
        return dir;
    }

    public String getFileContents(String fileName){
        //dir is C:\Users\Chaitanya Belwal\AndroidStudioProjects\KontaKit\app
        String filePath = getTestdataFolder() + "\\" + fileName;
        File f= new File(filePath);

        String fileContents =
                FileOperations.ReadFromFileWindowsFormat(f);
        return fileContents;
    }

    public void WriteFile(String fileName,
                            String data){
        FileOperations.DeleteFile(getTestdataFolder() + "//"
                                    + fileName);
        File folder= new File(getTestdataFolder());
        FileOperations.WriteFileToFolder(fileName,folder,data);
    }

    public ArrayList<DataContactTransfer>
        ImportFromVCF(String fileContents){
        int count=0;
        //Split based in BEGIN
        String [] sAllContacts=fileContents.split("BEGIN");
        ArrayList<DataContactTransfer> allDct = new ArrayList<>();
        DataContactTransfer dct;
        for(String sContact:sAllContacts){
            try {
                dct = new DataContactTransfer(0);
                dct.setFromVCardString(sContact);
                allDct.add(dct);
                count++;
            }
            catch(Exception ex){
            }
        }
        return allDct;
    }

    //File contents have '\r\n' at End of line irrespective
    //of whether it Unix/Windows
    public ArrayList<DataContactTransfer>
        ImportFromCSV(String fileContents){
        int count=0,ii=0;
        String sContact ="";
        //Split based in '\r\n'
        String [] sAllContacts=fileContents.split("\r\n");
        ArrayList<DataContactTransfer> allDct =
                new ArrayList<>();


        DataContactTransfer dct;

        for(ii=1;ii<sAllContacts.length;ii++){
            //SupportFunctions.DebugLog("Import","VCF","Contacts Data:"+sContact);
            try {
                sContact = sAllContacts[ii];
                dct = new DataContactTransfer(0);
                dct.setFromCSVString(sContact);
                allDct.add(dct);
                count++;
            }
            catch(Exception ex){

            }
        }
        return allDct;
    }

    //This function is not working correctly
    public byte[] base64Decode(String input)    {
        final String codes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

        int ii = input.length();

        if (input.length() % 4 != 0)    {
            throw new IllegalArgumentException("Invalid base64 input");
        }
        byte decoded[] = new byte[((input.length() * 3) / 4) - (input.indexOf('=') > 0 ? (input.length() - input.indexOf('=')) : 0)];
        char[] inChars = input.toCharArray();
        int j = 0;
        int b[] = new int[4];
        for (int i = 0; i < inChars.length; i += 4)     {
            // This could be made faster (but more complicated) by precomputing these index locations
            b[0] = codes.indexOf(inChars[i]);
            b[1] = codes.indexOf(inChars[i + 1]);
            b[2] = codes.indexOf(inChars[i + 2]);
            b[3] = codes.indexOf(inChars[i + 3]);
            decoded[j++] = (byte) ((b[0] << 2) | (b[1] >> 4));
            if (b[2] < 64)      {
                decoded[j++] = (byte) ((b[1] << 4) | (b[2] >> 2));
                if (b[3] < 64)  {
                    decoded[j++] = (byte) ((b[2] << 6) | b[3]);
                }
            }
        }

        return decoded;
    }

}
