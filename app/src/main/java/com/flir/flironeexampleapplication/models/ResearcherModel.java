package com.flir.flironeexampleapplication.models;

import io.realm.RealmObject;

public class ResearcherModel extends RealmObject {

    double outdoorTemp;
    double avgTemp;
    double minTemp;
    String result;


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
