package com.netglue.ngtmobile.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netglue.ngtmobile.IntroActivity;
import com.netglue.ngtmobile.MainActivity;
import com.netglue.ngtmobile.MapActivity;
import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.adapter.AlertListAdapter;
import com.netglue.ngtmobile.adapter.AssetDataItem;
import com.netglue.ngtmobile.adapter.AssetListAdapter;
import com.netglue.ngtmobile.adapter.AssetListItem;
import com.netglue.ngtmobile.adapter.HighLightArrayAdapter;
import com.netglue.ngtmobile.common.Constant;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.component.DividerItemDecorator;
import com.netglue.ngtmobile.model.AlertItem;
import com.netglue.ngtmobile.model.AppParam;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.NotificationItem;
import com.netglue.ngtmobile.model.SharedStorage;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.core.content.ContextCompat.getSystemService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlertFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlertFragment extends Fragment implements AlertListAdapter.Callback {
    public static final String TAG = AlertFragment.class.getSimpleName();

    private static final String PREF_SEARCH = "search";
    private static final String PREF_KEY_SORT = "sort";

    private Spinner searchfilterSpinner;
    private HighLightArrayAdapter searchfilterAdapter;

    private String mKeyword = "";
    private EditText etKeyword;

    private ImageView ivClear;

//    private List<AlertItem> mAlerts;
    private List<NotificationItem> mAlerts;
    private AlertListAdapter mAlertAdapter;

    private Spinner sortSpinner;
    private HighLightArrayAdapter sortAdapter;

    private Timer mUpdateTimer;
    private boolean connection_lost;

    private LinearLayout connect_lost_layout;
    private ImageView ackImageView;
    private RelativeLayout ackImageViewLayout;
    private LinearLayout ackSubmitLayout;
    private EditText ackEditText;
    private Button submitButton;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("body");
            if (!TextUtils.isEmpty(title)) {
                Log.d("call by FirebaseMsgService", "broadcast receiver calling");
                loadAlerts();
            }
        }
    };

    public AlertFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AlertFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlertFragment newInstance() {
        AlertFragment fragment = new AlertFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

//        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
//                new BroadcastReceiver() {
//                    @Override
//                    public void onReceive(Context context, Intent intent) {
//                        sortChanged();
//                    }
//                }, new IntentFilter(Constant.ACTION_PUSH));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alert, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(SharedStorage.getInstance().connection_lost) {
            connect_lost_layout.setVisibility(View.VISIBLE);
        } else {
            connect_lost_layout.setVisibility(View.GONE);
        }

        String alert_search_asset_name = SharedStorage.getInstance().alert_search_asset_name;

        if(alert_search_asset_name != null && !alert_search_asset_name.isEmpty()) {
            mKeyword = alert_search_asset_name;
            etKeyword.setText(mKeyword);
            ackImageViewLayout.setVisibility(View.VISIBLE);
        } else {
            ackImageViewLayout.setVisibility(View.GONE);
        }
        loadAlerts();
        SharedStorage.getInstance().alert_search_asset_name = "";

        startUpdateTimer();
    }

    @Override
    public void onStop() {
        super.onStop();

        saveSettings();

        stopUpdateTimer();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    private void startUpdateTimer() {
        if (mUpdateTimer != null)
            return;

        int interval = AppParam.getInstance().updateInterval;

        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                connection_lost = !isOnline();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if(connection_lost) {
                                connect_lost_layout.setVisibility(View.VISIBLE);
                            } else {
                                connect_lost_layout.setVisibility(View.GONE);
                            }
                        }
                    });
                }

            }
        }, 1000, 5000);
    }

    private void stopUpdateTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;

//        try {
//            int timeoutMs = 1500;
//            Socket sock = new Socket();
//            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);
//
//            sock.connect(sockaddr, timeoutMs);
//            sock.close();
//            Log.d("44444444444444", "is online function    success");
//            return true;
//        } catch (IOException e) { Log.d("44444444444444", e.getMessage()); return false; }
    }

    private void initView(View view) {

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(Constant.ACTION_PUSH));

        SharedPreferences pref = getActivity().getSharedPreferences(PREF_SEARCH, Context.MODE_PRIVATE);

        connect_lost_layout = view.findViewById(R.id.connect_lost_layout);

        // search keyword
        etKeyword = view.findViewById(R.id.keyword);
        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mKeyword = s.toString();
//                ivClear.setVisibility(mKeyword.isEmpty()? View.GONE : View.VISIBLE);

                search();
            }
        });

//        ivClear = view.findViewById(R.id.clear);
//        ivClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                etKeyword.setText("");
//
//                if (!TextUtils.isEmpty(mKeyword)) {
//                    mKeyword = "";
//                }
//
//                search();
//            }
//        });

        // search filter
        searchfilterSpinner = view.findViewById(R.id.searchfilterSpinner);
        searchfilterAdapter = new HighLightArrayAdapter(getContext(), R.layout.spinner_filter_item, getResources().getStringArray(R.array.alert_search_filter));
        searchfilterAdapter.setHighlightColor(getResources().getColor(R.color.bg_menu_select));
        searchfilterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        searchfilterSpinner.setAdapter(searchfilterAdapter);
        searchfilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchfilterAdapter.setSelection(position);

                mAlertAdapter.setFilterBy(position);
                search();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ackImageViewLayout = view.findViewById(R.id.ackImageViewLayout);
        ackImageView = view.findViewById(R.id.ackImageView);
        ackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ackSubmitLayout.setVisibility(View.VISIBLE);
            }
        });

        ackSubmitLayout = view.findViewById(R.id.ackSubmitLayout);
        ackEditText = view.findViewById(R.id.ackEditText);
        submitButton = view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ackString = ackEditText.getText().toString();
                if(!ackString.isEmpty()) {
                    ackSubmitLayout.setVisibility(View.GONE);
                    if (getActivity() != null) {
                        InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(ackEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        ackEditText.setText("");
                    }
                } else {
                    Toast.makeText(getContext(), "Please input acknowledgement contents", Toast.LENGTH_LONG).show();
                }
            }
        });

//        // Sort By
//        sortSpinner = view.findViewById(R.id.orderBy);
//        sortAdapter = new HighLightArrayAdapter(getContext(), R.layout.spinner_sort_item, getResources().getStringArray(R.array.alert_order_by));
//        sortAdapter.setHighlightColor(getResources().getColor(R.color.bg_menu_select));
//        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        sortSpinner.setAdapter(sortAdapter);
//        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                sortAdapter.setSelection(position);
//
//                sortChanged();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//        sortSpinner.setSelection(pref.getInt(PREF_KEY_SORT, 0));

        // Alert list
//        mAlerts = SharedStorage.getInstance().alerts;
        mAlerts = SharedStorage.getInstance().notificationList;
        RecyclerView rv = view.findViewById(R.id.list);
        mAlertAdapter = new AlertListAdapter(getContext(), mAlerts);
        rv.setAdapter(mAlertAdapter);
        rv.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(layoutManager);
        mAlertAdapter.setCallback(this);
        DividerItemDecorator dividerItemDecoration = new DividerItemDecorator(
                ContextCompat.getDrawable(getContext(), R.drawable.list_divider_gray));
        rv.addItemDecoration(dividerItemDecoration);
    }

    private void saveSettings() {
        SharedPreferences pref = getActivity().getSharedPreferences(PREF_SEARCH, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
//        int sortBy = sortSpinner.getSelectedItemPosition();
        int sortBy = 0;
        editor.putInt(PREF_KEY_SORT, sortBy);

        editor.commit();

        if(getActivity() != null) {
            SharedPreferences pref_alert = getActivity().getSharedPreferences(Constant.PREF_ALERT, Context.MODE_PRIVATE);

            Gson gson = new Gson();
            String json = pref_alert.getString(Constant.PREF_ALERT, "");
            Type type = new TypeToken<List<NotificationItem>>() {
            }.getType();
            List<NotificationItem> notificationItemList = gson.fromJson(json, type);
            if (notificationItemList != null) {
                for (int index = 0; index < notificationItemList.size(); index++) {
                    notificationItemList.get(index).new_noti = false;
                }
                SharedPreferences.Editor prefsEditor = pref_alert.edit();
                json = gson.toJson(notificationItemList);
                prefsEditor.putString(Constant.PREF_ALERT, json);
                prefsEditor.commit();
            }
        }

    }


    @Override
    public void onAlertClicked(int position) {
        //SharedStorage.getInstance().currentAsset = mAlerts.get(position);

        //gotoMap();
    }

    private void gotoMap() {
        Intent intent = new Intent(getContext(), MapActivity.class);
        startActivity(intent);
    }

    private void search() {
        mAlertAdapter.getFilter().filter(mKeyword);
    }

    public void sortChanged() {
//        int sortBy = sortSpinner.getSelectedItemPosition();
        int sortBy = 1;
        if (sortBy == 1) {    // order by time
            Collections.sort(mAlerts, new Comparator<NotificationItem>() {
                @Override
                public int compare(NotificationItem a1, NotificationItem a2) {
                    long at1 = a1.timestamp.getTime();
                    long at2 = a2.timestamp.getTime();

                    if (at1 < at2) return 1;
                    if (at1 > at2) return -1;

                    return 0;
                }
            });
        } else {                // order by A-Z
            Collections.sort(mAlerts, new Comparator<NotificationItem>() {
                @Override
                public int compare(NotificationItem a1, NotificationItem a2) {
                    return a1.title.compareTo(a2.title);
                }
            });
        }

        mAlertAdapter.resetFilter();

        search();

    }

    public void loadAlerts() {
        SharedPreferences pref = getActivity().getSharedPreferences(Constant.PREF_ALERT, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = pref.getString(Constant.PREF_ALERT, "");
        Type type = new TypeToken<List<NotificationItem>>(){}.getType();
        List<NotificationItem> notificationItemList = gson.fromJson(json, type);

        mAlerts.clear();
        if(notificationItemList != null) {
            for (int i = 0; i < notificationItemList.size(); i++) {
                mAlerts.add(notificationItemList.get(i));
            }
        }
        sortChanged();

    }
}
