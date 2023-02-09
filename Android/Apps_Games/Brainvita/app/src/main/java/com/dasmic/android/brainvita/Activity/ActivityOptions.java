package com.dasmic.android.brainvita.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dasmic.android.brainvita.Enum.AppOptions;
import com.dasmic.android.brainvita.Enum.GameOptions;
import com.dasmic.android.brainvita.R;

/**
 * Created by Chaitanya Belwal on 4/26/2016.
 */
public class ActivityOptions extends AppCompatActivity {
    GameOptions _selectedGameOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CKIT", "ActivityMain is created");
        setContentView(R.layout.ui_options);
        setLocalVariablesAndEventHandlers();
    }


    private void onButtonHighScores() {
        _selectedGameOption =GameOptions.HighScores;
        onClose();
    }

    private void onButtonRateThisApp() {
        _selectedGameOption =GameOptions.RateThisApp;
        onClose();
    }

    private void onHelpDocument() {
        _selectedGameOption =GameOptions.Help;
        onClose();
    }

    private void onPaidVersion() {
        _selectedGameOption =GameOptions.PurchasePaidVersion;
        onClose();
    }

    private void onDemoVideo() {
        _selectedGameOption =GameOptions.DemoVideo;
        onClose();
    }

    private void onSwitchBoard() {
        String message;
        if(AppOptions.currentBoardType==AppOptions.STANDARD_BOARD)
            message=this.getResources().getString(
                    R.string.message_confirm_switch_from_standard);
        else
            message=this.getResources().getString(
                    R.string.message_confirm_switch_from_French);

        displayBoardSwitchConfirmMessage(message);
    }


    private void onTellAFriend() {
        _selectedGameOption =GameOptions.TellAFriend;
        onClose();
    }

    private void onClose(){
        Intent resultData = getIntent();
        resultData.putExtra(AppOptions.SELECTED_OPTION_IDENTIFIER,
                _selectedGameOption.ordinal());
        setResult(RESULT_OK, resultData);
        finish();
    }

    private void callFunctionOnConfirmation(GameOptions selectedGameOption){
            _selectedGameOption = selectedGameOption;
            onClose();
    }


    private void displayBoardSwitchConfirmMessage(String message){
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        //dlgAlert.setTitle(this.getResources().getText(R.string.title_confirm));
        dlgAlert.setMessage(message);
        dlgAlert.setCancelable(false);

        dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if(AppOptions.currentBoardType==AppOptions.FRENCH_BOARD)
                    AppOptions.currentBoardType=AppOptions.STANDARD_BOARD;
                else
                    AppOptions.currentBoardType=AppOptions.FRENCH_BOARD;
                _selectedGameOption=GameOptions.SwitchBoardType;
                onClose();
                dialog.dismiss();
            }
        });

        dlgAlert.setNegativeButton(
                this.getResources().getText(R.string.button_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = dlgAlert.create();
        alert.show();
    }


    private void displayBoardResetConfirmMessage(final GameOptions selectedGameOption){
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        //dlgAlert.setTitle(this.getResources().getText(R.string.title_confirm));
        dlgAlert.setMessage(
                String.valueOf(this.getResources().getText(
                        R.string.message_board_reset_confirm)));
        dlgAlert.setCancelable(false);

        dlgAlert.setPositiveButton(this.getResources().getText(R.string.button_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                callFunctionOnConfirmation(selectedGameOption);
                dialog.dismiss();
            }
        });

        dlgAlert.setNegativeButton(
                this.getResources().getText(R.string.button_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = dlgAlert.create();
        alert.show();
    }

    private void setLocalVariablesAndEventHandlers() {
        Button button = (Button) findViewById(
                R.id.btnNewGame);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBoardResetConfirmMessage(GameOptions.NewGame);
            }
        });

        button = (Button) findViewById(
                R.id.btnSwitchBoard);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSwitchBoard();
            }
        });

        button = (Button) findViewById(
                R.id.btnTellAFriend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTellAFriend();
            }
        });

        button = (Button) findViewById(
                R.id.btnCustomBoard);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayBoardResetConfirmMessage(GameOptions.SetCustomBoard);
            }
        });

        button = (Button) findViewById(
                R.id.btnHighScores);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonHighScores();
            }
        });


        button = (Button) findViewById(
                R.id.btnRateThisApp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonRateThisApp();
            }
        });

        button = (Button) findViewById(
                R.id.btnDemoVideo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDemoVideo();
            }
        });

        button = (Button) findViewById(
                R.id.btnPaidVersion);
        if(AppOptions.isFreeVersion)
            button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPaidVersion();
            }
        });
        else
            button.setVisibility(View.GONE);

        button = (Button) findViewById(
                R.id.btnHelpDocument);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onHelpDocument();
            }
        });

        button = (Button) findViewById(
                R.id.btnTellAFriend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTellAFriend();
            }
        });

    }


}
