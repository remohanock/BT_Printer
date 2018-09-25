package com.remo.material.bluetoothprinter.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.remo.material.bluetoothprinter.R;
import com.remo.material.bluetoothprinter.apimanager.APIJSONHandler;
import com.remo.material.bluetoothprinter.interfaces.APIListener;
import com.remo.material.bluetoothprinter.interfaces.ListenerInterface;
import com.remo.material.bluetoothprinter.model.Trip;
import com.remo.material.bluetoothprinter.model.Tripsapi;
import com.remo.material.bluetoothprinter.model.User;
import com.remo.material.bluetoothprinter.utils.CustomLoadingClass;
import com.remo.material.bluetoothprinter.utils.DownloadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.remo.material.bluetoothprinter.activity.MainActivity.MyPREFERENCES;

public class TripSelectActivity extends AppCompatActivity {

    private String TAG = TripSelectActivity.class.getSimpleName();
    private ArrayList<String> filesList = new ArrayList<>();
    ListView lv_trips;
    SharedPreferences sharedpreferences;
    Button btn_menu;
    ProgressDialog mProgressDialog;
    List<Tripsapi> tripsAPIArrayList;
    TextView tv_deviceid,tv_sync_all;
    String deviceID;
    List<String> fileNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_select);
        lv_trips = findViewById(R.id.lv_trips);
        btn_menu = findViewById(R.id.btn_menu);
        tv_deviceid = findViewById(R.id.tv_deviceid);
        tv_sync_all = findViewById(R.id.tv_sync_all);
        deviceID = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        tv_deviceid.setText( deviceID);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (isReadStoragePermissionGranted()){
            getALlFiles();
        }
        mProgressDialog = new ProgressDialog(TripSelectActivity.this);
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(true);

        lv_trips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(TripSelectActivity.this, MainActivity.class);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("TRIP", tripsAPIArrayList.get(i).getRootid());
                editor.apply();
                startActivity(intent);
                int tripid = sharedpreferences.getInt("TRIPID",1);
                int ticketNumber = sharedpreferences.getInt("ticketNumber", 120000);
                Trip trip = new Trip(tripid,ticketNumber,0);
                trip.save();
                finish();
            }
        });


        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });

        tv_sync_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callTripsAPI();
            }
        });

    }

    public void showBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        final boolean[] isInspector = {false};
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        Button btn_show_tickets = dialog.findViewById(R.id.btn_show_tickets);
        Button btn_download = dialog.findViewById(R.id.btn_download);
        Button btn_end_trip = dialog.findViewById(R.id.btn_end_trip);
        btn_end_trip.setVisibility(View.GONE);
        final Button btn_collection_report = dialog.findViewById(R.id.btn_collection_report);
        Button btn_detailed_report = dialog.findViewById(R.id.btn_detailed_report);
        final Button btn_inspection = dialog.findViewById(R.id.btn_inspection);


        btn_show_tickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(TripSelectActivity.this, TicketListActivity.class);
                startActivity(intent);
            }
        });
        btn_collection_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(TripSelectActivity.this, CollectionReportActivity.class);
                startActivity(intent);
            }
        });
        btn_detailed_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(TripSelectActivity.this, DetailedReportActivity.class);
                if (isInspector[0]){
                    intent.putExtra("INSPECTOR",true);
                }else{
                    intent.putExtra("INSPECTOR",false);
                }
                startActivity(intent);
            }
        });
        btn_inspection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog pinDialog = new Dialog(TripSelectActivity.this);
                pinDialog.setContentView(R.layout.pin_layout);
                pinDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                pinDialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                final EditText et_pin = pinDialog.findViewById(R.id.et_pin);
                Button btn_enter = pinDialog.findViewById(R.id.btn_enter);
                btn_enter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String pin = et_pin.getText().toString().trim();
                        et_pin.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                et_pin.setError(null);
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });
                        if (!pin.equals("")) {
                            boolean passed = false;
                            List<User> users = User.listAll(User.class);
                            for (User user : users) {
                                if (pin.equals(user.getPasscode())) {
                                    passed = true;
                                    break;
                                }
                            }
                            if (passed) {
                                pinDialog.dismiss();
                                btn_collection_report.setVisibility(View.VISIBLE);
                                btn_inspection.setVisibility(View.GONE);
                                isInspector[0] = true;
                            } else {
                                et_pin.setError("Invalid pin! Please try again.");
                            }
                        } else {
                            et_pin.setError("Please enter a pin.");
                        }
                    }
                });
                pinDialog.show();
            }
        });

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DownloadTask downloadTask = new DownloadTask(TripSelectActivity.this, mProgressDialog, "Kadamba_Transport.apk",
                        new ListenerInterface() {
                            @Override
                            public void onCompleted() {

                            }
                        });
                downloadTask.execute("http://webappin.com/download/Kadamba_Transport.apk");

                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });
            }
        });


        dialog.show();

    }

    private void callTripsAPI() {
        try {
            Tripsapi.executeQuery("CREATE TABLE TRIPSAPI(APIID INT PRIMARY KEY, ROOTID TEXT, SNAME TEXT, SSTART TEXT, SEND TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Tripsapi.executeQuery("DELETE FROM TRIPSAPI");
        } catch (Exception e) {
            e.printStackTrace();
        }
        CustomLoadingClass.CustomLoadingShow(TripSelectActivity.this,true);
        APIJSONHandler apijsonHandler = new APIJSONHandler();
        apijsonHandler.getTripsAPI(TripSelectActivity.this,"http://ktcl.916.net.in/api/shedule/?key=5559&mcode="+deviceID, new APIListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(String response) {
                try {
                    CustomLoadingClass.CustomLoadingHide();
                    JSONObject jsonObject = new JSONObject(response);
                    Log.d(TAG, "onComplete: "+response);

                    if (jsonObject.getString("status").equals("success")) {
                        final int[] completed = {0};
                        JSONArray jsonArray = jsonObject.getJSONArray("data_");
                        tripsAPIArrayList = new ArrayList<>();
                        fileNames = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObjTrip = jsonArray.getJSONObject(i);
                            Tripsapi tripsAPI = new Tripsapi(i,jsonObjTrip.getString("root_id"),
                                    jsonObjTrip.getString("s_name"),
                                    jsonObjTrip.getString("s_start"),
                                    jsonObjTrip.getString("s_end"));
                            tripsAPI.save();
//                            tripsAPIArrayList.add(tripsAPI);
                        }
                        tripsAPIArrayList = Tripsapi.listAll(Tripsapi.class);
                        filesList = new ArrayList<>();
                        for (int i=0;i<tripsAPIArrayList.size();i++){
                            filesList.add(tripsAPIArrayList.get(i).getSname());
                            if (!fileNames.contains(tripsAPIArrayList.get(i).getRootid())) {
                                fileNames.add(tripsAPIArrayList.get(i).getRootid());
                            }
                        }
                        ArrayAdapter adapter = new ArrayAdapter<String>(TripSelectActivity.this, R.layout.trip_item, filesList);
                        lv_trips.setAdapter(adapter);
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/KadambaTransport";
                        Log.d("Files", "Path: " + path);
                        File directory = new File(path);
                        if (!directory.exists()) {
                            if (directory.mkdirs()) {
                                Log.d(TAG, "Successfully created the parent dir:" + directory.getName());
                            } else {
                                Log.d(TAG, "Failed to create the parent dir:" + directory.getName());
                            }
                        }
                        for (int j=0;j<fileNames.size();j++) {
                            CustomLoadingClass.CustomLoadingShow(TripSelectActivity.this,true);
                            DownloadTask downloadTask = new DownloadTask(TripSelectActivity.this, mProgressDialog, fileNames.get(j) + ".xls",
                                    new ListenerInterface() {
                                        @Override
                                        public void onCompleted() {
                                            completed[0]++;
                                            if (completed[0]==fileNames.size()*2) {
                                                CustomLoadingClass.CustomLoadingHide();
                                                Toast.makeText(TripSelectActivity.this, "All data synced.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            downloadTask.execute("http://ktcl.916.net.in/Data/"+fileNames.get(j)+".xls");
                            DownloadTask downloadTask_ = new DownloadTask(TripSelectActivity.this, mProgressDialog, fileNames.get(j) + "_.xls",
                                    new ListenerInterface() {
                                        @Override
                                        public void onCompleted() {
                                            completed[0]++;
                                            if (completed[0]==fileNames.size()*2) {
                                                CustomLoadingClass.CustomLoadingHide();
                                                Toast.makeText(TripSelectActivity.this, "All data synced.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            downloadTask_.execute("http://ktcl.916.net.in/Data/"+fileNames.get(j)+"_.xls");

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCompleteWithError(String error) {
                CustomLoadingClass.CustomLoadingHide();
            }
        });
    }

    private void getALlFiles(){
        tripsAPIArrayList = new ArrayList<>();
        try {
            tripsAPIArrayList = Tripsapi.listAll(Tripsapi.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tripsAPIArrayList.size()!=0){
            filesList=new ArrayList<>();
            for (int i=0; i<tripsAPIArrayList.size();i++){
                filesList.add(tripsAPIArrayList.get(i).getSname());
            }
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/KadambaTransport";
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    Log.d(TAG, "Successfully created the parent dir:" + directory.getName());
                } else {
                    Log.d(TAG, "Failed to create the parent dir:" + directory.getName());
                }
            }
            File[] files = directory.listFiles();
            Log.d("Files", "Size: "+ files.length);

            ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.trip_item, filesList);
            lv_trips.setAdapter(adapter);
            try {
                Trip.executeQuery("CREATE TABLE TRIP(TRIPID INT PRIMARY KEY, FIRSTTICKET INT, LASTTICKET INT)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Please tap on sync button to get all trips", Toast.LENGTH_SHORT).show();
        }

    }

    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.d(TAG, "External storage2");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                    //downloadPdfFile();
                }else{
                    //progress.dismiss();
                    Toast.makeText(this, "Please allow permission", Toast.LENGTH_SHORT).show();
                }
                break;

            case 3:
                Log.d(TAG, "External storage1");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                    getALlFiles();
                }else{
                    //progress.dismiss();
                    Toast.makeText(this, "Please allow permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
