package com.remo.material.bluetoothprinter.model;

import com.orm.SugarRecord;

public class Tripsapi extends SugarRecord {
    private int apiid;
    private String rootid;
    private String sname;
    private String sstart;
    private String send;

    public Tripsapi(int apiid, String root_id, String s_name, String s_start, String s_end) {
        this.apiid = apiid;
        this.rootid = root_id;
        this.sname = s_name;
        this.sstart = s_start;
        this.send = s_end;
    }

    public Tripsapi() {
    }

    public String getRootid() {
        return rootid;
    }

    public void setRootid(String rootid) {
        this.rootid = rootid;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getSstart() {
        return sstart;
    }

    public void setSstart(String sstart) {
        this.sstart = sstart;
    }

    public String getSend() {
        return send;
    }

    public void setSend(String send) {
        this.send = send;
    }

    public int getApiid() {
        return apiid;
    }

    public void setApiid(int apiid) {
        this.apiid = apiid;
    }

    @Override
    public String toString() {
        return "Tripsapi{" +
                "apiid=" + apiid +
                ", rootid='" + rootid + '\'' +
                ", sname='" + sname + '\'' +
                ", sstart='" + sstart + '\'' +
                ", send='" + send + '\'' +
                '}';
    }
}
