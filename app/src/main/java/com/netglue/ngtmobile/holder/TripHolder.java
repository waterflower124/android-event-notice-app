package com.netglue.ngtmobile.holder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;


/**
 * Created by ??? on 2018-01-21.
 */

public class TripHolder extends RecyclerView.ViewHolder {

    public TextView tvStopTime;
    public TextView tvStopAA;
    public TextView tvDist;
    public TextView tvDuration;
    public TextView tvStartTime;
    public TextView tvStartAA;

    public TextView tvStopLoc;
    public TextView tvStartLoc;

    public TextView tvDriver;

    public TextView tvIdleTime;

    public View vPrevStart;
    public TextView tvCountIndex;

    public TripHolder(View view) {
        super(view);

        tvStopTime = view.findViewById(R.id.stop_time);
        tvStopAA = view.findViewById(R.id.stop_aa);

        tvDist = view.findViewById(R.id.dist);
        tvDuration = view.findViewById(R.id.duration);

        tvStartTime = view.findViewById(R.id.start_time);
        tvStartAA = view.findViewById(R.id.start_aa);

        tvStopLoc = view.findViewById(R.id.stop_loc);
        tvStartLoc = view.findViewById(R.id.start_loc);

        tvDriver = view.findViewById(R.id.driver);

        tvIdleTime = view.findViewById(R.id.idle_time);

        vPrevStart = view.findViewById(R.id.prev_start);
        tvCountIndex = view.findViewById(R.id.countTextView);
    }
}
