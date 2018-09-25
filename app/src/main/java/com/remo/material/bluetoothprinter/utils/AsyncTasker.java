package com.remo.material.bluetoothprinter.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.remo.material.bluetoothprinter.interfaces.ListenerInterface;
import com.remo.material.bluetoothprinter.model.User;

public class AsyncTasker extends AsyncTask<Void, Void, Void> {

    private Context context;
    private ListenerInterface listenerInterface;

    public AsyncTasker(Context context, ListenerInterface listenerInterface) {
        this.context = context;
        this.listenerInterface = listenerInterface;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            User.executeQuery("CREATE TABLE USER(USERID INT PRIMARY KEY, NAME TEXT, PASSCODE TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (User.listAll(User.class).size()==0) {
            User user = new User(1, "Conductor", "1234");
            user.save();
            user = new User(2, "Inspector", "1111");
            user.save();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        listenerInterface.onCompleted();
    }
}
