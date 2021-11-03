package com.example.aplicaciong10;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewMetricsActivity extends AppCompatActivity {

    private TextView loginsDayTime;
    private TextView loginsNightTime;
    private TextView registersDayTime;
    private TextView registersNightTime;

    private final String LOGINS_DAY = "LoginsDay";
    private final String LOGINS_NIGHT = "LoginsNight";
    private final String REGISTERS_DAY = "RegistersDay";
    private final String REGISTERS_NIGHT = "RegistersNight";

    private Button btnGoBack;

    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_metrics);


        loginsDayTime = (TextView) findViewById(R.id.loginsDayTimeTextView);
        loginsNightTime = (TextView) findViewById(R.id.loginsNightTimeTextView);
        registersDayTime = (TextView) findViewById(R.id.registersDayTimeTextView);
        registersNightTime = (TextView) findViewById(R.id.registersNightTimeTextView);

        btnGoBack = (Button) findViewById(R.id.goBackButtonMetrics);

        preferences = getSharedPreferences("metricas", Context.MODE_PRIVATE);


        //SharedPreferences.Editor editor = preferences.edit();

        loginsDayTime.setText(Integer.toString(preferences.getInt(LOGINS_DAY, 0)));

        loginsNightTime.setText(Integer.toString(preferences.getInt(LOGINS_NIGHT, 0)));

        registersDayTime.setText(Integer.toString(preferences.getInt(REGISTERS_DAY, 0)));

        registersNightTime.setText(Integer.toString(preferences.getInt(REGISTERS_NIGHT, 0)));

        btnGoBack.setOnClickListener(HandlerCmdGoBack);
    }



    private View.OnClickListener HandlerCmdGoBack = v -> finish();

}