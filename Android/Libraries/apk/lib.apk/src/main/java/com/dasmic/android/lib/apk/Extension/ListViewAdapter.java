package com.dasmic.android.lib.apk.Extension;

/**
 * Created by Chaitanya Belwal on 8/10/2015.
 */
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
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


import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Interface.IGenericEvent;
import com.dasmic.android.lib.apk.Interface.IGenericParameterLessEvent;
import com.dasmic.android.lib.apk.R;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ListViewAdapter extends
        ArrayAdapter<DataPackageDisplay> implements Filterable{

    static class ViewHolder {
        CheckedTextView ctv;
        TextView tvSecondary;
        TextView tvTertiary;
        ImageView iv;
        LinearLayout ll;
    }


    private final Activity _context;
    private final ArrayList<DataPackageDisplay> _contactsData;
    private ArrayList<DataPackageDisplay> _filteredData;
    private IGenericParameterLessEvent _onFilterPublishResults;
    private IGenericEvent _onCheckBoxClicked;

    private Bitmap getImageBitmap(Uri imageURI){
        if(imageURI == null) return null;
        AssetFileDescriptor afd = null;
        try {
            afd = _context.getContentResolver().
                    openAssetFileDescriptor(imageURI, "r");
        /*
         * Gets a file descriptor from the asset file descriptor.
         * This object can be used across processes.
         */
            FileDescriptor fileDescriptor = afd.getFileDescriptor();
            // Decode the photo file and return the result as a Bitmap
            // If the file descriptor is valid
            if (fileDescriptor != null) {
                // Decodes the bitmap
                return BitmapFactory.decodeFileDescriptor(
                        fileDescriptor, null, null);
            }
        }
        // If the file isn't found
       catch (FileNotFoundException e) {
            /*
             * Handle file not found errors
             */
       }
        return null;
    }

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


    public ListViewAdapter(Activity context, ArrayList<DataPackageDisplay> contactsData) {
        super(context, R.layout.ui_listviewitem, contactsData);
        // TODO Auto-generated constructor stub
        _context=context;
        _contactsData = contactsData;

        _filteredData=contactsData;
    }

    public View getView(int position,View rowView, ViewGroup parent) {
        Log.i("ListViewAdapter", "getView()");
        //View rowView;
        DataPackageDisplay contact;
        ViewHolder holder;

        if(rowView == null) {
            Log.i("New Contact", "getView()");
            LayoutInflater inflater=_context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.ui_listviewitem, null, true);
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
            contact=_filteredData.get(position);

            CheckedTextView chTextView = holder.ctv;
            chTextView.setText(contact.toString());
            chTextView.setTag(contact);
            chTextView.setChecked(contact.isChecked);
            //textView.setFocusable(false);
            chTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CheckedTextView) v).toggle();
                    ((DataPackageDisplay) v.getTag()).isChecked = ((CheckedTextView) v).isChecked();
                    //Raise event
                    _onCheckBoxClicked.onEvent(((DataPackageDisplay) v.getTag()).isChecked ? 1 : 0);
                    //DebugLog.i("CKIT","Selection Raw Contact ID: " + ((DataPackageDisplay) v.getTag()).getRawContactId());
                }
            });

            TextView tvSecondary = holder.tvSecondary;//(TextView) rowView.findViewById(R.id.secondaryTextView);
            tvSecondary.setText(contact.getSecondaryValue());
            tvSecondary.setTag(chTextView);
            tvSecondary.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) { //Simulate clock on checkedTextView
                                                   ((CheckedTextView) v.getTag()).performClick();
                                               }
                                           }
            );

            TextView tvTertiary = holder.tvTertiary;//(TextView) rowView.findViewById(R.id.secondaryTextView);
            tvTertiary.setText(contact.getTertiaryValue());
            tvTertiary.setTag(chTextView);
            tvTertiary.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) { //Simulate clock on checkedTextView
                                               ((CheckedTextView)
                                                       v.getTag()).performClick();}
                                       }
            );

            ImageView iv = holder.iv;//(QuickContactBadge) rowView.findViewById(R.id.contactBadge);
            iv.setTag(contact);
            iv.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              DataPackageDisplay dpd =
                                                      (DataPackageDisplay) v.getTag();
                                              LaunchActivity(dpd.getPackageName(),
                                                               dpd.getLaunchIntentClass() );
                                          }
                                  }
                );

            Drawable icon = contact.getIcon();
            if (icon != null)
                iv.setImageDrawable(icon);

            LinearLayout ll = holder.ll;
            ll.setBackgroundColor(getColor(contact));

        return rowView;
    }

    //Construct color based on data
    private int getColor(DataPackageDisplay pkg){
        int red=Color.red(_context.getResources().getColor(R.color.BaseRed));
        int green=Color.green(_context.getResources().getColor(R.color.PackageSize));
        int blue=Color.blue(_context.getResources().getColor(R.color.IsSystemPackageBlue));
        int alpha=Color.alpha(_context.getResources().getColor(R.color.ContactListAlpha)); //Do not change this as in Adrnoid 5.0 this can lead to

        if(pkg.getIsSystemApp())
            blue = blue+60;

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
            _context.startActivity(intent);
            SupportFunctions.DisplayToastMessageLong(_context,
                    _context.getResources().getString(
                            R.string.message_activity_loading));
        }
        catch(Exception ex){
            SupportFunctions.DisplayToastMessageLong(_context,
                    _context.getResources().getString(
                            R.string.message_activity_load_fail));
        }
    }

    @Override
    public int getCount() {
        //if()
        return _filteredData != null? _filteredData.size() : 0;
    }

    @Override
    public DataPackageDisplay getItem(int position) {
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
                final ArrayList<DataPackageDisplay> currentData = _contactsData;

                int count = currentData.size();
                final ArrayList<DataPackageDisplay> filteredData =
                        new ArrayList<DataPackageDisplay>();

                DataPackageDisplay filterableData ;

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
                _filteredData = (ArrayList<DataPackageDisplay>) results.values;
                _onFilterPublishResults.onEvent();
                notifyDataSetChanged();

            }
        };
        return filter;
    }
}