package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by Chaitanya Belwal on 12/28/2015.
 */
public class CSVFileImportExportSingleContactTest {
    private  String CrLf = ExportImportTestAdapter.CrLf;

    @Test
    public void testSet() throws Exception {
        String testId="testSetvCardFileImportSingleContact";
        String fileName ="VCFTest.csv";
        ExportImportTestAdapter eia = new ExportImportTestAdapter();

        DataContactTransfer dct = eia.getSampleTransferObject();
        //Write vcf
        eia.WriteFile(fileName,"Name,Dummy Header\r\n" +
                dct.getCSVString());

        //Now Read it
        ArrayList<DataContactTransfer> allDct =
                eia.ImportFromCSV(eia.getFileContents(
                        fileName));
        //Check size
        assertTrue(testId,
                allDct.size()==1);

        assertTrue(testId,
                allDct.get(0).getVCardName().equals("Forrest Gump"));

        assertTrue(testId,
                allDct.get(0).getOrganization().getCSVString().equals(
                        "Bubba Gump Shrimp Co.,null,Shrimp Man"));

        assertTrue(testId,
                allDct.get(0).getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:forrestgump@example.com\r\n")
        );
        assertTrue(testId,
                allDct.get(0).getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=work,voice;VALUE=uri:tel:+11115551212" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:+14045551212"
                                + "\r\n"));

        String  a=allDct.get(0).getPostalAddress().getVCardString();
        assertTrue(testId,
                allDct.get(0).getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=work;LABEL=\"100 Waters Edge\\nBaytown, LA 30314\\nUnited States of America\":;;100 Waters Edge;Baytown;LA;30314;United States of America"
                                + "\r\n" +
                                "ADR;TYPE=home;LABEL=\"42 Plantation St.\\nBaytown, LA 30314\\nUnited States of America\":;;42 Plantation St.;Baytown;LA;30314;United States of America"
                                + "\r\n"));
    }

}
