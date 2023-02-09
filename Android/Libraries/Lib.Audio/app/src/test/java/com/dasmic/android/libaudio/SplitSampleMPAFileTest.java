package com.dasmic.android.libaudio;

import com.dasmic.android.lib.audio.Model.SoundFile;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class SplitSampleMPAFileTest {
    //@Test
    //public void addition_isCorrect() {
    //    assertEquals(4, 2 + 2);
    //}

    @Test
    public void splitSampleM4a(){
        String inputFileName = ExportImportTestAdapter.getTestdataFolder() + "MyVoiceTest.m4a";
        String outputFileName = ExportImportTestAdapter.getTestdataFolder() + "Split_1_6s_.m4a";
        SoundFile soundFile=null;
        try {
            File fIn = new File(inputFileName);
            soundFile = SoundFile.create(inputFileName, null);
        }
        catch(Exception ex){
            SupportFunctions.DebugLog("SplitSampleMPAFileText",
                                        "splitSampleM4a","Error");
        }
        try {
            float start=1f,end=6f;
            File fOut = new File(outputFileName);
            soundFile.WriteFile(fOut, start, end);
        }
        catch(Exception ex){

        }
    }

}