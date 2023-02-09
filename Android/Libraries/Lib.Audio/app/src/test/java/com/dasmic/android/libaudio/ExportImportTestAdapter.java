package com.dasmic.android.libaudio;

import com.dasmic.android.lib.support.Static.FileOperations;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 12/26/2015.
 */
public class ExportImportTestAdapter {

    public static String getTestdataFolder(){
        String dir = System.getProperty("user.dir");
        dir = dir + "\\src\\test\\TestData\\";

        return dir;
    }

}
