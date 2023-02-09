package com.dasmic.android.lib.calllog.Model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.CallLog;
import android.provider.ContactsContract;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/11/2017.
 */

public class ModelCallLogCreate {
    private Context _context;

    private final String MIME_WhatsApp="vnd.android.cursor.item/vnd.com.whatsapp.profile";

    public ModelCallLogCreate(Context context){
        _context=context;

    }

    public void createCallLog_Single(DataCallLogDisplay dcld)
    {
        ContentResolver cr = _context.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new
                ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(
                CallLog.Calls.CONTENT_URI)
                .withValue(CallLog.Calls._ID, null)
                .withValue(CallLog.Calls.TYPE, dcld.getType())
                .withValue(CallLog.Calls.DURATION, dcld.getDuration())
                .withValue(CallLog.Calls.DATE, dcld.getDate())
                .withValue(CallLog.Calls.NUMBER, dcld.getNumber())
                .build());


        // Asking the Contact provider to create a new contact
        try {
            ContentProviderResult[] results = cr.applyBatch(
                                    CallLog .AUTHORITY, ops);
        } catch (Exception ex) {
            SupportFunctions.DebugLog("ModelCreateCallLog",
                    "CreateContact", "Error:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
