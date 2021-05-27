package com.netglue.ngtmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netglue.ngtmobile.adapter.NotificationTonesListAdapter;
import com.netglue.ngtmobile.common.Constant;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.NotificationToneItem;
import com.netglue.ngtmobile.model.SharedStorage;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements NotificationTonesListAdapter.Callback {

    LinearLayout notification_tonesLayount, notification_optionLayout;
    ImageView back_notification_toneImaveView;
    TextView notification_ToneTextView;

    List<NotificationToneItem> mNotificationItemList = new ArrayList<>();
    List<String> toneNameList = new ArrayList<>();


    RecyclerView notificationToneRecyclerView;
    NotificationTonesListAdapter notificationTonesListAdapter;

    Ringtone notificationTone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        notification_optionLayout = findViewById(R.id.notification_option);
        back_notification_toneImaveView = findViewById(R.id.back_notification_tone);
        notification_tonesLayount = findViewById(R.id.notification_toneLayout);
        notification_ToneTextView = findViewById(R.id.notification_ToneTextView);

        initView();
    }

    @Override
    public void onBackPressed() {
        if(notification_tonesLayount.getVisibility() == View.VISIBLE) {
            notification_tonesLayount.setVisibility(View.INVISIBLE);
            if(notificationTone != null) {
                notificationTone.stop();
            }
        } else {
            finish();
        }
    }

    private void initView() {
        View v = findViewById(R.id.back);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notificationTone != null) {
                    notificationTone.stop();
                }
                finish();
            }
        });

        v = findViewById(R.id.show_again);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgainConfirm();
            }
        });

        mNotificationItemList = SharedStorage.getInstance().notificationToneItemList;

        for(int index = 0; index < mNotificationItemList.size(); index ++) {
            if(mNotificationItemList.get(index).isSelected()) {
                notification_ToneTextView.setText(mNotificationItemList.get(index).getTone_name());
                break;
            }
        }

        notificationToneRecyclerView = findViewById(R.id.notificationToneRecyclerView);
        notificationTonesListAdapter = new NotificationTonesListAdapter(this, mNotificationItemList);
        notificationToneRecyclerView.setAdapter(notificationTonesListAdapter);
        notificationToneRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        notificationToneRecyclerView.setLayoutManager(layoutManager);
        notificationTonesListAdapter.setCallback(this);

        notification_tonesLayount.setVisibility(View.INVISIBLE);

        notification_optionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notification_tonesLayount.setVisibility(View.VISIBLE);
            }
        });

        back_notification_toneImaveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notification_tonesLayount.setVisibility(View.INVISIBLE);
                if(notificationTone != null) {
                    notificationTone.stop();
                }
            }
        });
    }

    private void showAgainConfirm() {
        Utils.showConfirmDialog(SettingsActivity.this,
                getString(R.string.title_show_again), getString(R.string.message_show_again), false, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        enableShowAgain();
                    }
                }, null);
    }

    private void enableShowAgain() {
        SharedPreferences pref = getSharedPreferences(Constant.PREF_CONFIRM, Constant.PRIVATE_MODE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(Constant.KEY_SIGN_OUT_CONFIRM, true);
        editor.putBoolean(Constant.KEY_MONITOR_OFF_CONFIRM, true);

        editor.commit();
    }

    @Override
    public void onNotificationToneClicked(int position) {
        if(!mNotificationItemList.get(position).isSelected()) {
            for(int index = 0; index < mNotificationItemList.size(); index ++) {
                mNotificationItemList.get(index).setSelected(false);
            }
            mNotificationItemList.get(position).setSelected(true);
        }
        notification_ToneTextView.setText(mNotificationItemList.get(position).getTone_name());
        notificationTonesListAdapter.notifyDataSetChanged();

        Uri soundUri = Uri.parse(mNotificationItemList.get(position).getTone_uri());
        MediaPlayer mp = new MediaPlayer();
        try {
            if(notificationTone != null) {
                notificationTone.stop();
            }
            notificationTone = RingtoneManager.getRingtone(this, soundUri);
            notificationTone.play();

        } catch(Throwable throwable) {
            Log.d("dddd", throwable.getMessage());
        }


        SharedPreferences pref_noti_tone = getSharedPreferences(Constant.NOTIFICATION_TONE_INDEX, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref_noti_tone.edit();
        editor.putInt(Constant.NOTIFICATION_TONE_INDEX, position);
        editor.commit();
    }
}
