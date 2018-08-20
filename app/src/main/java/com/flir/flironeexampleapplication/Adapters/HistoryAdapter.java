package com.flir.flironeexampleapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.flir.flironeexampleapplication.R;
import com.flir.flironeexampleapplication.models.PhotoModel;

public class HistoryAdapter extends BaseAdapter {
    PhotoModel[][] data;
    LayoutInflater lInflater;
    Context ctx;

    public HistoryAdapter (PhotoModel[][] photoModels, Context context){
        ctx = context;
        data = photoModels;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int i) {
        return data[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.history_item, viewGroup, false);
        }

        ((TextView) view.findViewById(R.id.result)).setText(data[i][0].getResult());

        return view;
    }
}
