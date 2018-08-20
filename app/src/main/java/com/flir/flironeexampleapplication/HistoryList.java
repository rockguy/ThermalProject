package com.flir.flironeexampleapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.flir.flironeexampleapplication.Adapters.HistoryAdapter;
import com.flir.flironeexampleapplication.models.PhotoModel;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class HistoryList extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.history_list);

        realm = Realm.getDefaultInstance();
        Log.i("Test", realm.getPath());
        RealmResults<PhotoModel> query = realm.where(PhotoModel.class).findAll();
        PhotoModel[] models = query.sort("folder_name", Sort.ASCENDING).toArray(new PhotoModel[0]);
        PhotoModel[][] data = new PhotoModel[models.length / 4][4];
        for(int i = 0; i < models.length / 4; i++){
            data[i][0] = models[i*4];
            data[i][1] = models[i*4+1];
            data[i][2] = models[i*4+2];
            data[i][3] = models[i*4+3];
        }
        listView.setAdapter(new HistoryAdapter(data, this));

    }

}
