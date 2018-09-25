package com.remo.material.bluetoothprinter.apimanager;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.remo.material.bluetoothprinter.R;
import com.remo.material.bluetoothprinter.interfaces.APIListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class APIManager {
    Context apiContext;

    private static APIManager instance = null;
    public RequestQueue requestQueue;


    public APIManager(Context context) {
        this.apiContext = context;
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());

    }



    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            Cache cache = new DiskBasedCache(apiContext.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            requestQueue = new RequestQueue(cache, network);

            // Don't forget to start the volley request queue
            requestQueue.start();
        }
        return requestQueue;
    }

    public static synchronized APIManager getInstance(Context context) {


        if (null == instance)
            instance = new APIManager(context);
        return instance;
    }

    //this is so you don't need to pass context each time
    public static synchronized APIManager getInstance() {

        if (null == instance) {
            throw new IllegalStateException(APIManager.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }

    /**
     * Common GET API call for communicating with server
     * @param url - url for server communication
     * @param listener - API Listener interface
     */
    public void sendGETAPI( String url, final APIListener listener){
        try {
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    listener.onComplete(response.toString());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    if(error instanceof NetworkError) {
                        listener.onCompleteWithError(apiContext.getString(R.string.no_internet_connection));
                    }else if(error instanceof ServerError){
                        listener.onCompleteWithError("Server Error");
                    } else if (error instanceof TimeoutError) {
                        listener.onCompleteWithError("Server Error");
                    } else {
                        listener.onCompleteWithError(error.getMessage());
                    }

                }
            }) {
                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    /*headers.put("Content-Type", "application/json" );*/
                   /*headers.put( "charset", "utf-8");*/
                    return headers;
                }
            };
            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                    2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Adding request to request queue
           /* if(requestQueue!=null)
                requestQueue.cancelAll(jsonObjReq);*/
            //  requestQueue.start();
            if (requestQueue != null) {
                requestQueue.getCache().clear();
            }
            getRequestQueue().add(jsonObjReq);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Common API call for communicating with server
     * @param jsonObject - json object to be given to server
     * @param url - url for server communication
     * @param listener - API Listener interface
     */
    public void sendPOSTAPI(JSONObject jsonObject , String url, final APIListener listener){
        try {
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    listener.onComplete(response.toString());
                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    if(error instanceof NetworkError) {
                        listener.onCompleteWithError(apiContext.getString(R.string.no_internet_connection));
                    }else if(error instanceof ServerError){
                        listener.onCompleteWithError("Server Error");
                    } else if (error instanceof TimeoutError) {
                        listener.onCompleteWithError("Server Error");
                    } else {
                        listener.onCompleteWithError(error.getMessage());
                    }

                }
            }) {
                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    /*headers.put("Content-Type", "application/json" );*/
                   /*headers.put( "charset", "utf-8");*/
                    return headers;
                }
            };
            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                    2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Adding request to request queue

            /*if(requestQueue!=null)
                requestQueue.cancelAll(jsonObjReq);*/
            // requestQueue.start();
            if (requestQueue != null) {
                requestQueue.getCache().clear();
            }
            getRequestQueue().add(jsonObjReq);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
