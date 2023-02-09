package com.dasmic.android.brainvita.ViewModel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.dasmic.android.brainvita.Data.MiniBoardBase;
import com.dasmic.android.brainvita.Data.MiniBoardStandard;
import com.dasmic.android.brainvita.Enum.AppOptions;
import com.dasmic.android.brainvita.Model.SolverEngineBase;
import com.dasmic.android.brainvita.Model.SolverEngineFrench;
import com.dasmic.android.brainvita.Model.SolverEngineStandard;
import com.dasmic.android.brainvita.R;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 5/4/2016.
 */
 public class VMSolverAsyncTask extends AsyncTask<Void, Integer, Void> {
    OnAsyncTaskComplete mAsyncTaskCompleteListener;
    MiniBoardStandard mStartBoard;
    SolverEngineBase mSe;
    ProgressDialog pd;
    Activity mActivity;
    String mMessage;

    private final SolverEngineStandard.OnSolverUpdate
            mSolverUpdate = new
            SolverEngineStandard.OnSolverUpdate(){
                public void onSolverUpdate(int stateCount,
                                           int lowestCount) {
                    publishProgress(stateCount, lowestCount);
                    try {
                        //Thread.sleep(500);
                    }
                    catch(Exception ex){

                    }
                }
            };

    VMSolverAsyncTask( Activity activity,
                        MiniBoardStandard startBoard,
                      OnAsyncTaskComplete asyncTaskCompleteListener)
    {
        super();
        mStartBoard=startBoard;
        mAsyncTaskCompleteListener=asyncTaskCompleteListener;
        mActivity=activity;
    }

    public interface OnAsyncTaskComplete {
        void onAsyncTaskComplete(ArrayList<MiniBoardBase> allStates,
                              int count);
    }


    @Override
    protected Void doInBackground(Void... params) {
        if(AppOptions.currentBoardType==AppOptions.STANDARD_BOARD)
            mSe = new SolverEngineStandard(mSolverUpdate);
        else
            mSe = new SolverEngineFrench(mSolverUpdate);
        mSe.startSolver(mStartBoard);
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        super.onPostExecute(param);
        SupportFunctions.DebugLog("AsyncTask","PostExecute","Entry");
        mAsyncTaskCompleteListener.onAsyncTaskComplete(
                mSe.getCurrentLowestBoard(),mSe.getCurrentLowestCount());
        mSe=null;
        pd.hide();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if(pd == null) {
            initProgressBar();
        }
        else if (!pd.isShowing()) {
            initProgressBar();
        }

    }

    private void onCancel(){
        if(mSe != null) mSe.abortComputation();
    }

    private void initProgressBar(){
        int currentCount = mStartBoard.getPegCount();
        mMessage= mActivity.getString(R.string.message_solver_inprogress);
        pd = new ProgressDialog(mActivity);
        pd.setMessage(mMessage+ currentCount);
        pd.setTitle(mActivity.getString(R.string.message_solver_inprogress_title));
        pd.setCancelable(false);
        pd.setButton(DialogInterface.BUTTON_NEGATIVE,
                mActivity.getString(R.string.button_abort),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCancel();
                dialog.dismiss();
            }
        });
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMax(SolverEngineStandard.MAX_STATES);
        pd.show();
    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        pd.setMessage(mMessage + params[1].toString());
        pd.setProgress(params[0]);
    }
}
