package com.vinnik.richest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.vinnik.richest.Adapters.HistoryAdapter;
import com.vinnik.richest.models.DiamondModel;

import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmResults;

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
        RealmResults<DiamondModel> query = realm.where(DiamondModel.class).findAll();
        DiamondModel[] models = query.toArray(new DiamondModel[0]);
//        ResearcherModel[][] data = new ResearcherModel[models.length][4];
//        for(int i = 0; i < models.length; i++){
//            data[i][0] = models[i].getMinTemp();
//            data[i][1] = models[i];
//            data[i][2] = models[i];
//            data[i][3] = models[i];
//        }
        listView.setAdapter(new HistoryAdapter(Arrays.asList(models), this));
        //realm.close();
    }

    public void onBackPressed() {
        final Intent i = new Intent(this, StartActivity.class);
        startActivity(i);
    }

}
