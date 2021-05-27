package com.netglue.ngtmobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.WebViewActivity;
import com.netglue.ngtmobile.holder.AlertHolder;
import com.netglue.ngtmobile.model.AlertItem;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.NotificationItem;
import com.netglue.ngtmobile.model.SharedStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ??? on 21-Jan-18.
 */

public class AlertListAdapter extends RecyclerView.Adapter<AlertHolder> implements Filterable {

    public static final String TAG = AlertListAdapter.class.getSimpleName();

    private final Object lock = new Object();

    private Context context;
    public List<NotificationItem> itemList;
    private List<NotificationItem> mOriginalValues; // Original Values

    private View layoutView;

    private int sort = 0;

    private int filter = 0;

    private Callback mListener;
    private Context wrapper;

    public AlertListAdapter(Context context, List<NotificationItem> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public AlertHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(context).inflate(R.layout.item_alert, parent, false);
        AlertHolder holder = new AlertHolder(layoutView);

        wrapper = new ContextThemeWrapper(context, R.style.popup_style);
        return holder;
    }

    @Override
    public void onBindViewHolder(final AlertHolder holder, final int position) {

        final NotificationItem item = itemList.get(position);

        holder.tvName.setText(item.a_name);
        holder.tvSubtitle.setText(item.title);
        holder.tvAddr.setText(item.body);
//        holder.tvSpeed.setText(String.format("%s km/h", item.subtitle));
        holder.tvSpeed.setText(item.subtitle);
        if(item.subtitle_color != null && !item.subtitle_color.isEmpty()) {
            holder.tvSpeed.setTextColor(Color.parseColor("#" + item.subtitle_color));
        }
        holder.tvTime.setText(item.getTimestamp(item.timestamp));
        if(item.new_noti) {
            holder.llNewTag.setVisibility(View.VISIBLE);
        } else {
            holder.llNewTag.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onAlertClicked(holder.getAdapterPosition());
                }
            }
        });

//        holder.llRedBar.setVisibility(View.INVISIBLE);
//        List<AssetItem> assets = SharedStorage.getInstance().assets;
//        for(int index = 0; index < assets.size(); index ++) {
//            if(assets.get(index).id.equals(item.a_id) && assets.get(index).alarm == 1) {
//                holder.llRedBar.setVisibility(View.VISIBLE);
//                break;
//            }
//        }
        if(item.category_color != null && !item.category_color.isEmpty()) {
            holder.llRedBar.setCardBackgroundColor(Color.parseColor("#" + item.category_color));
        }

        holder.ivItem_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu alertPopupMenu = new PopupMenu(wrapper, v);
                alertPopupMenu.inflate(R.menu.alert_popup_menu);
                Menu menuOpts = alertPopupMenu.getMenu();

                try {
                    String jstring = item.option;
                    JSONArray jarray = new JSONArray(jstring);
                    for (int i = 0; i < jarray.length(); i++) {
                        menuOpts.getItem(i).setTitle(jarray.getJSONObject(i).getString("caption"));
                    }
                    if(jarray.length() == 1) {
                        menuOpts.getItem(1).setVisible(false);
                    }
                } catch(Throwable e) {

                }

                alertPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int item_index = holder.getAdapterPosition();
                        String json = itemList.get(item_index).option;
                        if(json == null) {
                            return false;
                        }
                        JSONObject jobj = new JSONObject();

                        try {
                            JSONArray jsonArray = new JSONArray(json);
                            if(jsonArray.length() == 0) {
                                return false;
                            }
                            switch (item.getItemId()) {
                                case R.id.alert_popup_item_map:
                                    jobj = jsonArray.getJSONObject(0);
                                    if(jobj == null) {
                                        return false;
                                    }
                                    if (jobj.getString("action").equals("1")) { // open map
                                        if(jobj.getString("lat") != null && !jobj.getString("lat").isEmpty() && jobj.getString("lon") != null && !jobj.getString("lon").isEmpty()) {
                                            String urlAddress = "http://maps.google.com/maps?q="+ jobj.getString("lat")  +"," + jobj.getString("lon") +"("+ itemList.get(item_index).a_name + ")&iwloc=A&hl=es";
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                                            intent.setPackage("com.google.android.apps.maps");
                                            if (intent.resolveActivity(context.getPackageManager()) != null) {
                                                context.startActivity(intent);
                                            }
                                        }
                                    }

                                    return true;
                                case R.id.alert_popup_item_action:
                                    jobj = jsonArray.getJSONObject(1);
                                    if(jobj == null) {
                                        return false;
                                    }
                                    if (jobj.getString("action").equals("2")) { // open web
                                        if(jobj.getString("url") != null && !jobj.getString("url").isEmpty()) {
                                            String url = jobj.getString("url");
                                            String extbrowser = jobj.getString("extbrowser");
                                            Intent intent = new Intent(context, WebViewActivity.class);
                                            intent.putExtra("url", url);
                                            if(extbrowser != null && extbrowser.equals("No")) {
                                                intent.putExtra("extbrowser", extbrowser);
                                                context.startActivity(intent);
                                            } else {
//                                                String url = jobj.getString("url");
                                                Intent i = new Intent(Intent.ACTION_VIEW);
                                                i.setData(Uri.parse(url));
                                                context.startActivity(i);
                                            }
                                        }
                                    }
                                    return true;
                                default:
                                    return false;
                            }
                        } catch (Exception e) {

                            return false;
                        }
                    }
                });
                alertPopupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString().toLowerCase();

                List<NotificationItem> filtered = new ArrayList<>();

                if (mOriginalValues == null || mOriginalValues.size() == 0) {
                    synchronized (lock) {
                        mOriginalValues = new ArrayList<>(itemList);
                    }
                }

                if (sort == AssetListItem.FILTER_ALL && (query == null || query.isEmpty())) {
                    filtered.addAll(mOriginalValues);
                } else {
                    for (NotificationItem item : mOriginalValues) {
                        if (item.title.toLowerCase().contains(query.toLowerCase()) ||
                                item.body.toLowerCase().contains(query.toLowerCase()) || item.a_name.toLowerCase().contains(query.toLowerCase()) ||
                                item.a_id.toLowerCase().contains(query.toLowerCase()) || item.alarm_id.toLowerCase().contains(query.toLowerCase())) {
                            if(filter == 1) { // today
                                if(item.timestamp.getDate() == new Date().getDate()) {
                                    filtered.add(item);
                                }
                            } else if(filter == 2) { // last 7 days
                                if((new Date().getDate() - 7) < item.timestamp.getDate() && item.timestamp.getDate() <= new Date().getDate()) {
                                    filtered.add(item);
                                }
                            } else if(filter == 3) { // this month
                                if(item.timestamp.getMonth() == new Date().getMonth()) {
                                    filtered.add(item);
                                }
                            } else if(filter == 4) { // last month
                                if(item.timestamp.getMonth() == new Date().getMonth() - 1) {
                                    filtered.add(item);
                                }
                            } else if(filter == 5) { // older than 2 months
                                if(item.timestamp.getMonth() < (new Date().getMonth() - 1)) {
                                    filtered.add(item);
                                }
                            } else {
                                filtered.add(item);
                            }

                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.count = filtered.size();
                results.values = filtered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                itemList.clear();
                itemList.addAll((ArrayList<NotificationItem>) results.values);

                notifyDataSetChanged();

                if (layoutView != null) {
                    layoutView.setVisibility(itemList.size() == 0? View.GONE : View.VISIBLE);
                }
            }
        };
    }

    public void setFilterBy(int filter) {
        this.filter = filter;
    }

    public void setSortBy(int sort) {
        this.sort = sort;
    }

    public void setLayoutView(View view) {
        layoutView = view;
    }

    public void resetFilter() {
        synchronized (lock) {
            mOriginalValues = null;
        }
    }

    public boolean emtpy() {
        return itemList.size() == 0 &&
                (mOriginalValues == null || mOriginalValues.size() == 0);
    }

    public void setCallback(Callback listener) {
        mListener = listener;
    }

    public interface Callback {
        void onAlertClicked(int position);
    }
}