package com.example.aplicaciong10;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;

import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.View;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public class SuccessfulPatternActivity extends AppCompatActivity {

    /* Variables para request con la API */
    private EditText txtMail;
    private EditText txtPassword;

    private final String REGISTER_EVENT_LOGIN = "RegisterEventLogin";
    private final String LOGIN = "Login";

    /* SharedPreferences */
    private SharedPreferences preferences;
    private int logins;
    private String date;

    private String key;

    private final int dayThresholdMin = 8;
    private final int dayThresholdMax = 17;
    private final int nightThresholdMin = dayThresholdMax;
    private final int nightThresholdMax = 23;

    private String success;

    public IntentFilter filtro;
    public IntentFilter registerEventFilterLogin;

    private ReceptorOperacionLogin receiver = new ReceptorOperacionLogin();
    private ReceptorRegisterEventLogin registerEventReceiverLogin = new ReceptorRegisterEventLogin();

    private static final String URI_LOGIN = "http://so-unlam.net.ar/api/api/login";
    private static final String URI_REGISTER_USER = "http://so-unlam.net.ar/api/api/register";
    private static final String URI_REGISTER_EVENT = "http://so-unlam.net.ar/api/api/event";

    /* Buttons */
    private Button btnLogin;
    private Button btnRegister;
    private Button btnViewMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful_pattern);

        txtMail = (EditText) findViewById(R.id.textEmailLogin);
        txtPassword = (EditText) findViewById(R.id.textPasswordLogin);

        btnLogin = (Button) findViewById(R.id.loginButton);
        btnRegister = (Button) findViewById(R.id.goToRegisterButton);
        btnViewMetrics = (Button) findViewById(R.id.viewMetricsButton);

        btnLogin.setOnClickListener(HandlerCmdLogin);
        btnRegister.setOnClickListener(HandlerCmdRegister);
        btnViewMetrics.setOnClickListener(HandlerCmdViewMetrics);

        testFields();

        //Para Login y Register
        configurarBroadcastReceiverLogin();

        //Para Registrar evento de Login
        configurarBroadcastRegisterEventLogin();

        //SharedPreferences
        preferences = getSharedPreferences("metricas", Context.MODE_PRIVATE);
    }


    /**************     Para chequear el estado de la conexión a internet **********************/


    private void showCustomDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SuccessfulPatternActivity.this);

        builder.setMessage("Connect to the internet to proceed further.")
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
                        //startActivity(new Intent(getApplicationContext(), SuccessfulPatternActivity.class));
                        //finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isConnected(SuccessfulPatternActivity login) {
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


    /******************************   Para realizar el POST a la API  ********************************/

    private void testFields() {
        txtMail.setText("ramon@hotmail.com");
        txtPassword.setText("12345678");
    }

    private View.OnClickListener HandlerCmdLogin = (v -> {
        if (!isConnected(SuccessfulPatternActivity.this)) {
            showCustomDialog();
        }

        //Seteamos en true este boolean para al recibir la respuesta chequear y asignar el token

        JSONObject obj = new JSONObject();
        JSONObject empty = new JSONObject();
        try {
            obj.put("email", txtMail.getText().toString());
            obj.put("password", txtPassword.getText().toString());

            empty.put("loginRegisterJson", "empty");

            Intent i = new Intent(SuccessfulPatternActivity.this, APIConnect_HttpPOST.class);

            i.putExtra("uri", URI_LOGIN);
            i.putExtra("datosJson", obj.toString());
            i.putExtra("sendMode", LOGIN);
            i.putExtra("loginRegisterJson", empty.toString());

            startService(i);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //startActivity(new Intent(SuccessfulPatternActivity.this, MenuActivity.class));
    });


    private View.OnClickListener HandlerCmdRegister = v -> {

        Intent intentRegister = new Intent(SuccessfulPatternActivity.this, RegisterActivity.class);
        startActivity(intentRegister);

    };

    private View.OnClickListener HandlerCmdViewMetrics = v -> {

        Intent intentMetrics = new Intent(SuccessfulPatternActivity.this, ViewMetricsActivity.class);
        startActivity(intentMetrics);

    };

    public class ReceptorOperacionLogin extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            try {
                String datosJsonString = intent.getStringExtra("datosJson");
                JSONObject datosJson = new JSONObject(datosJsonString);

                Log.i("LOGUEO MAIN", "Datos Json Main Thread: " + datosJsonString);

                //txtResult.setText(datosJsonString);
                //Toast.makeText(getApplicationContext(), "Se recibio respuesta del Server", Toast.LENGTH_LONG).show();


                //Tomamos el resultado de la request

                try {
                    success = (datosJson.get("success").toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (success.equals("true")) {

                        Toast.makeText(SuccessfulPatternActivity.this, "Inicio de sesión correcto!", Toast.LENGTH_SHORT).show();

                        //Guardamos el login en sharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();

                        Calendar calendar = Calendar.getInstance();
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

                        if(currentHour >= dayThresholdMin && currentHour < dayThresholdMax){
                            key = "LoginsDay";
                            date = "Dia";
                        }  else if(currentHour >= nightThresholdMin && currentHour < nightThresholdMax){
                            key = "LoginsNight";
                            date = "Noche";
                        }

                        //Para los horarios que no están dentro de los umbrales
                        if(key != null && !key.equals("null"))
                        {
                            logins = preferences.getInt(key, 0);

                            if(logins == 0)
                                Toast.makeText(SuccessfulPatternActivity.this, "No hay logins registrados", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(SuccessfulPatternActivity.this, "La cantidad de logins almacenados (" + date + ") es " + logins, Toast.LENGTH_SHORT).show();


                            logins++;
                            editor.putInt(key, logins);
                            editor.commit();
                        }

                        //SystemClock.sleep(5000);


                        //Registramos el evento de login
                        registerEventLogin(datosJson);

                        Intent intentMenu = new Intent(SuccessfulPatternActivity.this, LibraryListActivity.class);

                        intentMenu.putExtra("datosJson", datosJson.toString());

                        startActivity(intentMenu);
                } else if (success.equals("false"))
                    Toast.makeText(SuccessfulPatternActivity.this, "Inicio de sesión inválido. (" + datosJson.get("msg").toString() + ")", Toast.LENGTH_SHORT).show();



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void configurarBroadcastReceiverLogin() {
        filtro = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_LOGIN");

        filtro.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(receiver, filtro);
    }

    /******************************   Para realizar el POST a la API  ********************************/

    /********************************** Register Event Login ********************************/

    private void registerEventLogin(JSONObject loginRegisterJson)
    {
        if (!isConnected(SuccessfulPatternActivity.this)) {
            showCustomDialog();
        }

        JSONObject obj = new JSONObject();

        try {
            obj.put("env", "PROD");
            obj.put("type_events", "Login");
            obj.put("description", "Se ha iniciado sesión en la aplicación");

            Intent i = new Intent(SuccessfulPatternActivity.this, APIConnect_HttpPOST.class);

            i.putExtra("uri", URI_REGISTER_EVENT);
            i.putExtra("datosJson", obj.toString());
            i.putExtra("sendMode", REGISTER_EVENT_LOGIN);
            i.putExtra("loginRegisterJson", loginRegisterJson.toString());

            startService(i);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public class ReceptorRegisterEventLogin extends BroadcastReceiver  {
        public void onReceive(Context context, Intent intent) {

            try {
                String datosJsonString = intent.getStringExtra("datosJson");
                JSONObject datosJson = new JSONObject(datosJsonString);

                Log.i("LOGUEO MAIN", "Datos Json Main Thread: " + datosJsonString);

                success = datosJson.get("success").toString();
                if(success.equals("true")){
                    Toast.makeText(SuccessfulPatternActivity.this, "Se ha registrado el evento de login satisfactoriamente!", Toast.LENGTH_SHORT).show();
                }

                else if(success.equals("false")){
                    Toast.makeText(SuccessfulPatternActivity.this, "Hubo un error en registrar el evento de login. (" + datosJson.get("msg").toString() + ")", Toast.LENGTH_SHORT).show();
                }

            } catch(JSONException e){
                e.printStackTrace();
            }

        }
    }

    private void configurarBroadcastRegisterEventLogin()
    {
        registerEventFilterLogin = new IntentFilter("com.example.intentservice.intent.action.REGISTER_EVENT_LOGIN");
        registerEventFilterLogin.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(registerEventReceiverLogin, registerEventFilterLogin);
    }

    /********************************** Register Event Login ********************************/
}