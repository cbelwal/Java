package com.dasmic.android.brainvita.Data;

import com.dasmic.android.brainvita.Enum.BlockState;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 5/22/2016.
 */
public class BoardFrench extends BoardBase{

    public BoardFrench(final OnBoardDisplayStateChanged listnerBoardStateChange,
                     final OnBoardIsStale listenerBoardStale,
                     final OnBoardInitialized listenerBoardInitialized){
        super(listnerBoardStateChange,
                listenerBoardStale,
                listenerBoardInitialized);
        mMiniBoard = new MiniBoardFrench();
    }

    @Override
    public void init(){
        mBoard=new BlockState[mSize][mSize];

        for(byte row = 0; row< mSize; row++)
            for(byte col = 0; col< mSize; col++){
                setValue(row,col,BlockState.isPeg);
            }
        setValue((byte)2,(byte)3,BlockState.isPegHole); //For french board is peg hole is different

        mListenerBoardInitialized.onBoardInitialized();
    }

    //Set up for Standard Board
    @Override
    protected void setValue(byte row, byte col, BlockState value)
    {
        SupportFunctions.DebugLog("BoardFrench","setValue","Row:"+row+",Col:"+col);
        mBoard[row][col] = BlockState.isInValidLocation;
        if(row<0 || col <0) return;//Dont do anything
        if(row >=2 && row <=4){ //Fill all columns
            mBoard[row][col] = value;
        }
        else{
            if(row==0 || row==mSize-1) { //first and last rows
                if (col >= 2 && col <= 4)
                    mBoard[row][col] = value;
            }
            if(row==1 || row==mSize-2) { //second and second last rows
                if (col >= 1 && col <= 5)
                    mBoard[row][col] = value;
            }
        }
        mListenerBoardStateChanged.onBoardStateChanged(row,col);
    }
}
