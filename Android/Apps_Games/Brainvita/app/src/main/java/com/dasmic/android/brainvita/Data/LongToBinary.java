package com.dasmic.android.brainvita.Data;

/**
 * Created by Chaitanya Belwal on 5/12/2016.
 */
public class LongToBinary {
    public long [] longBinary;
    private static LongToBinary mPtrSelf;
    private int mSize=7;

    public static LongToBinary getInstance(){
        if(mPtrSelf==null){
            mPtrSelf=new LongToBinary();
            mPtrSelf.initLongBinary();
        }


        return mPtrSelf;
    }

    private void initLongBinary(){
        longBinary=new long[mSize*mSize];
        for(int ii=0;ii<mSize*mSize;ii++)
        {
            longBinary[ii]=1L<<((mSize*mSize-1)-ii);
        }
    }

    private void LongToBinary(){

    }

}
