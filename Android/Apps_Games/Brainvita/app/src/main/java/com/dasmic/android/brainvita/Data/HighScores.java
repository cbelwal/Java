package com.dasmic.android.brainvita.Data;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dasmic.android.lib.support.Static.AppSettings;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 5/14/2016.
 */
public class HighScores {
    private final String prefName="highscores";

    private ArrayList<SingleHighScore> _allScores;
    private Activity _activity;

    private void readScoresFromFile(){
        String scoresS= PreferenceManager.getDefaultSharedPreferences(_activity).
                getString(prefName,"");

        if(scoresS != "") {
            _allScores = (ArrayList<SingleHighScore>) new Gson().fromJson(
                    scoresS,
                    new TypeToken<ArrayList<SingleHighScore>>() {
                    }.getType());
        }
        else
            _allScores=new ArrayList<>();
    }

    private void persistScores(){
        String jsonS=getJSONString();
        //Save to Pref.
        SharedPreferences.Editor prefEdit=
                PreferenceManager.getDefaultSharedPreferences(_activity).edit();
        prefEdit.putString(prefName, jsonS);
        prefEdit.commit();

    }

    private String getJSONString(){
        Gson gson = new GsonBuilder().create();
        JsonArray jArray =
                gson.toJsonTree(_allScores).getAsJsonArray();
        return jArray.toString();
    }

    private void cleanExtraScores(){
        if(_allScores.size()<9) return;
        for(int ii=9;ii<_allScores.size();ii++)
            _allScores.remove(ii);
    }

    public HighScores(Activity activity){
        _activity=activity;
        readScoresFromFile();

    }

    public boolean addScore(int count, long time){
        int idx=0;
        for(SingleHighScore shs:_allScores)
        {
            if(shs != null){
                if(count < shs.Count){
                    break;
                }
                if(count == shs.Count){
                    if(time < shs.Time)
                        break;
                }
                idx++;
            }

        }
        if(idx < 9) {//there is space
            SingleHighScore newShs = new SingleHighScore(count,time);
            _allScores.add(idx,newShs);
            //Store all scores
            cleanExtraScores();//Remove scores more than 100
            persistScores();

            return true;
        }


        return false;
    }



    public ArrayList<SingleHighScore> getAllScores(){
        return _allScores;
    }
}
