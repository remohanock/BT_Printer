package com.remo.material.bluetoothprinter.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.remo.material.bluetoothprinter.R;
import com.remo.material.bluetoothprinter.adapter.TicketAdapter;
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

import static com.remo.material.bluetoothprinter.activity.MainActivity.MyPREFERENCES;

public class CollectionReportActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private int firstTicket;
    private int lastTicket;
    private double fullFare = 0.0;
    private int fullCount = 0;
    private double halfFare = 0.0;
    private int halfCount = 0;
    private int luggageFare = 0;
    private int luggageCount = 0;
    private int passCount = 0;
    private double totalFare = 0.0;

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
    Spinner spinner;
    List<Trip> tripList = new ArrayList<>();
    List<Ticket> tickets;

    private TextView tv_totalFare, tv_halfCount, tv_halfFare, tv_fullCount, tv_fullFare, tv_luggageCount, tv_luggageFare, tv_passCount, tv_passFare, tv_ticketNumbers;
    private String TAG = CollectionReportActivity.class.getSimpleName();
    Button btn_prnt_clctn_rprt;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_report);
        spinner = findViewById(R.id.spinner);
        bindControls();

        if (!getAllData()) {
            Toast.makeText(this, "Something is not right. Please try again later.", Toast.LENGTH_SHORT).show();
        }

        btn_prnt_clctn_rprt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPrinter();
                printBill();
            }
        });

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        spinner.setOnItemSelectedListener(this);

        tripList = Trip.listAll(Trip.class);
        List<String> tripHeadings = new ArrayList<>();
        tripHeadings.add("All trips");
        for (int i=0; i<tripList.size(); i++){
            tripHeadings.add("Trip "+(i+1));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tripHeadings);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private void bindControls() {
        tv_totalFare = findViewById(R.id.tv_totalFare);
        tv_halfCount = findViewById(R.id.tv_halfCount);
        tv_halfFare = findViewById(R.id.tv_halfFare);
        tv_fullCount = findViewById(R.id.tv_fullCount);
        tv_fullFare = findViewById(R.id.tv_fullFare);
        tv_luggageCount = findViewById(R.id.tv_lugaggeCount);
        tv_luggageFare = findViewById(R.id.tv_lugaggeFare);
        tv_passCount = findViewById(R.id.tv_passCount);
        tv_passFare = findViewById(R.id.tv_passFare);
        tv_ticketNumbers = findViewById(R.id.tv_ticketNumbers);
        btn_prnt_clctn_rprt = findViewById(R.id.btn_prnt_clctn_rprt);
    }

    private boolean getAllData() {
        try {
            tickets = Ticket.listAll(Ticket.class);
            firstTicket = tickets.get(0).getTicketID();
            lastTicket = tickets.get(tickets.size() - 1).getTicketID();
            fullFare = 0;
            fullCount = 0;
            halfFare = 0;
            halfCount = 0;
            luggageFare = 0;
            passCount = 0;
            totalFare = 0;
            luggageCount = 0;
            for (Ticket ticket : tickets) {
                fullFare = fullFare + ticket.getFullFare();
                fullCount = fullCount + ticket.getFullCount();
                halfFare = halfFare + ticket.getHalfFare();
                halfCount = halfCount + ticket.getHalfCount();
                luggageFare = luggageFare + ticket.getLuggageFare();
                passCount = passCount + ticket.getPassCount();
                totalFare = totalFare + ticket.getTotalFare();
            }
            luggageCount = luggageFare / 10;

            tv_totalFare.setText(String.format("%.2f",totalFare));
            tv_halfCount.setText(halfCount+"");
            tv_halfFare.setText(String.format("%.2f",halfFare));
            tv_fullCount.setText(fullCount+"");
            tv_fullFare.setText(String.format("%.2f",fullFare));
            tv_luggageCount.setText(luggageCount+"");
            tv_luggageFare.setText(luggageFare+".00");
            tv_passCount.setText(passCount+"");
            tv_passFare.setText("0.00");
            tv_ticketNumbers.setText("Tickets from: "+firstTicket+" to "+lastTicket);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean getFilteredData(int i) {
        try {
            tickets = Ticket.listAll(Ticket.class);
            List<Ticket> ticketsToSet = new ArrayList<>();
            if (i!=0) {
                int firstTicket = tripList.get(i - 1).getFirstticket();
                int lastTicket = tripList.get(i - 1).getLastticket() == 0 ? sharedpreferences.getInt("ticketNumber", 120000) : tripList.get(i - 1).getLastticket();
                for (int j = 0; j < tickets.size(); j++) {
                    if (tickets.get(j).getTicketID() >= firstTicket && tickets.get(j).getTicketID() <= lastTicket) {
                        ticketsToSet.add(tickets.get(j));
                    }
                }

            }else{
                ticketsToSet = tickets;
            }
            fullFare = 0.0;
            fullCount = 0;
            halfFare = 0.0;
            halfCount = 0;
            luggageFare = 0;
            passCount = 0;
            totalFare = 0.0;
            luggageCount = 0;
            firstTicket = ticketsToSet.get(0).getTicketID();
            lastTicket = ticketsToSet.get(ticketsToSet.size() - 1).getTicketID();
            for (Ticket ticket : ticketsToSet) {
                fullFare = fullFare + ticket.getFullFare();
                fullCount = fullCount + ticket.getFullCount();
                halfFare = halfFare + ticket.getHalfFare();
                halfCount = halfCount + ticket.getHalfCount();
                luggageFare = luggageFare + ticket.getLuggageFare();
                passCount = passCount + ticket.getPassCount();
                totalFare = totalFare + ticket.getTotalFare();
            }
            luggageCount = luggageFare / 10;

            tv_totalFare.setText(String.format("%.2f",totalFare));
            tv_halfCount.setText(halfCount+"");
            tv_halfFare.setText(String.format("%.2f",halfFare));
            tv_fullCount.setText(fullCount+"");
            tv_fullFare.setText(String.format("%.2f",fullFare));
            tv_luggageCount.setText(luggageCount+"");
            tv_luggageFare.setText(luggageFare+".00");
            tv_passCount.setText(passCount+"");
            tv_passFare.setText("0.00");
            tv_ticketNumbers.setText("Tickets from: "+firstTicket+" to "+lastTicket);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tv_totalFare.setText(String.format("%.2f",totalFare));
            tv_halfCount.setText(halfCount+"");
            tv_halfFare.setText(String.format("%.2f",halfFare));
            tv_fullCount.setText(fullCount+"");
            tv_fullFare.setText(String.format("%.2f",fullFare));
            tv_luggageCount.setText(luggageCount+"");
            tv_luggageFare.setText(luggageFare+".00");
            tv_passCount.setText(passCount+"");
            tv_passFare.setText("0.00");
            tv_ticketNumbers.setText("Tickets from: 0 to 0");
            return false;

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
//        ticketNumber = sharedpreferences.getInt("ticketNumber", 120000);
        try {

            outputStream = socket.getOutputStream();
            byte[] printformat = new byte[]{0x1B, 0x21, 0x03};
            //outputStream.write(printformat);

            printCustom("KADAMBA TRANSPORT CORP. LTD.", 1, 1);
            printCustom("PANAJIM - DEPOT", 1, 1);
            printNewLine();
            printCustom("COLLECTION REPORT", 3, 1);
            printCustom(dateTime[0]+"        "+ dateTime[1], 1, 1);
            printCustom("MID: 22038132",0,1);
//            printText(leftRightAlignForBigText(dateTime[0], dateTime[1]));
            printCustom("Local", 0, 1);
            printCustom("TRIP NO    : ALL TRIPS", 1, 0);
            printCustom("TKTS FROM  : "+firstTicket+" to "+lastTicket, 1, 0);
            printCustom("FULL       : "+fullCount+"   "+(String.format("%.2f",fullFare)), 1, 0);
            printCustom("HALF       : "+halfCount+"   "+(String.format("%.2f",halfFare)), 1, 0);
            printCustom("LUGGAGE    : "+luggageCount+"   "+luggageFare+".00", 1, 0);
            printCustom("PASS       : "+passCount+"   "+"0.00", 1, 0);
            printCustom("TOTAL Rs   : "+"  "+(String.format("%.2f",totalFare)), 3, 0);
            printNewLine();
            printNewLine();
            outputStream.flush();
        } catch (Exception e) {
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        getFilteredData(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
