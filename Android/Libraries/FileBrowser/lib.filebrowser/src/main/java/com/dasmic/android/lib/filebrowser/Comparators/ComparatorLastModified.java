package com.dasmic.android.lib.filebrowser.Comparators;

import com.dasmic.android.lib.filebrowser.Data.DataFileDisplay;

import java.util.Comparator;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ComparatorLastModified implements Comparator<DataFileDisplay> {

    public ComparatorLastModified(){

    }

    @Override
    public int compare(DataFileDisplay o1, DataFileDisplay o2) {
        if(o1.LastModifiedDate == o2.LastModifiedDate) return 0;
        return o1.LastModifiedDate < o2.LastModifiedDate ? -1:1;
    }
}
