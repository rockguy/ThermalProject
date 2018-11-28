package com.vinnik.richest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.realm.Realm;

public class StartActivity extends AppCompatActivity {
    public static final String TAG = "RICHEST";
    Realm realm;

    @Override
    protected void onPause() {
        super.onPause();
        realm.close();
    }

    protected void onResume() {
        super.onResume();
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText factorK = (EditText) findViewById(R.id.factorK);
        final EditText factorM = (EditText) findViewById(R.id.factorM);
        Button button = (Button) findViewById(R.id.start_button);

        final Intent i = new Intent(this, GLUserPreviewActivity.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    //TODO: по другому хранить значения
                    i.putExtra("factorK", Double.parseDouble(factorK.getText().toString()));
                    i.putExtra("factorM", Double.parseDouble(factorM.getText().toString()));

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
