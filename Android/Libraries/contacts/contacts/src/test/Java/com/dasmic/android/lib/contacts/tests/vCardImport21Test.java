package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Chaitanya Belwal on 12/25/2015.
 */
public class vCardImport21Test {
    private static final String CrLf ="\r\n";

    private final String vCard21=
            "BEGIN:VCARD" + CrLf +
                    "VERSION:2.1" + CrLf +
                    "N:Gump;Forrest;Middle" + CrLf +
                    "FN:Forrest Gump" + CrLf +
                    "ORG:Bubba Gump Shrimp Co." + CrLf +
                    "TITLE:Shrimp Man" + CrLf +
                    "BDAY:1976-12-03" + CrLf +
                    "ANNIVERSARY:2007-12-04" + CrLf +
                    "PHOTO;GIF:http://www.example.com/dir_photos/my_photo.gif" + CrLf +
                    "TEL;WORK;VOICE:(111) 555-1212" + CrLf +
                    "TEL;HOME;VOICE:(404) 555-1212" + CrLf +
                    "ADR;WORK:;;100 Waters Edge;Baytown;LA;30314;United States of America" + CrLf +
                    "LABEL;WORK;ENCODING=QUOTED-PRINTABLE:100 Waters Edge=0D=0ABaytown, LA 30314=0D=0AUnited States of America" + CrLf +
                    "ADR;HOME:;;42 Plantation St.;Baytown;LA;30314;United States of America" + CrLf +
                    "LABEL;HOME;ENCODING=QUOTED-PRINTABLE:42 Plantation St.=0D=0ABaytown, LA 30314=0D=0AUnited States of America" + CrLf +
                    "EMAIL;PREF;INTERNET:forrestgump@example.com" + CrLf +
                    "REV:20080424T195243Z" + CrLf +
                    "END:VCARD";

    @Test
    public void testSet() throws Exception {
        DataContactTransfer dct = new DataContactTransfer(0);
        dct.setFromVCardString(vCard21);
        String testId="testSetFromVCard21String";

        assertTrue("testSetFromVCard21String",
                dct.getVCardName().equals("Forrest Middle Gump"));
        assertTrue("testSetFromVCard21String",
                dct.getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:forrestgump@example.com\r\n")
        );
        assertTrue("testSetFromVCard30String",
                dct.getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=work,voice;VALUE=uri:tel:(111) 555-1212" + "\r\n" +
                                "TEL;TYPE=home,voice;VALUE=uri:tel:(404) 555-1212"
                                + "\r\n"));
        assertTrue("testSetFromVCard30String",
                dct.getPostalAddress().getVCardString().equals(
                        "ADR;TYPE=work;LABEL=\"100 Waters Edge\\nBaytown, LA 30314\\nUnited States of America\":;;100 Waters Edge;Baytown;LA;30314;United States of America"
                                + "\r\n" +
                                "ADR;TYPE=home;LABEL=\"42 Plantation St.\\nBaytown, LA 30314\\nUnited States of America\":;;42 Plantation St.;Baytown;LA;30314;United States of America"
                                + "\r\n"));

        String value= dct.getEvents().getVCardString();
        assertTrue(testId,
                value.equals(
                        "BDAY:1976-12-03" + "\r\n" +
                                "ANNIVERSARY:2007-12-04" + "\r\n"));
    }
}
