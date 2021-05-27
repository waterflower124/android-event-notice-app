package com.netglue.ngtmobile.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppParam {
    private static final String TAG = AppParam.class.getSimpleName();

    private static final String PREF_NAME = "app_param";
    private static final int PRIVATE_MODE = 0;

    public static final String KEY_UPDATE_INTERVAL = "update_interval";
    public static final String KEY_CARD_TYPE = "card_type";
    public static final String KEY_TILE_SERVER = "tile_server";

    private static final AppParam ourInstance = new AppParam();

    public static AppParam getInstance() {
        return ourInstance;
    }

    private AppParam() {
        sys_tile_server = new String[4];
    }


    public int updateInterval;

    public String[] sys_tile_server;

    public List<CardTypeItem> cardTypes = new ArrayList<>();

    public void parse(JSONObject jdata) {
        try {
            updateInterval = Integer.parseInt(jdata.getString("update_int"));

            sys_tile_server[0] = jdata.getString("sys_tile_server_1");
            sys_tile_server[1] = jdata.getString("sys_tile_server_2");
            sys_tile_server[2] = jdata.getString("sys_tile_server_3");
            sys_tile_server[3] = jdata.getString("sys_tile_server_4");

            cardTypes.clear();
            JSONArray jcard = jdata.getJSONArray("asset_cards");
            for (int i = 0; i < jcard.length(); ++i) {
                CardTypeItem cardType = CardTypeItem.parse(jcard.getJSONObject(i));
                cardTypes.add(cardType);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Exception", e);
        }
    }

    public CardTypeItem getCardType(int cardNo) {
        for (CardTypeItem item : cardTypes) {
            if (item.no == cardNo)
                return item;
        }

        return null;
    }

    public void load(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

        updateInterval = pref.getInt(KEY_UPDATE_INTERVAL, 30);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        String json = pref.getString(KEY_CARD_TYPE, "");
        if (!json.isEmpty()) {
            Type listType = new TypeToken<List<CardTypeItem>>(){}.getType();
            try {
                cardTypes = gson.fromJson(json, listType);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }

        json = pref.getString(KEY_TILE_SERVER, "");
        if (!json.isEmpty()) {
            Type listType = new TypeToken<String[]>(){}.getType();
            try {
                sys_tile_server = gson.fromJson(json, listType);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }
    }

    public void save(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(KEY_UPDATE_INTERVAL, updateInterval);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        editor.putString(KEY_CARD_TYPE, gson.toJson(cardTypes));
        editor.putString(KEY_TILE_SERVER, gson.toJson(sys_tile_server));

        editor.commit();
    }
}
