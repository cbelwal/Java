package com.dasmic.android.videolib.Comparators;



import com.dasmic.android.videolib.Data.DataVideoDisplay;

import java.util.Comparator;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ComparatorLastModified implements Comparator<DataVideoDisplay> {

    public ComparatorLastModified(){

    }

    @Override
    public int compare(DataVideoDisplay o1, DataVideoDisplay o2) {
        if(o1.LastModifiedDate == o2.LastModifiedDate) return 0;
        return o1.LastModifiedDate < o2.LastModifiedDate ? -1:1;
    }
}
