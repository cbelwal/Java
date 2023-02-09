package com.dasmic.android.lib.filebrowser.Data;

import android.os.Parcel;
import android.os.Parcelable;

import com.dasmic.android.lib.support.InAppPurchase.InAppPurchases;

/**
 * Created by Chaitanya Belwal on 10/14/2017.
 */

//Contains data that needs to passed to Initialize activity
public class ActivityStartupData implements Parcelable {
    public String HelpURL;
    public String Demo_video_id;
    public String Base64EncodedPublicKey;
    public String Paid_version_sku_id;
    public String Ad_interstitial_id;
    public  String StartupFolderPath;
    public  String TellAFriendText;
    public int StartupFolderType; //0=External Storage, 1 = Internal Storage
    public String FileProviderAuthority;
    public int IdNavigationMenu;
    public InAppPurchases InAppPurchases;
    //public int FreeVersion;
    public int ForAmazon;


    public ActivityStartupData() {
    }

    //parcel part
    public ActivityStartupData(Parcel in){
        String[] data= new String[10];
        in.readStringArray(data);

        HelpURL = data[0];
        Demo_video_id= data[1];
        Base64EncodedPublicKey= data[2];
        Paid_version_sku_id=data[3];
        Ad_interstitial_id=data[4];
        StartupFolderPath=data[5];
        TellAFriendText =data[6];
        FileProviderAuthority = data[7];
        ForAmazon = Integer.parseInt(data[8]);
        StartupFolderType = Integer.parseInt(data[9]);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeStringArray(new String[]{
                                    HelpURL,
                                    Demo_video_id,
                                    Base64EncodedPublicKey,
                                    Paid_version_sku_id,
                                    Ad_interstitial_id,
                                    StartupFolderPath,
                                    TellAFriendText,
                                    FileProviderAuthority,
                                    String.valueOf(ForAmazon),
                                    String.valueOf(StartupFolderType)});
    }

    public static final Parcelable.Creator<ActivityStartupData> CREATOR= new Parcelable.Creator<ActivityStartupData>() {
        @Override
        public ActivityStartupData createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new ActivityStartupData(source);  //using parcelable constructor
        }

        @Override
        public ActivityStartupData[] newArray(int size) {
            // TODO Auto-generated method stub
            return new ActivityStartupData[size];
        }
    };
}
