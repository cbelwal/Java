package com.dasmic.android.lib.contacts.Extension;

/**
 * Created by Chaitanya Belwal on 8/10/2015.
 */
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.dasmic.android.lib.contacts.Data.DataContactDisplay;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;
import com.dasmic.android.lib.contacts.Interface.IGenericEvent;
import com.dasmic.android.lib.contacts.Interface.IGenericParameterLessEvent;
import com.dasmic.android.lib.contacts.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ListViewAdapter extends
        ArrayAdapter<DataContactDisplay> implements Filterable{

    static class ViewHolder {
        CheckedTextView ctv;
        TextView tv;
        QuickContactBadge qcb;
        LinearLayout ll;
    }


    private final Activity _context;
    private getColorBasedOnData _getColorBasedOnData;
    private final ArrayList<DataContactDisplay> _contactsData;
    private ArrayList<DataContactDisplay> _filteredData;
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
                           ArrayList<DataContactDisplay> contactsData,
                           final getColorBasedOnData funcGetColorBasedOnData) {
        super(context, R.layout.ui_listviewitem, contactsData);
        // TODO Auto-generated constructor stub
        _context=context;
        _contactsData = contactsData;
        _getColorBasedOnData = funcGetColorBasedOnData;
        _filteredData=contactsData;
    }

    public View getView(int position,View rowView, ViewGroup parent) {
        Log.i("ListViewAdapter", "getView()");
        //View rowView;
        DataContactDisplay contact;
        ViewHolder holder;

        if(rowView == null) {
            Log.i("New Contact", "getView()");
            LayoutInflater inflater=_context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.ui_listviewitem, null, true);
            holder = new ViewHolder();
            holder.ctv=(CheckedTextView) rowView.findViewById(R.id.checkedTextView);
            holder.tv=(TextView) rowView.findViewById(R.id.secondaryTextView);
            holder.qcb=(QuickContactBadge) rowView.findViewById(R.id.contactBadge);
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
                    ((DataContactDisplay) v.getTag()).isChecked = ((CheckedTextView) v).isChecked();
                    //Raise event
                    _onCheckBoxClicked.onEvent(((DataContactDisplay) v.getTag()));
                    Log.i("CKIT","Selection Contact ID: " + ((DataContactDisplay) v.getTag()).getContactId());
                    //DebugLog.i("CKIT","Selection Raw Contact ID: " + ((DataContactDisplay) v.getTag()).getRawContactId());
                }
            });

            TextView textView = holder.tv;//(TextView) rowView.findViewById(R.id.secondaryTextView);
            textView.setText(contact.getSecondaryValue());
            textView.setTag(chTextView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //Simulate clock on checkedTextView
                ((CheckedTextView) v.getTag()).performClick();
                }}
            );

            QuickContactBadge contactBadge = holder.qcb;//(QuickContactBadge) rowView.findViewById(R.id.contactBadge);
            contactBadge.setMode(ContactsContract.QuickContact.MODE_SMALL);

            if(contact.getContactId()==0)
                contactBadge.setVisibility(View.GONE);
            else {
                contactBadge.assignContactUri(contact.getContactUri());
                if (contact.getThumbUri() != null)
                    contactBadge.setImageURI(contact.getThumbUri());
                else
                    contactBadge.setImageToDefault();
            }

            LinearLayout ll = holder.ll;
            ll.setBackgroundColor(
                    _getColorBasedOnData.onGetColorBasedOnData(contact));

        return rowView;
    }


    public interface getColorBasedOnData{
        int onGetColorBasedOnData(DataContactDisplay contact);

    }


    @Override
    public int getCount() {
        //if()
        return _filteredData != null? _filteredData.size() : 0;
    }

    @Override
    public DataContactDisplay getItem(int position) {
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
                final ArrayList<DataContactDisplay> currentData =
                        _contactsData;

                int count = currentData.size();
                final ArrayList<DataContactDisplay> filteredData =
                        new ArrayList<DataContactDisplay>();

                DataContactDisplay filterableData ;

                for (int i = 0; i < count; i++) {
                    filterableData = currentData.get(i);
                    //Search by current Value or name

                    if(filterString.equals(ActivityOptions.FILTER_ON_DUPLICATES)){
                        if(filterableData.isChecked ||
                                filterableData.isDuplicateMaster) filteredData.add(filterableData);
                    }
                    else if (filterableData.toString().toLowerCase().contains(filterString) ||
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
                _filteredData = (ArrayList<DataContactDisplay>) results.values;
                _onFilterPublishResults.onEvent();
                notifyDataSetChanged();

            }
        };
        return filter;
    }
}