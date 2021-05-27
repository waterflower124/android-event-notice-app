package com.netglue.ngtmobile.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netglue.ngtmobile.R;

import java.util.List;

public class HighLightArrayAdapter extends ArrayAdapter<CharSequence> {

    private int mSelectedIndex = 1;

    private int mHighlightColor;

    Context context;
    CharSequence[] objects;
    int resource;


    public void setSelection(int position) {
        mSelectedIndex = position;
        notifyDataSetChanged();
    }

    public HighLightArrayAdapter(Context context, int resource, CharSequence[] objects) {
        super(context, resource, objects);
        this.context = context;
        CharSequence[] temp = objects;

        this.objects = objects;
        this.resource = resource;
    }

    @Override
    public boolean isEnabled(int position) {
        if(position == 0) {
            return false;
        } else {
            return true;
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() != R.id.interval) {
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText("");
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= LayoutInflater.from(context);
        View itemView;

        if(resource == R.layout.spinner_interval_item) {
            itemView = super.getDropDownView(position, convertView, parent);
            if (position == mSelectedIndex) {
                itemView.setBackgroundColor(mHighlightColor);
            } else {
                itemView.setBackgroundColor(Color.WHITE);
            }
        } else {
            if (position == 0) {
                itemView = inflater.inflate(R.layout.spinner_dropdown_header, parent, false);
                TextView textView = (TextView) itemView.findViewById(R.id.headerTextView);
                textView.setText(objects[position]);
            } else {
                itemView = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
                TextView textView = (TextView) itemView.findViewById(R.id.itemtext);
                textView.setText(objects[position]);
                if (position == mSelectedIndex) {
                    itemView.setBackgroundColor(mHighlightColor);
                } else {
                    itemView.setBackgroundColor(Color.WHITE);
                }
            }
        }

        return itemView;
    }

    public void setHighlightColor(int color) {
        mHighlightColor = color;
    }
}
