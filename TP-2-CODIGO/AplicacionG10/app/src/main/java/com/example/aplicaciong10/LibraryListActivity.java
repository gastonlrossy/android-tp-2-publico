package com.example.aplicaciong10;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import io.paperdb.Paper;

public class LibraryListActivity extends AppCompatActivity {
    private ListView items;
    private Adapter adapter;
    private ArrayList<Library> entities = new ArrayList<>();
    Button btnSeeBooking;
    Context context = this;

    /* Variables para interactuar con los sensores */
    private SensorManager mSensorManager;

    public TextView countdownText;

    private long timeLeftInMilliseconds = 15000;

    private boolean timerRunning = false;

    private final String URI_REFRESH_TOKEN = "http://so-unlam.net.ar/api/api/refresh";
    private final String URI_REGISTER_EVENT = "http://so-unlam.net.ar/api/api/event";

    private final String REGISTER_EVENT_TEMPERATURE = "RegisterEventTemperature";
    private final String REGISTER_EVENT_SHAKE = "RegisterEventShake";
    private final String REFRESH_TOKEN = "RefreshToken";

    private boolean isEventTemperature = false;
    private boolean isEventShake = false;

    private String success;
    public JSONObject jsonObject;

    public IntentFilter shakeIntentFilter;

    public IntentFilter refreshIntentFilter;

    public IntentFilter registerEventFilterTemperature;

    private LibraryListPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_list);

        Paper.init(this);
        Paper.book().destroy();

        if(getIntent().getStringExtra("datosJson") != null) {
            try {
                jsonObject = new JSONObject(getIntent().getStringExtra("datosJson"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        presenter = new LibraryListPresenter(this);

        presenter.setupSensorManager();
        presenter.setupShakeEvent();
        presenter.setupReceptorRegisterEventTemperature();
        presenter.setupRefreshTokenEvent();

        countdownText = (TextView) findViewById(R.id.textCounterMenu);

        presenter.updateTimer();

        configurarBroadcastRegisterEventLowTemperature();

        items = (ListView) findViewById(R.id.items);

        fillItems();

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,- 1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,- 1);

        float batteryPct = level * 100 / (float) scale;

        int  batteryPctInt = (int)batteryPct;

        Toast.makeText(LibraryListActivity.this, "Battery level: " + batteryPctInt + "%", Toast.LENGTH_SHORT).show();

        configurarBroadcastReceiverRefresh();

        configurarBroadcastReceiverShake();

        btnSeeBooking = (Button) findViewById(R.id.btn_see_booking);

        btnSeeBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LibraryListActivity.this, BookingActivity.class));
            }
        });
    }

    private void fillItems(){
        entities.add(new Library(R.drawable.biblioteca_marechal, "LEOPOLDO MARECHAL",
                "Florencio Varela 1998, San Justo, Buenos Aires",
                "Lunes a viernes.",
                "De 08 a 20 hs."));
        entities.add(new Library(R.drawable.biblioteca_almafuerte, "BIBLIOTECA HAEDO",
                "Tacuarí 674, Haedo, Buenos Aires",
                "Lunes a viernes.",
                "De 08 a 20 hs."));
        entities.add(new Library(R.drawable.biblioteca_haedo, "BIBLIOTECA ROTARIA",
                "Sarrachaga 6198, Isidro Casanova, Buenos Aires",
                "Lunes a viernes.",
                "De 08 a 20 hs."));
        entities.add(new Library(R.drawable.biblioteca_jauretche, "BIBLIOTECA JAURETCHE",
                "Crescencia Acosta 971, El Palomar, Buenos Aires",
                "Lunes a viernes.",
                "De 08 a 20 hs."));
        entities.add(new Library(R.drawable.biblioteca_rotaria, "BIBLIOTECA ALMAFUERTE",
                "Sarratea 4267, San Justo, Buenos Aires",
                "Lunes a viernes.",
                "De 08 a 20 hs."));

        adapter = new Adapter(this, entities);
        items.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Aca debería chequear la temperatura del ambiente

        presenter.onResume();

        // Una vez que detecto una baja temperatura, deberia setear una var en TRUE asi no sigue chequeando
        // Y paramos la ejecución del sensor, asi no gasta tanta bateria
    }


    @Override
    protected void onStop() {
        super.onStop();

        presenter.onStop();
    }

    void temperaturePopUp()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(LibraryListActivity.this);

        builder.setMessage("Detectamos temperaturas bajas, antes de salir, Abrigate!!!\nEn esta pandemia nos cuidamos entre todos! ")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void registerEventShake()
    {
        if (!isConnected(LibraryListActivity.this)) {
            showCustomDialog();
        }

        JSONObject obj = new JSONObject();

        try {
            obj.put("env", "PROD");
            obj.put("type_events", "Acelerómetro");
            obj.put("description", "Se ha registrado un shake");

            Intent i = new Intent(LibraryListActivity.this, APIConnect_HttpPOST.class);

            i.putExtra("uri", URI_REGISTER_EVENT);
            i.putExtra("datosJson", obj.toString());
            i.putExtra("sendMode", REGISTER_EVENT_SHAKE);
            i.putExtra("loginRegisterJson", this.jsonObject.toString());

            startService(i);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void registerEventTemperature()
    {
        if (!isConnected(LibraryListActivity.this)) {
            showCustomDialog();
        }

        JSONObject obj = new JSONObject();

        try {
            obj.put("env", "PROD");
            obj.put("type_events", "Temperatura Baja");
            obj.put("description", "Se ha registrado una baja temperatura");

            Intent i = new Intent(LibraryListActivity.this, APIConnect_HttpPOST.class);

            i.putExtra("uri", URI_REGISTER_EVENT);
            i.putExtra("datosJson", obj.toString());
            i.putExtra("sendMode", REGISTER_EVENT_TEMPERATURE);
            i.putExtra("loginRegisterJson", this.jsonObject.toString());

            startService(i);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void configurarBroadcastRegisterEventLowTemperature()
    {
        registerEventFilterTemperature = new IntentFilter("com.example.intentservice.intent.action.REGISTER_EVENT_LOW_TEMPERATURE");
        registerEventFilterTemperature.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(this.presenter.registerEventReceiverTemperature, registerEventFilterTemperature);
    }


    private void configurarBroadcastReceiverRefresh()
    {
        refreshIntentFilter = new IntentFilter("com.example.intentservice.intent.action.REFRESH_TOKEN");
        refreshIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(this.presenter.refreshReceiver, refreshIntentFilter);
    }

    private void configurarBroadcastReceiverShake()
    {
        shakeIntentFilter = new IntentFilter("com.example.intentservice.intent.action.REGISTER_EVENT_SHAKE");
        shakeIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(this.presenter.shakeReceiver, shakeIntentFilter);
    }

    private void showCustomDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LibraryListActivity.this);

        builder.setMessage("Conéctese a internet para proceder.")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isConnected(LibraryListActivity login) {
        ConnectivityManager connectivityManager = (ConnectivityManager) login.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())) {
            return true;
        } else {
            return false;
        }

    }
}