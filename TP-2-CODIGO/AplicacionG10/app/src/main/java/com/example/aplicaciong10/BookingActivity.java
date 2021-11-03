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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class BookingActivity extends AppCompatActivity {

    private TextView title, description, address, hour, day;
    private Button btnBack;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        prefs = getSharedPreferences("book-info", Context.MODE_PRIVATE);

        if(prefs.getString("title",null) == null || prefs.getString("title",null) == ""){
            Toast.makeText(BookingActivity.this, "No tenés ninguna reserva efectuada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(BookingActivity.this, LibraryListActivity.class));
        }

        title = (TextView) findViewById(R.id.booking_title);
        address = (TextView) findViewById(R.id.booking_address);
        day = (TextView) findViewById (R.id.booking_day);
        hour = (TextView) findViewById (R.id.booking_hour);

        title.setText(prefs.getString("title",null));
        address.setText(prefs.getString("address",null));
        hour.setText(prefs.getString("hour",null));
        day.setText(prefs.getString("day",null));

        btnBack = (Button) findViewById(R.id.btn_go_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BookingActivity.this, LibraryListActivity.class));
            }
        });
    }
}