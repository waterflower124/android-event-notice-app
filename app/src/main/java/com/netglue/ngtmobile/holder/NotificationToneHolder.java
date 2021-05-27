package com.netglue.ngtmobile.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;

public class NotificationToneHolder extends RecyclerView.ViewHolder {

    public ImageView optionImageView;
    public TextView notificationToneTextView;
    public LinearLayout notificationToneItemLayout;

    public NotificationToneHolder(View view) {
        super(view);

        optionImageView = view.findViewById(R.id.optionImageView);
        notificationToneTextView = view.findViewById(R.id.notificationToneTextView);
        notificationToneItemLayout = view.findViewById(R.id.notificationToneItemLayout);
    }
}
