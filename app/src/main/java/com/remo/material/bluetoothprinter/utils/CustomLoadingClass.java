package com.remo.material.bluetoothprinter.utils;

import android.app.ProgressDialog;
import android.content.Context;



public class CustomLoadingClass {

    private static ProgressDialog pDialog;

    public static void CustomLoadingShow(Context context, Boolean visibilty) {
        if(pDialog != null &&  pDialog.isShowing()) {
            try {
                pDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ;
            pDialog = null;
        }
        if(null!= context) {
            pDialog = new ProgressDialog(context);
            if(pDialog!=null) {
                pDialog.setMessage("Please wait...");
                pDialog.setCancelable(visibilty);
                try {
                    pDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void CustomLoadingHide() {
        try {
            if(pDialog != null &&  pDialog.isShowing())
            pDialog.dismiss();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
