package com.netglue.ngtmobile.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;


/**
 * Created by ??? on 2018-01-21.
 */

public class MoreHolder extends RecyclerView.ViewHolder {

    public TextView seqTextView;
    public TextView titleTextView;
    public TextView infoTextView;
    public TextView bodyTextView;
    public CardView moreItemView;

    public MoreHolder(View view) {
        super(view);

        seqTextView = view.findViewById(R.id.seqTextView);
        titleTextView = view.findViewById(R.id.titleTextView);
        infoTextView = view.findViewById(R.id.infoTextView);
        bodyTextView = view.findViewById(R.id.bodyTextView);
        moreItemView = view.findViewById(R.id.moreItemView);
    }
}
