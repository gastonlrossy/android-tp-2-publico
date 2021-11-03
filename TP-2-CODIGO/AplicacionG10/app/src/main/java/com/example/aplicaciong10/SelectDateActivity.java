package com.example.aplicaciong10;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.paperdb.Paper;

public class SelectDateActivity extends AppCompatActivity {
    private ListView items;
    private HourAdapter adapter;
    private ArrayList<Hour> hours = new ArrayList<>();
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_date);
        items = (ListView) findViewById(R.id.items_hours);
        fillItems();

    }

    private void fillItems(){
        hours.add(new Hour("08-10"));
        hours.add(new Hour("10-12"));
        hours.add(new Hour("12-14"));
        hours.add(new Hour("14-16"));
        hours.add(new Hour("16-18"));
        hours.add(new Hour("18-20"));

        adapter = new HourAdapter(this, hours);
        items.setAdapter(adapter);
    }
}