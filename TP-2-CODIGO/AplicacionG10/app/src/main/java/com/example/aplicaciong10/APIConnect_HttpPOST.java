package com.example.aplicaciong10;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class APIConnect_HttpPOST extends IntentService {

    private Exception mException = null;

    private HttpURLConnection httpConnection;

    private final String REGISTER_EVENT_LOGIN = "RegisterEventLogin";
    private final String REGISTER_EVENT_TEMPERATURE = "RegisterEventTemperature";
    private final String REGISTER_EVENT_SHAKE = "RegisterEventShake";
    private final String REFRESH_TOKEN = "RefreshToken";

    private final String LOGIN = "Login";
    private final String REGISTER = "Register";

    private URL mUrl;

    public APIConnect_HttpPOST() { super("ServicesHttp_POST");}

    @Override
    public void onCreate(){

        super.onCreate();

        Log.i("LOGUEO SERVICE", "Service onCreate()");
    }

    protected void onHandleIntent(Intent intent){
        try{
            String uri = intent.getExtras().getString("uri");
            JSONObject datosJson = new JSONObject(intent.getExtras().getString("datosJson"));
            String sendMode = intent.getExtras().getString("sendMode");
            JSONObject loginRegisterJson = new JSONObject(intent.getExtras().getString("loginRegisterJson"));

            if(sendMode.equals(REFRESH_TOKEN))
            {
                String result = PUT(uri, loginRegisterJson);
                if(result == null){
                    Log.e("LOGUEO SERVICE", "Error en GET:\n" + mException.toString());
                    return;
                }

                if(result == "NO_OK"){
                    Log.e("LOGUEO SERVICE", " se recibio response NO_OK");
                    return;
                }

                Intent i = new Intent("com.example.intentservice.intent.action.REFRESH_TOKEN");
                i.putExtra("datosJson", result);
                sendBroadcast(i);
            }
            else
                ejecutarPost(uri, datosJson, sendMode, loginRegisterJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StringBuilder convertInputStreamToString(InputStreamReader inputStream) throws IOException{
        BufferedReader br = new BufferedReader(inputStream);
        StringBuilder result = new StringBuilder();
        String line;
        while((line = br.readLine()) != null){
            result.append(line + "\n");
        }
        br.close();
        return result;
    }

    private String POST(String uri, JSONObject datosJson)
    {
        HttpURLConnection urlConnection = null;
        String result = "";

        try
        {
            URL mUrl = new URL(uri);


            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestMethod("POST");


            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(datosJson.toString().getBytes("UTF-8"));
            Log.i("LOGUEO SERVICE", "Se va a enviar al Servidor" + datosJson.toString());

            wr.flush();


            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            if((responseCode == HttpURLConnection.HTTP_OK) || (responseCode == HttpURLConnection.HTTP_CREATED))
            {
                InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
                result = convertInputStreamToString(inputStream).toString();
            }
            else if(responseCode == HttpURLConnection.HTTP_BAD_REQUEST)
            {
                InputStreamReader inputStream = new InputStreamReader(urlConnection.getErrorStream());
                result = convertInputStreamToString(inputStream).toString();
            }
            else
            {
                result = "NO_OK";
            }

            mException = null;
            wr.close();
            urlConnection.disconnect();
            return result;

        } catch (Exception e) {
            mException = e;
            return null;
        }
    }

    private String PUT(String uri, JSONObject datosJson)
    {
        HttpURLConnection urlConnection = null;
        String result = "";

        try
        {
            URL mUrl = new URL(uri);


            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setRequestProperty("Authorization", "Bearer " + datosJson.get("token_refresh").toString());
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestMethod("PUT");


            /*
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(datosJson.toString().getBytes("UTF-8"));
            Log.i("LOGUEO SERVICE", "Se va a enviar al Servidor" + datosJson.toString());

            wr.flush();
            */



            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            if((responseCode == HttpURLConnection.HTTP_OK) || (responseCode == HttpURLConnection.HTTP_CREATED))
            {
                InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
                result = convertInputStreamToString(inputStream).toString();
            }
            else if(responseCode == HttpURLConnection.HTTP_BAD_REQUEST)
            {
                InputStreamReader inputStream = new InputStreamReader(urlConnection.getErrorStream());
                result = convertInputStreamToString(inputStream).toString();
            }
            else
            {
                result = "NO_OK";
            }

            mException = null;
            //wr.close();
            urlConnection.disconnect();
            return result;

        } catch (Exception e) {
            mException = e;
            return null;
        }
    }




    protected void ejecutarPost(String uri, JSONObject datosJson, String sendMode, JSONObject loginRegisterJson){

        if(sendMode.equals(LOGIN))
        {
            String result = POST(uri, datosJson);

            if(result == null){
                Log.e("LOGUEO SERVICE", "Error en GET:\n" + mException.toString());
                return;
            }

            if(result == "NO_OK"){
                Log.e("LOGUEO SERVICE", " se recibio response NO_OK");
                return;
            }

            Intent i = new Intent("com.example.intentservice.intent.action.RESPUESTA_LOGIN");
            i.putExtra("datosJson", result);
            sendBroadcast(i);

        } else if(sendMode.equals(REGISTER))
        {
            String result = POST(uri, datosJson);

            if(result == null){
                Log.e("LOGUEO SERVICE", "Error en GET:\n" + mException.toString());
                return;
            }

            if(result == "NO_OK"){
                Log.e("LOGUEO SERVICE", " se recibio response NO_OK");
                return;
            }

            Intent i = new Intent("com.example.intentservice.intent.action.RESPUESTA_REGISTER");
            i.putExtra("datosJson", result);
            sendBroadcast(i);
        }
        else if(sendMode.equals(REGISTER_EVENT_LOGIN) || sendMode.equals(REGISTER_EVENT_TEMPERATURE) || sendMode.equals(REGISTER_EVENT_SHAKE))
        {
            String result = POSTRegisterEvent(uri, datosJson, loginRegisterJson);

            if(result == null){
                Log.e("LOGUEO SERVICE", "Error en GET:\n" + mException.toString());
                return;
            }

            if(result == "NO_OK"){
                Log.e("LOGUEO SERVICE", " se recibio response NO_OK");
                return;
            }

            String tag = null;

            if(sendMode.equals(REGISTER_EVENT_LOGIN))
                tag = "com.example.intentservice.intent.action.REGISTER_EVENT_LOGIN";
            else if(sendMode.equals(REGISTER_EVENT_TEMPERATURE))
                tag = "com.example.intentservice.intent.action.REGISTER_EVENT_LOW_TEMPERATURE";
            else if(sendMode.equals(REGISTER_EVENT_SHAKE))
                tag = "com.example.intentservice.intent.action.REGISTER_EVENT_SHAKE";

            Intent i = new Intent(tag);
            i.putExtra("datosJson", result);
            sendBroadcast(i);
        }

        /*
        String result = POST(uri, datosJson);

        if(result == null){
            Log.e("LOGUEO SERVICE", "Error en GET:\n" + mException.toString());
            return;
        }

        if(result == "NO_OK"){
            Log.e("LOGUEO SERVICE", " se recibio response NO_OK");
            return;
        }

        Intent i = new Intent("com.example.intentservice.intent.action.RESPUESTA_OPERACION" );
        i.putExtra("datosJson", result);
        sendBroadcast(i);
         */
    }

    private String POSTRegisterEvent(String uri, JSONObject datosJson, JSONObject loginRegisterJson)
    {
        HttpURLConnection urlConnection = null;
        String result = "";

        try
        {
            URL mUrl = new URL(uri);


            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setRequestProperty("Authorization", "Bearer " + loginRegisterJson.get("token").toString());
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestMethod("POST");


            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(datosJson.toString().getBytes("UTF-8"));
            Log.i("LOGUEO SERVICE", "Se va a enviar al Servidor" + datosJson.toString());

            wr.flush();


            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            if((responseCode == HttpURLConnection.HTTP_OK) || (responseCode == HttpURLConnection.HTTP_CREATED))
            {
                InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
                result = convertInputStreamToString(inputStream).toString();
            }
            else if(responseCode == HttpURLConnection.HTTP_BAD_REQUEST)
            {
                InputStreamReader inputStream = new InputStreamReader(urlConnection.getErrorStream());
                result = convertInputStreamToString(inputStream).toString();
            }
            else
            {
                result = "NO_OK";
            }

            mException = null;
            wr.close();
            urlConnection.disconnect();
            return result;

        } catch (Exception e) {
            mException = e;
            return null;
        }











    }

    @Override
    public void onDestroy(){
        super.onCreate();

        Log.i("LOGUEO SERVICE", "Service onDestroy()");
    }


}