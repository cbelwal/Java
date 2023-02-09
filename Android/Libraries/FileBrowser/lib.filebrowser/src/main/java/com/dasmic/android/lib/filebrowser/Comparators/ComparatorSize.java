package com.dasmic.android.lib.filebrowser.Comparators;

import com.dasmic.android.lib.filebrowser.Data.DataFileDisplay;

import java.util.Comparator;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ComparatorSize implements Comparator<DataFileDisplay> {


    public ComparatorSize(){

    }

    @Override
    public int compare(DataFileDisplay o1, DataFileDisplay o2) {
         if(o1.Size == o2.Size) return 0;
         return o1.Size < o2.Size ? -1:1;
    }
}
