package com.dasmic.android.brainvita.Data;

import com.dasmic.android.brainvita.Enum.BlockState;

/**
 * Created by Chaitanya Belwal on 4/24/2016.
 */
public class DataBlock {
    private byte mCol;
    private byte mRow;
    private BlockState mBlockState;

    public DataBlock(byte blockRow, byte blockCol){
        mCol =blockCol;
        mRow =blockRow;
    }

    public DataBlock(){
        setNullValues();
    }

    public DataBlock(byte blockRow, byte blockCol,BlockState bs){
        mCol =blockCol;
        mRow =blockRow;
        mBlockState =bs;
    }

    public void setValues(byte blockRow, byte blockCol,BlockState bs)
    {
        mCol =blockCol;
        mRow =blockRow;
        mBlockState =bs;
    }

    public void setNullValues()
    {
        mCol =-1;
        mRow =-1;
        mBlockState =BlockState.isInValidLocation;
    }

    public byte getCol(){return mCol;}
    public byte getRow(){return mRow;}
    public BlockState getBS(){return mBlockState;}
}
