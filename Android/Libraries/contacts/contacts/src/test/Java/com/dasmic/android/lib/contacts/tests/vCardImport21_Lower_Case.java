package com.dasmic.android.lib.contacts.tests;
import com.dasmic.android.lib.contacts.Data.DataContactTransfer;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Chaitanya Belwal on 9/1/2017.
 */

public class vCardImport21_Lower_Case {
    private static final String CrLf ="\r\n";

    private final String vCard21=
            CrLf +
            "begin:vcard"+CrLf+
            "email;internet:emclellan@verizon.net"+CrLf+
            "version:2.1"+CrLf+
            "end:vcard"+CrLf+
            CrLf;
            /*"begin:vcard"+CrLf+
            "email;internet:bsistare@comcast.net"+CrLf+
            "version:2.1"+CrLf+
            "end:vcard"+CrLf+
            CrLf+
            "begin:vcard"+CrLf+
            "email;internet:JNPNorton@verizon.net"+CrLf+
            "version:2.1"+CrLf+
            "end:vcard"+CrLf+
            CrLf;*/

    @Test
    public void testSet() throws Exception {
        DataContactTransfer dct = new DataContactTransfer(0);
        dct.setFromVCardString(vCard21);
        String testId="testSetFromVCard21String_lower_case";

        Assert.assertTrue(testId,
                dct.getEmailAddresses().getVCardString().equals(
                        "EMAIL;TYPE=home:emclellan@verizon.net\r\n")
        );
    }
}
