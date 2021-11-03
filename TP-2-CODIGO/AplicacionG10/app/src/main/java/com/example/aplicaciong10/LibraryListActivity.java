package com.example.aplicaciong10;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
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

public class LibraryListActivity extends AppCompatActivity implements  SensorEventListener {
    private ListView items;
    private Adapter adapter;
    private ArrayList<Library> entities = new ArrayList<>();
    Button btnSeeBooking;
    Context context = this;

    /* Variables para interactuar con los sensores */
    private SensorManager mSensorManager;

    private TextView countdownText;
    private CountDownTimer countDownTimer;
    private long timeLeftInMilliseconds = 15000;

    private boolean timerRunning = false;

    /* Acelerómetro */
    private float currentX, currentY, currentZ;
    private float lastX, lastY, lastZ;
    private float xDiff, yDiff, zDiff;
    private final float shakeThreshold = 10f;
    private boolean isFirstTime = true;
    private Vibrator vibrator;


    //Var. para que al detectar una baja temperatura no siga midiendo
    private boolean detectedLowTemperature = false;
    private final int temperatureThreshold = 10;
    private boolean timerTimeout = false;

    private static final String URI_REGISTER_EVENT = "http://so-unlam.net.ar/api/api/event";
    private static final String URI_REFRESH_TOKEN = "http://so-unlam.net.ar/api/api/refresh";

    private final String REGISTER_EVENT_TEMPERATURE = "RegisterEventTemperature";
    private final String REGISTER_EVENT_SHAKE = "RegisterEventShake";
    private final String REFRESH_TOKEN = "RefreshToken";

    private boolean isEventTemperature = false;
    private boolean isEventShake = false;

    private String success;
    private JSONObject jsonObject;

    public IntentFilter shakeIntentFilter;
    private ReceptorShakeEvent shakeReceiver = new ReceptorShakeEvent();

    public IntentFilter refreshIntentFilter;
    private ReceptorRefreshToken  refreshReceiver = new ReceptorRefreshToken();

    public IntentFilter registerEventFilterTemperature;
    private ReceptorRegisterEventTemperature registerEventReceiverTemperature = new ReceptorRegisterEventTemperature();

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

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        countdownText = (TextView) findViewById(R.id.textCounterMenu);

        updateTimer();

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


    /******************************     Para configurar los sensores  ********************************/

    protected void initializeTemperatureSensor()
    {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void initializeAccelerometerSensor()
    {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void stopAccelerometerSensor()
    {
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    private void stopTemperatureSensor()
    {
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Aca debería chequear la temperatura del ambiente
        startStop();

        //Agregar el TIMER
        if(!detectedLowTemperature && !timerTimeout)
            initializeTemperatureSensor();

        initializeAccelerometerSensor();

        // Una vez que detecto una baja temperatura, deberia setear una var en TRUE asi no sigue chequeando
        // Y paramos la ejecución del sensor, asi no gasta tanta bateria
    }


    @Override
    protected void onStop() {
        super.onStop();

        if(!detectedLowTemperature && !timerTimeout)
            stopTemperatureSensor();

        stopAccelerometerSensor();

        startStop();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String txt = "";

        synchronized (this) {
            Log.d("sensor", event.sensor.getName());

            switch (event.sensor.getType()) {
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    txt += "Temperatura\n";
                    txt += event.values[0] + " C \n";

                    if(event.values[0] < temperatureThreshold && !detectedLowTemperature && !timerTimeout) //Si la temperatura es menor a 10°C
                    {
                        //Para que ya no siga entrando a detectar
                        detectedLowTemperature = true;

                        temperaturePopUp();

                        stopTemperatureSensor();

                        //Para Registrar evento de temperatura baja
                        isEventTemperature = true;

                        registerEventTemperature(jsonObject);
                    }

                    break;

                case Sensor.TYPE_ACCELEROMETER:
                    currentX = event.values[0];
                    currentY = event.values[1];
                    currentZ = event.values[2];

                    if(!isFirstTime)
                    {
                        xDiff = Math.abs(currentX - lastX);
                        yDiff = Math.abs(currentY - lastY);
                        zDiff = Math.abs(currentZ - lastZ);

                        if((xDiff > shakeThreshold && yDiff > shakeThreshold) ||
                                (xDiff > shakeThreshold && zDiff > shakeThreshold) ||
                                (yDiff > shakeThreshold && zDiff > shakeThreshold))
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(500);
                            }

                            isEventShake = true;

                            registerEventShake(jsonObject);
                        }

                    }

                    lastX = currentX;
                    lastY = currentY;
                    lastZ = currentZ;
                    isFirstTime = false;

                    break;

            }
        }


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

    /******************************     Para configurar los sensores  ********************************/

    /************************* Timer Sensor de Temperatura *************************************/

    private void startStop() {
        if(timerRunning)
        {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer(){
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliseconds = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timerTimeout = true;
            }
        }.start();

        timerRunning = true;
    }

    private void updateTimer(){
        int minutes = (int) timeLeftInMilliseconds / 60000;
        int seconds = (int) timeLeftInMilliseconds % 60000 / 1000;

        String timeLeftText;

        timeLeftText = "" + minutes;
        timeLeftText += ":";

        if(seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;

        countdownText.setText(timeLeftText);
    }

    private void stopTimer(){
        countDownTimer.cancel();
        timerRunning = false;
    }


    /************************* Timer Sensor de Temperatura *************************+***********/


    /************************** Evento de Registro de Temperatura *************************/


    private void registerEventShake(JSONObject loginRegisterJson)
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
            i.putExtra("loginRegisterJson", loginRegisterJson.toString());

            startService(i);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void registerEventTemperature(JSONObject loginRegisterJson)
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
            i.putExtra("loginRegisterJson", loginRegisterJson.toString());

            startService(i);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class ReceptorRegisterEventTemperature extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            try {
                String datosJsonString = intent.getStringExtra("datosJson");
                JSONObject datosJson = new JSONObject(datosJsonString);

                Log.i("LOGUEO MAIN", "Datos Json Main Thread: " + datosJsonString);

                success = datosJson.get("success").toString();
                if(success.equals("true")){
                    Toast.makeText(LibraryListActivity.this, "Se ha registrado el evento de temperatura baja satisfactoriamente!", Toast.LENGTH_SHORT).show();
                }

                else if(success.equals("false")){
                    if(datosJson.get("msg").toString().equals("Tiempo de sesión expirado."))
                    {
                        Toast.makeText(LibraryListActivity.this, "Tiempo de sesión expirado, se actualizará el token. ", Toast.LENGTH_SHORT).show();
                        //Aca tenemos que realizar un PUT y luego volver a registrar el evento

                        JSONObject obj = new JSONObject();

                        obj.put("empty", "empty");

                        Intent i = new Intent(LibraryListActivity.this, APIConnect_HttpPOST.class);

                        i.putExtra("uri", URI_REFRESH_TOKEN);
                        i.putExtra("datosJson", obj.toString());
                        i.putExtra("sendMode", REFRESH_TOKEN);
                        i.putExtra("loginRegisterJson", jsonObject.toString());

                        startService(i);

                    } else
                        Toast.makeText(LibraryListActivity.this, "Hubo un error en registrar el evento de temperatura baja. (" + datosJson.get("msg").toString() + ")", Toast.LENGTH_SHORT).show();
                }

            } catch(JSONException e){
                e.printStackTrace();
            }

        }
    }



    public class ReceptorRefreshToken extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            try {
                String datosJsonString = intent.getStringExtra("datosJson");
                JSONObject datosJson = new JSONObject(datosJsonString);

                Log.i("LOGUEO MAIN", "Se tratará de actualizar el token...");

                success = datosJson.get("success").toString();
                if(success.equals("true")){
                    Toast.makeText(LibraryListActivity.this, "Se ha actualizado el token satisfactoriamente!", Toast.LENGTH_SHORT).show();
                    jsonObject = datosJson;

                    if(isEventTemperature)
                        registerEventTemperature(jsonObject);
                    else if(isEventShake)
                        registerEventShake(jsonObject);
                }

                else if(success.equals("false")){
                    Toast.makeText(LibraryListActivity.this, "Hubo un error en actualizar el token. (" + datosJson.get("msg").toString() + ")", Toast.LENGTH_SHORT).show();
                }

                isEventTemperature = false;
                isEventShake = false;

            } catch(JSONException e){
                e.printStackTrace();
            }

        }
    }


    private void configurarBroadcastRegisterEventLowTemperature()
    {
        registerEventFilterTemperature = new IntentFilter("com.example.intentservice.intent.action.REGISTER_EVENT_LOW_TEMPERATURE");
        registerEventFilterTemperature.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(registerEventReceiverTemperature, registerEventFilterTemperature);
    }


    private void configurarBroadcastReceiverRefresh()
    {
        refreshIntentFilter = new IntentFilter("com.example.intentservice.intent.action.REFRESH_TOKEN");
        refreshIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(refreshReceiver, refreshIntentFilter);
    }


    public class ReceptorShakeEvent extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            try {
                String datosJsonString = intent.getStringExtra("datosJson");
                JSONObject datosJson = new JSONObject(datosJsonString);

                Log.i("LOGUEO MAIN", "Registrando shake event...");

                success = datosJson.get("success").toString();
                if(success.equals("true"))
                    Toast.makeText(LibraryListActivity.this, "Se ha registrado el evento de shake satisfactoriamente!", Toast.LENGTH_SHORT).show();


                else if(success.equals("false")){
                    if(datosJson.get("msg").toString().equals("Tiempo de sesión expirado."))
                    {
                        Toast.makeText(LibraryListActivity.this, "Tiempo de sesión expirado, se actualizará el token. ", Toast.LENGTH_SHORT).show();
                        //Aca tenemos que realizar un PUT y luego volver a registrar el evento

                        JSONObject obj = new JSONObject();

                        obj.put("empty", "empty");

                        Intent i = new Intent(LibraryListActivity.this, APIConnect_HttpPOST.class);

                        i.putExtra("uri", URI_REFRESH_TOKEN);
                        i.putExtra("datosJson", obj.toString());
                        i.putExtra("sendMode", REFRESH_TOKEN);
                        i.putExtra("loginRegisterJson", jsonObject.toString());

                        startService(i);

                    } else
                        Toast.makeText(LibraryListActivity.this, "Hubo un error en registrar el evento de shake. (" + datosJson.get("msg").toString() + ")", Toast.LENGTH_SHORT).show();
                }

            } catch(JSONException e){
                e.printStackTrace();
            }

        }
    }


    private void configurarBroadcastReceiverShake()
    {
        shakeIntentFilter = new IntentFilter("com.example.intentservice.intent.action.REGISTER_EVENT_SHAKE");
        shakeIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(shakeReceiver, shakeIntentFilter);
    }

    /******************************   Para realizar el POST a la API  ********************************/


    /**************     Para chequear el estado de la conexión a internet **********************/


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

    /**************     Para chequear el estado de la conexión a internet **********************/


}