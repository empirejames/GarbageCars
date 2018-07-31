package com.james.garbagecar;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by 101716 on 2017/6/16.
 */

public class CarsAdapter extends BaseAdapter {
    private String TAG = CarsAdapter.class.getSimpleName();
    private ArrayList<GarbageCar> mListItems;
    private Context mContext;
    private ViewHolder holder;
    private int layoutResourceId;

    public CarsAdapter(Context context, int layoutResourceId, ArrayList<GarbageCar> itemList) {
        this.mListItems = itemList;
        this.mContext = context;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    public GarbageCar getItem(int position) {
        return mListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        GarbageCar item = (GarbageCar) mListItems.get(position);
        Long idInt = Long.valueOf(item.getId());
        //Long idInt = Long.parseLong(item.getId());
        return idInt;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //View row = inflater.inflate(R.layout.activity_equiment_layout, parent, false);
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }
        holder = new ViewHolder();
        GarbageCar items = mListItems.get(position);
        holder.imageView = convertView.findViewById(R.id.imageView_pic);
        holder.item = convertView.findViewById(R.id.tv_item);
        holder.item.setText(items.getCar());
        holder.location = convertView.findViewById(R.id.tv_location);
        holder.location.setText(items.getLocation());
        holder.distance = convertView.findViewById(R.id.tv_distance);
        holder.distance.setText(items.getDistance());

        holder.summary = convertView.findViewById(R.id.tv_summary);
        holder.summary.setText(items.getTime());
            Picasso.with(mContext)
                    .load(R.drawable.garbage)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView item, location, distance, summary, unit;
    }

}
