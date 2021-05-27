package com.netglue.ngtmobile.model;

import java.util.ArrayList;

public class AssetPushItem {

    private String assets_id;
    private ArrayList<Integer> alarm_id_list;

    public AssetPushItem(String assets_id, ArrayList<Integer> alarm_id_list) {
        this.assets_id = assets_id;
        this.alarm_id_list = alarm_id_list;
    }

    public String getAssets_id() {
        return assets_id;
    }

    public void setAssets_id(String assets_id) {
        this.assets_id = assets_id;
    }

    public ArrayList<Integer> getAlarm_id_list() {
        return alarm_id_list;
    }

    public void setAlarm_id_list(ArrayList<Integer> alarm_id_list) {
        this.alarm_id_list = alarm_id_list;
    }
}
