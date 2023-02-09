package com.dasmic.android.lib.audio.Comparators;

import com.dasmic.android.lib.audio.Data.DataAudioDisplay;

import java.util.Comparator;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ComparatorSize implements Comparator<DataAudioDisplay> {


    public ComparatorSize(){

    }

    @Override
    public int compare(DataAudioDisplay o1, DataAudioDisplay o2) {
         if(o1.Size == o2.Size) return 0;
         return o1.Size < o2.Size ? -1:1;
    }
}
