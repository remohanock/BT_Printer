package com.remo.material.bluetoothprinter.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.remo.material.bluetoothprinter.R;
import com.remo.material.bluetoothprinter.model.Ticket;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.MyViewHolder> {

    private List<Ticket> tickets;

    public TicketAdapter(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        holder.tv_tkt_from.setText(ticket.getFroms()==null? "N/A":ticket.getFroms());
        holder.tv_tkt_to.setText(ticket.getTos()==null? "N/A":ticket.getTos());
        holder.tv_date_time.setText(ticket.getDate()+" "+ticket.getTime());
        holder.tv_ticket_number.setText("Ticket: "+ticket.getTicketID()+"");
        if (ticket.getFullFare()!=0.0) {
            holder.tv_full.setVisibility(View.VISIBLE);
            holder.tv_full.setText("FULL: "+ticket.getFullCount()+" nos. = "+ticket.getFullFare());
        }else{
            holder.tv_full.setVisibility(View.GONE);
        }
        if (ticket.getHalfFare()!=0.0) {
            holder.tv_half.setVisibility(View.VISIBLE);
            holder.tv_half.setText("HALF: "+ticket.getHalfCount()+" nos. = "+ticket.getHalfFare());
        }else{
            holder.tv_half.setVisibility(View.GONE);
        }
        if (ticket.getPassCount()!=0) {
            holder.tv_pass.setVisibility(View.VISIBLE);
            holder.tv_pass.setText(ticket.getPassCount()+" passes");
        }else{
            holder.tv_pass.setVisibility(View.GONE);
        }
        if (ticket.getLuggageFare()!=0) {
            holder.tv_luggage.setVisibility(View.VISIBLE);
            holder.tv_luggage.setText("Luggage cost: "+ticket.getLuggageFare());
        }else{
            holder.tv_luggage.setVisibility(View.GONE);
        }
        holder.tv_distance.setText(ticket.getDistance()+" kms");
        holder.tv_total_fare.setText("Rs. "+String.format("%.2f",ticket.getTotalFare()));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_tkt_to;
        TextView tv_tkt_from;
        TextView tv_date_time,tv_ticket_number,tv_full,tv_half,tv_pass,tv_luggage,tv_total_fare,tv_distance;
        MyViewHolder(View itemView) {
            super(itemView);
            tv_tkt_from = itemView.findViewById(R.id.tv_tkt_from);
            tv_tkt_to = itemView.findViewById(R.id.tv_tkt_to);
            tv_date_time = itemView.findViewById(R.id.tv_date_time);
            tv_ticket_number = itemView.findViewById(R.id.tv_ticket_number);
            tv_full = itemView.findViewById(R.id.tv_full);
            tv_half = itemView.findViewById(R.id.tv_half);
            tv_pass = itemView.findViewById(R.id.tv_pass);
            tv_luggage = itemView.findViewById(R.id.tv_luggage);
            tv_total_fare = itemView.findViewById(R.id.tv_total_fare);
            tv_distance = itemView.findViewById(R.id.tv_distance);
        }
    }
}
