package com.netglue.ngtmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.netglue.ngtmobile.common.Constant;
import com.netglue.ngtmobile.common.RestApi;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.AppParam;
import com.netglue.ngtmobile.model.SessionManager;
import com.netglue.ngtmobile.model.SharedStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = SignInActivity.class.getSimpleName();

    private EditText etUsername;
    private EditText etPassword;

    private String username;
    private String password;

    private ProgressDialog mProgressDlg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initView();
    }

    private void initView() {
        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);

        if (BuildConfig.DEBUG) {
            etUsername.setText("ngt202009");
            etPassword.setText("$42hxm9fJey2");
        }

        Button btn = findViewById(R.id.submit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignIn();
            }
        });

        // Progress dialog
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setCancelable(true);
        mProgressDlg.setMessage(getString(R.string.progress_load));
    }

    private void showProgress() {
        if (!mProgressDlg.isShowing())
            mProgressDlg.show();
    }

    private void hideProgress() {
        if (mProgressDlg != null && mProgressDlg.isShowing())
            mProgressDlg.dismiss();
    }

    private void attemptSignIn() {
        if (!validateInput())
            return;

        signIn();
    }

    private boolean validateInput() {
        username = etUsername.getText().toString();
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Please input username");
            return false;
        }

        password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etUsername.setError("Please input password");
            return false;
        }

        return true;
    }

    private void signIn() {
        showProgress();

        try {
            JSONObject jo = new JSONObject();
            jo.put("uid", username);
            jo.put("pwd", password);
            jo.put("android_ver", Build.VERSION.RELEASE);

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.SIGN_IN)
                    .post(reqBody)
                    .build();

            Log.d(TAG, request.url().toString());
            Log.d(TAG, Utils.requestBodyToString(request));

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Exception", e);

                    runOnUiThread(new Runnable() { public void run() {
                        hideProgress();
                    }});
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {

                    runOnUiThread(new Runnable() { public void run() {
                        hideProgress();
                    }});

                    ResponseBody responseBody = response.body();

                    // Unknown error
                    if (!response.isSuccessful()) {
                        runOnUiThread(new Runnable() { public void run() {
                            Toast.makeText(SignInActivity.this, response.message(), Toast.LENGTH_LONG).show();
                        }});
                        Log.e(TAG, response.toString());
                        return;
                    }

                    final String resp = responseBody.string();
                    Log.d(TAG, resp);

                    runOnUiThread(new Runnable() { public void run() {
                        try {
                            JSONObject jr = new JSONObject(resp);

                            if (jr.getString("status").equals("1")) {
                                // app param
                                AppParam.getInstance().parse(jr);
                                AppParam.getInstance().save(SignInActivity.this);

                                String group_distance_str = jr.getString("group_distance");
                                if(group_distance_str != null) {
                                    SharedPreferences pref = getSharedPreferences(Constant.GROUP_DISTANCE, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor prefsEditor = pref.edit();
                                    prefsEditor.putString(Constant.GROUP_DISTANCE, group_distance_str);
                                    prefsEditor.commit();
                                }

                                // session
                                onSignInSuccess(jr);
                            } else {
                                String message = jr.getString("message");
                                Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Exception", e);
                        }
                    }});
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    private void onSignInSuccess(JSONObject jr) throws JSONException {
        SessionManager sm = SessionManager.getInstance();

        sm.username = username;
        sm.token = jr.getString("token");

        sm.save(this);

        //gotoPermission();
        Utils.gotoMain(this);

        finish();
    }

    private void gotoPermission() {
        Intent intent = new Intent(this, PermissionActivity.class);
        startActivity(intent);
    }

}
