package com.remo.material.bluetoothprinter.model;

import com.orm.SugarRecord;

public class Trip extends SugarRecord {

    private int tripid;
    private int firstticket;
    private int lastticket;

    public Trip(int tripid, int firstticket, int lastticket) {
        this.tripid = tripid;
        this.firstticket = firstticket;
        this.lastticket = lastticket;
    }

    public Trip() {
    }

    public int getTripid() {
        return tripid;
    }

    public void setTripid(int tripid) {
        this.tripid = tripid;
    }

    public int getFirstticket() {
        return firstticket;
    }

    public void setFirstticket(int firstticket) {
        this.firstticket = firstticket;
    }

    public int getLastticket() {
        return lastticket;
    }

    public void setLastticket(int lastticket) {
        this.lastticket = lastticket;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "tripid=" + tripid +
                ", firstticket=" + firstticket +
                ", lastticket=" + lastticket +
                '}';
    }
}
