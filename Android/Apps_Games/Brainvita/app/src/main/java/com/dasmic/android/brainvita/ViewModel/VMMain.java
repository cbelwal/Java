package com.dasmic.android.brainvita.ViewModel;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dasmic.android.brainvita.Data.BoardBase;
import com.dasmic.android.brainvita.Data.BoardFrench;
import com.dasmic.android.brainvita.Data.BoardStandard;
import com.dasmic.android.brainvita.Data.DataBlock;
import com.dasmic.android.brainvita.Data.MiniBoardBase;
import com.dasmic.android.brainvita.Data.MiniBoardStandard;
import com.dasmic.android.brainvita.Enum.AppOptions;
import com.dasmic.android.brainvita.Enum.BlockState;
import com.dasmic.android.brainvita.R;
import com.dasmic.android.lib.support.Ad.Interstilial;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 4/23/2016.
 */
public class VMMain {
    Activity _activity;
    BoardBase mCurrentBoard;
    Chronometer mChrono;
    LinearLayout mBoardUILayout;
    ImageView [][] mViewBlocks;
    ArrayList<BlockState[][]> mListBoardHistory;
    int mIdxHistory;
    int mClickCount;
    boolean mIsFirstBoardForMessages;
    boolean mIsUseChrono;
    boolean mIsChronoStarted; //Do not deleted used when first clicked
    boolean mIsAllowBoardPlay;
    OnBoardStale mListenerBoardStale;
    OnSolverComplete mListenerSolverComplete;
    protected String _ad_interstitial_id;
    protected Interstilial _interstilial;

    private void updateImage(int row, int col){
        int resId;
        switch(mCurrentBoard.getImage(row,col)){
            case isPegHole:
                resId= R.drawable.peg_hole;
                break;
            case isPeg:
                resId= R.drawable.peg;
                break;
            case isReadyPegHole:
                resId= R.drawable.ready_peg_hole;
                break;
            case isPegSelected:
                resId= R.drawable.peg_selected;
                break;
            case isPegSelectedInvalid:
                resId= R.drawable.peg_selected_invalid;
                break;
            default:
                resId= R.drawable.invalid_location;
        }
        mViewBlocks[row][col].setImageResource(resId);
    }

    private void onBlockClicked(View view){
        if(!mIsAllowBoardPlay){
            SupportFunctions.DisplayToastMessageLong(_activity,
                    _activity.getString(R.string.message_solver_mode_play_board));
            return;//Dont do anything
        }
        DataBlock db = (DataBlock)view.getTag();
        if(mIsFirstBoardForMessages && mIsUseChrono) {
            mClickCount++;
            if(mClickCount==1)
                SupportFunctions.DisplayToastMessageLong(_activity,
                    _activity.getString(R.string.message_intro_second));
            if(mClickCount==2) {
                SupportFunctions.DisplayToastMessageLong(_activity,
                        _activity.getString(R.string.message_intro_third));
            }

            if(mClickCount==3) {
                SupportFunctions.DisplayToastMessageLong(_activity,
                        _activity.getString(R.string.message_intro_fourth));
                mIsFirstBoardForMessages =false; //All message displayed turn off
            }
        }

        if(!mIsChronoStarted) chronoStart();
        mCurrentBoard.setClick(db.getRow(),db.getCol());

        //Show Ad
        showInterstitialAd();
    }

    private void showInterstitialAd(){
        if(AppOptions.isFreeVersion){
            if(_ad_interstitial_id != null) { //If not even defined
                if (_interstilial == null && !_ad_interstitial_id.trim().equals("")) {
                    _interstilial = new Interstilial(_ad_interstitial_id, _activity);
                    _interstilial.SetShowOnceEveryCount(48); //Change to higher so as not to both user
                }
                if (_interstilial != null)
                    _interstilial.showAd(); //Show during load
            }
        }
    }


    private void cleanFutureBoardHistory()
    {
        for(int ii=mIdxHistory;ii<mListBoardHistory.size();ii++)
            mListBoardHistory.remove(mListBoardHistory.size()-1); //Delete last one
    }

    private void chronoStart(){
        if(mIsUseChrono && getIsCustomBoardSetup()==false) {
            mChrono.setBase(SystemClock.elapsedRealtime());

            mChrono.start();
            mIsChronoStarted=true;
        }
    }

    private void chronoStop(){
            mChrono.stop();
            mChrono.setBase(SystemClock.elapsedRealtime());
            mIsUseChrono=false;
            mIsChronoStarted=false;
            SupportFunctions.DebugLog("VMMain","chronoStop","Chrono Stopped");

    }


    private final BoardStandard.OnBoardIsStale
            mBoardIsStale = new
            BoardStandard.OnBoardIsStale(){
                public void onBoardIsStale() {
                    long elapsedTime=
                            SystemClock.elapsedRealtime()-mChrono.getBase();
                    chronoPause();//Keep on displaying time
                    mListenerBoardStale.onBoardStale(
                            mCurrentBoard.getCount() ,elapsedTime, mIsUseChrono);
                    mIsUseChrono =false;//Do not restart on some user action
                }
            };

    private final BoardStandard.OnBoardDisplayStateChanged
            mBoardStateChange = new
             BoardStandard.OnBoardDisplayStateChanged(){
                public void onBoardStateChanged(int row,
                                                int col) {
                    updateImage(row,col);
                }
            };

    private void storeCurrentBoardInHistory(){
        if(mCurrentBoard != null)
            storeInHistory(mCurrentBoard.getBoardState());
    }

    private void storeInHistory(BlockState[][] board ){
        //Save Current Board
        if(mIdxHistory < mListBoardHistory.size()-1) //Board in past
            cleanFutureBoardHistory(); //Clean all future state beyong current
        mListBoardHistory.add(board);
        mIdxHistory++;
    }

    private final BoardStandard.OnBoardInitialized
            mBoardInitialized = new
            BoardStandard.OnBoardInitialized(){
                public void onBoardInitialized() {
                    initBoardHistory();
                    storeCurrentBoardInHistory();
                }
            };

    private final BoardStandard.OnBoardDataStateChanged
            mBoardDataStateChanged = new
            BoardStandard.OnBoardDataStateChanged(){
                public void onBoardDataStateChanged() {
                    //Save Current Board
                    storeCurrentBoardInHistory();
                }
            };


    private final VMSolverAsyncTask.OnAsyncTaskComplete
            mAsyncTaskComplete = new
            VMSolverAsyncTask.OnAsyncTaskComplete(){
                public void onAsyncTaskComplete(
                        ArrayList<MiniBoardBase> allStates,
                                             int count) {
                    //Display message
                    String message;
                    if(AppOptions.isFreeVersion)
                        message=_activity.getString(R.string.message_solver_complete_free);
                    else
                        message=_activity.getString(R.string.message_solver_complete);
                    SupportFunctions.AsyncDisplayGenericDialog(_activity,message,
                            _activity.getString(R.string.message_solver_complete_title)+count);

                    //Save Current Board
                    storeMiniBoardsInHistory(allStates);
                    //Set display state to first board
                    displayBoardFromHistory(0);
                    //Raise event to tell client Solver done
                    mListenerSolverComplete.onSolverComplete();
                    }
            };

    private void displayBoardFromHistory(int idx)
    {
        if(mListBoardHistory.size() > idx)
            mCurrentBoard.setCurrentBoardStateTo(mListBoardHistory.get(idx));
        mIdxHistory=idx;
    }

    private void storeMiniBoardsInHistory(ArrayList<MiniBoardBase> allStates){
        initBoardHistory();
        MiniBoardBase miniBoard;
        for(int ii=allStates.size()-1;ii>=0;ii--) { //Start from first state
            miniBoard=allStates.get(ii);
            storeInHistory(miniBoard.getLargeBoard());
        }
    }

    private void initBoardHistory(){
        mListBoardHistory =new ArrayList<>();
        mIdxHistory=-1;
    }

    public VMMain(Activity activity,
                  LinearLayout boardLayout,
                  Chronometer chrono,
                  String  ad_interstitial_id,
                  final OnBoardStale listenerBoardStale,
                  final OnSolverComplete listenerSolverComplete) {
        _activity = activity;
        mListenerBoardStale = listenerBoardStale;
        mListenerSolverComplete=listenerSolverComplete;
        mBoardUILayout = boardLayout;
        mChrono=chrono;
        mIsFirstBoardForMessages =true;
        setBoardUI(); //NEVER CALL this TWICE make sure mBoard is Setup
        switchBoardStyle(); //CALL AFTER UI SETUP
        _ad_interstitial_id = ad_interstitial_id;

    }

    public void switchBoardStyle(){
        mCurrentBoard=null; //Make sure to delete old reference
        if(AppOptions.currentBoardType==
                AppOptions.STANDARD_BOARD)
            mCurrentBoard = new BoardStandard(mBoardStateChange,
                    mBoardIsStale,mBoardInitialized);
        else
            mCurrentBoard = new BoardFrench(mBoardStateChange,
                    mBoardIsStale,mBoardInitialized);

        mCurrentBoard.setDataStateListeners(null,
                mBoardDataStateChanged);

        newGame();
    }

    public void startSolver(){
        MiniBoardStandard mb=new MiniBoardStandard();
        mb.setFromLargeBoard(mCurrentBoard.getBoardState());
        chronoStop();
        mIsAllowBoardPlay =false;

        AsyncTask<Void,Integer,Void> aTask= new
                VMSolverAsyncTask(_activity,mb,
                mAsyncTaskComplete);
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void chronoResume(){
        if(mIsUseChrono && mIsChronoStarted) { //Dont do anything if not started
            mChrono.start();
        }
    }

    public void chronoPause(){
        if(mIsUseChrono) {
            mChrono.stop();
        }
    }

    public void newGame(){
        //initBoardHistory();
        chronoStop();
        mClickCount =0;
        mIsUseChrono =true;
        mIsAllowBoardPlay=true;
        mCurrentBoard.init(); //Will populate with defaultvalues

        if(mIsFirstBoardForMessages && mIsUseChrono && mClickCount ==0)
            SupportFunctions.DisplayToastMessageLong(_activity,
                    _activity.getString(R.string.message_intro_first));
    }

    public interface OnBoardStale {
        void onBoardStale(int count,
                            long time,
                          boolean recordHighScore);
    }

    public interface OnSolverComplete {
        void onSolverComplete();
    }

    public boolean getIsCustomBoardSetup(){
        return mCurrentBoard.getIsCustomBoardSetup();
    }

    public void setIsCustomBoard(boolean value){
        //initBoardHistory(); //Clean board History in either case
        mCurrentBoard.setIsCustomBoardSetup(value);
        if(value){
            chronoStop();
            mIsUseChrono =false;
            mIsAllowBoardPlay=true;
        }

    }

    public void goBack(){
        mIdxHistory--;
        if(mIdxHistory <= 0){
            mIdxHistory=0;
        }
        if(mListBoardHistory.size()>mIdxHistory)
            mCurrentBoard.setCurrentBoardStateTo(mListBoardHistory.get(mIdxHistory));
    }

    public void goForward(){
        if(mListBoardHistory.size() == 0) return;//Added for a crash, could not replicate it so added this generic check
        mIdxHistory++;
        if(mIdxHistory > mListBoardHistory.size()-1) mIdxHistory=mListBoardHistory.size()-1;
        if(mIdxHistory < mListBoardHistory.size())
            mCurrentBoard.setCurrentBoardStateTo(mListBoardHistory.get(mIdxHistory));
    }

    public void setBoardUI(){
        int size = 7;//Hard-code for now, very unlikely this will change

        float density =
                _activity.getResources().getDisplayMetrics().density;
        int idx=0;
        mViewBlocks= new ImageView[size][size];
        LinearLayout.LayoutParams lpLinearLayout = new
                LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        LinearLayout.LayoutParams lpImageView = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f);
        //Set device independent
        lpImageView.setMargins((int) (2.0f * density),
                (int) (2.0f * density),
                (int) (2.0f * density),
                (int) (2.0f * density));

        for(byte row=0;row<size;row++) {
            //Add new LinearLayout
            LinearLayout ll=new LinearLayout(_activity);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(lpLinearLayout);
            mBoardUILayout.addView(ll);
            for (byte col = 0; col < size; col++) {
                mViewBlocks[row][col] = new ImageView(_activity);
                mViewBlocks[row][col].setLayoutParams(lpImageView);
                mViewBlocks[row][col].setTag(new DataBlock(row,col));
                mViewBlocks[row][col].setScaleType(ImageView.ScaleType.FIT_CENTER);

                mViewBlocks[row][col].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBlockClicked(view);
                    }
                });
                ll.addView(mViewBlocks[row][col]);
            }
        }

    }
}
