package com.netglue.ngtmobile.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netglue.ngtmobile.common.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AssetItem {
    private static final String TAG = AssetItem.class.getSimpleName();

    public static final int STATUS_STOP = 0;
    public static final int STATUS_MOVE = 1;
    public static final int STATUS_IDLE = 2;

    public String id;
    public String name;
    public String timestamp;
    public String type;     // 0=Truck, 1=Car, 2=Bike, 3=People, 4=Asset, 5=Trailer, 6=Others

    public double lat;
    public double lng;
    public String address;

    public String user;

    public String speed;
    public float direction;
    public int odometer;

    public int fav;         // Asset favourite flag (1=Yes, 0=No)
    public int mon;         // Asset alarm (1=Yes, 0=No)
    public int stat;        // Asset status (1=Moving, 2=Idle, 0=Stop)
    public int alarm;       // Asset alarm (1=Yes, 0=No)
    public int a_last_ack_alarm_id;

    public int mon_interval;
    public String mon_start_timestamp;
    public String mon_stop_timestamp;
    public JSONArray more_items;

    public static AssetItem parse(JSONObject jdata) {
        AssetItem item = new AssetItem();

        try {
            item.id = jdata.getString("a_id");
            item.name = jdata.getString("a_name");
            item.timestamp = jdata.getString("a_timestamp");
            item.type = jdata.getString("a_type");

            item.lat = Double.parseDouble(jdata.getString("a_lat"));
            item.lng = Double.parseDouble(jdata.getString("a_lon"));
            item.address = jdata.getString("a_loc");

            item.user = jdata.getString("a_user");

            item.speed = jdata.getString("a_sp");
            item.direction = Float.parseFloat(jdata.getString("a_dir"));
            item.odometer = Integer.parseInt(jdata.getString("a_odo"));

            item.fav = Integer.parseInt(jdata.getString("a_fav"));
            item.mon = Integer.parseInt(jdata.getString("a_mon"));
            if (!jdata.getString("a_stat").isEmpty())
                item.stat = Integer.parseInt(jdata.getString("a_stat"));
            if (!jdata.getString("a_alarm").isEmpty())
                item.alarm = Integer.parseInt(jdata.getString("a_alarm"));
            item.a_last_ack_alarm_id = Integer.parseInt(jdata.getString("a_last_ack_alarm_id"));

            item.mon_interval = Integer.parseInt(jdata.getString("mon_interval"));
            item.mon_start_timestamp = jdata.getString("mon_start_timestamp");
            item.mon_stop_timestamp = jdata.getString("mon_stop_timestamp");


            item.more_items = jdata.getJSONArray("more_items");

        } catch (JSONException e) {
            Log.e(TAG, "Exception", e);
        }

        return item;
    }

    public boolean isFav() {
        return fav == 1;
    }

    public boolean isAlarm() {
        return alarm == 1;
    }

    public boolean isMon() {
        return mon == 1;
    }

    public boolean filteredBy(int filter) { // filter value is referred by AssetListItem.filter
        // all
        if (filter == 0 || filter == 1)
            return true;

        // alert
        if (filter == 2 && isAlarm())
            return true;

        // favourite
        if (filter == 6 && isFav())
            return true;

        if (isMon()) {
            // monitored
            if (filter == 3)
                return true;

        } else {
//            // moving
//            if (filter == 4 && stat == 1)
//                return true;
//
//            // stopped
//            if (filter == 5 && stat == 0)
//                return true;
//
//            // idle
//            if (filter == 7 && stat == 2)
//                return true;
        }
        // moving
        if (filter == 4 && stat == 1)
            return true;

        // stopped
        if (filter == 5 && stat == 0)
            return true;

        // idle
        if (filter == 7 && stat == 2)
            return true;

        return false;
    }

    public String getStatus() {
        if (isMon())
            return "MONITORED";

        return getOriginStatus();
    }

    public String getOriginStatus() {
        if (isAlarm())
            return "ALERT";

        if (stat == 0)
            return "STOPPED";
        if (stat == 1)
            return "MOVING";
        if (stat == 2)
            return "IDLE";

        return "UNKNOWN";
    }

}
