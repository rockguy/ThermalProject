package com.vinnik.richest.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vinnik.richest.DiamondDetail;
import com.vinnik.richest.R;
import com.vinnik.richest.models.DiamondModel;

import java.util.List;

import static com.vinnik.richest.StartActivity.TAG;

public class ImageViewAdapter extends BaseAdapter {
    protected final Context ctx;
    protected List<DiamondModel> data;
    protected LayoutInflater lInflater;

    public ImageViewAdapter(List<DiamondModel> data, Context context){
        this.data = data;
        ctx = context;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
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
                view = lInflater.inflate(R.layout.diamond_result_item, viewGroup, false);
            }
            ((TextView) view.findViewById(R.id.result_text)).setText(getResultString(data.get(i)));
            ImageButton save = (ImageButton) view.findViewById(R.id.save_button);
            save.setOnClickListener(view1 -> {
                Toast.makeText(ctx, "Сохраняю", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ctx, DiamondDetail.class);
                intent.putExtra("Diamond",data.get(i));
                ctx.startActivity(intent);
            });
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        return view;
    }

    protected String getResultString(DiamondModel diamond){
        return String.format("factorM: %s; factorD: %s; factorK: %s result: %s",
                diamond.getFactorM(), diamond.getFactorD(), diamond.getFactorK(),
                diamond.getType());
    }
}
