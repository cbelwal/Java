package com.dasmic.android.lib.contacts.tests;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Chaitanya Belwal on 11/23/2015.
 */
public class CSVEmptyEmailFieldImportTest {
    private static final String CrLf ="\r\n";

    private final String valueCSVString=
        "Belwal,Chaitanya,Middle,<>,null,null,null,<>,cell,(281) 222-3975,work,(713) 174-1617,<>,,<>,home,9222 Symphonic Ln,Houston,TX,USA, 77040,<>,-1,<>,-1,<>,-1,<>,"+ CrLf;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testSetFromvalueCSVString() throws Exception {
        DataContactTransfer dct = new DataContactTransfer(0);
        dct.setFromCSVString(valueCSVString);

        assertTrue("testSetFromvalueCSVString",
                dct.getVCardName().equals("Chaitanya Middle Belwal"));


        assertTrue("testSetFromvalueCSVString",
                dct.getPhoneNumbers().getVCardString().equals(
                        "TEL;TYPE=cell,text;VALUE=uri:tel:(281) 222-3975" + "\r\n" +
                                "TEL;TYPE=work,voice;VALUE=uri:tel:(713) 174-1617"
                                + "\r\n"));
        assertTrue("testSetFromvalueCSVString",
                dct.getPostalAddress().getVCardString().equals(
                                "ADR;TYPE=home;LABEL=\"9222 Symphonic Ln\\nHouston, TX 77040\\nUSA\":;;9222 Symphonic Ln;Houston;TX;77040;USA"
                                + "\r\n"));
    }
}