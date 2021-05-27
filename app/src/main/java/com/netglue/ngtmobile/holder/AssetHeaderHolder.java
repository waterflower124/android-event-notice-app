package com.netglue.ngtmobile.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;


/**
 * Created by ??? on 2018-01-21.
 */

public class AssetHeaderHolder extends RecyclerView.ViewHolder {

    public TextView tvTitle;

    public AssetHeaderHolder(View view) {
        super(view);

        tvTitle = view.findViewById(R.id.group_label);
    }
}
