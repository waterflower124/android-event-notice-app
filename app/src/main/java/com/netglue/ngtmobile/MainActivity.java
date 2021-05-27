package com.netglue.ngtmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.netglue.ngtmobile.common.Constant;
import com.netglue.ngtmobile.common.RestApi;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.fragment.AlertFragment;
import com.netglue.ngtmobile.fragment.ListFragment;
import com.netglue.ngtmobile.fragment.MapFragment;
import com.netglue.ngtmobile.model.AlertItem;
import com.netglue.ngtmobile.model.AppParam;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.NotificationToneItem;
import com.netglue.ngtmobile.model.SessionManager;
import com.netglue.ngtmobile.model.SharedStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements ListFragment.Callback, MapFragment.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private View llPopupMenu;

    private RadioGroup rgMenu;

    private ImageButton ibMenuToggle;

    private ProgressDialog mProgressDlg;

    private boolean doubleBackToExitPressedOnce = false;

    List<NotificationToneItem> mNotificationItemList = new ArrayList<>();

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("body");
            if (!TextUtils.isEmpty(title)) {
                SharedStorage.getInstance().alerts.add(new AlertItem(title, message));

                Utils.alertMsg(MainActivity.this, title, message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for osmdroid
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        setContentView(R.layout.activity_main);

        initView();

        sendDeviceToken();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(Constant.ACTION_PUSH));

        if (getIntent().getBooleanExtra("from_notification", false)) {
            selectAlertMenu();
        }

        initNotificationSoundList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SessionManager.getInstance().load(this);
        AppParam.getInstance().load(this);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.msg_back_to_exit), Toast.LENGTH_SHORT).show();
        MapFragment map_fragment = (MapFragment)getSupportFragmentManager().
                findFragmentByTag(MapFragment.TAG);
        if(map_fragment != null) {
            map_fragment.removeSearchResultView();
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("from_notification", false)) {
            RadioButton rb = rgMenu.findViewById(R.id.alert);
            if (rb.isChecked()) {
                AlertFragment fragment = (AlertFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                fragment.sortChanged();
            } else {
                selectAlertMenu();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private void initView() {
        // fragment container
        View v = findViewById(R.id.fragment_container);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llPopupMenu.getVisibility() == View.VISIBLE) {
                    llPopupMenu.setVisibility(View.GONE);
                }
            }
        });

        // popup menu
        llPopupMenu = findViewById(R.id.popup_menu);

        // sign out
        v = findViewById(R.id.sign_out);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llPopupMenu.setVisibility(View.GONE);

                attemptSignOut();
            }
        });

        // settings
        v = findViewById(R.id.settings);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llPopupMenu.setVisibility(View.GONE);

                gotoSettings();
            }
        });

        // bottom menu
        rgMenu = findViewById(R.id.rg_menu);
        rgMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    selectMenu(checkedId);
                }
            }
        });
        selectMenu(rgMenu.getCheckedRadioButtonId());

        // menu
        ibMenuToggle = findViewById(R.id.menu);
        ibMenuToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePopupMenu();
            }
        });

        // Progress dialog
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setCancelable(true);
        mProgressDlg.setMessage(getString(R.string.progress_load));
    }

    private void initNotificationSoundList() {
        SharedPreferences pref_noti_tone = getSharedPreferences(Constant.NOTIFICATION_TONE_INDEX, Context.MODE_PRIVATE);
        int noti_tone_index = pref_noti_tone.getInt(Constant.NOTIFICATION_TONE_INDEX, 0);

        boolean selected;

        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);

        Cursor cursor = manager.getCursor();
        int ringtone_count = 0;
        while (cursor.moveToNext()) {
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = manager.getRingtoneUri(cursor.getPosition()).toString();

            String ringtoneName= cursor.getString(cursor.getColumnIndex("title"));

            if(noti_tone_index == cursor.getPosition()) {
                selected = true;
            } else {
                selected = false;
            }
            NotificationToneItem notificationToneItem = new NotificationToneItem(ringtoneName, selected, uri);
            mNotificationItemList.add(notificationToneItem);
            ringtone_count ++;
        }

        if(noti_tone_index > ringtone_count - 1) {
            noti_tone_index = 0;
            mNotificationItemList.get(0).setSelected(true);
        }

        SharedStorage.getInstance().notificationToneItemList = mNotificationItemList;
    }

    private void showProgress() {
        if (!mProgressDlg.isShowing())
            mProgressDlg.show();
    }

    private void hideProgress() {
        if (mProgressDlg != null && mProgressDlg.isShowing())
            mProgressDlg.dismiss();
    }

    private void selectMenu(int menuId) {
        switch (menuId) {
            case R.id.map:
                loadFragment(MapFragment.class, MapFragment.TAG, false);
                break;

            case R.id.list:
                loadFragment(ListFragment.class, ListFragment.TAG, false);
                break;

            case R.id.alert:
                loadFragment(AlertFragment.class, AlertFragment.TAG, false);
                break;
        }
    }

    public Fragment loadFragment(Class fragmentClass, String tag, boolean addToBack) {
        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment == null) {
            try {
                fragment = (Fragment)fragmentClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
        ft.replace(R.id.fragment_container, fragment, tag);

        if (addToBack) ft.addToBackStack(tag);

        ft.commit();

        return fragment;
    }


    private void selectMapMenu() {
        RadioButton rb = rgMenu.findViewById(R.id.map);
        rb.setChecked(true);
    }

    private void selectAlertMenu() {
        RadioButton rb = rgMenu.findViewById(R.id.alert);
        rb.setChecked(true);
    }

    private void togglePopupMenu() {
        if (llPopupMenu.getVisibility() == View.VISIBLE) {
            llPopupMenu.setVisibility(View.GONE);
        } else {
            llPopupMenu.setVisibility(View.VISIBLE);
            llPopupMenu.requestFocus();
        }
    }

    private void attemptSignOut() {
        if (signOutConfirmEnabled())
            showSignOutConfirm();
        else
            signOut();
    }

    private void showSignOutConfirm() {
        Utils.showConfirmDialog(MainActivity.this,
                getString(R.string.title_sign_out_confirm),
                getString(R.string.message_sign_out_confirm), true, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 1)
                    disableSignOutConfirm();

                signOut();
            }
        }, null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect viewRect1 = new Rect();
        llPopupMenu.getGlobalVisibleRect(viewRect1);

        Rect viewRect2 = new Rect();
        ibMenuToggle.getGlobalVisibleRect(viewRect2);

        if (!viewRect1.contains((int) ev.getRawX(), (int) ev.getRawY()) &&
                !viewRect2.contains((int) ev.getRawX(), (int) ev.getRawY())) {
            llPopupMenu.setVisibility(View.GONE);
        }

        return super.dispatchTouchEvent(ev);
    }

    private void gotoIntro() {
        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);
    }

    private void gotoSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private boolean signOutConfirmEnabled() {
        SharedPreferences pref = getSharedPreferences(Constant.PREF_CONFIRM, Constant.PRIVATE_MODE);
        return pref.getBoolean(Constant.KEY_SIGN_OUT_CONFIRM, true);
    }

    private void disableSignOutConfirm() {
        SharedPreferences pref = getSharedPreferences(Constant.PREF_CONFIRM, Constant.PRIVATE_MODE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(Constant.KEY_SIGN_OUT_CONFIRM, false);

        editor.commit();
    }


    private void clearAlerts() {
        SharedPreferences pref = getSharedPreferences(Constant.PREF_ALERT, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.remove(Constant.PREF_KEY_ALERT);
        editor.commit();
    }

    /**
     * send push token to server
     */
    private void sendDeviceToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.d(TAG, "Device token: " + token);

                        RestApi.sendRegistrationToServer(token);
                    }
                });
    }

    private void signOut() {
        final SessionManager sm = SessionManager.getInstance();

        try {
            JSONObject jo = new JSONObject();
            jo.put("uid", sm.username);
            jo.put("token", sm.token);

            showProgress();

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.SIGN_OUT)
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
                            Toast.makeText(MainActivity.this, response.message(), Toast.LENGTH_LONG).show();
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
                                sm.signOut(MainActivity.this);
                                SharedStorage.getInstance().assets.clear();
                                SharedStorage.getInstance().asset_push.clear();
                                SharedStorage.getInstance().notificationList.clear();
                                clearAlerts();

                                gotoIntro();

                                finish();
                            } else {
                                String message = jr.getString("message");
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
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


    @Override
    public void onAssetListItemSelected() {
        selectMapMenu();
    }


    @Override
    public void onAssetBellClicked() {
        selectAlertMenu();
    }
}
