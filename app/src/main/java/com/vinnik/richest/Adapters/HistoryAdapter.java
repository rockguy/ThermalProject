package com.vinnik.richest.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vinnik.richest.R;
import com.vinnik.richest.models.ResearcherModel;

import static com.vinnik.richest.StartActivity.TAG;

public class HistoryAdapter extends BaseAdapter {
    ResearcherModel[] data;
    LayoutInflater lInflater;
    Context ctx;

    public HistoryAdapter (ResearcherModel[] photoModels, Context context){
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
        try {
            if (view == null) {
                view = lInflater.inflate(R.layout.history_item, viewGroup, false);
            }
            //TODO: сделать нормально
            String theWeather = data[i].getTheWeather();
            if (!theWeather.equals("") && !theWeather.equals(null)) {
                ((TextView) view.findViewById(R.id.the_weather)).setText(theWeather);
            }
            int WindSpeed = data[i].getWindSpeed();
            if (WindSpeed != 0) {
                ((TextView) view.findViewById(R.id.wind_speed)).setText(String.valueOf(WindSpeed));
            }

            ((TextView) view.findViewById(R.id.outdoor_temp)).setText(String.valueOf(data[i].getOutdoorTemp()));
            ((TextView) view.findViewById(R.id.avg_temp)).setText(String.valueOf(data[i].getAvgTemp()));
            ((TextView) view.findViewById(R.id.min_temp)).setText(String.valueOf(data[i].getMinTemp()));
            ((TextView) view.findViewById(R.id.air_humidity)).setText(String.valueOf(data[i].getAirHumidity()));
            ((TextView) view.findViewById(R.id.time)).setText(String.valueOf(data[i].getTime()));
            ((TextView) view.findViewById(R.id.result)).setText(data[i].getResult());
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        return view;
    }
}
