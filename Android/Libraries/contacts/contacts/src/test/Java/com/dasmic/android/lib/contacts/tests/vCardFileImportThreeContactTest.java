package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Created by Chaitanya Belwal on 12/26/2015.
 */
public class vCardFileImportThreeContactTest {

    @Test
    public void testSet() throws Exception{
        String testId="testSetvCardFileImportThreeContacts";
        ExportImportTestAdapter eia = new ExportImportTestAdapter();

        ArrayList<DataContactTransfer> allDct =
                eia.ImportFromVCF(eia.getFileContents(
                        "TestThreeContacts.vcf"));

        //Check size
        assertTrue(testId,
                allDct.size() == 3);

        DataContactTransfer dct=allDct.get(0);

        assertTrue(testId,
                allDct.get(0).getVCardName().equals("Chaitanya VCF1 Belwal"));
        assertTrue(testId,
                allDct.get(1).getVCardName().equals("Chaitanya VCF2 Belwal"));
        assertTrue(testId,
                allDct.get(2).getVCardName().equals("Chaitanya VCF3 Belwal"));

        assertTrue(testId,
                allDct.get(0).getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=work,voice;VALUE=uri:tel:+1 (001) 123-1234" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:+1 (001) 965-6497" + "\r\n" +
                                "TEL;TYPE=cell,text;VALUE=uri:tel:+1 (001) 965-6497" + "\r\n"));

        assertTrue(testId,
                allDct.get(1).getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=work,voice;VALUE=uri:tel:+1 (002) 123-1234" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:+1 (002) 965-6497" + "\r\n" +
                                "TEL;TYPE=cell,text;VALUE=uri:tel:+1 (002) 965-6497" + "\r\n"));

        assertTrue(testId,
                allDct.get(2).getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=work,voice;VALUE=uri:tel:+1 (003) 123-1234" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:+1 (003) 965-6497" + "\r\n" +
                                "TEL;TYPE=cell,text;VALUE=uri:tel:+1 (003) 965-6497" + "\r\n"));


        assertTrue(testId,
                allDct.get(2).getPostalAddress().getVCardString().equals(
                                "ADR;TYPE=home;LABEL=\"9222 Symphonic Ln.,\\nHouston, TX 77040\\nUSA\"" +
                                ":;;9222 Symphonic Ln.,;Houston;TX;77040;USA"+
                                "\r\n"));
    }

}
