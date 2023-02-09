package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Created by Chaitanya Belwal on 12/26/2015.
 */
public class vCardFileImportSixContactTest {

    @Test
    public void testSet() throws Exception{
        String testId="testSetvCardFileImportSixContacts";
        ExportImportTestAdapter eia = new ExportImportTestAdapter();

        ArrayList<DataContactTransfer> allDct =
                eia.ImportFromVCF(eia.getFileContents(
                        "TestSixContacts.vcf"));

        //Check size
        assertTrue(testId,
                allDct.size() == 6);

        assertTrue(testId,
                allDct.get(5).getVCardName().equals("Chaitanya VCF6 Belwal"));

        assertTrue(testId,
                allDct.size() < ActivityOptions.FREE_VERSION_CONTACT_LIMIT);
    }

}
