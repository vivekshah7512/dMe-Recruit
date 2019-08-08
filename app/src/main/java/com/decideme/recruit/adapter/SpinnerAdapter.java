package com.decideme.recruit.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.decideme.recruit.R;

import java.util.ArrayList;
import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<String> items;
    private ArrayList<String> arrayList;
    private int mResource;
    private CheckedEvent checkedEvent;
    private String[] cat_id;

    public SpinnerAdapter(@NonNull Context context, @LayoutRes int resource,
                          @NonNull List objects, ArrayList<String> arrayList,
                          String[] cat_id) {
        super(context, resource, 0, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        items = objects;
        this.arrayList = arrayList;
        this.cat_id = cat_id;
        checkedEvent = (CheckedEvent) ((Activity) mContext);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(final int position, View convertView, ViewGroup parent) {
        final View view = mInflater.inflate(mResource, parent, false);

        TextView tv_name = (TextView) view.findViewById(R.id.spinner_text);
        CheckBox cb_check = (CheckBox) view.findViewById(R.id.checkbox);

        tv_name.setText(items.get(position));
        cb_check.setTag(position);

        if (position == 0) {
            cb_check.setVisibility(View.GONE);
        } else {
            cb_check.setVisibility(View.VISIBLE);
        }

        if (arrayList.contains(cat_id[position])) {
            cb_check.setChecked(true);
        } else {
            cb_check.setChecked(false);
        }

        cb_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int getPosition = (Integer) buttonView.getTag();
                if (isChecked) {
                    checkedEvent.click(position, items.get(position), "add");
                } else {
                    checkedEvent.click(position, items.get(position), "remove");
                }
            }
        });

        return view;
    }

    public interface CheckedEvent {
        void click(int pos, String category, String flag);
    }
}
