package com.vinnik.richest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.realm.Realm;

public class StartActivity extends AppCompatActivity {
    public static final String TAG = "RICHEST";
    public static final String DIAMOND_FACTORS = "diamond_factors";

    public static final String FACTOR_K = "factor_k";
//    public static final String FACTOR_M = "factor_m";

    EditText factorK;
//    EditText factorM;

    Realm realm;
    SharedPreferences preferences;

    @Override
    protected void onPause() {
        super.onPause();
        realm.close();
        //Возможно не сработает
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(FACTOR_K, Float.parseFloat(factorK.getText().toString()));
//        editor.putFloat(FACTOR_M, Float.parseFloat(factorM.getText().toString()));
        editor.apply();
    }

    protected void onResume() {
        super.onResume();
        realm = Realm.getDefaultInstance();

        factorK.setText(String.valueOf(preferences.getFloat(FACTOR_K, 0)));
//        factorM.setText(String.valueOf(preferences.getFloat(FACTOR_M, 0)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        factorK = (EditText) findViewById(R.id.factorK);
//        factorM = (EditText) findViewById(R.id.factorM);
        Button button = (Button) findViewById(R.id.start_button);

        preferences = getSharedPreferences(DIAMOND_FACTORS ,MODE_PRIVATE);

        final Intent i = new Intent(this, GLUserPreviewActivity.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(i);
            }
        });

        Button historyButton = (Button) findViewById(R.id.historyButton);
        final Intent i2 = new Intent(this, HistoryList.class);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(i2);
            }
        });

    }

}
