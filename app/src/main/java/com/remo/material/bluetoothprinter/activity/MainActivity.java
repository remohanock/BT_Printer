package com.remo.material.bluetoothprinter.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.remo.material.bluetoothprinter.interfaces.ListenerInterface;
import com.remo.material.bluetoothprinter.model.Fare;
import com.remo.material.bluetoothprinter.model.Trip;
import com.remo.material.bluetoothprinter.model.User;
import com.remo.material.bluetoothprinter.utils.DownloadTask;
import com.remo.material.bluetoothprinter.utils.PrinterCommands;
import com.remo.material.bluetoothprinter.R;
import com.remo.material.bluetoothprinter.model.Ticket;
import com.remo.material.bluetoothprinter.utils.Utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice bluetoothDevice;
    OutputStream outputStream;
    InputStream inputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    String value = "";
    ArrayList<String> stages;
    ArrayList<Double> distance;
    double adultPerCost = 0;
    double childPerCost = adultPerCost / 2;
    Button btn_print;
    Button btn_from_prev, btn_from_next, btn_to_prev, btn_to_next;
    Button btn_add_adult, btn_rem_adult, btn_add_child, btn_rem_child, btn_add_pass, btn_rem_pass, btn_add_luggage, btn_rem_luggage, btn_reset;
    TextView tv_to, tv_from, tv_adult_count, tv_child_count, tv_pass_count, tv_luggage_count;
    TextView tv_total_fare;
    TextView tv_child_total, tv_child_fare, tv_adult_total, tv_adult_fare, tv_distance, tv_pass_fare, tv_pass_total, tv_luggage_total;
    int fromPosition, toPosition;
    int adultCount = 0;
    int childCount = 0;
    int passCount = 0;
    int luggageCount = 0;
    double adultCost = 0;
    double childCost = 0;
    double[][] fareArray;
    private int luggageFare = 10;
    private int ticketNumber;
    public static String MyPREFERENCES = "MyPREFERENCES";
    SharedPreferences sharedpreferences;
    BottomSheetBehavior sheetBehavior;
    LinearLayout bottom_sheet;
    Button btn_menu;
    double distanceInTicket;
    ProgressDialog mProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        bindControls();
        isReadStoragePermissionGranted();
        //isWriteStoragePermissionGranted();
        initializeValues();
        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(true);
        //df.setRoundingMode(RoundingMode.CEILING);
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPrinter();
                if (calcFare() != 0 || passCount != 0) {
                    printBill();
                } else {
                    if (childCount == 0 && adultCount == 0) {
                        Toast.makeText(MainActivity.this, "Add at least one passenger", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Select appropriate route", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

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
                    initializeValues();
                }else{
                    //progress.dismiss();
                    Toast.makeText(this, "Please allow permission", Toast.LENGTH_SHORT).show();
                }
                break;
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

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }

    private void initializeValues() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        ticketNumber = sharedpreferences.getInt("ticketNumber", 120000);
        stages = new ArrayList<>();
        distance = new ArrayList<>();

        try {
            Ticket.executeQuery("CREATE TABLE TICKET(TICKET_ID INT PRIMARY KEY, DATE TEXT, TIME TEXT, FROM_STAGE INT, TO_STAGE INT,FULL_COUNT INT,FULL_FARE NUMBER,HALF_COUNT INT, HALF_FARE NUMBER, PASS_COUNT INT,LUGGAGE_FARE INT,TOTAL_FARE NUMBER,FROMS TEXT,TOS TEXT, DISTANCE NUMBER)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        readExcel(MainActivity.this, sharedpreferences.getString("TRIP", "")+".xls");
        readExcel(MainActivity.this, sharedpreferences.getString("TRIP", "")+"_.xls");


        fromPosition = 0;
        toPosition = 1;
        try {
            tv_from.setText(stages.get(fromPosition));
            tv_to.setText(stages.get(toPosition));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindControls() {
        btn_print = findViewById(R.id.btn_print);
        btn_from_prev = findViewById(R.id.btn_from_prev);
        btn_from_next = findViewById(R.id.btn_from_next);
        btn_to_prev = findViewById(R.id.btn_to_prev);
        btn_to_next = findViewById(R.id.btn_to_next);
        tv_from = findViewById(R.id.tv_from);
        tv_to = findViewById(R.id.tv_to);
        btn_add_adult = findViewById(R.id.btn_add_adult);
        btn_rem_adult = findViewById(R.id.btn_rem_adult);
        btn_add_child = findViewById(R.id.btn_add_child);
        btn_rem_child = findViewById(R.id.btn_rem_child);
        btn_add_pass = findViewById(R.id.btn_add_pass);
        btn_rem_pass = findViewById(R.id.btn_rem_pass);
        btn_add_luggage = findViewById(R.id.btn_add_luggage);
        btn_rem_luggage = findViewById(R.id.btn_rem_luggage);
        btn_reset = findViewById(R.id.btn_reset);
        tv_adult_count = findViewById(R.id.tv_adult_count);
        tv_child_count = findViewById(R.id.tv_child_count);
        tv_pass_count = findViewById(R.id.tv_pass_count);
        tv_total_fare = findViewById(R.id.tv_total_fare);
        tv_adult_fare = findViewById(R.id.tv_adult_fare);
        tv_adult_total = findViewById(R.id.tv_adult_total);
        tv_child_fare = findViewById(R.id.tv_child_fare);
        tv_child_total = findViewById(R.id.tv_child_total);
        tv_distance = findViewById(R.id.tv_distance);
        tv_pass_fare = findViewById(R.id.tv_pass_fare);
        tv_pass_total = findViewById(R.id.tv_pass_total);
        tv_luggage_count = findViewById(R.id.tv_luggage_count);
        tv_luggage_total = findViewById(R.id.tv_luggage_total);
        bottom_sheet = findViewById(R.id.bottom_sheet);
        btn_menu = findViewById(R.id.btn_menu);

        btn_from_next.setOnClickListener(this);
        btn_from_prev.setOnClickListener(this);
        btn_to_next.setOnClickListener(this);
        btn_to_prev.setOnClickListener(this);
        btn_add_adult.setOnClickListener(this);
        btn_rem_adult.setOnClickListener(this);
        btn_add_child.setOnClickListener(this);
        btn_rem_child.setOnClickListener(this);
        btn_add_pass.setOnClickListener(this);
        btn_rem_pass.setOnClickListener(this);
        btn_add_luggage.setOnClickListener(this);
        btn_rem_luggage.setOnClickListener(this);
        btn_reset.setOnClickListener(this);
        tv_to.setOnClickListener(this);
    }

    public void showBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        final boolean[] isInspector = {false};
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        Button btn_show_tickets = dialog.findViewById(R.id.btn_show_tickets);
        Button btn_download = dialog.findViewById(R.id.btn_download);
        final Button btn_collection_report = dialog.findViewById(R.id.btn_collection_report);
        Button btn_detailed_report = dialog.findViewById(R.id.btn_detailed_report);
        Button btn_end_trip = dialog.findViewById(R.id.btn_end_trip);
        final Button btn_inspection = dialog.findViewById(R.id.btn_inspection);


        btn_show_tickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, TicketListActivity.class);
                startActivity(intent);
            }
        });
        btn_collection_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, CollectionReportActivity.class);
                startActivity(intent);
            }
        });
        btn_detailed_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, DetailedReportActivity.class);
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
                final Dialog pinDialog = new Dialog(MainActivity.this);
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
                final DownloadTask downloadTask = new DownloadTask(MainActivity.this, mProgressDialog, "Kadamba_Transport.apk",
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

        btn_end_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("TRIP", "");
                Intent intent = new Intent(MainActivity.this, TripSelectActivity.class);
                startActivity(intent);
                dialog.dismiss();
                int tripid = sharedpreferences.getInt("TRIPID",1);
                Trip.executeQuery("UPDATE TRIP SET LASTTICKET = "+(ticketNumber==120000? ticketNumber:(ticketNumber-1))+" WHERE TRIPID = "+tripid);
                editor.putInt("TRIPID",tripid+1);
                editor.apply();
                finish();
            }
        });
        dialog.show();

    }

    private void initPrinter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("Inner printer")) //Note, you will need to change this to match the name of your device
                    {
                        bluetoothDevice = device;
                        break;
                    }
                }

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                beginListenForData();
            } else {
                value = "No Devices found";
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception ex) {
            value = ex.toString() + "\n" + " InitPrinter \n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }
    }

    private void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.d("e", data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void intentPrint(String txtvalue) {
        byte[] buffer = txtvalue.getBytes();
        byte[] printHeader = {(byte) 0xAA, 0x55, 2, 0};
        printHeader[3] = (byte) buffer.length;
        initPrinter();
        if (printHeader.length > 128) {
            value = "\nValue is more than 128 size\n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        } else {
            try {

                outputStream.write(txtvalue.getBytes());
                outputStream.close();
                socket.close();
            } catch (Exception ex) {
                value = ex.toString() + "\n" + "Excep IntentPrint \n";
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
            }
        }
    }

    //print custom
    private void printCustom(String msg, int size, int align) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
                case 0:
                    outputStream.write(cc);
                    break;
                case 1:
                    outputStream.write(bb);
                    break;
                case 2:
                    outputStream.write(bb2);
                    break;
                case 3:
                    outputStream.write(bb3);
                    break;
            }

            switch (align) {
                case 0:
                    //left align
                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }
            outputStream.write(msg.getBytes());
            outputStream.write(PrinterCommands.LF);
            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void printBill() {

        OutputStream opstream = null;
        try {
            opstream = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        outputStream = opstream;

        //print command
        String dateTime[] = Utils.getDateTime();
        Log.d(TAG, "printBill: " + dateTime[0] + " " + dateTime[1]);
        ticketNumber = sharedpreferences.getInt("ticketNumber", 120000);
        try {

            outputStream = socket.getOutputStream();
            byte[] printformat = new byte[]{0x1B, 0x21, 0x03};
            //outputStream.write(printformat);

            printCustom("KADAMBA TRANSPORT CORP. LTD.", 1, 1);
            printCustom("PANAJIM - DEPOT", 1, 1);
            printCustom("No." + ticketNumber+ "   "+dateTime[0]+"  "+ dateTime[1], 0, 1);
            printCustom("LOCAL", 0, 1);
            //printText(leftRightAlignForBigText(dateTime[0], dateTime[1]));
            printCustom(tv_from.getText().toString().toUpperCase() + " to " + tv_to.getText().toString().toUpperCase(), 1, 1);
            //printCustom(new String(new char[32]).replace("\0", "."),0,1);
            int stageCount = toPosition - fromPosition;
            printCustom("( JOURNEY kms: "+get3DigitString((int)distanceInTicket+"")+")", 0, 1);
            if (adultCost != 0) {
                printCustom("FULL: " + adultCount + " x " + String.format("%.2f", adultPerCost) + " = Rs." + String.format("%.2f", adultCost), 1, 1);
            }
            if (childCost != 0) {
                printCustom("HALF: " + childCount + " x " + String.format("%.2f", childPerCost) + " = Rs." + String.format("%.2f", childCost), 1, 1);
            }
            if (passCount != 0) {
                printCustom("PASS: " + passCount + " X " + passCount + " = Rs.0", 1, 1);
            }
            if (luggageCount != 0) {
                printCustom("LUGGAGE: " + (luggageCount * luggageFare), 1, 1);
            }
            //printNewLine();
            printCustom("TOTAL : Rs." + String.format("%.2f", calcFare()), 3, 1);
            printCustom("MID: 22038132",0,1);
            printCustom("Drive Safe, Be Safe", 0, 1);
            printCustom("NOT TRANSFERABLE", 0, 1);
            printNewLine();
            printNewLine();
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Ticket ticket = new Ticket(ticketNumber, dateTime[0], dateTime[1], fromPosition, toPosition, adultCount, adultCost, childCount,
                childCost, passCount, luggageFare * luggageCount, calcFare(), tv_from.getText().toString(), tv_to.getText().toString(),
                distanceInTicket);
        ticket.save();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        ticketNumber = ticketNumber + 1;
        editor.putInt("ticketNumber", ticketNumber);
        editor.apply();


        List<Fare> fareFrom = Fare.find(Fare.class, "STAGE = ?", fromPosition + "");
        List<Fare> faresTo = Fare.find(Fare.class, "STAGE = ?", toPosition + "");
        int ffrom = fareFrom.get(0).getFfrom();
        int hfrom = fareFrom.get(0).getHfrom();
        int pfrom = fareFrom.get(0).getPfrom();
        int lfrom = fareFrom.get(0).getLfrom();
        int fto = faresTo.get(0).getFto();
        int hto = faresTo.get(0).getHto();
        int pto = faresTo.get(0).getPto();
        int lto = faresTo.get(0).getLto();
        double totalFare = fareFrom.get(0).getTotalfare();

        if (adultCount != 0) {
            Fare.executeQuery("UPDATE FARE SET FFROM=" + (ffrom + adultCount) + " where STAGE = " + fromPosition);
            Fare.executeQuery("UPDATE FARE SET FTO=" + (fto + adultCount) + " where STAGE = " + toPosition);
        }
        if (childCount != 0) {
            Fare.executeQuery("UPDATE FARE SET HFROM=" + (hfrom + childCount) + " where STAGE = " + fromPosition);
            Fare.executeQuery("UPDATE FARE SET HTO=" + (hto + childCount) + " where STAGE = " + toPosition);
        }
        if (passCount != 0) {
            Fare.executeQuery("UPDATE FARE SET PFROM=" + (pfrom + passCount) + " where STAGE = " + fromPosition);
            Fare.executeQuery("UPDATE FARE SET PTO=" + (pto + passCount) + " where STAGE = " + toPosition);
        }
        if (luggageCount != 0) {
            Fare.executeQuery("UPDATE FARE SET LFROM=" + (lfrom + luggageCount) + " where STAGE = " + fromPosition);
            Fare.executeQuery("UPDATE FARE SET LTO=" + (lto + luggageCount) + " where STAGE = " + toPosition);
        }
        Fare.executeQuery("UPDATE FARE SET TOTALFARE=" + (totalFare + calcFare()) + " where STAGE = " + fromPosition);



    }

    //print photo
    public void printPhoto(int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                    img);
            if (bmp != null) {
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(command);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    //print text
    private void printText(String msg) {
        try {
            // Print normal text
            outputStream.write(msg.getBytes());
            outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print new line
    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private String leftRightAlign(String str1, String str2) {
        String ans = str1 + str2;
        if (ans.length() < 20) {
            int n = (20 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
    }

    private String leftRightAlignForBigText(String str1, String str2) {
        String ans = str1 + str2;
        if (ans.length() < 15) {
            int n = (15 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_from_next:
                if (fromPosition != stages.size() - 1) {
                    fromPosition++;
                }
                if (fromPosition < stages.size()) {
                    tv_from.setText(stages.get(fromPosition));
                }
//                Log.d("POSITION", "onClick: " + fromPosition);
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_from_prev:
                if (fromPosition != 0) {
                    fromPosition--;
                }
                if (fromPosition >= 0) {
                    tv_from.setText(stages.get(fromPosition));
                }
//                Log.d("PREV_POSITION", "onClick: " + fromPosition);
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_to_next:
                if (toPosition != stages.size() - 1) {
                    toPosition++;
                }
                if (toPosition < stages.size()) {
                    tv_to.setText(stages.get(toPosition));
                }
//                Log.d("POSITION", "onClick: " + toPosition);
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_to_prev:
                if (toPosition != 0) {
                    toPosition--;
                }
                if (toPosition >= 0) {
                    tv_to.setText(stages.get(toPosition));
                }
//                Log.d("PREV_POSITION", "onClick: " + toPosition);
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_add_adult:
                adultCount++;
                tv_adult_count.setText(adultCount + "");
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_rem_adult:
                if (adultCount != 0) {
                    adultCount--;
                }
                tv_adult_count.setText(adultCount + "");
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_add_child:
                childCount++;
                tv_child_count.setText(childCount + "");
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_rem_child:
                if (childCount != 0) {
                    childCount--;
                }
                tv_child_count.setText(childCount + "");
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.tv_to:
                showToPopUp();
                break;
            case R.id.btn_add_pass:
                passCount++;
                tv_pass_count.setText(passCount + "");
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_rem_pass:
                if (passCount != 0) {
                    passCount--;
                }
                tv_pass_count.setText(passCount + "");
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_add_luggage:
                luggageCount++;
                tv_luggage_count.setText(luggageCount + "");
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_rem_luggage:
                if (luggageCount != 0) {
                    luggageCount--;
                }
                tv_luggage_count.setText(luggageCount + "");
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                break;
            case R.id.btn_reset:
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.setTitle("Reset Data");
                dialog.setMessage("Are you sure you want to reset all counts and fare?");
                dialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetData();
                    }
                });
                dialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            default:
                break;
        }
    }

    private void resetData() {
        /*fromPosition = 0;
        toPosition = 1;*/
        adultCount = 0;
        childCount = 0;
        luggageCount = 0;
        passCount = 0;
        /*try {
            tv_from.setText(stages.get(0));
            tv_to.setText(stages.get(1));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        tv_adult_count.setText(adultCount + "");
        tv_child_count.setText(childCount + "");
        tv_pass_count.setText(passCount + "");
        tv_luggage_count.setText(luggageCount + "");
        tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
    }

    private void showToPopUp() {
        final Dialog dialog = new Dialog(this, R.style.PauseDialog);

        dialog.setTitle("Select your destination");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_view_layout);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.listview_layout, stages);
        ListView lv_stages = dialog.findViewById(R.id.lv_stages);
        lv_stages.setAdapter(adapter);
        dialog.show();
        lv_stages.setSelection(toPosition);
        lv_stages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                toPosition = i;
                if (toPosition < stages.size()) {
                    tv_to.setText(stages.get(toPosition));
                }
                Log.d("POSITION", "onClick: " + toPosition);
                tv_total_fare.setText(String.format("%.2f", calcFare()) + "");
                dialog.dismiss();
            }
        });
        lv_stages.setClickable(true);
    }

    private double calcFare() {
        int stageCount = toPosition - fromPosition;
        if (stageCount >= 0) {
            try {
                adultPerCost = fareArray[fromPosition][toPosition];
                childPerCost = Math.round(fareArray[fromPosition][toPosition] / 2);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            adultPerCost = fareArray[toPosition][fromPosition];
            childPerCost = Math.round(fareArray[toPosition][fromPosition] / 2);
//            Toast.makeText(this, "This will be a return ticket.", Toast.LENGTH_SHORT).show();
        }
        adultCost = adultPerCost * adultCount;
        childCost = childPerCost * childCount;
        tv_adult_fare.setText(Html.fromHtml("<b><i>Full: </b></i>" + (adultPerCost) + " X " + adultCount));
        tv_adult_total.setText("= " + String.format("%.2f",adultCost));
        tv_child_fare.setText(Html.fromHtml("<b><i>Half: </b></i>" + (childPerCost) + " X " + childCount));
        tv_child_total.setText("= " + String.format("%.2f",childCost));
        tv_pass_fare.setText(Html.fromHtml("<b><i>Pass: </b></i>0 X " + passCount));
        tv_pass_total.setText("= 0.00");
        try {
            tv_distance.setText("Journey: " + Math.abs(distance.get(toPosition) - distance.get(fromPosition)) + " kms");
            distanceInTicket = Math.abs(distance.get(toPosition) - distance.get(fromPosition));
        } catch (Exception e) {
            e.printStackTrace();
            return 0.00;
        }
        tv_luggage_total.setText((luggageCount * luggageFare) + "");
        return adultCost + childCost + (luggageCount * luggageFare);
    }

    private void readExcel(Context context, String fileName) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/KadambaTransport/", ""+fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            POIFSFileSystem myFileSystem = new POIFSFileSystem(fileInputStream);
            HSSFWorkbook myworkbook = new HSSFWorkbook(myFileSystem);
            HSSFSheet mySheet = myworkbook.getSheetAt(0);
            Iterator rowIter = mySheet.rowIterator();
            if (fileName.equals(sharedpreferences.getString("TRIP", "")+"_.xls")) {
                int row = 0;
                int column = 0;
                fareArray = new double[200][200];
                while (rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    Iterator cellIter = myRow.cellIterator();
                    column = 0;
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
//                        Log.d("CELL_VALUE", "readExcel: double value in " + row + ":" + column + "=" + Double.parseDouble(myCell.toString()));
                        fareArray[row][column] = Double.parseDouble(myCell.toString());
                        column++;
                    }
                    row++;
                }
            } else if (fileName.equals(sharedpreferences.getString("TRIP", "")+".xls")) {
                while (rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    Iterator cellIter = myRow.cellIterator();
                    int cellPosition = 0;
                    while (cellIter.hasNext()) {
                        HSSFCell placeCell = (HSSFCell) cellIter.next();
                        if (cellPosition == 0) {
                            stages.add(placeCell.toString());
                        } else {
                            distance.add(Double.valueOf(placeCell.toString()));
                        }
                        cellPosition++;
                    }
                }
                Log.d(TAG, "readExcel: " + distance.toString());
                try {
                    Fare.executeQuery("CREATE TABLE FARE(STAGE INT PRIMARY KEY, FFROM INT, HFROM INT, PFROM INT, LFROM INT, FTO INT, HTO INT, PTO INT, LTO INT, TOTALFARE NUMBER)");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List<Fare> fares = Fare.listAll(Fare.class);
                if (fares.size() == 0) {
                    for (int i = 0; i < stages.size(); i++) {
                        Fare fare = new Fare(i, 0, 0, 0, 0, 0, 0, 0, 0,0);
                        fare.save();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            callErrorMethod(context);
        } catch (IOException e) {
            e.printStackTrace();
            callErrorMethod(context);
        }
    }

    private void callErrorMethod(Context context){
        Toast.makeText(context, "Error syncing data. Please tap on sync button to get the data.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, TripSelectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String get3DigitString(String strng){
        switch (strng.length()){
            case 1:
                return "00"+strng;
            case 2:
                return "0"+strng;
            default:
                return strng;
        }
    }
}
