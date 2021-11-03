package com.example.aplicaciong10;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.paperdb.Paper;

public class SuccessfulBookingActivity extends AppCompatActivity {

    private Button btnGoOverview;
    private TextView title, address, day, hour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful_booking);
        Paper.init(this);

        title = (TextView) findViewById(R.id.successful_title);
        address = (TextView) findViewById(R.id.successful_address);
        day = (TextView) findViewById (R.id.successful_day);
        hour = (TextView) findViewById (R.id.successful_hour);

        title.setText(Paper.book().read("library-title"));
        address.setText(Paper.book().read("library-address"));
        day.setText(Paper.book().read("library-day-selected"));
        hour.setText(Paper.book().read("library-hour-selected"));

        btnGoOverview = (Button) findViewById(R.id.btn_go_overview);
        btnGoOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SuccessfulBookingActivity.this, LibraryListActivity.class));
            }
        });
    }
}