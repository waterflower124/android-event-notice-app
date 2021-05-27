package com.netglue.ngtmobile.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.common.Constant;
import com.netglue.ngtmobile.holder.AssetDataHolder;
import com.netglue.ngtmobile.holder.AssetHeaderHolder;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.AssetPushItem;
import com.netglue.ngtmobile.model.NotificationItem;
import com.netglue.ngtmobile.model.SharedStorage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ??? on 21-Jan-18.
 */

public class AssetListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    public static final String TAG = AssetListAdapter.class.getSimpleName();

    private final Object lock = new Object();

    private Context context;
    public List<AssetListItem> itemList;
    private List<AssetListItem> mOriginalValues; // Original Values

    private View layoutView;

    private int filter = 1;

    private Callback mListener;

    public AssetListAdapter(Context context, List<AssetListItem> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case AssetListItem.TYPE_HEADER: {
                View itemView = inflater.inflate(R.layout.item_asset_header, parent, false);
                return new AssetHeaderHolder(itemView);
            }
            case AssetListItem.TYPE_ASSET: {
                View itemView = inflater.inflate(R.layout.item_asset_data, parent, false);
                return new AssetDataHolder(itemView);
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case AssetListItem.TYPE_HEADER: {
                AssetHeaderItem header = (AssetHeaderItem) itemList.get(position);
                AssetHeaderHolder holder = (AssetHeaderHolder) viewHolder;

                holder.tvTitle.setText(header.getTitle());
                if(position == 0) {
                    
                }
                break;
            }
            case AssetListItem.TYPE_ASSET: {
                AssetDataItem item = (AssetDataItem) itemList.get(position);
                final AssetDataHolder holder = (AssetDataHolder) viewHolder;
                final AssetItem asset = item.getAsset();

                holder.ivIcon.setBackground(context.getResources().getDrawable(getIconBackground(asset)));
//                holder.ivIcon.setImageResource(getIcon(asset));
                holder.iconLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        holder.tbFav.toggle();
                        int position = holder.getAdapterPosition();
                        AssetItem item = ((AssetDataItem)itemList.get(position)).getAsset();

                        if (mListener != null) {
                            mListener.onMonitorChange(AssetListAdapter.this, position, !item.isMon());
                        }
                    }
                });

//                holder.tbFav.setChecked(asset.isFav());
//                holder.tbFav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        int position = holder.getAdapterPosition();
//                        AssetItem item = ((AssetDataItem)itemList.get(position)).getAsset();
//
//                        if (mListener != null && isChecked != item.isFav())
//                            mListener.onLikeClicked(AssetListAdapter.this, position, isChecked);
//                    }
//                });

                holder.tvName.setText(String.valueOf(asset.name));
                holder.tvAddr.setText(String.valueOf(asset.address));

//                AssetPushItem assetPushItem = item.getAsset_push();
//                if(assetPushItem == null) {
//                    holder.alertcountLayout.setVisibility(View.INVISIBLE);
//                } else {
//
//                    int alarm_count = 0;
//                    for(int index = 0; index < assetPushItem.getAlarm_id_list().size(); index ++) {
//                        if(assetPushItem.getAlarm_id_list().get(index) > asset.a_last_ack_alarm_id) {
//                            alarm_count ++;
//                        }
//                    }
//                    if(alarm_count > 0) {
//                        if(asset.alarm != 1) {
//                            if (asset.type.equalsIgnoreCase("truck")) {
//                                holder.ivIcon.setBackground(context.getResources().getDrawable(R.drawable.truck_red));
//                            } else if (asset.type.equalsIgnoreCase("car")) {
//                                holder.ivIcon.setBackground(context.getResources().getDrawable(R.drawable.car_red));
//                            } else if (asset.type.equalsIgnoreCase("bike")) {
//                                holder.ivIcon.setBackground(context.getResources().getDrawable(R.drawable.motorcycle_red));
//                            } else if (asset.type.equalsIgnoreCase("people")) {
//                                holder.ivIcon.setBackground(context.getResources().getDrawable(R.drawable.person_red));
//                            } else if (asset.type.equalsIgnoreCase("asset")) {
//                                holder.ivIcon.setBackground(context.getResources().getDrawable(R.drawable.genset_red));
//                            } else if (asset.type.equalsIgnoreCase("bus_red")) {
//                                holder.ivIcon.setBackground(context.getResources().getDrawable(R.drawable.car_red));
//                            } else {
//                                holder.ivIcon.setBackground(context.getResources().getDrawable(R.drawable.others_red));
//                            }
//                        }
//
//                        holder.alertcountTextView.setText(String.valueOf(alarm_count));
//                        holder.alertcountLayout.setVisibility(View.VISIBLE);
//                    } else {
//                        holder.alertcountLayout.setVisibility(View.INVISIBLE);
//                    }
//                }


                int noti_count = 0;
                boolean noti_exist = false;

                SharedPreferences pref = context.getSharedPreferences(Constant.PREF_ALERT, Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String json = pref.getString(Constant.PREF_ALERT, "");
                Type type = new TypeToken<List<NotificationItem>>(){}.getType();
                List<NotificationItem> notificationItemList = gson.fromJson(json, type);

                if(notificationItemList != null) {
                    for (int index = 0; index < notificationItemList.size(); index++) {
                        if (notificationItemList.get(index).a_id.equals(asset.id)) {
                            noti_exist = true;
                        }
                        if (notificationItemList.get(index).a_id.equals(asset.id) && notificationItemList.get(index).new_noti) {
                            noti_count++;
                        }
                    }
                }
                if(noti_exist) {
                    holder.alertcountLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.alertcountLayout.setVisibility(View.GONE);
                }
                if(noti_count > 0) {
                    holder.alertcountTextView.setText(String.valueOf(noti_count));
                    holder.alertcountLayout.setVisibility(View.VISIBLE);
                    holder.alertcountTextView.setVisibility(View.VISIBLE);

                } else {
                    holder.alertcountTextView.setVisibility(View.GONE);
                }

                holder.alertcountLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SharedStorage.getInstance().alert_search_asset_name = asset.name;
                        mListener.onAssetBellClicked(AssetListAdapter.this, holder.getAdapterPosition());
                    }
                });


//                holder.cbMonitor.setChecked(asset.isMon());
//                holder.cbMonitor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        int position = holder.getAdapterPosition();
//                        AssetItem item = ((AssetDataItem)itemList.get(position)).getAsset();
//
//                        if (mListener != null && isChecked != item.isMon())
//                            mListener.onMonitorChange(AssetListAdapter.this, position, isChecked);
//                    }
//                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onAssetClicked(AssetListAdapter.this, holder.getAdapterPosition());
                            v.setBackgroundResource(R.color.bg_menu_select);
                        }
                    }
                });

//                holder.itemView.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        v.setBackgroundResource(R.color.sulu);
//                        return true;
//                    }
//                });

                break;
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString().toLowerCase();

                List<AssetListItem> filtered = new ArrayList<>();

                synchronized (lock) {
                    if (mOriginalValues == null || mOriginalValues.size() == 0) {
                        mOriginalValues = new ArrayList<>(itemList);
                    }

                    if ((filter == AssetListItem.FILTER_ALL_TITLE || filter == AssetListItem.FILTER_ALL) && (query == null || query.isEmpty())) {
                        filtered.addAll(mOriginalValues);
                    } else {
                        for (AssetListItem listItem : mOriginalValues) {
                            if (listItem.getType() == AssetListItem.TYPE_ASSET) {
                                AssetItem item = ((AssetDataItem)listItem).getAsset();

                                if (!item.filteredBy(filter))
                                    continue;

                                if (!item.name.toLowerCase().contains(query.toLowerCase()) &&
                                        !item.address.toLowerCase().contains(query.toLowerCase())) {
                                    continue;
                                }
                            } else {
                                removeLastHeader(filtered);
                            }

                            filtered.add(listItem);
                        }

                        removeLastHeader(filtered);
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
                itemList.addAll((ArrayList<AssetListItem>) results.values);

                notifyDataSetChanged();

                if (layoutView != null) {
                    layoutView.setVisibility(itemList.size() == 0? View.GONE : View.VISIBLE);
                }
            }
        };
    }

    private void removeLastHeader(List<AssetListItem> list) {
        int size = list.size();
        // remove empty group header
        if (size > 0) {
            AssetListItem lastItem = list.get(size - 1);
            if (lastItem.getType() == AssetListItem.TYPE_HEADER)
                list.remove(size - 1);
        }
    }

    public void setFilterBy(int filter) {
        this.filter = filter;
    }

    public void setLayoutView(View view) {
        layoutView = view;
    }

    public void resetFilter() {
        synchronized (lock) {
            mOriginalValues = null;
        }
    }

    private int getIconBackground(AssetItem asset) {
        if (asset.alarm == 1) { //  alarm
            //            return R.drawable.bg_asset_alert;
            if (asset.type.equalsIgnoreCase("truck"))
                return R.drawable.truck_red;

            if (asset.type.equalsIgnoreCase("car"))
                return R.drawable.car_red;

            if (asset.type.equalsIgnoreCase("motorcycle"))
                return R.drawable.motorcycle_red;

            if (asset.type.equalsIgnoreCase("person"))
                return R.drawable.person_red;

            if (asset.type.equalsIgnoreCase("genset"))
                return R.drawable.genset_red;

            if (asset.type.equalsIgnoreCase("bus"))
                return R.drawable.bus_red;

            return R.drawable.others_red;
        }
        if (asset.stat == 0) { // stop
            if (asset.type.equalsIgnoreCase("truck"))
                return R.drawable.truck_grey;

            if (asset.type.equalsIgnoreCase("car"))
                return R.drawable.car_grey;

            if (asset.type.equalsIgnoreCase("motorcycle"))
                return R.drawable.motorcycle_grey;

            if (asset.type.equalsIgnoreCase("person"))
                return R.drawable.person_grey;

            if (asset.type.equalsIgnoreCase("genset"))
                return R.drawable.genset_grey;

            if (asset.type.equalsIgnoreCase("bus"))
                return R.drawable.bus_grey;

            return R.drawable.others_grey;
        } else if (asset.stat == 1) {  // moving
//            return R.drawable.bg_asset_moving;
            if (asset.type.equalsIgnoreCase("truck"))
                return R.drawable.truck_green;

            if (asset.type.equalsIgnoreCase("car"))
                return R.drawable.car_green;

            if (asset.type.equalsIgnoreCase("motorcycle"))
                return R.drawable.motorcycle_green;

            if (asset.type.equalsIgnoreCase("person"))
                return R.drawable.person_green;

            if (asset.type.equalsIgnoreCase("genset"))
                return R.drawable.genset_green;

            if (asset.type.equalsIgnoreCase("bus"))
                return R.drawable.bus_green;

            return R.drawable.others_green;
        } else if (asset.stat == 2) { /// idle
//            return R.drawable.bg_asset_idle;
            if (asset.type.equalsIgnoreCase("truck"))
                return R.drawable.truck_yellow;

            if (asset.type.equalsIgnoreCase("car"))
                return R.drawable.car_yellow;

            if (asset.type.equalsIgnoreCase("motorcycle"))
                return R.drawable.motorcycle_yellow;

            if (asset.type.equalsIgnoreCase("person"))
                return R.drawable.person_yellow;

            if (asset.type.equalsIgnoreCase("genset"))
                return R.drawable.genset_yellow;

            if (asset.type.equalsIgnoreCase("bus"))
                return R.drawable.bus_yellow;

            return R.drawable.others_yellow;
        } else {
            return R.drawable.bg_asset_stopped;
        }
//        return R.drawable.bg_asset_stopped;
    }

    private int getIcon(AssetItem asset) {
        if (asset.type.equalsIgnoreCase("truck"))
            return R.drawable.ic_asset_truck;

        if (asset.type.equalsIgnoreCase("car"))
            return R.drawable.ic_asset_car;

        if (asset.type.equalsIgnoreCase("motorcycle"))
            return R.drawable.ic_asset_bike;

        if (asset.type.equalsIgnoreCase("person"))
            return R.drawable.ic_asset_person;

        if (asset.type.equalsIgnoreCase("genset"))
            return R.drawable.ic_asset_asset;

        if (asset.type.equalsIgnoreCase("bus"))
            return R.drawable.ic_asset_trailer;

        return R.drawable.ic_asset_others;
    }

    public void setCallback(Callback listener) {
        mListener = listener;
    }

    public interface Callback {
        void onAssetClicked(AssetListAdapter adapter, int position);
        void onLikeClicked(AssetListAdapter adapter, int position, boolean isfav);
        void onMonitorChange(AssetListAdapter adapter, int position, boolean enable);
        void onAssetBellClicked(AssetListAdapter adapter, int position);
    }
}