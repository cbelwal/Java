package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Chaitanya Belwal on 11/23/2015.
 */
public class vCardImport40Test {
    private String CrLf = ExportImportTestAdapter.CrLf;

    private final String vCard40=
            "BEGIN:VCARD" + CrLf +
            "VERSION:4.0" + CrLf +
            "N:Gump;Forrest;Middle;;" + CrLf +
            "FN:Forrest Gump" + CrLf +
            "ORG:Bubba Gump Shrimp Co." + CrLf +
            "TITLE:Shrimp Man" + CrLf +
            "BDAY:1976-12-03" + CrLf +
            "ANNIVERSARY:2007-12-04" + CrLf +
            "PHOTO;MEDIATYPE=image/gif:http://www.example.com/dir_photos/my_photo.gif" + CrLf +
            "TEL;TYPE=work,voice;VALUE=uri:tel:+11115551212" + CrLf +
            "TEL;TYPE=home,voice;VALUE=uri:tel:+14045551212" + CrLf +
            "ADR;TYPE=work;LABEL=\"100 Waters Edge\nBaytown, LA 30314\nUnited States of America\":;;100 Waters Edge;Baytown;LA;30314;United States of America" + CrLf +
            "ADR;TYPE=home;LABEL=\"42 Plantation St.\nBaytown, LA 30314\nUnited States of America\":;;42 Plantation St.;Baytown;LA;30314;United States of America" + CrLf +
            "EMAIL:forrestgump@example.com" + CrLf +
            "REV:20080424T195243Z" + CrLf +
            "END:VCARD";

    @Test
    public void testSet() throws Exception {
        DataContactTransfer dct = new DataContactTransfer(0);
        dct.setFromVCardString(vCard40);
        String testId="testSetFromVCard40String";

        assertTrue("testSetFromVCard40String",
                dct.getVCardName().equals("Forrest Middle Gump"));
        assertTrue("testSetFromVCard40String",
                dct.getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:forrestgump@example.com\r\n")
                );
        assertTrue("testSetFromVCard40String",
                dct.getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=work,voice;VALUE=uri:tel:+11115551212" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:+14045551212"
                        + "\r\n"));
        String value=dct.getPostalAddress().getVCardString();
        assertTrue("testSetFromVCard40String",
                dct.getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=work;LABEL=\"100 Waters Edge\\nBaytown, LA 30314\\nUnited States of America\":;;100 Waters Edge;Baytown;LA;30314;United States of America"
                                + "\r\n" +
                                "ADR;TYPE=home;LABEL=\"42 Plantation St.\\nBaytown, LA 30314\\nUnited States of America\":;;42 Plantation St.;Baytown;LA;30314;United States of America"
                                + "\r\n"));
        value= dct.getEvents().getVCardString();
        assertTrue(testId,
                value.equals(
                        "BDAY:1976-12-03" + "\r\n" +
                                "ANNIVERSARY:2007-12-04" + "\r\n"));
    }
}