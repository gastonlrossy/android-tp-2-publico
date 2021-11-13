package com.example.aplicaciong10;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class ReceptorRegisterEventTemperature extends BroadcastReceiver {
    private String success;
    private LibraryListActivity activity;
    private LibraryListPresenter presenter;

    private static final String URI_REFRESH_TOKEN = "http://so-unlam.net.ar/api/api/refresh";
    private final String REFRESH_TOKEN = "RefreshToken";

    public ReceptorRegisterEventTemperature(LibraryListActivity activity, LibraryListPresenter presenter){
        this.activity = activity;
        this.presenter = presenter;
    }

    public void onReceive(Context context, Intent intent) {

        try {
            String datosJsonString = intent.getStringExtra("datosJson");
            JSONObject datosJson = new JSONObject(datosJsonString);

            Log.i("LOGUEO MAIN", "Datos Json Main Thread: " + datosJsonString);

            success = datosJson.get("success").toString();
            if(success.equals("true")){
                Toast.makeText(this.activity, "Se ha registrado el evento de temperatura baja satisfactoriamente!", Toast.LENGTH_SHORT).show();
            }

            else if(success.equals("false")){
                if(datosJson.get("msg").toString().equals("Tiempo de sesión expirado."))
                {
                    Toast.makeText(this.activity, "Tiempo de sesión expirado, se actualizará el token. ", Toast.LENGTH_SHORT).show();
                    //Aca tenemos que realizar un PUT y luego volver a registrar el evento

                    JSONObject obj = new JSONObject();

                    obj.put("empty", "empty");

                    Intent i = new Intent(this.activity, APIConnect_HttpPOST.class);

                    i.putExtra("uri", URI_REFRESH_TOKEN);
                    i.putExtra("datosJson", obj.toString());
                    i.putExtra("sendMode", REFRESH_TOKEN);
                    i.putExtra("loginRegisterJson", this.activity.jsonObject.toString());

                    this.activity.startService(i);

                } else
                    Toast.makeText(this.activity, "Hubo un error en registrar el evento de temperatura baja. (" + datosJson.get("msg").toString() + ")", Toast.LENGTH_SHORT).show();
            }

        } catch(JSONException e){
            e.printStackTrace();
        }

    }
}