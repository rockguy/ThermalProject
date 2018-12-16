package com.vinnik.richest.models;

import java.io.Serializable;

import io.realm.RealmObject;

public class DiamondModel extends RealmObject implements Serializable {

    private String type;
    private String form;
    private byte countOfEdges;
    private float weight;
    private int clarity;
    private float diameter;
    private String color;
    private String cutType;
    private float factorK;
    private float factorD;
    private float factorM;
    private String shape;

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public byte getCountOfEdges() {
        return countOfEdges;
    }

    public String getCountOfEdgesT() {
        return String.valueOf(countOfEdges);
    }

    public void setCountOfEdges(byte countOfEdges) {
        this.countOfEdges = countOfEdges;
    }

    public float getWeight() {
        return weight;
    }
    public String getWeightT() {
        return String.valueOf(weight);
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getDiameter() {
        return diameter;
    }

    public int getClarity() {
        return clarity;
    }

    public String getClarityT() {
        return String.valueOf(clarity);
    }

    public void setClarity(int clarity) {
        this.clarity = clarity;
    }

    public void setDiameter(float diameter) {
        this.diameter = diameter;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCutType() {
        return cutType;
    }

    public void setCutType(String cutType) {
        this.cutType = cutType;
    }

    public float getFactorK() {
        return factorK;
    }

    public String getFactorKT() {
        return String.valueOf(factorK);
    }

    public void setFactorK(float factorK) {
        this.factorK = factorK;
    }

    public float getFactorD() {
        return factorD;
    }

    public String getFactorDT() {
        return String.valueOf(factorD);
    }

    public void setFactorD(float factorD) {
        this.factorD = factorD;
    }

    public float getFactorM() {
        return factorM;
    }

    public String getFactorMT() {
        return String.valueOf(factorM);
    }

    public void setFactorM(float factorM) {
        this.factorM = factorM;
    }

    public String getDiameterT() {
        return String.valueOf(diameter);
    }
}
