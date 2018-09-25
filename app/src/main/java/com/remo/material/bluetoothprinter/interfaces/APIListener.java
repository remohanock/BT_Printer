package com.remo.material.bluetoothprinter.interfaces;

public interface APIListener {
    public void onStart();
    public void onComplete(String response);
    public void onCompleteWithError(String error);
}
