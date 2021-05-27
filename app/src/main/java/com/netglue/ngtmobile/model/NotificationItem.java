package com.netglue.ngtmobile.model;

import android.util.Log;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotificationItem {
    private static final String TAG = NotificationItem.class.getSimpleName();
    public String a_id;
    public String title;
    public String subtitle;
    public String a_name;
    public String category;
    public String alarm_id;
    public String image;
    public String body;
    public String option;
    public boolean new_noti;
    public Date timestamp;
    public String category_color;
    public String subtitle_color;

    public static NotificationItem parse(JSONObject jdata) {
        NotificationItem item = new NotificationItem();
        try {
            item.a_id = jdata.getString("a_id");
            item.title = jdata.getString("title");
            item.subtitle = jdata.getString("subtitle");
            item.a_name = jdata.getString("a_name");
            item.category = jdata.getString("category");
            item.alarm_id = jdata.getString("alarm_id");
            item.image = jdata.getString("image");
            item.body = jdata.getString("body");
            item.option = jdata.getString("option");
            item.new_noti = true;
            item.timestamp = new Date();
            item.category_color = jdata.getString("category_color");
            item.subtitle_color = jdata.getString("subtitle_color");
        } catch (JSONException e) {
            Log.e(TAG, "Exception", e);
        }
        return item;
    }

    public boolean isNew_noti() {
        return new_noti;
    }

    public void setNew_noti(boolean new_noti) {
        this.new_noti = new_noti;
    }

    public String getTimestamp(Date timestamp) {
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        return sdf.format(timestamp);
    }

}
