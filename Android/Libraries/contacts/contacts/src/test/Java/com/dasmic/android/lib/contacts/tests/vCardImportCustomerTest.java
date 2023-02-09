package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Chaitanya Belwal on 12/25/2015.
 */
public class vCardImportCustomerTest {
    private static final String CrLf ="\r\n";

    @Test
    public void testSetFrom_2019_1_30() throws Exception {
        final String vCard=
                "BEGIN:VCARD" + CrLf +
                "VERSION:4.0" + CrLf +
                "PRODID:pm-ez-vcard 0.0.1" + CrLf +
                "FN:Adam Haywood" + CrLf +
                "UID:protonmail-ios-00CEDD3A-B38D-4F58-83D4-A036471E8251" + CrLf +
                "item1.EMAIL;TYPE=x-TYPE:ad_haywood@yahoo.co.uk" + CrLf +
                "TEL:‭+44 7710 849425‬" + CrLf +
                "END:VCARD";
        DataContactTransfer dct = new DataContactTransfer(0);
        dct.setFromVCardString(vCard);
        assertTrue("testSetFromVCardOutlookString",
                dct.getVCardName().equals("Adam Haywood"));

    }
}
