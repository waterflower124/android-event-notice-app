package com.netglue.ngtmobile.common;

import android.util.Log;

import com.netglue.ngtmobile.model.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RestApi {

    public static final String TAG = RestApi.class.getSimpleName();

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static final String BASE_URL = ""; 


    public static final String SIGN_IN = BASE_URL + "/requestSignIn";
    public static final String SIGN_OUT = BASE_URL + "/requestSignOut";
    public static final String ASSET = BASE_URL + "/getAsset";
    public static final String TRIP = BASE_URL + "/getTripSummary";
    public static final String ROUTE = BASE_URL + "/getRoute";
    public static final String UPDATE_ASSET = BASE_URL + "/updateAsset";
    public static final String UPDATE_TOKEN = BASE_URL + "/updateToken";

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    public static void sendRegistrationToServer(String token) {
        SessionManager sm = SessionManager.getInstance();

        try {
            JSONObject jo = new JSONObject();
            jo.put("token", sm.token);
            jo.put("uid", sm.username);
            jo.put("app_token", token);
            jo.put("phone_id", sm.deviceId);

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.UPDATE_TOKEN)
                    .post(reqBody)
                    .build();

            Log.d(TAG, request.url().toString());
            Log.d(TAG, Utils.requestBodyToString(request));

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(final Call call, IOException e) {
                    Log.e(TAG, "Exception", e);
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    ResponseBody responseBody = response.body();

                    if (!response.isSuccessful()) {
                        Log.e(TAG, response.toString());
                         return;
                    }

                    String resp = responseBody.string();
                    Log.d(TAG, resp);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
