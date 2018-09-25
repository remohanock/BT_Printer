package com.remo.material.bluetoothprinter.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.remo.material.bluetoothprinter.interfaces.ListenerInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog progressDialog;
    private String fileName;
    private ListenerInterface listenerInterface;

    public DownloadTask(Context context, ProgressDialog progressDialog, String fileName, ListenerInterface listenerInterface) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.fileName = fileName;
        this.listenerInterface = listenerInterface;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Toast.makeText(context, "Downloading "+fileName, Toast.LENGTH_SHORT).show();
        if (progressDialog!=null) {
            progressDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/KadambaTransport/"+fileName);
            Log.d("DOWNLOAD", "doInBackground: "+Environment.getExternalStorageDirectory().getAbsoluteFile() + "/KadambaTransport/"+fileName);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
        //Toast.makeText(context, "Downloaded "+fileName, Toast.LENGTH_SHORT).show();
        listenerInterface.onCompleted();

        if (fileName.equals("Kadamba_Transport.apk")) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/KadambaTransport/Kadamba_Transport.apk");
            MimeTypeMap map = MimeTypeMap.getSingleton();
            String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
            String type = map.getMimeTypeFromExtension(ext);

            if (type == null)
                type = "*/*";

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.fromFile(file);

            intent.setDataAndType(data, type);

            context.startActivity(intent);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
    }
}