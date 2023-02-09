package com.dasmic.android.lib.contacts.Interface;

import android.content.Context;

/**
 * Created by Chaitanya Belwal on 10/2/2015.
 */
public interface IExportImport {
    public String getFormattedString(Context _context);
    public String getCSVString();
    public String getVCardString();
    public void setFromCSVString(String value);
    public void setFromVCard40String(String value);
    public void setFromVCard30String(String value);
    public void setFromVCard21String(String value);
}
