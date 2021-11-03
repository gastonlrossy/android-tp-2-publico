package com.example.aplicaciong10;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Adapter extends BaseAdapter {
    private ArrayList<Library> list;
    private Context context;
    private LayoutInflater inflater;

    public Adapter(Context context, ArrayList<Library> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final Library library = (Library) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.item, null);
        ImageView image = (ImageView) convertView.findViewById(R.id.item_image);
        TextView title = (TextView) convertView.findViewById(R.id.item_title);
        TextView address = (TextView) convertView.findViewById(R.id.item_address);
        TextView days = (TextView) convertView.findViewById(R.id.item_days);
        TextView hoursRange = (TextView) convertView.findViewById(R.id.item_hoursRange);

        image.setImageResource(library.getImage());
        title.setText(library.getTitle());
        address.setText(library.getAddress());
        days.setText(library.getDays());
        hoursRange.setText(library.getHoursRange());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context ,LibraryOverviewActivity.class);
                intent.putExtra("library", library);
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}