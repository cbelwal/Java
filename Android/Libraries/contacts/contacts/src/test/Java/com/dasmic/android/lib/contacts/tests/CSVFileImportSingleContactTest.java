package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Created by Chaitanya Belwal on 12/26/2015.
 */
public class CSVFileImportSingleContactTest {

    @Test
    public void testSet() throws Exception{
        String testId="testSetCSVFileImportSingleContact";
        ExportImportTestAdapter eia = new ExportImportTestAdapter();

        ArrayList<DataContactTransfer> allDct =
                eia.ImportFromCSV(eia.getFileContents(
                        "TestSingleContact.csv"));

        //Check size
        assertTrue(testId,
                allDct.size()==1);

        DataContactTransfer dct=allDct.get(0);

        assertTrue(testId,
                dct.getVCardName().equals("Chaitanya CSV Belwal"));

        assertTrue(testId,
                allDct.get(0).getOrganization().getCSVString().equals(
                        "Dasmic,null,Founder"));

        assertTrue(testId,
                dct.getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:cbelwal@gmail123.com\r\n")
        );


        assertTrue(testId,
                dct.getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=cell,text;VALUE=uri:tel:(281) 222-3975" + "\r\n" +
                                "TEL;TYPE=work,voice;VALUE=uri:tel:(713) 174-1617"
                                + "\r\n"));
        assertTrue(testId,
                dct.getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=home;LABEL=\"9222 Symphonic Ln\\nHouston, TX 77040\\nUSA\":;;9222 Symphonic Ln;Houston;TX;77040;USA"
                                + "\r\n"));

        assertTrue(testId,
                dct.getTimesContacted()==200);
        assertTrue(testId,
                dct.getLastTimeContacted() == 300);
        assertTrue(testId,
                dct.getInFavorites() == 1);

    }

}
