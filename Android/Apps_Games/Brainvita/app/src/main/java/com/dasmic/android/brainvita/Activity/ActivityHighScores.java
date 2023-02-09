package com.dasmic.android.brainvita.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.dasmic.android.brainvita.Data.HighScores;
import com.dasmic.android.brainvita.Data.SingleHighScore;
import com.dasmic.android.brainvita.R;
import com.dasmic.android.lib.support.Static.DateOperations;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 4/26/2016.
 */
public class ActivityHighScores extends AppCompatActivity {
    private final int maxScores=10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_highscores);
        setLocalVariablesAndEventHandlers();
        setUIandShowHighScores();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setUIandShowHighScores(){
        float density = getResources().getDisplayMetrics().density;
        int idx=0;
        HighScores hs = new HighScores(this);
        ArrayList<SingleHighScore> allHs= hs.getAllScores();
        TableLayout tlMain = (TableLayout) findViewById(
                R.id.textTableLayout);
        TableLayout.LayoutParams lpTableRow = new
                TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT,
                1f);
        TableRow.LayoutParams lpTextView = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f);

        //Set device independent
        lpTableRow.setMargins((int) (0.0f * density),
                (int) (0.0f * density),
                (int) (0.0f * density),
                (int) (5.0f * density));

        for(int row=0;row<maxScores;row++) {
            //Add new LinearLayout
            TableRow tr=new TableRow(this);
            //ll.setOrientation(LinearLayout.HORIZONTAL);
            tlMain.addView(tr,lpTableRow);
            //--------------------------
            TextView tv1 = new TextView(this);
            tv1.setLayoutParams(lpTextView);
            tv1.setTextSize(21);
            tv1.setGravity(Gravity.CENTER);
            tr.addView(tv1);

            if(allHs != null) {
                if (row < allHs.size())
                    tv1.setText(String.valueOf(allHs.get(row).Count));
                else
                    tv1.setText(R.string.text_high_score_none);
            }
            else
                tv1.setText(R.string.text_high_score_none);
            //--------------------------
            TextView tv2 = new TextView(this);
            tv2.setLayoutParams(lpTextView);
            tv2.setTextSize(21);
            tv2.setGravity(Gravity.CENTER);
            tr.addView(tv2);
            if(row<allHs.size())
                tv2.setText(
                    DateOperations.getFormattedTime(
                            allHs.get(row).Time));
            else
                tv2.setText(R.string.text_high_score_none);
        }
    }

    private void setLocalVariablesAndEventHandlers() {
        Button button = (Button) findViewById(
                R.id.btnClose);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
