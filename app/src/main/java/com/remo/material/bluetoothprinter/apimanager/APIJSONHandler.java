package com.remo.material.bluetoothprinter.apimanager;

import android.content.Context;

import com.remo.material.bluetoothprinter.interfaces.APIListener;

import org.json.JSONObject;



public class APIJSONHandler {
    APIManager manager = APIManager.getInstance();


    public APIJSONHandler() {

    }

    /**
     * Retrieves list of trips for the particular device
     *
     * @param apiListener - API Listener interface
     */
    public void getTripsAPI(Context context,String url, APIListener apiListener) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("currentdate", "");
            manager.sendGETAPI(url, apiListener);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
