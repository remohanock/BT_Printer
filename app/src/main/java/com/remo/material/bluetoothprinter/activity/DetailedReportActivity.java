package com.remo.material.bluetoothprinter.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remo.material.bluetoothprinter.R;
import com.remo.material.bluetoothprinter.adapter.ReportAdapter;
import com.remo.material.bluetoothprinter.model.Fare;
import com.remo.material.bluetoothprinter.model.Ticket;
import com.remo.material.bluetoothprinter.model.Trip;
import com.remo.material.bluetoothprinter.utils.PrinterCommands;
import com.remo.material.bluetoothprinter.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DetailedReportActivity extends AppCompatActivity {

    RecyclerView rv_fare_report;
    TextView tv_back, tv_print;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice bluetoothDevice;
    OutputStream outputStream;
    InputStream inputStream;
    String value = "";
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    private String TAG = DetailedReportActivity.class.getSimpleName();
    List<Fare> fares;
    boolean isInspector;
    TextView tv_totalAmount;
    LinearLayout ll_alight;
    List<Trip> tripList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_report);
        rv_fare_report = findViewById(R.id.rv_fare_report);
        tv_back = findViewById(R.id.tv_back);
        tv_print = findViewById(R.id.tv_print);
        ll_alight = findViewById(R.id.ll_alight);
        tv_totalAmount = findViewById(R.id.tv_totalAmount);
        rv_fare_report.setLayoutManager(new LinearLayoutManager(DetailedReportActivity.this,LinearLayoutManager.VERTICAL,false));
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPrinter();
                printBill();
            }
        });
        isInspector = getIntent().hasExtra("INSPECTOR") && getIntent().getBooleanExtra("INSPECTOR", false);
        if (isInspector){
            tv_totalAmount.setVisibility(View.GONE);
            ll_alight.setVisibility(View.VISIBLE);
        }else{
            tv_totalAmount.setVisibility(View.VISIBLE);
            ll_alight.setVisibility(View.GONE);
        }
        getReport();

    }

    private void getReport(){
        try {
            fares= Fare.listAll(Fare.class);
            ReportAdapter adapter = new ReportAdapter(fares, isInspector);
            rv_fare_report.setAdapter(adapter);
        } catch (Exception e) {
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
        try {

            outputStream = socket.getOutputStream();
            byte[] printformat = new byte[]{0x1B, 0x21, 0x03};
            //outputStream.write(printformat);

            printCustom("KADAMBA TRANSPORT CORP. LTD.", 1, 1);
            printCustom("PANAJIM - DEPOT", 1, 1);
            printNewLine();
            printCustom("STAGE WISE REPORT", 3, 1);
            printCustom(dateTime[0]+"    "+ dateTime[1], 1, 1);
            printCustom("MID: 22038132",0,1);
            printCustom("LOCAL", 0, 1);
//            printText(leftRightAlignForBigText(dateTime[0], dateTime[1]));
            if (!isInspector) {
                printCustom("STG  F  H  P  L   AMOUNT", 1, 0);
                for (Fare fare : fares) {
                    printCustom(get3DigitString(fare.getStage() + "") + "  " +
                            (fare.getFfrom() + "") + "  " +
                            (fare.getHfrom() + "") + "  " +
                            (fare.getPfrom() + "") + "  " +
                            (fare.getLfrom() + "") + "   " +
                            fare.getTotalfare(), 1, 0);
                }
            }else{
                printCustom("STG    BOARD       ALIGHT", 1, 0);
                printCustom("No.  F  H  P  L   F  H  P  L", 1, 0);

                for (Fare fare : fares) {
                    printCustom(get3DigitString(fare.getStage() + "") + "  " +
                            (fare.getFfrom() + "") + "  " +
                            (fare.getHfrom() + "") + "  " +
                            (fare.getPfrom() + "") + "  " +
                            (fare.getLfrom() + "") + "   " +
                            (fare.getFto()+"")+"  "+
                            (fare.getHto()+"")+"  "+
                            (fare.getPto()+"")+"  "+
                            (fare.getLto()+"")+"  ", 1, 0);
                }
            }
            printNewLine();
            printNewLine();
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String leftRightAlignForBigText(String str1, String str2) {
        String ans = str1 + str2;
        if (ans.length() < 15) {
            int n = (15 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
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
