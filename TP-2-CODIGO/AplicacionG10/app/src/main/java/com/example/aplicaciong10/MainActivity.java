package com.example.aplicaciong10;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity {

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
            Intent intent = new Intent(MainActivity.this, confirmPatternActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_main);
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
                }

                @Override
                public void onCleared() {

                }
            });


            Button btnGuardar = (Button) findViewById(R.id.btnGuardarPatron);
            btnGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(savePatternKey, finalPattern);
                    editor.commit();

                    Toast.makeText(MainActivity.this, "Patron guardado correctamente!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, confirmPatternActivity.class);
                    startActivity(intent);
                }
            });

        }
    }
}