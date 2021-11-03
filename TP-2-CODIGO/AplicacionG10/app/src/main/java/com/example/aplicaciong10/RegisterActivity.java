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
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText txtName;
    private EditText txtLastname;
    private EditText txtDni;
    private EditText txtMail;
    private EditText txtPassword;

    private final int dayThresholdMin = 8;
    private final int dayThresholdMax = 17;
    private final int nightThresholdMin = dayThresholdMax;
    private final int nightThresholdMax = 23;

    private String key;
    private String date;
    private int registers;

    private SharedPreferences preferences;

    private boolean invalidParameters = false;

    private static final String URI_REGISTER_USER = "http://so-unlam.net.ar/api/api/register";

    private final String REGISTER = "Register";

    private Button btnRegister;
    private Button btnGoBack;

    private String success;

    public IntentFilter filtroRegister;
    private ReceptorOperacionRegister receiverRegister = new ReceptorOperacionRegister();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        txtName = (EditText) findViewById(R.id.textNameRegister);
        txtLastname = (EditText) findViewById(R.id.textLastnameRegister);
        txtDni = (EditText) findViewById(R.id.textDNIRegister);
        txtMail = (EditText) findViewById(R.id.textEmailRegister);
        txtPassword = (EditText) findViewById(R.id.textPasswordRegister);

        btnRegister = (Button) findViewById(R.id.registerButton);
        btnGoBack = (Button) findViewById(R.id.goBackButton);

        btnRegister.setOnClickListener(HandlerCmdRegister);
        btnGoBack.setOnClickListener(HandlerCmdGoBack);

        configurarBroadcastReceiverRegister();

        testFieldsRegister();

        preferences = getSharedPreferences("metricas", Context.MODE_PRIVATE);
    }

    /************* Para Testear ********************/

    private void testFieldsRegister() {
        txtName.setText("Juan");
        txtLastname.setText("Ramon");
        txtDni.setText("12345677");
        txtMail.setText("ramon@hotmail.com");
        txtPassword.setText("12345678");
    }

    /********************** Volver Atras **************************/

    private View.OnClickListener HandlerCmdGoBack = v -> finish();

    /********************** Volver Atras **************************/

    /************ Realizar Register a la API *********************/


    private View.OnClickListener HandlerCmdRegister = (v -> {
            realizarRegistro();
    });

    private void realizarRegistro()
    {
        if (!isConnected(RegisterActivity.this)) {
            showCustomDialog();
        }

        JSONObject obj = new JSONObject();
        JSONObject empty = new JSONObject();

        try {

            if(!txtName.getText().toString().equals("") &&
                    !txtLastname.getText().toString().equals("") && !txtDni.getText().toString().equals("")  &&
                    android.text.TextUtils.isDigitsOnly(txtDni.getText().toString()) && !txtMail.getText().toString().equals("") &&
                    !txtPassword.getText().toString().equals(""))
            {
                obj.put("env", "PROD");
                obj.put("name", txtName.getText().toString());
                obj.put("lastname", txtLastname.getText().toString());
                obj.put("dni", Integer.parseInt(txtDni.getText().toString()));
                obj.put("email", txtMail.getText().toString());
                obj.put("password", txtPassword.getText().toString());
                obj.put("commission", Integer.parseInt("3900"));
                obj.put("group", Integer.parseInt("10"));

                empty.put("loginRegisterJson", "empty");

                Intent i = new Intent(RegisterActivity.this, APIConnect_HttpPOST.class);

                i.putExtra("uri", URI_REGISTER_USER);
                i.putExtra("datosJson", obj.toString());
                i.putExtra("sendMode", REGISTER);
                i.putExtra("loginRegisterJson", empty.toString());

                startService(i);
            } else
            {
                Toast.makeText(RegisterActivity.this, "Parametros Invalidos. Ingrese nuevamente", Toast.LENGTH_SHORT).show();
                txtName.getText().clear();
                txtLastname.getText().clear();
                txtDni.getText().clear();
                txtMail.getText().clear();
                txtPassword.getText().clear();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class ReceptorOperacionRegister extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            try {
                String datosJsonString = intent.getStringExtra("datosJson");
                JSONObject datosJson = new JSONObject(datosJsonString);

                Log.i("LOGUEO MAIN", "Datos Json Main Thread: " + datosJsonString);

                //txtResult.setText(datosJsonString);
                //Toast.makeText(getApplicationContext(), "Se recibio respuesta del Server", Toast.LENGTH_LONG).show();


                //Tomamos el resultado de la request

                success = (datosJson.get("success").toString());

                if(success.equals("true"))
                {
                    Toast.makeText(RegisterActivity.this, "Registro Correcto!", Toast.LENGTH_SHORT).show();


                    SharedPreferences.Editor editor = preferences.edit();

                    Calendar calendar = Calendar.getInstance();
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

                    if(currentHour >= dayThresholdMin && currentHour < dayThresholdMax){
                        key = "RegistersDay";
                        date = "Dia";
                    }  else if(currentHour >= nightThresholdMin && currentHour < nightThresholdMax){
                        key = "RegistersNight";
                        date = "Noche";
                    }

                    //Para los horarios que no están dentro de los umbrales
                    if(key != null && !key.equals("null"))
                    {
                        registers = preferences.getInt(key, 0);

                        if(registers == 0)
                            Toast.makeText(RegisterActivity.this, "No hay registers almacenados", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(RegisterActivity.this, "La cantidad de registers almacenados (" + date + ") es " + registers, Toast.LENGTH_SHORT).show();


                        registers++;
                        editor.putInt(key, registers);
                        editor.commit();
                    }

                    Intent intentMenu = new Intent(RegisterActivity.this, LibraryListActivity.class);

                    intentMenu.putExtra("datosJson", datosJson.toString());

                    startActivity(intentMenu);

                } else
                    Toast.makeText(RegisterActivity.this, "Register incorrecto (" + datosJson.get("msg").toString() + ")", Toast.LENGTH_SHORT).show();


                } catch (JSONException e) {
                e.printStackTrace();
                }
            }
    }




    private void configurarBroadcastReceiverRegister() {
        filtroRegister = new IntentFilter("com.example.intentservice.intent.action.RESPUESTA_REGISTER");

        filtroRegister.addCategory(Intent.CATEGORY_DEFAULT);

        registerReceiver(receiverRegister, filtroRegister);
    }

    /************ Realizar Register a la API *********************/

    /************ Chequear conexión a Internet *********************/

    private void showCustomDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

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

    private boolean isConnected(RegisterActivity login) {
        ConnectivityManager connectivityManager = (ConnectivityManager) login.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())) {
            return true;
        } else {
            return false;
        }

    }

    /************ Chequear conexión a Internet *********************/


}