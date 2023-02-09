package com.dasmic.android.lib.contacts.Enum;

import android.content.Context;

import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 11/16/2015.
 */
public class ActivityOptions {
    public  static final int FILTER_ACTIVITY_REQUEST=100;
    public  static final int OPERATION_ACTIVITY_REQUEST=111;
    public  static final int EXPORT_ACTIVITY_REQUEST=112;
    public  static final int EDIT_ACTIVITY_REQUEST=113;
    public  static final int IMPORT_ACTIVITY_REQUEST=114;
    public  static final int FILELOAD_ACTIVITY_REQUEST=115;
    public  static final int BLUETOOTH_ACTIVITY_REQUEST=118;
    public  static final int DUPLICATES_ACTIVITY_REQUEST=120;
    public  static final int CONTACTS_RESTORE_ACTIVITY_REQUEST=140;
    public  static final int BACKUP_ACTIVITY_REQUEST=142;
    public  static final int RESTORE_ACTIVITY_REQUEST=144;

    public  static final int PERMISSION_CONTACTS_REQUEST=201;
    public  static final int PERMISSION_STORAGE_REQUEST=203;
    public  static final int PERMISSION_SMS_REQUEST=205;
    public  static final int PERMISSION_SHARE_STORAGE_REQUEST =205;

    public  static int FREE_VERSION_CONTACT_LIMIT =300; //Updated
    public  static String FILE_PROVIDER_AUTHORITY;
    public  static final String BACKUP_FOLDER_NAME ="ContactsBackup"; //If change, change in file provider too
    public  static final String BACKUP_FILE_NAME_PREFIX="Backup";
    public  static final String BACKUP_FILE_NAME =
                        BACKUP_FILE_NAME_PREFIX+"AllContacts.vcf";
    public  static final String APP_NAME ="AppName";
    public static final String FILTER_ON_DUPLICATES="***filter_on_duplicates**";

    public  static boolean isFreeVersion=true;
    public static boolean IS_FOR_AMAZON=false;
}
