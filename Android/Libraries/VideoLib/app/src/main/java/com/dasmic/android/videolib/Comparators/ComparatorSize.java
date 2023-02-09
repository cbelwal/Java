package com.dasmic.android.videolib.Comparators;

import com.dasmic.android.videolib.Data.DataVideoDisplay;

import java.util.Comparator;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ComparatorSize implements Comparator<DataVideoDisplay> {


    public ComparatorSize(){

    }

    @Override
    public int compare(DataVideoDisplay o1, DataVideoDisplay o2) {
         if(o1.Size == o2.Size) return 0;
         return o1.Size < o2.Size ? -1:1;
    }
}
