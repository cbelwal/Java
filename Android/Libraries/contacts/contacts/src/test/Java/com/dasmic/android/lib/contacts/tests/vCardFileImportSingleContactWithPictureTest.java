package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;
import com.dasmic.android.lib.contacts.Data.DatavCardPhoto;
import com.dasmic.android.lib.support.Static.FileOperations;

import java.io.FileOutputStream;
import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Created by Chaitanya Belwal on 12/26/2015.
 */
public class vCardFileImportSingleContactWithPictureTest {
    @Test
    public void testSet() throws Exception{
        String testId="testSetvCardFileImportWithPicture";
        ExportImportTestAdapter eia = new ExportImportTestAdapter();
        String photoFileName = eia.getTestdataFolder()
                +"\\TestPhoto.jpg";

        //Delete file
        FileOperations.DeleteFile(photoFileName);

        ArrayList<DataContactTransfer> allDct =
                eia.ImportFromVCF(eia.getFileContents(
                        "TestSingleWithPhoto.vcf"));

        //Check size
        assertTrue("testSetvCardFileImportWithPicture",
                allDct.size()==1);

        DataContactTransfer dct=allDct.get(0);

        assertTrue(testId,
                dct.getVCardName().equals("Photo Test"));
        assertTrue(testId,
                dct.getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:phototest@gmail.com\r\n")
        );

        //Generate Image
        DatavCardPhoto dvp = dct.getvCardPhotoObject();
        String rawBytes= dvp.getRawEncodedBytes();

        byte[] bytes= eia.base64Decode(rawBytes);


        FileOutputStream out = null;
        try {
            out = new FileOutputStream(eia.getTestdataFolder()
            +"\\TestPhoto.jpg");

            FileOperations.WriteBytestoFile(eia.getTestdataFolder()
                    +"\\TestPhoto.jpg",bytes);
            //bmp.compress(Bitmap.CompressFormat.JPEG,
            //        100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception ex){
            assertTrue(testId, false);
        }
    }

}
