package com.netglue.ngtmobile.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlertItem {
    private static final String TAG = AlertItem.class.getSimpleName();

    public String title;
    public String message;
    public Date timestamp;

    public AlertItem() {
    }

    public AlertItem(String title, String message) {
        this.title = title;
        this.message = message;
        this.timestamp = new Date();
    }

    public static AlertItem parse(JSONObject jdata) {
        AlertItem item = new AlertItem();

        try {
            item.title = jdata.getString("title");
            item.message = jdata.getString("message");
            item.timestamp = new Date(jdata.getLong("timestamp"));
        } catch (JSONException e) {
            Log.e(TAG, "Exception", e);
        }

        return item;
    }

    public String getTimestamp() {
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        return sdf.format(timestamp);
    }
}
