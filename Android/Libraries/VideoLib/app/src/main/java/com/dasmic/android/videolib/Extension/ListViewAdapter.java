package com.dasmic.android.videolib.Extension;

/**
 * Created by Chaitanya Belwal on 8/10/2015.
 */

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.videolib.Activity.ActivityBaseMain;
import com.dasmic.android.videolib.Data.DataVideoDisplay;
import com.dasmic.android.videolib.Interface.IGenericEvent;
import com.dasmic.android.videolib.Interface.IGenericParameterLessEvent;
import com.dasmic.android.videolib.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ListViewAdapter extends
        ArrayAdapter<DataVideoDisplay> implements Filterable{

    static class ViewHolder {
        CheckedTextView ctv;
        TextView tvSecondary;
        TextView tvTertiary;
        ImageView iv;
        LinearLayout ll;
    }


    private final ActivityBaseMain _activityBaseMain;
    private final ArrayList<DataVideoDisplay> _contactsData;
    private ArrayList<DataVideoDisplay> _filteredData;
    private IGenericParameterLessEvent _onFilterPublishResults;
    private IGenericEvent _onCheckBoxClicked;

    @Override
    public boolean hasStableIds() {
        return true;
    } //Do not remove this

    public void setListener(IGenericParameterLessEvent listener){
        this._onFilterPublishResults = listener;
    }

    public void setListener(IGenericEvent listener){
        this._onCheckBoxClicked = listener;
    }


    public ListViewAdapter(ActivityBaseMain activityBaseMain, ArrayList<DataVideoDisplay> contactsData) {
        super(activityBaseMain, R.layout.ui_listviewitem_fb, contactsData);
        // TODO Auto-generated constructor stub
        _activityBaseMain =activityBaseMain;
        _contactsData = contactsData;
        _filteredData=contactsData;
    }


    public View getView(int position,View rowView, ViewGroup parent) {
        Log.i("ListViewAdapter", "getView()");
        //View rowView;
        DataVideoDisplay dvd;
        ViewHolder holder;

        if(rowView == null) {
            Log.i("New Contact", "getView()");
            LayoutInflater inflater= _activityBaseMain.getLayoutInflater();
            rowView = inflater.inflate(R.layout.ui_listviewitem_fb, null, true);
            holder = new ViewHolder();
            holder.ctv=(CheckedTextView) rowView.findViewById(R.id.checkedTextView);
            holder.tvSecondary =(TextView) rowView.findViewById(R.id.secondaryTextView);
            holder.tvTertiary =(TextView) rowView.findViewById(R.id.tertiaryTextView);
            holder.iv =(ImageView) rowView.findViewById(R.id.imageView);
            holder.ll=(LinearLayout) rowView.findViewById(R.id.mainLayout);
            rowView.setTag(holder);
        }
        else {
            holder= (ViewHolder)rowView.getTag();
        }
        dvd=_filteredData.get(position);

        CheckedTextView chTextView = holder.ctv;
        chTextView.setText(dvd.toString());


        //if top level folder to now show checked
        //CB: Commented on 8/25/2018 - not sure why this is needed
        /*
        if(dvd.isGotoParentFolder()) {
            chTextView.setCheckMarkDrawable(null);
            SupportFunctions.DebugLog("ListViewAdapter",
                    "getView","GotoParentFolder:"+dvd.getName());
        }
        else {
            //chTextView.setChecked(false);
            chTextView.setCheckMarkDrawable(getDefaultCheckboxResourceId());

        }*/



        chTextView.setTag(dvd);
        chTextView.setChecked(dvd.IsChecked);
        //textView.setFocusable(false);
        chTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
                ((DataVideoDisplay) v.getTag()).IsChecked = ((CheckedTextView) v).isChecked();
                //Raise event
                _onCheckBoxClicked.onEvent(((DataVideoDisplay) v.getTag()).IsChecked ? 1 : 0);
                //DebugLog.i("CKIT","Selection Raw Contact ID: " + ((DataPackageDisplay) v.getTag()).getRawContactId());
            }
        });

        TextView tvSecondary = holder.tvSecondary;//(TextView) rowView.findViewById(R.id.secondaryTextView);
        tvSecondary.setText(dvd.getSecondaryValue());
        tvSecondary.setTag(chTextView);
        tvSecondary.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) { //Simulate clock on checkedTextView
                                               ((CheckedTextView) v.getTag()).performClick();
                                           }
                                       }
        );

        TextView tvTertiary = holder.tvTertiary;//(TextView) rowView.findViewById(R.id.secondaryTextView);
        tvTertiary.setText(dvd.getTertiaryValue());
        tvTertiary.setTag(chTextView);
        tvTertiary.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) { //Simulate clock on checkedTextView
                                              ((CheckedTextView)
                                                      v.getTag()).performClick();}
                                      }
        );

        ImageView iv = holder.iv;//(QuickContactBadge) rowView.findViewById(R.id.contactBadge);
        iv.setTag(dvd);
        iv.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) { //When Image is clicked do some action base on type
                                      DataVideoDisplay dvd =
                                              (DataVideoDisplay) v.getTag();
                                      _activityBaseMain.onClickItem(dvd);
                                  }
                              }
        );


        Drawable icon = getDrawableObject(dvd);
        if (icon != null)
            iv.setImageDrawable(icon);

        LinearLayout ll = holder.ll;
        ll.setBackgroundColor(getColor(dvd));

        return rowView;
    }

    private int getDefaultCheckboxResourceId(){
        ViewHolder holder;

        TypedValue value = new TypedValue();
        Resources.Theme theme =  getContext().getTheme();
        int checkMarkDrawableResId;

        theme.resolveAttribute(android.R.attr.listChoiceIndicatorMultiple, value, true);
        checkMarkDrawableResId = value.resourceId;

        return checkMarkDrawableResId;
    }

    private Drawable getDrawableObject(DataVideoDisplay dfd){
        //Is a Video file
        return getSpecialFileThumbnail(dfd);

    }

    private Drawable getSpecialFileThumbnail(DataVideoDisplay dvd){
        String fileName =dvd.getName().trim().toUpperCase();
        Drawable d;
        if(!fileName.endsWith("MP4"))
            d = Drawable.createFromPath(dvd.AbsoluteFilePath);
        else
            d = getVideoFileThumbnail(dvd);
        return d;
    }


    private Drawable getVideoFileThumbnail(DataVideoDisplay dfd){
      return dfd.getThumbnail(_activityBaseMain);
    }

    //Construct color based on data
    private int getColor(DataVideoDisplay dfd){
        int red=Color.red(_activityBaseMain.getResources().getColor(R.color.BaseRed));
        int green=Color.green(_activityBaseMain.getResources().getColor(R.color.BaseGreen));
        int blue=Color.blue(_activityBaseMain.getResources().getColor(R.color.BaseBlue));
        int alpha=Color.alpha(_activityBaseMain.getResources().getColor(R.color.BaseAlpha)); //Do not change this as in Adrnoid 5.0 this can lead to


        if(!dfd.HasReadPermission)
            red=red+90;

        //green = green + (int)Math.floor(pkg.getSize() % 100);
        int color= Color.argb(alpha,red,green,blue);
        return color;
    }

    private void LaunchActivity(String packageName,
                                String launchIntentClass){
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);


        final ComponentName cn = new ComponentName(packageName,
                launchIntentClass);
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            _activityBaseMain.startActivity(intent);
            SupportFunctions.DisplayToastMessageLong(_activityBaseMain,
                    _activityBaseMain.getResources().getString(
                            R.string.message_activity_loading));
        }
        catch(Exception ex){
            SupportFunctions.DisplayToastMessageLong(_activityBaseMain,
                    _activityBaseMain.getResources().getString(
                            R.string.message_activity_load_fail));
        }
    }

    @Override
    public int getCount() {
        //if()
        return _filteredData != null? _filteredData.size() : 0;
    }

    @Override
    public DataVideoDisplay getItem(int position) {
        //if()
        return _filteredData != null? _filteredData.get(position) :
                _contactsData.get(position);
    }


    @Override
    public Filter getFilter() {
        Log.i("getFilter","ListViewAdapter");
        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                final ArrayList<DataVideoDisplay> currentData = _contactsData;

                int count = currentData.size();
                final ArrayList<DataVideoDisplay> filteredData =
                        new ArrayList<DataVideoDisplay>();

                DataVideoDisplay filterableData ;

                for (int i = 0; i < count; i++) {
                    filterableData = currentData.get(i);
                    //Search by current Value or name
                    if (filterableData.toString().toLowerCase().contains(filterString) ||
                            filterableData.getSecondaryValue().toLowerCase().contains(filterString)) {
                        filteredData.add(filterableData);
                    }
                }

                results.values = filteredData;
                results.count = filteredData.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.i("publishResult","ListViewAdapter");
                //_filter = constraint.toString();
                _filteredData = (ArrayList<DataVideoDisplay>) results.values;
                _onFilterPublishResults.onEvent();
                notifyDataSetChanged();

            }
        };
        return filter;
    }
}