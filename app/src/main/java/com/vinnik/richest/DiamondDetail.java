package com.vinnik.richest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.vinnik.richest.models.DiamondModel;

import io.realm.Realm;

public class DiamondDetail extends AppCompatActivity {

    Realm realm;

    EditText type;
    EditText form;
    EditText countOfEdges;
    EditText shape;
    EditText weight;
    EditText purity;
    EditText diameter;
    EditText color;
    EditText cutType;
    EditText factorK;
    EditText factorD;
    EditText factorM;

    Button save;
    Button delete;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diamond_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSaveEnabled(true);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        DiamondModel diamond = (DiamondModel) intent.getSerializableExtra("Diamond");


        type = (EditText) findViewById(R.id.type);
        form = (EditText) findViewById(R.id.form);
        countOfEdges = (EditText) findViewById(R.id.countOfEdges);
        shape = (EditText) findViewById(R.id.quality);
        weight = (EditText) findViewById(R.id.weight);
        purity = (EditText) findViewById(R.id.purity);
        diameter = (EditText) findViewById(R.id.diameter);
        color = (EditText) findViewById(R.id.color);
        cutType = (EditText) findViewById(R.id.cutType);
        factorK = (EditText) findViewById(R.id.factorK);
        factorD = (EditText) findViewById(R.id.factorD);
        factorM = (EditText) findViewById(R.id.factorM);

        save = (Button) findViewById(R.id.save_button);
        delete = (Button) findViewById(R.id.delete_button);


        type.setText(diamond.getType());
        form.setText(diamond.getForm());
        countOfEdges.setText(diamond.getCountOfEdgesT());
        shape.setText(diamond.getShape());
        weight.setText(diamond.getWeightT());
        purity.setText(diamond.getClarityT());
        diameter.setText(diamond.getDiameterT());
        color.setText(diamond.getColor());
        cutType.setText(diamond.getCutType());
        factorK.setText(diamond.getFactorKT());
        factorD.setText(diamond.getFactorDT());
        factorM.setText(diamond.getFactorMT());

        delete.setVisibility(diamond.getId() == 0 ? View.GONE : View.VISIBLE);

        save.setOnClickListener(view -> {

            if (diamond.getId() == 0) {
                Number id = realm.where(DiamondModel.class).max("id");
                diamond.setId(id == null ? 1 : id.longValue() + 1);
            }

            diamond.setType(type.getText().toString().toLowerCase());
            diamond.setForm(form.getText().toString());
            diamond.setCountOfEdges(Byte.parseByte(countOfEdges.getText().toString()));
            diamond.setShape(shape.getText().toString());
            diamond.setWeight(Float.parseFloat(weight.getText().toString()));
            diamond.setClarity(Integer.parseInt(purity.getText().toString()));
            diamond.setDiameter(Float.parseFloat(diameter.getText().toString()));
            diamond.setColor(color.getText().toString());
            diamond.setCutType(cutType.getText().toString());
            diamond.setFactorK(Float.parseFloat(factorK.getText().toString()));
            diamond.setFactorD(Float.parseFloat(factorD.getText().toString()));
            diamond.setFactorM(Float.parseFloat(factorM.getText().toString()));


            realm.beginTransaction();
            realm.copyToRealmOrUpdate(diamond);
            realm.commitTransaction();

            onBackPressed();
        });

        delete.setOnClickListener(view -> {
            diamond.deleteFromRealm();
            onBackPressed();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        realm.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        realm = Realm.getDefaultInstance();
    }

}
