package com.remo.material.bluetoothprinter.model;

import com.orm.SugarRecord;

public class User extends SugarRecord{
    private int userid;
    private String name;
    private String passcode;

    public User(int userid, String name, String passcode) {
        this.userid = userid;
        this.name = name;
        this.passcode = passcode;
    }

    public User() {
    }

    public int getUserid() {
        return userid;
    }

    public void setId(int userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    @Override
    public String toString() {
        return "User{" +
                "userid=" + userid +
                ", name='" + name + '\'' +
                ", passcode='" + passcode + '\'' +
                '}';
    }
}