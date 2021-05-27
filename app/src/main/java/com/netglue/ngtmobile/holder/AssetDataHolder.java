package com.netglue.ngtmobile.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;


/**
 * Created by ??? on 2018-01-21.
 */

public class AssetDataHolder extends RecyclerView.ViewHolder {

    public ImageView ivIcon;
    public ToggleButton tbFav;
    public TextView tvName;
    public TextView tvAddr;
    public CheckBox cbMonitor;
    public RelativeLayout alertcountLayout;
    public TextView alertcountTextView;
    public LinearLayout iconLayout;

    public AssetDataHolder(View view) {
        super(view);

        ivIcon = view.findViewById(R.id.icon);
//        tbFav = view.findViewById(R.id.fav);
        tvName = view.findViewById(R.id.name);
        tvAddr = view.findViewById(R.id.address);
//        cbMonitor = view.findViewById(R.id.monitor);
        alertcountLayout = view.findViewById(R.id.alertcountLayout);
        alertcountTextView = view.findViewById(R.id.alertcountTextView);
        iconLayout = view.findViewById(R.id.iconLayout);
    }
}
