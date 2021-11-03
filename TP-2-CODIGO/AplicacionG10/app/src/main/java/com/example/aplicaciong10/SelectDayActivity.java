package com.example.aplicaciong10;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andrognito.patternlockview.PatternLockView;

import java.lang.reflect.Field;

import io.paperdb.Paper;



public class SelectDayActivity extends AppCompatActivity {

    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_day);
        Paper.init(this);
    }

    public void OnClickCard(View view) {
        try {
            Paper.book().write("library-day-selected", getIDName(view.getId(), R.id.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(new Intent(SelectDayActivity.this, SelectDateActivity.class));
    }

    public static String getIDName(Integer id, Class<?> clazz) throws Exception {

        Field[] ids = clazz.getFields();
        for (int i = 0; i < ids.length; i++) {
            Object val = ids[i].get(null);
            if (val != null && val instanceof Integer
                    && ((Integer) val).intValue() == id.intValue()) {
                return ids[i].getName();
            }
        }

        return "";

    }
}