package com.vinnik.richest.util;

import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.nfc.tech.TagTechnology;
import android.os.Bundle;
import android.util.Log;

import com.vinnik.richest.GLPreviewActivity;

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.vinnik.richest.StartActivity.TAG;

public class MyLocationListener implements LocationListener {


    @Override
    public void onLocationChanged(Location location) {
        GLPreviewActivity.latitude = location.getLatitude();
        GLPreviewActivity.longitude = location.getLongitude();

        Log.i(TAG,"Latitude: " + location.getLatitude());
        Log.i(TAG,"Longitude: " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
