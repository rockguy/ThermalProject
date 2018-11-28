package com.vinnik.richest.models;

import io.realm.RealmObject;

public class PhotoModel extends RealmObject {

    private String folder_name;
    private String result;
    private String file_name;
    private double min_temp;
    private double hand_temp;
    private float delta_time;

    public String getFolder_name() {
        return folder_name;
    }

    public void setFolder_name(String folder_name) {
        this.folder_name = folder_name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public double getMin_temp() {
        return min_temp;
    }

    public void setMin_temp(double min_temp) {
        this.min_temp = min_temp;
    }

    public double getHand_temp() {
        return hand_temp;
    }

    public void setHand_temp(double hand_temp) {
        this.hand_temp = hand_temp;
    }

    public float getDelta_time() {
        return delta_time;
    }

    public void setDelta_time(float delta_time) {
        this.delta_time = delta_time;
    }


}
