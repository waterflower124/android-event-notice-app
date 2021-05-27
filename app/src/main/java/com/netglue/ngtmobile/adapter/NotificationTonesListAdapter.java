package com.netglue.ngtmobile.adapter;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.holder.AlertHolder;
import com.netglue.ngtmobile.holder.NotificationToneHolder;
import com.netglue.ngtmobile.model.NotificationItem;
import com.netglue.ngtmobile.model.NotificationToneItem;

import java.util.List;

public class NotificationTonesListAdapter extends RecyclerView.Adapter<NotificationToneHolder> {

    public static final String TAG = NotificationTonesListAdapter.class.getSimpleName();

    Context context;
    List<NotificationToneItem> itemList;

    private NotificationTonesListAdapter.Callback mListener;

    public NotificationTonesListAdapter(Context context, List<NotificationToneItem> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public NotificationToneHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(context).inflate(R.layout.item_notification_tone, parent, false);
        NotificationToneHolder holder = new NotificationToneHolder(layoutView);

        return holder;
    }

    @Override
    public void onBindViewHolder(final NotificationToneHolder holder, final int position) {

        final NotificationToneItem notificationToneItem = itemList.get(position);
        if(notificationToneItem.isSelected()) {
            holder.optionImageView.setImageResource(R.drawable.ic_notification_tone_check);
        } else {
            holder.optionImageView.setImageResource(R.drawable.ic_notification_tone_uncheck);
        }
        holder.notificationToneTextView.setText(notificationToneItem.getTone_name());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onNotificationToneClicked(holder.getAdapterPosition());
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    public void setCallback(NotificationTonesListAdapter.Callback listener) {
        mListener = listener;
    }

    public interface Callback {
        void onNotificationToneClicked(int position);
    }

}
