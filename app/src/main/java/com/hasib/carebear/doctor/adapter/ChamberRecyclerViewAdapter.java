package com.hasib.carebear.doctor.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hasib.carebear.R;
import com.hasib.carebear.doctor.container.Chamber;
import com.hasib.carebear.doctor.listener.ChamberEventListener;
import com.hasib.carebear.support.DayPicker;

import java.util.ArrayList;
import java.util.List;

public class ChamberRecyclerViewAdapter extends RecyclerView.Adapter<ChamberRecyclerViewAdapter.ChamberViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private List<Chamber> aChamberList = new ArrayList<>();
    private final Context aContext;

    private String comingClass = "";

    private ChamberEventListener listener;

    public ChamberRecyclerViewAdapter(Context aContext, List<Chamber> aChamberList) {
        this.aChamberList = aChamberList;
        this.aContext = aContext;
    }

    public ChamberRecyclerViewAdapter(Context aContext, List<Chamber> aChamberList, String comingClass) {
        this.aChamberList = aChamberList;
        this.aContext = aContext;
        this.comingClass = comingClass;
    }

    @NonNull
    @Override
    public ChamberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chamber_info, parent, false);
        return new ChamberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChamberViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");

        holder.cChamberName.setText(aChamberList.get(position).getChamberName());
        holder.cChamberFees.setText(aChamberList.get(position).getChamberFees() + " Tk");
        holder.cChamberTime.setText(aChamberList.get(position).getChamberTime());
        holder.cChamberAddress.setText(aChamberList.get(position).getChamberAddress());

        Log.d(TAG, "onBindViewHolder: "+aChamberList.get(position).getChamberOpenDays().toString());

        DayPicker dayPicker = new DayPicker(holder.cChamberActiveDays, aChamberList.get(position).getChamberOpenDays());
        dayPicker.setMarkedDays();

        Log.d(TAG, "onBindViewHolder: chamber count "+aChamberList.size());

        final Chamber chamber = aChamberList.get(position);

        holder.cCamberInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + aChamberList.get(position).getChamberName());
                if (comingClass.isEmpty()) {
                    listener.onChamberClick(chamber, position);
                }
            }
        });

        holder.cCamberInfoLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "onLongClick: " + position);
                if (comingClass.isEmpty()) {
                    listener.onChamberLongClick(chamber, position);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return aChamberList.size();
    }

    public void setListener(ChamberEventListener listener) {
        this.listener = listener;
    }

    public class ChamberViewHolder extends RecyclerView.ViewHolder {
        TextView cChamberName;
        TextView cChamberFees;
        TextView cChamberTime;
        TextView cChamberAddress;
        View cChamberActiveDays;
        LinearLayout cCamberInfoLayout;

        public ChamberViewHolder(@NonNull View itemView) {
            super(itemView);

            cChamberName = itemView.findViewById(R.id.chamber_name_id);
            cChamberFees = itemView.findViewById(R.id.chamber_fee_id);
            cChamberTime = itemView.findViewById(R.id.chamber_time_id);
            cChamberAddress = itemView.findViewById(R.id.chamber_address_id);
            cChamberActiveDays = itemView.findViewById(R.id.daypicker);
            cCamberInfoLayout = itemView.findViewById(R.id.chamber_info_id);
        }
    }
}