package com.netglue.ngtmobile.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MoreItem {
    private static final String TAG = MoreItem.class.getSimpleName();

    public String seq;
    public String more_title;
    public String info;
    public String more_body;
    public String url;
    public String extbrowser;

    public static MoreItem parse(JSONObject jdata) {
        MoreItem item = new MoreItem();
        try {
            item.seq = jdata.getString("seq");
            item.more_title = jdata.getString("title");
            item.info = jdata.getString("info");
            item.url = jdata.getString("url");
            item.extbrowser = jdata.getString("extbrowser");
            item.more_body = jdata.getString("body");
        } catch (JSONException e) {
            Log.e(TAG, "Exception", e);
        }
        return item;
    }
}
