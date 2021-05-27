package com.netglue.ngtmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.holder.TripHolder;
import com.netglue.ngtmobile.model.TripItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ??? on 21-Jan-18.
 */

public class TripListAdapter extends RecyclerView.Adapter<TripHolder> {

    public static final String TAG = TripListAdapter.class.getSimpleName();

    private Context context;
    public List<TripItem> itemList;

    private View layoutView;

    private int sort = 0;

    private Callback mListener;

    public TripListAdapter(Context context, List<TripItem> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public TripHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(context).inflate(R.layout.item_trip_gap, parent, false);
        TripHolder holder = new TripHolder(layoutView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final TripHolder holder, final int position) {

        final TripItem item = itemList.get(position);

        holder.tvStopTime.setText(item.getStopTime());
        holder.tvStopAA.setText(item.getStopAA());

        holder.tvDist.setText(String.format("%s km", item.dist));
        holder.tvDuration.setText(Utils.millisecondsToTime(context, item.duration * 1000));

        holder.tvStartTime.setText(item.getStartTime());
        holder.tvStartAA.setText(item.getStartAA());

        holder.tvStopLoc.setText(item.stop_loc);
        holder.tvDriver.setText(item.driver);
        holder.tvStartLoc.setText(item.start_loc);

        if (position == getItemCount() - 1) {
            holder.vPrevStart.setVisibility(View.VISIBLE);
        } else {
            TripItem prevItem = itemList.get(position + 1);

            long idleTime = item.start_time.getTime() - prevItem.stop_time.getTime();
            holder.tvIdleTime.setText(String.format("Stopped %s", Utils.millisecondsToTime(context, idleTime)));

            holder.vPrevStart.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onTripClicked(holder.getAdapterPosition());
                }
            }
        });

        holder.tvCountIndex.setText(String.valueOf(itemList.size() - position));
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    public void setLayoutView(View view) {
        layoutView = view;
    }

    public void setCallback(Callback listener) {
        mListener = listener;
    }

    public interface Callback {
        void onTripClicked(int position);
    }
}