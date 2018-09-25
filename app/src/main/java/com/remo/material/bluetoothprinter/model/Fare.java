package com.remo.material.bluetoothprinter.model;

import com.orm.SugarRecord;

public class Fare extends SugarRecord {
    private int stage;
    private int ffrom;
    private int hfrom;
    private int pfrom;
    private int lfrom;
    private int fto;
    private int hto;
    private int pto;
    private int lto;
    private double totalfare;

    public Fare(){

    }

    public Fare(int stage, int ffrom, int hfrom, int pfrom, int lfrom, int fto, int hto, int pto, int lto, double totalfare) {
        this.stage = stage;
        this.ffrom = ffrom;
        this.hfrom = hfrom;
        this.pfrom = pfrom;
        this.lfrom = lfrom;
        this.fto = fto;
        this.hto = hto;
        this.pto = pto;
        this.lto = lto;
        this.totalfare = totalfare;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getFfrom() {
        return ffrom;
    }

    public void setFfrom(int ffrom) {
        this.ffrom = ffrom;
    }

    public int getHfrom() {
        return hfrom;
    }

    public void setHfrom(int hfrom) {
        this.hfrom = hfrom;
    }

    public int getPfrom() {
        return pfrom;
    }

    public void setPfrom(int pfrom) {
        this.pfrom = pfrom;
    }

    public int getLfrom() {
        return lfrom;
    }

    public void setLfrom(int lfrom) {
        this.lfrom = lfrom;
    }

    public int getFto() {
        return fto;
    }

    public void setFto(int fto) {
        this.fto = fto;
    }

    public int getHto() {
        return hto;
    }

    public void setHto(int hto) {
        this.hto = hto;
    }

    public int getPto() {
        return pto;
    }

    public void setPto(int pto) {
        this.pto = pto;
    }

    public int getLto() {
        return lto;
    }

    public void setLto(int lto) {
        this.lto = lto;
    }

    public double getTotalfare() {
        return totalfare;
    }

    public void setTotalfare(double totalfare) {
        this.totalfare = totalfare;
    }

    @Override
    public String toString() {
        return "Fare{" +
                "stage=" + stage +
                ", ffrom=" + ffrom +
                ", hfrom=" + hfrom +
                ", pfrom=" + pfrom +
                ", lfrom=" + lfrom +
                ", fto=" + fto +
                ", hto=" + hto +
                ", pto=" + pto +
                ", lto=" + lto +
                ", fullfare=" + totalfare +
                '}';
    }
}
