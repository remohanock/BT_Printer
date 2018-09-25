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

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.MyViewHolder> {

    List<Fare> fares;
    boolean isInspector;

    public ReportAdapter(List<Fare> fares, boolean isInspector) {
        this.fares = fares;
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
        Fare fare = fares.get(position);
        holder.tv_stage.setText(fare.getStage()+"");
        holder.tv_ffrom.setText(fare.getFfrom()+"");
        holder.tv_hfrom.setText(fare.getHfrom()+"");
        holder.tv_pfrom.setText(fare.getPfrom()+"");
        holder.tv_lfrom.setText(fare.getLfrom()+"");
        holder.tv_totalfare.setText(String.format("%.2f", fare.getTotalfare())+"");
        holder.tv_fto.setText(fare.getFto()+"");
        holder.tv_hto.setText(fare.getHto()+"");
        holder.tv_pto.setText(fare.getPto()+"");
        holder.tv_lto.setText(fare.getLto()+"");
    }

    @Override
    public int getItemCount() {
        return fares.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_stage, tv_ffrom, tv_hfrom, tv_pfrom, tv_lfrom,tv_totalfare, tv_fto, tv_hto, tv_pto, tv_lto;
        View view_end;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_stage = itemView.findViewById(R.id.tv_stage);
            tv_ffrom = itemView.findViewById(R.id.tv_ffrom);
            tv_hfrom = itemView.findViewById(R.id.tv_hfrom);
            tv_pfrom = itemView.findViewById(R.id.tv_pfrom);
            tv_lfrom = itemView.findViewById(R.id.tv_lfrom);
            view_end = itemView.findViewById(R.id.view_end);
            tv_totalfare = itemView.findViewById(R.id.tv_totalfare);
            tv_fto = itemView.findViewById(R.id.tv_fto);
            tv_hto = itemView.findViewById(R.id.tv_hto);
            tv_pto = itemView.findViewById(R.id.tv_pto);
            tv_lto = itemView.findViewById(R.id.tv_lto);
            if (!isInspector){
                tv_fto.setVisibility(View.GONE);
                tv_hto.setVisibility(View.GONE);
                tv_pto.setVisibility(View.GONE);
                tv_lto.setVisibility(View.GONE);
                view_end.setVisibility(View.GONE);
                tv_totalfare.setVisibility(View.VISIBLE);
            }else{
                tv_fto.setVisibility(View.VISIBLE);
                tv_hto.setVisibility(View.VISIBLE);
                tv_pto.setVisibility(View.VISIBLE);
                tv_lto.setVisibility(View.VISIBLE);
                view_end.setVisibility(View.VISIBLE);
                tv_totalfare.setVisibility(View.GONE);
            }
        }
    }
}
