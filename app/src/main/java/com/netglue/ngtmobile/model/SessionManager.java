package com.netglue.ngtmobile.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.UUID;


public class SessionManager {
    private static final SessionManager ourInstance = new SessionManager();

    private static final String PREF_NAME = "session";
    private static final int PRIVATE_MODE = 0;

    public static final String KEY_USER_NAME = "username";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_DEVICE = "device";


    public static SessionManager getInstance() {
        return ourInstance;
    }

    private SessionManager() {
    }

    public String username;
    public String token;        // ngt token

    public String deviceId;     // pseudo id

    public void load(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

        deviceId = pref.getString(KEY_DEVICE, UUID.randomUUID().toString());

        username = pref.getString(KEY_USER_NAME, "");
        if (username.isEmpty())
            return;

        token = pref.getString(KEY_TOKEN, "");
    }

    public void save(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(KEY_USER_NAME, username);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_DEVICE, deviceId);

        editor.commit();
    }

    public boolean isSignIn() {
        return !TextUtils.isEmpty(username);
    }

    public void signOut(Context context) {
        username = "";
        token = "";

        save(context);
    }
}
