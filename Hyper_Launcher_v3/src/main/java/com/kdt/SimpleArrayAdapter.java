package com.kdt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Basic adapter, expect it uses the what is passed by the code, no the resources
 * @param <T>
 */
public class SimpleArrayAdapter<T> extends BaseAdapter implements Filterable {
    private List<T> mObjects;
    private int mSelectedIndex = -1;

    public SimpleArrayAdapter(List<T> objects) {
        setObjects(objects);
    }

    public void setObjects(@Nullable List<T> objects) {
        if(objects == null){
            if(mObjects != Collections.emptyList()) {
                mObjects = Collections.emptyList();
                mSelectedIndex = -1;
                notifyDataSetChanged();
            }
        } else {
            if(objects != mObjects){
                mObjects = objects;
                mSelectedIndex = -1;
                notifyDataSetChanged();
            }
        }
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.mSelectedIndex = selectedIndex;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public T getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView v = (TextView) convertView;
        v.setText(mObjects.get(position).toString());
        return v;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.values = mObjects;
                results.count = mObjects.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }
}
