package com.example.aplicaciong10;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import io.paperdb.Paper;

public class LibraryOverviewActivity extends AppCompatActivity {

    private TextView title, address, days, hoursRange;
    private Button btnInit;
    private Library item;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_overview);
        Paper.init(this);
        item = (Library) getIntent().getSerializableExtra("library");
        if (item != null) {
            title = findViewById(R.id.overview_title);
            address = findViewById(R.id.overview_address);
            hoursRange = findViewById(R.id.overview_hoursRange);
            days = findViewById(R.id.overview_days);
            image = findViewById(R.id.overview_image);

            title.setText(item.getTitle());
            address.setText(item.getAddress());
            image.setImageResource(item.getImage());
            days.setText(item.getDays());
            hoursRange.setText(item.getHoursRange());
        }

        btnInit = (Button) findViewById(R.id.overview_btn_start_booking);
        btnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = (Library) getIntent().getSerializableExtra("library");

                Paper.book().write("library-title",item.getTitle());
                Paper.book().write("library-address",item.getAddress());
                Paper.book().write("library-days",item.getDays());

                startActivity(new Intent(LibraryOverviewActivity.this, SelectDayActivity.class));
            }
        });
    }
}