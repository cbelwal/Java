package com.dasmic.android.lib.support.Static;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Chaitanya Belwal on 9/6/2015.
 */
public  class DateOperations {

    public static long getMilliSecondForDays(long days){
        return(days * 24 * 60 * 60 * 1000);
    }

    public static long getCurrentMilliseconds(){
        Date curDate = new Date();
        return curDate.getTime();
    }

    public static int getDayOfMonth(long milliSeconds){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSeconds);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int getMonth(long milliSeconds){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSeconds);
        return cal.get(Calendar.MONTH);
    }

    public static int getYear(long milliSeconds){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSeconds);
        return cal.get(Calendar.YEAR);
    }

    //Get Hours in 12 Hour format
    public static int getHour12Hr(long milliSeconds){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSeconds);
        return cal.get(Calendar.HOUR);
    }

    // Get hour in 24 hr format
    public static int getHour24Hr(long milliSeconds){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSeconds);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute(long milliSeconds){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSeconds);
        return cal.get(Calendar.MINUTE);
    }

    public static int getSecond(long milliSeconds){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSeconds);
        return cal.get(Calendar.SECOND);
    }

    //Hour is in 24 hr format
    public static long getMilliseconds(int year,int month, int dayOfMonth,
                                            int hour, int minute, int second){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth,
                hour,minute, second);
        return calendar.getTimeInMillis();
    }

    public static long getYearMilliseconds(){
        return getCurrentMilliseconds() - getMilliSecondForDays(365);
    }

    public static long get7DayMilliseconds(){
        return getCurrentMilliseconds() - getMilliSecondForDays(7);
    }

    public static long get30DayMilliseconds(){
        return getCurrentMilliseconds() - getMilliSecondForDays(30);
    }

    public static String getFormattedDate(long date){
        if(date==0) return "NA";
        Date usrDate = new Date(date);
        return usrDate.toString();
    }

    public static String getFormattedDateForFileName(long date){
        if(date==0) return "NA";
        SimpleDateFormat simpleDateFormat = new
                SimpleDateFormat("MMM_dd_yyyy");
        return simpleDateFormat.format(date);
    }

    public static String getCurrentFormattedDateForFileName(){
        return getFormattedDateForFileName(getCurrentDate());
    }


    public static String getFormattedTime(long value){
        if(value==0) return "NA";
        return String.format("%d:%d:%d",
                TimeUnit.MILLISECONDS.toHours(value),
                TimeUnit.MILLISECONDS.toMinutes(value),
                TimeUnit.MILLISECONDS.toSeconds(value));

    }

    public static long getCurrentDate(){
        return getCurrentMilliseconds();
    }

}
