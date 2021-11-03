package com.example.aplicaciong10;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class confirmPatternActivity extends AppCompatActivity {

    String savePatternKey = "pattern_code";
    String finalPattern = "";
    PatternLockView mPatternLockView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("pattern", Context.MODE_PRIVATE);

        final String savePattern = prefs.getString(savePatternKey,null);
        
        if(savePattern != null && !savePattern.equals("null")){
            setContentView(R.layout.activity_confirm_pattern);
            mPatternLockView = (PatternLockView) findViewById(R.id.pattern_lock_view);
            mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onProgress(List<PatternLockView.Dot> progressPattern) {

                }

                @Override
                public void onComplete(List<PatternLockView.Dot> pattern) {
                    finalPattern = PatternLockUtils.patternToString(mPatternLockView, pattern);

                    if(finalPattern.equals(savePattern)){
                        Toast.makeText(confirmPatternActivity.this, "Password Correcta!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(confirmPatternActivity.this, SuccessfulPatternActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(confirmPatternActivity.this, "Password Incorrecta!", Toast.LENGTH_SHORT).show();
                        Log.i("Ejecuto", savePattern);
                    }

                }

                @Override
                public void onCleared() {

                }
            });
        }
    }
}