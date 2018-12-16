package com.vinnik.richest.util;

import android.annotation.SuppressLint;

public class TemperatureHelper {

    public static float kelvinToCelsius(int kelvin) {
        return (float) (kelvin / 100 - 273.15);
    }

    public static float celsiusToKelvin(float celsius) {
        return (float) ((celsius + 273.15) * 100);
    }

    @SuppressLint("DefaultLocale")
    public static String temperatureToString(float temp){
        return String.format("% 3.2f",temp);
    }


}
