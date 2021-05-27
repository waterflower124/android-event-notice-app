package com.netglue.ngtmobile.model;

import android.util.Log;

import com.netglue.ngtmobile.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.TimeZone;

public class RouteItem {

    private static final String TAG = RouteItem.class.getSimpleName();

    public String timestamp;

    public double lat;
    public double lng;

    public String speed;
    public float direction;

    public int alarm;

    public static RouteItem parse(JSONObject jdata) {
        RouteItem item = new RouteItem();

        try {
            item.timestamp = jdata.getString("a_timestamp");

            item.lat = Double.parseDouble(jdata.getString("a_lat"));
            item.lng = Double.parseDouble(jdata.getString("a_lon"));

            item.speed = jdata.getString("a_sp");
            item.direction = Float.parseFloat(jdata.getString("a_dir"));

            //item.alarm = Integer.parseInt(jdata.getString("a_alarm"));
        } catch (JSONException e) {
            Log.e(TAG, "Exception", e);
        }

        return item;
    }
}
