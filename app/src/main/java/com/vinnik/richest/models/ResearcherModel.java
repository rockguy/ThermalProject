package com.vinnik.richest.models;

import java.util.Date;

import io.realm.RealmObject;

public class ResearcherModel extends RealmObject {

    private double outdoorTemp;
    private double avgTemp;
    private double minTemp;
    private String theWeather;
    private int airHumidity;
    private int windSpeed;
    private Date time;
    private String result;

    public String getTheWeather() {
        return theWeather;
    }

    public void setTheWeather(String theWeather) {
        this.theWeather = theWeather;
    }

    public int getAirHumidity() {
        return airHumidity;
    }

    public void setAirHumidity(int airHumidity) {
        this.airHumidity = airHumidity;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    double delta;


    public double getOutdoorTemp() {
        return outdoorTemp;
    }

    public void setOutdoorTemp(double outdoorTemp) {
        this.outdoorTemp = outdoorTemp;
    }

    public double getAvgTemp() {
        return avgTemp;
    }

    public void setAvgTemp(double avgTemp) {
        this.avgTemp = avgTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(double minTemp) {
        this.minTemp = minTemp;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


}
