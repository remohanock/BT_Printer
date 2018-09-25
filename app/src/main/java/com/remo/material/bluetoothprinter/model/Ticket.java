package com.remo.material.bluetoothprinter.model;

import com.orm.SugarRecord;

public class Ticket extends SugarRecord {

    private int ticketID;
    private String date;
    private String time;
    private int fromStage;
    private int toStage;
    private int fullCount;
    private double fullFare;
    private int halfCount;
    private double halfFare;
    private int passCount;
    private int luggageFare;
    private double totalFare;
    private String froms;
    private String tos;
    private double distance;

    public Ticket(int ticketID, String date, String time, int fromStage, int toStage, int fullCount, double fullFare, int halfCount, double halfFare, int passCount, int luggageFare, double totalFare, String froms, String tos, double distance) {
        this.ticketID = ticketID;
        this.date = date;
        this.time = time;
        this.fromStage = fromStage;
        this.toStage = toStage;
        this.fullCount = fullCount;
        this.fullFare = fullFare;
        this.halfCount = halfCount;
        this.halfFare = halfFare;
        this.passCount = passCount;
        this.luggageFare = luggageFare;
        this.totalFare = totalFare;
        this.froms = froms;
        this.tos = tos;
        this.distance = distance;
    }

    public Ticket() {
    }

    public int getTicketID() {
        return ticketID;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getFromStage() {
        return fromStage;
    }

    public void setFromStage(int fromStage) {
        this.fromStage = fromStage;
    }

    public int getToStage() {
        return toStage;
    }

    public void setToStage(int toStage) {
        this.toStage = toStage;
    }

    public int getFullCount() {
        return fullCount;
    }

    public void setFullCount(int fullCount) {
        this.fullCount = fullCount;
    }

    public double getFullFare() {
        return fullFare;
    }

    public void setFullFare(double fullFare) {
        this.fullFare = fullFare;
    }

    public int getHalfCount() {
        return halfCount;
    }

    public void setHalfCount(int halfCount) {
        this.halfCount = halfCount;
    }

    public double getHalfFare() {
        return halfFare;
    }

    public void setHalfFare(double halfFare) {
        this.halfFare = halfFare;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public int getLuggageFare() {
        return luggageFare;
    }

    public void setLuggageFare(int luggageFare) {
        this.luggageFare = luggageFare;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    public String getFroms() {
        return froms;
    }

    public void setFroms(String froms) {
        this.froms = froms;
    }

    public String getTos() {
        return tos;
    }

    public void setTos(String tos) {
        this.tos = tos;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketID=" + ticketID +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", fromStage=" + fromStage +
                ", toStage=" + toStage +
                ", fullCount=" + fullCount +
                ", fullFare=" + fullFare +
                ", halfCount=" + halfCount +
                ", halfFare=" + halfFare +
                ", passCount=" + passCount +
                ", luggageFare=" + luggageFare +
                ", totalFare=" + totalFare +
                ", froms='" + froms + '\'' +
                ", tos='" + tos + '\'' +
                ", distance=" + distance +
                '}';
    }
}
