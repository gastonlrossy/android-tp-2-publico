package com.example.aplicaciong10;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;

import java.util.ArrayList;

public class HourAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Hour> list;


    public HourAdapter(Context context,  ArrayList<Hour> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount(){
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Hour hour = (Hour) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.hour_item, null);
        TextView title = (TextView) convertView.findViewById(R.id.hour_title);

        title.setText(hour.getDescription());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context , SummaryActivity.class);
                intent.putExtra("hour", hour);
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}