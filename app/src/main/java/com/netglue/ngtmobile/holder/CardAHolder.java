package com.netglue.ngtmobile.holder;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;


/**
 * Created by ??? on 2018-01-21.
 */

public class CardAHolder extends RecyclerView.ViewHolder {

    public TextView tvTitle;
    public TextView tvItem1;
    public TextView tvNote;

    public CardAHolder(View view) {
        super(view);

        tvTitle = view.findViewById(R.id.title);
        tvItem1 = view.findViewById(R.id.item1);
        tvItem1.setMovementMethod(LinkMovementMethod.getInstance());
        tvNote = view.findViewById(R.id.note);
    }
}
