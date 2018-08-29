package com.flir.flironeexampleapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.flir.flironeexampleapplication.models.PhotoModel;

import io.realm.Realm;

public class StartActivity extends AppCompatActivity {
    public static final String TAG = "RICHEST";
    //RICHEST

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();

        realm.beginTransaction();

// Add a person
        PhotoModel photo = realm.createObject(PhotoModel.class);
        photo.setFile_name("name");
        photo.setFolder_name("folder");
        photo.setMin_temp(-10);
        photo.setDelta_time(5000);
        photo.hand_temp = 10;
        photo.setResult("Lol result");

        realm.commitTransaction();

        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText delta = (EditText) findViewById(R.id.delta);
        final EditText minTemp = (EditText) findViewById(R.id.minTemp);
        final EditText maxTemp = (EditText) findViewById(R.id.maxTemp);
        Button button = (Button) findViewById(R.id.button);

        final Intent i = new Intent(this, GLPreviewActivity.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO: разобраться с выбором режима
                String deltaVal = (String) delta.getText().toString();

                Log.i("Test", "delta:" + deltaVal);
                if(!deltaVal.equals("")){
                    i.putExtra("delta", Integer.parseInt(deltaVal));
                }
                else{
                    i.putExtra("minTemp", Integer.parseInt(minTemp.getText().toString()));
                    i.putExtra("maxTemp", Integer.parseInt(maxTemp.getText().toString()));

                }



//                if(in != "") {
//                    i.putExtra("delta", Integer.parseInt(in));
//                }
//                else {

                //}
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
