package com.netglue.ngtmobile.holder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;

import at.grabner.circleprogress.CircleProgressView;


/**
 * Created by ??? on 2018-01-21.
 */

public class CardCHolder extends RecyclerView.ViewHolder {

    public TextView tvTitle;
    public TextView tvItem1Caption;
    public TextView tvItem1;
    public TextView tvItem2;
    public CircleProgressView csbValue2;
    public TextView tvNote;

    public CardCHolder(View view) {
        super(view);

        tvTitle = view.findViewById(R.id.title);
        tvItem1Caption = view.findViewById(R.id.item1_caption);
        tvItem1 = view.findViewById(R.id.item1);
        tvItem2 = view.findViewById(R.id.item2);

        csbValue2 = view.findViewById(R.id.seek_bar);
        csbValue2.setEnabled(false);

        tvNote = view.findViewById(R.id.note);
    }
}
