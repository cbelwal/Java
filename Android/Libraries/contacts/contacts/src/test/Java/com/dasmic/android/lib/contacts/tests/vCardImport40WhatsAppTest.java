package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Chaitanya Belwal on 11/23/2015.
 */
public class vCardImport40WhatsAppTest {
    private String CrLf = ExportImportTestAdapter.CrLf;

    private final String vCard40=
            "BEGIN:VCARD" + CrLf +
            "VERSION:4.0" + CrLf +
            "N:Gump;Forrest;Middle;;" + CrLf +
            "FN:Forrest Gump" + CrLf +
            "ORG:Bubba Gump Shrimp Co." + CrLf +
            "TITLE:Shrimp Man" + CrLf +
            "PHOTO;MEDIATYPE=image/gif:http://www.example.com/dir_photos/my_photo.gif" + CrLf +
            "X-DASMIC-WHATSAPP;111222333@s.whatsapp.com" + CrLf +
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

        assertTrue("testSetFromVCard40WhatsAppString",
                dct.getVCardName().equals("Forrest Middle Gump"));
        assertTrue("testSetFromVCard40WhatsAppString",
                dct.getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:forrestgump@example.com\r\n")
                );
        assertTrue("testSetFromVCard40WhatsAppString",
                dct.getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=work,voice;VALUE=uri:tel:+11115551212" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:+14045551212"
                        + "\r\n"));


        assertTrue("testSetFromVCard40WhatsAppString",
                dct.getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=work;LABEL=\"100 Waters Edge\\nBaytown, LA 30314\\nUnited States of America\":;;100 Waters Edge;Baytown;LA;30314;United States of America"
                                + "\r\n" +
                                "ADR;TYPE=home;LABEL=\"42 Plantation St.\\nBaytown, LA 30314\\nUnited States of America\":;;42 Plantation St.;Baytown;LA;30314;United States of America"
                                + "\r\n"));
        assertTrue("testSetFromVCard40WhatsAppString",
                dct.getWhatsApp().getVCardString().equals(
                        "X-DASMIC-WHATSAPP;111222333@s.whatsapp.com"
                                + "\r\n"));

    }
}