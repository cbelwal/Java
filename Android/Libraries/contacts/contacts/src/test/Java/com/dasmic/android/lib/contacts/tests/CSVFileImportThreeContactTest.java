package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Created by Chaitanya Belwal on 12/26/2015.
 */
public class CSVFileImportThreeContactTest {
    @Test
    public void testSet() throws Exception{
        String testId="testSetCSVFileImportThreeContacts";
        ExportImportTestAdapter eia = new ExportImportTestAdapter();

        ArrayList<DataContactTransfer> allDct =
                eia.ImportFromCSV(eia.getFileContents(
                        "TestThreeContacts.csv"));

        //Check size
        assertTrue(testId,
                allDct.size()==3);

        assertTrue(testId,
                allDct.get(0).getVCardName().equals("Chaitanya CSV1 Belwal"));
        assertTrue(testId,
                allDct.get(1).getVCardName().equals("Chaitanya CSV2 Belwal"));
        assertTrue(testId,
                allDct.get(2).getVCardName().equals("Chaitanya CSV3 Belwal"));

        assertTrue(testId,
                allDct.get(0).getOrganization().getCSVString().equals("Dasmic,null,Programmer1"));
        assertTrue(testId,
                allDct.get(1).getOrganization().getCSVString().equals("Dasmic,null,Programmer2"));
        assertTrue(testId,
                allDct.get(2).getOrganization().getCSVString().equals("Dasmic,null,Programmer3"));

        assertTrue(testId,
                allDct.get(0).getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:cbelwal@gmail1.com\r\n")
        );
        assertTrue(testId,
                allDct.get(1).getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:cbelwal@gmail2.com\r\n")
        );
        assertTrue(testId,
                allDct.get(2).getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:cbelwal@gmail3.com\r\n")
        );

        assertTrue(testId,
                allDct.get(0).getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=cell,text;VALUE=uri:tel:(001) 222-3975" + "\r\n" +
                                "TEL;TYPE=work,voice;VALUE=uri:tel:(001) 174-1617"
                                + "\r\n"));
        assertTrue(testId,
                allDct.get(1).getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=cell,text;VALUE=uri:tel:(002) 222-3975" + "\r\n" +
                                "TEL;TYPE=work,voice;VALUE=uri:tel:(002) 174-1617"
                                + "\r\n"));
        assertTrue(testId,
                allDct.get(2).getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=cell,text;VALUE=uri:tel:(003) 222-3975" + "\r\n" +
                                "TEL;TYPE=work,voice;VALUE=uri:tel:(003) 174-1617"
                                + "\r\n"));

        assertTrue(testId,
                allDct.get(0).getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=home;LABEL=\"9222 Symphonic Ln\\nHouston, TX 77041\\nUSA\":;;9222 Symphonic Ln;Houston;TX;77041;USA"
                                + "\r\n"));
        assertTrue(testId,
                allDct.get(1).getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=home;LABEL=\"9222 Symphonic Ln\\nHouston, TX 77042\\nUSA\":;;9222 Symphonic Ln;Houston;TX;77042;USA"
                                + "\r\n"));
        assertTrue(testId,
                allDct.get(2).getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=home;LABEL=\"9222 Symphonic Ln\\nHouston, TX 77043\\nUSA\":;;9222 Symphonic Ln;Houston;TX;77043;USA"
                                + "\r\n"));

        assertTrue(testId,
                allDct.get(0).getTimesContacted()==100);
        assertTrue(testId,
                allDct.get(0).getLastTimeContacted() == 1000);
        assertTrue(testId,
                allDct.get(0).getInFavorites() == 1);

        assertTrue(testId,
                allDct.get(1).getTimesContacted()==200);
        assertTrue(testId,
                allDct.get(1).getLastTimeContacted() == 2000);
        assertTrue(testId,
                allDct.get(1).getInFavorites() == 0);

        assertTrue(testId,
                allDct.get(2).getTimesContacted()==300);
        assertTrue(testId,
                allDct.get(2).getLastTimeContacted() == 3000);
        assertTrue(testId,
                allDct.get(2).getInFavorites() == 0);
    }
}
