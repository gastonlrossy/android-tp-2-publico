package com.example.aplicaciong10;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import io.paperdb.Paper;

public class SummaryActivity extends AppCompatActivity {

    TextView title, address, day, hour;
    Hour hourItem;
    Button btnConfirm;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Paper.init(this);
        title = (TextView) findViewById(R.id.summary_title);
        address = (TextView) findViewById(R.id.summary_address);
        day = (TextView) findViewById (R.id.summary_day);
        hour = (TextView) findViewById (R.id.summary_hour);

        title.setText(Paper.book().read("library-title"));
        address.setText(Paper.book().read("library-address"));
        day.setText(Paper.book().read("library-day-selected"));

        hourItem = (Hour) getIntent().getSerializableExtra("hour");

        hour.setText(hourItem.getDescription());

        Paper.book().write("library-hour-selected", hourItem.getDescription());

        btnConfirm = (Button) findViewById(R.id.btn_confirm_booking);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs = getSharedPreferences("book-info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString("title", Paper.book().read("library-title"));
                editor.putString("address", Paper.book().read("library-address"));
                editor.putString("hour", Paper.book().read("library-hour-selected"));
                editor.putString("day", Paper.book().read("library-day-selected"));
                editor.commit();

                startActivity(new Intent(SummaryActivity.this, SuccessfulBookingActivity.class));
            }
        });

    }


}