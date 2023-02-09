package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Chaitanya Belwal on 11/23/2015.
 */
public class vCardImport30Test_With_Org {
    private static final String CrLf ="\r\n";

    private final String vCard30=
                    "BEGIN:VCARD" + CrLf +
                    "VERSION:3.0" + CrLf +
                    "N:" + CrLf +
                    "FN:" + CrLf +
                    "TEL;TYPE=WORK:707-422-9600" + CrLf +
                    "ORG:Appliance Parts FF" + CrLf +
                    "END:VCARD";

    @Test
    public void testSetFromVCard30String() throws Exception {
        String testId="testSetFromVCard30String";
        DataContactTransfer dct = new DataContactTransfer(0);
        dct.setFromVCardString(vCard30);

        assertTrue(testId,
                dct.getVCardName().equals("Appliance Parts FF"));
    }
}