package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by Chaitanya Belwal on 12/28/2015.
 */
public class vCardFileImportOutlookTest {
    @Test
    public void testSet() throws Exception{
        String testId="testSetvCardFileImportSingleContact";
        ExportImportTestAdapter eia = new ExportImportTestAdapter();

        ArrayList<DataContactTransfer> allDct =
                eia.ImportFromVCF(eia.getFileContents(
                        "TestFromOutlook.vcf"));

        //Check size
        assertTrue("testSetvCardFileImport",
                allDct.size()==1);

        DataContactTransfer dct=allDct.get(0);

        assertTrue(testId,
                dct.getVCardName().equals("Chaitanya Belwal"));

        assertTrue(testId,
                dct.getEmailAddresses().getCount()==0);

        assertTrue(testId,
                dct.getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=work,voice;VALUE=uri:tel:12812223975" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:18329656497" + "\r\n" +
                                "TEL;TYPE=cell,text;VALUE=uri:tel:18329656497" + "\r\n"));

        String a = dct.getPostalAddress().getVCardString();
        assertTrue(testId,
                dct.getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=work;LABEL=\"9222 Symphonic Ln\\nHouston, TX 77040\\nUnited States of America\"" +
                    ":;;9222 Symphonic Ln;Houston;TX;77040;United States of America"+
                    "\r\n"));
    }

}
