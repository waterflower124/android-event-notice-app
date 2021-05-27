package com.netglue.ngtmobile.holder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;


/**
 * Created by ??? on 2018-01-21.
 */

public class CardBHolder extends RecyclerView.ViewHolder {

    public TextView tvTitle;
    public TextView tvItem1;
    public TextView tvItem1Caption;
    public TextView tvItem2;
    public TextView tvItem2Caption;
    public TextView tvNote;

    public CardBHolder(View view) {
        super(view);

        tvTitle = view.findViewById(R.id.title);
        tvItem1Caption = view.findViewById(R.id.item1_caption);
        tvItem2Caption = view.findViewById(R.id.item2_caption);
        tvItem1 = view.findViewById(R.id.item1);
        tvItem2 = view.findViewById(R.id.item2);
        tvNote = view.findViewById(R.id.note);
    }
}
