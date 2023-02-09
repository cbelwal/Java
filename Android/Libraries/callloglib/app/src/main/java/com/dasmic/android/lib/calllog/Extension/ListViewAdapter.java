package com.dasmic.android.lib.calllog.Extension;

/**
 * Created by Chaitanya Belwal on 8/10/2015.
 */
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;


import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Data.ViewHolder;
import com.dasmic.android.lib.calllog.Interface.IGenericEvent;
import com.dasmic.android.lib.calllog.Interface.IGenericParameterLessEvent;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.StaticFunctions;
import com.dasmic.android.lib.support.Static.SupportFunctions;


public class ListViewAdapter extends
        ArrayAdapter<DataCallLogDisplay> implements Filterable{


    private final Activity _context;
    private getColorBasedOnData _getColorBasedOnData;
    private final ArrayList<DataCallLogDisplay> _contactsData;
    private ArrayList<DataCallLogDisplay> _filteredData;
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


    public ListViewAdapter(Activity context,
                           ArrayList<DataCallLogDisplay> contactsData,
                           final getColorBasedOnData funcGetColorBasedOnData) {
        super(context, R.layout.ui_listviewitem_cl, contactsData);

        // TODO Auto-generated constructor stub
        _context=context;
        _contactsData = contactsData;
        _getColorBasedOnData = funcGetColorBasedOnData;
        _filteredData=contactsData;
    }

    public void addItem(DataCallLogDisplay cld){
        if(_contactsData != null) {
            _contactsData.add(cld);
            this.notifyDataSetChanged();
        }
    }


    public View getView(int position,View rowView, ViewGroup parent) {
        Log.i("ListViewAdapter", "getView()");
        //View rowView;
        final DataCallLogDisplay contact;
        ViewHolder holder;

        if(rowView == null) {
            Log.i("New Contact", "getView()");
            LayoutInflater inflater=_context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.ui_listviewitem_cl, null, true);
            holder = new ViewHolder();
            holder.primaryTV =(TextView) rowView.findViewById(R.id.primaryTextView);
            holder.secondaryCTV =(CheckedTextView) rowView.findViewById(R.id.secondaryCheckedTextView);
            holder.qcb=(QuickContactBadge) rowView.findViewById(R.id.contactBadge);
            holder.durationTV=(TextView) rowView.findViewById(R.id.durationTextView);
            holder.geoLocationTV=(TextView) rowView.findViewById(R.id.geolocationTextView);
            holder.callTypeIV = (ImageView) rowView.findViewById(R.id.callTypeImageView);
            holder.callButton = (ImageButton) rowView.findViewById(R.id.buttonImageCall);
            holder.searchButton = (ImageButton) rowView.findViewById(R.id.buttonImageSearch);
            holder.ll=(LinearLayout) rowView.findViewById(R.id.mainLayout);
            rowView.setTag(holder);
        }
        else {
            holder= (ViewHolder)rowView.getTag();
        }
            contact=_filteredData.get(position);

        CheckedTextView chTextView = holder.secondaryCTV;
        chTextView.setText(contact.getSecondaryValue());
        chTextView.setTag(contact);
        chTextView.setChecked(contact.isChecked);
        //textView.setFocusable(false);
        chTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
                ((DataCallLogDisplay) v.getTag()).isChecked = ((CheckedTextView) v).isChecked();
                //Raise event
                _onCheckBoxClicked.onEvent(((DataCallLogDisplay) v.getTag()));
                //Log.i("CKIT","Selection Contact ID: " + ((DataCallLogDisplay) v.getTag()).getContactId());
                //DebugLog.i("CKIT","Selection Raw Contact ID: " + ((DataCallLogDisplay) v.getTag()).getRawContactId());
            }
        });

        String name = contact.getName();
        if(name.equals("-1"))
            name = _context.getString(R.string.call_from_unknown);
        holder.primaryTV.setText(name);

        holder.primaryTV.setTag(chTextView);
        holder.primaryTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //Simulate clock on checkedTextView
                ((CheckedTextView) v.getTag()).performClick();
                }}
            );

        holder.durationTV.setText(contact.getDurationString());
        holder.durationTV.setTag(chTextView);
        holder.durationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //Simulate clock on checkedTextView
                ((CheckedTextView) v.getTag()).performClick();
            }}
            );

        holder.geoLocationTV.setText(contact.getGeoLocation());
        holder.geoLocationTV.setTag(chTextView);
        holder.geoLocationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //Simulate clock on checkedTextView
                ((CheckedTextView) v.getTag()).performClick();
                }}
            );

        //Set Value of icon
        //Set default as missed
        holder.callTypeIV.setImageResource(R.drawable.ic_call_missed_white_48dp);
        if(contact.isIncomingCall())
            holder.callTypeIV.setImageResource(R.drawable.ic_call_received_white_48dp);
        else if(contact.isOutgoingCall())
            holder.callTypeIV.setImageResource(R.drawable.ic_call_made_white_48dp);

        holder.callTypeIV.setTag(chTextView);
        holder.callTypeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //Simulate clock on checkedTextView
                ((CheckedTextView) v.getTag()).performClick();
            }}
        );

        //Call Button
        holder.callButton.setTag(chTextView);
        holder.callButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StaticFunctions.makeCall(_context,contact.getNumber());
            }
        });

        //search button
        holder.searchButton.setTag(chTextView);
        holder.searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StaticFunctions.searchNumber(_context,contact.getNumber());
            }
        });

        QuickContactBadge contactBadge = holder.qcb;//(QuickContactBadge) rowView.findViewById(R.id.contactBadge);
        contactBadge.setMode(ContactsContract.QuickContact.MODE_SMALL);
        contactBadge.assignContactFromPhone(contact.getNumber(),true);
        if (contact.getPictureUri() != null)
            contactBadge.setImageURI(contact.getPictureUri());
        else
            contactBadge.setImageToDefault();
        LinearLayout ll = holder.ll;
        ll.setBackgroundColor(
                    _getColorBasedOnData.onGetColorBasedOnData(contact));

        return rowView;
    }

    public interface getColorBasedOnData{
        int onGetColorBasedOnData(DataCallLogDisplay contact);
    }

    @Override
    public int getCount() {
        //if()
        return _filteredData != null? _filteredData.size() : 0;
    }

    @Override
    public DataCallLogDisplay getItem(int position) {
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
                final ArrayList<DataCallLogDisplay> currentData =
                        _contactsData;

                int count = currentData.size();
                final ArrayList<DataCallLogDisplay> filteredData =
                        new ArrayList<DataCallLogDisplay>();

                DataCallLogDisplay filterableData ;

                for (int i = 0; i < count; i++) {
                    filterableData = currentData.get(i);
                    //Search by current Value or name

                    /*if(filterString.equals(ActivityOptions.FILTER_ON_DUPLICATES)){
                        if(filterableData.isChecked ||
                                filterableData.isDuplicateMaster) filteredData.add(filterableData);
                    }*/
                    try {
                        if (filterableData.getName().toLowerCase().contains(filterString) ||
                                filterableData.getSecondaryValue().toLowerCase().contains(filterString) ||
                                filterableData.getNumber().toLowerCase().contains(filterString) ||
                                filterableData.getGeoLocation().toLowerCase().contains(filterString) ||
                                filterableData.getDurationString().toLowerCase().contains(filterString)) {
                            filteredData.add(filterableData);
                        }
                    }
                    catch(Exception ex){
                        //Catch any exception if last values are null, continue processing
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
                _filteredData = (ArrayList<DataCallLogDisplay>) results.values;
                _onFilterPublishResults.onEvent();
                notifyDataSetChanged();

            }
        };
        return filter;
    }
}