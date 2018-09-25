package com.remo.material.bluetoothprinter.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.remo.material.bluetoothprinter.R;
import com.remo.material.bluetoothprinter.adapter.TicketAdapter;
import com.remo.material.bluetoothprinter.model.Ticket;
import com.remo.material.bluetoothprinter.model.Trip;

import java.util.ArrayList;
import java.util.List;

import static com.remo.material.bluetoothprinter.activity.MainActivity.MyPREFERENCES;

public class TicketListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{


    RecyclerView rv_tickets;
    TextView tv_back;
    private SharedPreferences sharedpreferences;
    Spinner spinner;
    List<Ticket> ticketsToSet = new ArrayList<>();
    List<Trip> tripList = new ArrayList<>();
    List<Ticket> tickets = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_list);
        rv_tickets = findViewById(R.id.rv_tickets);
        spinner = findViewById(R.id.spinner);
        tv_back = findViewById(R.id.tv_back);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        spinner.setOnItemSelectedListener(this);

        try {
            tripList = Trip.listAll(Trip.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> tripHeadings = new ArrayList<>();
        tripHeadings.add("All trips");
        for (int i=0; i<tripList.size(); i++){
            tripHeadings.add("Trip "+(i+1));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tripHeadings);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);



        rv_tickets.setLayoutManager(new LinearLayoutManager(TicketListActivity.this,LinearLayoutManager.VERTICAL,false));
        try {
            tickets = Ticket.listAll(Ticket.class);
        } catch (Exception e) {
            e.printStackTrace();
            tickets = new ArrayList<>();
        }
        TicketAdapter ticketAdapter = new TicketAdapter(tickets);
        rv_tickets.setAdapter(ticketAdapter);

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
        TicketAdapter ticketAdapter;
        ticketsToSet = new ArrayList<>();
        if (i!=0) {
            int firstTicket = tripList.get(i - 1).getFirstticket();
            int lastTicket = tripList.get(i - 1).getLastticket() == 0 ? sharedpreferences.getInt("ticketNumber", 120000) : tripList.get(i - 1).getLastticket();
            for (int j = 0; j < tickets.size(); j++) {
                if (tickets.get(j).getTicketID() >= firstTicket && tickets.get(j).getTicketID() <= lastTicket) {
                    ticketsToSet.add(tickets.get(j));
                }
            }
            ticketAdapter = new TicketAdapter(ticketsToSet);
            rv_tickets.setAdapter(ticketAdapter);
        }else{
            ticketAdapter = new TicketAdapter(tickets);
            rv_tickets.setAdapter(ticketAdapter);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
