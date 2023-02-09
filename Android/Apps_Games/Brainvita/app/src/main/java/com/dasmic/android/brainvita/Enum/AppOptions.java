package com.dasmic.android.brainvita.Enum;

/**
 * Created by Chaitanya Belwal on 4/27/2016.
 */
public class AppOptions {
    public  static final int OPTIONS_ACTIVITY_REQUEST=101;
    public  static final int HIGHSCORE_ACTIVITY_REQUEST =103;
    public static final int STANDARD_BOARD=1001;
    public static final int FRENCH_BOARD=1003;

    public  static final String SELECTED_OPTION_IDENTIFIER ="SELECTED_OPTION";
    public  static boolean isFreeVersion=true;
    public  static int MinimumSolverPegCount =3;

    public static int currentBoardType=STANDARD_BOARD;
    public static final boolean IS_FOR_AMAZON=false;
}
