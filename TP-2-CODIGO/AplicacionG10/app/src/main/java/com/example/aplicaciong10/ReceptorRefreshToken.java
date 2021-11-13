package com.example.aplicaciong10;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
public class ReceptorRefreshToken extends BroadcastReceiver {
    private String success;
    private LibraryListActivity activity;
    private LibraryListPresenter presenter;

    public ReceptorRefreshToken(LibraryListActivity activity, LibraryListPresenter presenter){
        this.activity = activity;
        this.presenter = presenter;
    }

    public void onReceive(Context context, Intent intent) {

        try {
            String datosJsonString = intent.getStringExtra("datosJson");
            JSONObject datosJson = new JSONObject(datosJsonString);

            Log.i("LOGUEO MAIN", "Se tratará de actualizar el token...");

            success = datosJson.get("success").toString();
            if(success.equals("true")){
                Toast.makeText(this.activity, "Se ha actualizado el token satisfactoriamente!", Toast.LENGTH_SHORT).show();
                this.activity.jsonObject = datosJson;

                if(this.presenter.isEventTemperature)
                    this.activity.registerEventTemperature();
                else if(this.presenter.isEventShake)
                    this.activity.registerEventShake();
            }

            else if(success.equals("false")){
                Toast.makeText(this.activity, "Hubo un error en actualizar el token. (" + datosJson.get("msg").toString() + ")", Toast.LENGTH_SHORT).show();
            }

            this.presenter.isEventTemperature = false;
            this.presenter.isEventShake = false;

        } catch(JSONException e){
            e.printStackTrace();
        }

    }
}