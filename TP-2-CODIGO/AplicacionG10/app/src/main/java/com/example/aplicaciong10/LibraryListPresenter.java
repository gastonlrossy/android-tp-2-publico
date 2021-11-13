package com.example.aplicaciong10;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class LibraryListPresenter implements SensorEventListener{

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
    private final float shakeThreshold = 8f;
    private boolean isFirstTime = true;
    private Vibrator vibrator;
    private LibraryListActivity activity;

    public boolean isEventTemperature = false;
    public boolean isEventShake = false;

    private boolean detectedLowTemperature = false;
    private final int temperatureThreshold = 10;
    private boolean timerTimeout = false;
    public ReceptorShakeEvent shakeReceiver;
    public ReceptorRefreshToken  refreshReceiver;
    public ReceptorRegisterEventTemperature registerEventReceiverTemperature;

    public LibraryListPresenter(LibraryListActivity activity){
        this.activity = activity;
        this.vibrator = (Vibrator) this.activity.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setupSensorManager(){
        mSensorManager = (SensorManager) this.activity.getSystemService(Context.SENSOR_SERVICE);
    }
    public void setupShakeEvent(){
        this.shakeReceiver = new ReceptorShakeEvent(this.activity,this);
    }
    public void setupRefreshTokenEvent(){
        this.refreshReceiver = new ReceptorRefreshToken(this.activity,this);
    }
    public void setupReceptorRegisterEventTemperature(){
        this.registerEventReceiverTemperature = new ReceptorRegisterEventTemperature(this.activity,this);
    }


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

    protected void stopTemperatureSensor()
    {
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE));
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

                        this.activity.temperaturePopUp();

                        stopTemperatureSensor();

                        //Para Registrar evento de temperatura baja
                        isEventTemperature = true;

                        this.activity.registerEventTemperature();
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

                            this.activity.registerEventShake();
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


    public void startStop() {
        if(timerRunning)
        {
            stopTimer();
        } else {
            startTimer();
        }
    }

    public void startTimer(){
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

    public void updateTimer(){
        int minutes = (int) timeLeftInMilliseconds / 60000;
        int seconds = (int) timeLeftInMilliseconds % 60000 / 1000;

        String timeLeftText;

        timeLeftText = "" + minutes;
        timeLeftText += ":";

        if(seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;

        this.activity.countdownText.setText(timeLeftText);
    }

    private void stopTimer(){
        countDownTimer.cancel();
        timerRunning = false;
    }

    /*
    @Override
    public void onSensorChanged(SensorEvent event) {

    }
     */

    public void onStop()
    {
        if(!detectedLowTemperature && !timerTimeout)
            stopTemperatureSensor();

        stopAccelerometerSensor();

        startStop();
    }

    public void onResume()
    {
        startStop();

        if(!detectedLowTemperature && !timerTimeout)
            initializeTemperatureSensor();

        initializeAccelerometerSensor();
    }


}
