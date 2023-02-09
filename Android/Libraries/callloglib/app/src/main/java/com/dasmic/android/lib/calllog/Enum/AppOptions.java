package com.dasmic.android.lib.calllog.Enum;

/**
 * Created by Chaitanya Belwal on 7/16/2016.
 */
public class AppOptions {
    public  static final int CALLLOG_ADD_ACTIVITY_REQUEST=1044;
    public  static final int CALLLOG_EDIT_ACTIVITY_REQUEST=1046;
    public  static final int CALLLOG_EXPORT_ACTIVITY_REQUEST=1048;
    public  static final int CALLLOG_FILELOAD_ACTIVITY_REQUEST =1050;
    public  static final int CALLLOG_RESTORE_ACTIVITY_REQUEST =1052;
    public  static final int CALLLOG_BACKUP_ACTIVITY_REQUEST =1054;
    public  static final int CALLLOG_IMPORT_ACTIVITY_REQUEST =1056;
    public  static final int CALLLOG_BLUETOOTH_ACTIVITY_REQUEST =1060;
    public  static final int CALLLOG_FILTER_ACTIVITY_REQUEST=1070;

    public  static final int PERMISSION_ALL_REQUEST =2001;
    public  static final int PERMISSION_WRITE_CALLLOG_REQUEST =2002;
    public  static final int PERMISSION_READ_REQUEST =2004;
    public  static final int PERMISSION_STORAGE_REQUEST=2003;
    public  static final int PERMISSION_SMS_STORAGE_REQUEST =2005;

    public  static final int PERMISSION_DEFAULT_DIALER_REQUEST =2010;

    public  static int FREE_VERSION_LIMIT=300;
    public  static String FILE_PROVIDER_AUTHORITY;
    public  static final String BACKUP_FOLDER_NAME ="CallLogBackup"; //If change, change in file provider too
    public  static final String BACKUP_FILE_NAME_PREFIX="Backup";
    public  static final String BACKUP_FILE_NAME =
            BACKUP_FILE_NAME_PREFIX+"_AllCallLogs.clm";
    public  static final String APP_NAME ="AppName";

    public static boolean isFreeVersion=true;
    public static final boolean IS_FOR_AMAZON=false;
}
