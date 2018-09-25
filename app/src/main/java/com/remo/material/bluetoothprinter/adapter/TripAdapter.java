package com.remo.material.bluetoothprinter.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.remo.material.bluetoothprinter.R;
import com.remo.material.bluetoothprinter.model.Fare;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.MyViewHolder> {

    List<String> trips;
    boolean isInspector;

    public TripAdapter(List<String> trips) {
        this.trips = trips;
        this.isInspector = isInspector;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_adapter_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String trip = trips.get(position);
        holder.tv_trip_item.setText(trip);

    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_trip_item;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_trip_item = itemView.findViewById(R.id.tv_trip_item);
        }
    }
}
