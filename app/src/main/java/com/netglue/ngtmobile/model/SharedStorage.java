package com.netglue.ngtmobile.model;

import java.util.ArrayList;
import java.util.List;

public class SharedStorage {
    private static final SharedStorage ourInstance = new SharedStorage();

    public static SharedStorage getInstance() {
        return ourInstance;
    }

    private SharedStorage() {
        assets = new ArrayList<>();
        alerts = new ArrayList<>();
        asset_push = new ArrayList<>();
        notificationList = new ArrayList<>();
        alert_search_asset_name = "";
        trips_list = new ArrayList<>();
        notificationToneItemList = new ArrayList<>();
        currentAsset = new AssetItem();
        connection_lost = false;
    }

    public List<AssetItem> assets;

    public AssetItem currentAsset;

    public TripItem currentTrip;

    public List<AlertItem> alerts;

    public List<AssetPushItem> asset_push;

    public boolean connection_lost;

    public List<NotificationItem> notificationList;

    public String alert_search_asset_name; // when click bell icon from List page

    public List<TripItem> trips_list;

    public List<NotificationToneItem> notificationToneItemList;

}
