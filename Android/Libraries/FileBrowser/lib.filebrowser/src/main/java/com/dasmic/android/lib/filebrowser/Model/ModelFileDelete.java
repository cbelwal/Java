package com.dasmic.android.lib.filebrowser.Model;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dasmic.android.lib.support.Static.FileOperations;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 2/10/2018.
 */

public class ModelFileDelete {
    Context _context;
    private final Handler mHandler;
    public static final int DELETE_COUNT_UPDATE=0;

    public ModelFileDelete(Context context
            ,Handler handler){
        _context=context;
        mHandler=handler;
    }

    private void sendHandlerMessage(int messageIdx,
                                    int arg1){
        if(mHandler==null) return;
        Message msg = mHandler.obtainMessage(
                messageIdx,arg1,-1);

        mHandler.sendMessage(msg);
    }

    public int DeleteFiles(ArrayList<String> filePaths){
        Log.i("CKIT", "Model::DeleteFiles");
        int retVal;
        int mmCount=0;
        int idx;
        for(idx=0;idx<filePaths.size();idx++) {
            retVal = DeleteFile(filePaths.get(idx));
            if(retVal==1) mmCount++;
            sendHandlerMessage(DELETE_COUNT_UPDATE,
                    mmCount);
        }
        return mmCount;
    }

    public int DeleteFile(String filePath){
        try {
            FileOperations.DeleteFile(filePath);
            return 1;
        }
        catch(Exception ex){
            return 0;
        }
    }
}
