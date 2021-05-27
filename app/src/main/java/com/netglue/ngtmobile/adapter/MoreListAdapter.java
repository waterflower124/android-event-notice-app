package com.netglue.ngtmobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.RouteActivity;
import com.netglue.ngtmobile.WebViewActivity;
import com.netglue.ngtmobile.holder.AlertHolder;
import com.netglue.ngtmobile.holder.MoreHolder;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.MoreItem;
import com.netglue.ngtmobile.model.NotificationItem;
import com.netglue.ngtmobile.model.SharedStorage;

import org.json.JSONObject;

import java.util.List;

public class MoreListAdapter extends RecyclerView.Adapter<MoreHolder> {

    public static final String TAG = MoreListAdapter.class.getSimpleName();

    private final Object lock = new Object();

    private Context context, wrapper;
    public List<MoreItem> itemList;


    public MoreListAdapter(Context context, List<MoreItem> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public MoreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(context).inflate(R.layout.item_more, parent, false);
        MoreHolder holder = new MoreHolder(layoutView);

        wrapper = new ContextThemeWrapper(context, R.style.popup_style);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MoreHolder holder, final int position) {

        final MoreItem item = itemList.get(position);
        holder.seqTextView.setText(item.seq);
        holder.titleTextView.setText(item.more_title);
        holder.infoTextView.setText(item.info);
        holder.bodyTextView.setText(item.more_body);

        holder.moreItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(item.url != null && !item.url.isEmpty()) {
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("url", item.url);
                    if(item.extbrowser != null) {
                        intent.putExtra("extbrowser", item.extbrowser);
                    }
                    context.startActivity(intent);

                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}
