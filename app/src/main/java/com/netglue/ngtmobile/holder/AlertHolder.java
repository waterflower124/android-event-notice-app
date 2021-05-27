package com.netglue.ngtmobile.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;


/**
 * Created by ??? on 2018-01-21.
 */

public class AlertHolder extends RecyclerView.ViewHolder {

    public TextView tvName;
    public TextView tvSubtitle;
    public TextView tvAddr;
    public TextView tvSpeed;
    public TextView tvTime;
    public ImageView ivItem_menu;
    public LinearLayout llNewTag;
    public CardView llRedBar;

    public AlertHolder(View view) {
        super(view);

        tvName = view.findViewById(R.id.item_title);
        tvSubtitle = view.findViewById(R.id.subtitle);
        tvSpeed = view.findViewById(R.id.speed);
        tvTime = view.findViewById(R.id.time);
        tvAddr = view.findViewById(R.id.address);
        ivItem_menu = view.findViewById(R.id.item_menu);
        llNewTag = view.findViewById(R.id.new_tag);
        llRedBar = view.findViewById(R.id.redbar);
    }
}
