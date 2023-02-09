package com.dasmic.android.lib.audio.Comparators;



import com.dasmic.android.lib.audio.Data.DataAudioDisplay;

import java.util.Comparator;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ComparatorLastModified implements Comparator<DataAudioDisplay> {

    public ComparatorLastModified(){

    }

    @Override
    public int compare(DataAudioDisplay o1, DataAudioDisplay o2) {
        if(o1.LastModifiedDate == o2.LastModifiedDate) return 0;
        return o1.LastModifiedDate < o2.LastModifiedDate ? -1:1;
    }
}
