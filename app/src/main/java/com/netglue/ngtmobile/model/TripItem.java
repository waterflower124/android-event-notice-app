package com.netglue.ngtmobile.model;

import android.util.Log;

import com.netglue.ngtmobile.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

public class TripItem implements Serializable {

    private static final String TAG = TripItem.class.getSimpleName();

    public int id;

    public String start_loc;
    public Date start_time;

    public String stop_loc;
    public Date stop_time;

    public String dist;
    public int duration;
    public String driver;
    public String remark;

    private void setStartTime(String startTime) {
        start_time = Utils.string2Date(startTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getDefault());
    }

    private void setStopTime(String stopTime) {
        stop_time = Utils.string2Date(stopTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getDefault());
    }

    public String getStartAA() {
        return  Utils.getDateTimeString(start_time, "aa");
    }

    public String getStopAA() {
        return  Utils.getDateTimeString(stop_time, "aa");
    }

    public String getStartTime() {
        return  Utils.getDateTimeString(start_time, "HH:mm");
    }

    public String getStopTime() {
        return  Utils.getDateTimeString(stop_time, "HH:mm");
    }


    public static TripItem parse(JSONObject jdata) {
        TripItem item = new TripItem();

        try {
            item.id = jdata.getInt("t_id");

            item.start_loc = jdata.getString("t_start_loc");
            item.setStartTime(jdata.getString("t_start_time"));

            item.stop_loc = jdata.getString("t_stop_loc");
            item.setStopTime(jdata.getString("t_stop_time"));

            item.dist = jdata.getString("t_dist");
            item.duration = Integer.parseInt(jdata.getString("t_duration"));

            item.driver = jdata.getString("t_driver");
            item.remark = jdata.getString("t_remark");

        } catch (JSONException e) {
            Log.e(TAG, "Exception", e);
        }

        return item;
    }
}
