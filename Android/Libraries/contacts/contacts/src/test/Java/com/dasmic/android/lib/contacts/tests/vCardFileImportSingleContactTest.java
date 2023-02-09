package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Created by Chaitanya Belwal on 12/26/2015.
 */
public class vCardFileImportSingleContactTest {

    @Test
    public void testSet() throws Exception{
        String testId="testSetvCardFileImportSingleContact";
        ExportImportTestAdapter eia = new ExportImportTestAdapter();

        ArrayList<DataContactTransfer> allDct =
            eia.ImportFromVCF(eia.getFileContents(
                    "TestSingleContact.vcf"));

        //Check size
        assertTrue(testId,
                allDct.size()==1);

        DataContactTransfer dct=allDct.get(0);

        assertTrue(testId,
                dct.getVCardName().equals("Chaitanya VCF Belwal"));

        assertTrue(testId,
                dct.getOrganization().getVCardString().equals(
                        "ORG:Dasmic\r\nTITLE:Programmer\r\n"));

        assertTrue(testId,
                dct.getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:cbelwal@gmail.com\r\n")
        );


        assertTrue(testId,
                dct.getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=work,voice;VALUE=uri:tel:+1 (123) 123-1234" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:+1 (832) 965-6497" + "\r\n" +
                                "TEL;TYPE=cell,text;VALUE=uri:tel:+1 (832) 965-6497" + "\r\n"));

        assertTrue(testId,
                dct.getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=home;LABEL=\"9222 Symphonic Ln.,\\nHouston, TX 77040\\nUSA\"" +
                                ":;;9222 Symphonic Ln.,;Houston;TX;77040;USA"+
                                "\r\n"));
    }

}
