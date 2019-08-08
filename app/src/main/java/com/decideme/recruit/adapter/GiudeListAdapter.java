package com.decideme.recruit.adapter;

/**
 * Created by vivek_shah on 23/10/17.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.decideme.recruit.R;

public class GiudeListAdapter extends BaseAdapter {
    Context context;
    private String[] image;
    private LayoutInflater inflater = null;
    private String[] title;

    public class Holder {
        ImageView img;
        TextView tv_name;
    }

    public GiudeListAdapter(Activity context, String[] image, String[] title) {
        this.context = context;
        this.image = image;
        this.title = title;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return this.title.length;
    }

    public Object getItem(int position) {
        return this.title[position];
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.custom_guide_list_item, null);
            holder = new Holder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_rules);
            holder.img = (ImageView) convertView.findViewById(R.id.img_rules);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.tv_name.setText(this.title[position]);
        Glide.with(this.context).load(this.image[position]).thumbnail(0.5f).into(holder.img);
        return convertView;
    }
}
