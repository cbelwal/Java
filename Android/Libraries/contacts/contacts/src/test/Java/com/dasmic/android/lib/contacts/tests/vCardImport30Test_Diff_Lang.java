package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Chaitanya Belwal on 11/23/2015.
 */
public class vCardImport30Test_Diff_Lang {
    private static final String CrLf ="\r\n";

    private final String vCard30=
            "BEGIN:VCARD" + CrLf +
            "VERSION:3.0" + CrLf +
            "FN:Betito" + CrLf +
            "PHOTO:" + CrLf +
            "TEL;TYPE=móvil:+15203299522" + CrLf +
            "TEL;TYPE=móvil:+15203299522" + CrLf +
            "ADR:;;;;;;" + CrLf +
            "PRODID:ez-vcard 0.9.5" + CrLf +
            "END:VCARD" + CrLf;


    @Test
    public void testSetFromVCard30String() throws Exception {
        String testId="testSetFromVCard30String";
        DataContactTransfer dct = new DataContactTransfer(0);
        dct.setFromVCardString(vCard30);

        assertTrue(testId,
                dct.getVCardName().equals("Betito"));

        assertTrue(testId,
                dct.getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=home,voice;VALUE=uri:tel:+15203299522" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:+15203299522"
                                + "\r\n"));
    }
}