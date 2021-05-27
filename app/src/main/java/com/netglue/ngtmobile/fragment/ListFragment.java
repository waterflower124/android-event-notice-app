package com.netglue.ngtmobile.fragment;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netglue.ngtmobile.MonitorActivity;
import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.adapter.AssetDataItem;
import com.netglue.ngtmobile.adapter.AssetHeaderItem;
import com.netglue.ngtmobile.adapter.AssetListAdapter;
import com.netglue.ngtmobile.adapter.AssetListItem;
import com.netglue.ngtmobile.adapter.HighLightArrayAdapter;
import com.netglue.ngtmobile.common.Constant;
import com.netglue.ngtmobile.common.RestApi;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.AppParam;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.AssetPushItem;
import com.netglue.ngtmobile.model.SessionManager;
import com.netglue.ngtmobile.model.SharedStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment implements AssetListAdapter.Callback {
    public static final String TAG = ListFragment.class.getSimpleName();

    private static final String PREF_SEARCH = "search";
    private static final String PREF_KEY_FILTER = "filter";
    private static final String PREF_KEY_SORT = "sort";

    private static final int RC_MONITOR = 1;

    private static final String PREF_ASSET = "assets";
    private static final int PRIVATE_MODE = 0;
    public static final String KEY_ASSETS_LIST = "assets_list";

    private ProgressDialog mProgressDlg;

    private Spinner filterSpinner;
    private Spinner sortSpinner;
    private Spinner searchfilterSpinner;

    private HighLightArrayAdapter filterAdapter;
    private HighLightArrayAdapter sortAdapter;
    private HighLightArrayAdapter searchfilterAdapter;

    private String mKeyword = "";
    private EditText etKeyword;

    private ImageView ivClear;

    private List<AssetListItem> mAssets = new ArrayList<>();
    private AssetListAdapter mAssetAdapter;

    private Map<String, List<AssetItem>> mAssetMap = new TreeMap<>();

    private int currentPosition;

    private String monInterval;
    private String monStartTimestamp;
    private String monStopTimestamp;

    private Callback mListener;

    private boolean isAssetQuery = false;

    private Timer mUpdateTimer;

    private LinearLayout connect_list_layout;

    // connecting bar animation
    private RelativeLayout connecting_bar_increase_layout;
    private ValueAnimator anim_increase;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("body");
            if (!TextUtils.isEmpty(title)) {
                Log.d("call by FirebaseMsgService", "broadcast receiver calling");
                refreshAlertCount(message);
            }
        }
    };

    public ListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFragment newInstance() {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        groupAssets();

        initView(view);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_MONITOR) {
            if (resultCode == Activity.RESULT_OK) {
                monInterval = data.getStringExtra("mon_interval");
                monStartTimestamp = data.getStringExtra("mon_start_timestamp");
                monStopTimestamp = data.getStringExtra("mon_stop_timestamp");

                AssetItem item = ((AssetDataItem) mAssets.get(currentPosition)).getAsset();
                toggleMonitor(item);

            } else {
                mAssetAdapter.notifyItemChanged(currentPosition);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(SharedStorage.getInstance().connection_lost) {
            connect_list_layout.setVisibility(View.VISIBLE);
            etKeyword.setEnabled(false);
            searchfilterSpinner.setEnabled(false);
        } else {
            connect_list_layout.setVisibility(View.GONE);
            etKeyword.setEnabled(true);
            searchfilterSpinner.setEnabled(true);
        }

        getAssets();

        startUpdateTimer();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopUpdateTimer();
    }

    @Override
    public void onStop() {
        super.onStop();

        saveSettings();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            mListener = (Callback) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    private void initView(View view) {
        SharedPreferences pref = getActivity().getSharedPreferences(PREF_SEARCH, Context.MODE_PRIVATE);

        connect_list_layout = view.findViewById(R.id.connect_list_layout);
        connecting_bar_increase_layout = view.findViewById(R.id.connecting_bar_increase_layout);

        /// connecting bar animation
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int device_width = metrics.widthPixels;

        anim_increase = ValueAnimator.ofInt(0, device_width);
        anim_increase.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)connecting_bar_increase_layout.getLayoutParams();
                layoutParams.width = val;
                connecting_bar_increase_layout.setLayoutParams(layoutParams);
            }
        });

        // register broadcast listener
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter(Constant.ACTION_PUSH));

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
//                mAssetAdapter.setFilterBy(searchfilterSpinner.getSelectedItemPosition());
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
//
//                    search();
//                }
//            }
//        });

        // Asset list
        RecyclerView rv = view.findViewById(R.id.list);
        mAssetAdapter = new AssetListAdapter(getContext(), mAssets);
        rv.setAdapter(mAssetAdapter);
        rv.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(layoutManager);
        mAssetAdapter.setCallback(this);

        // search filter
        searchfilterSpinner = view.findViewById(R.id.searchfilterSpinner);
        searchfilterAdapter = new HighLightArrayAdapter(getContext(), R.layout.spinner_filter_item, getResources().getStringArray(R.array.filter_search));
        searchfilterAdapter.setHighlightColor(getResources().getColor(R.color.bg_menu_select));
        searchfilterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        searchfilterSpinner.setAdapter(searchfilterAdapter);

        searchfilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchfilterAdapter.setSelection(position);
                mAssetAdapter.setFilterBy(position);

                if (mAssetMap.size() > 0)
                    search();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


//        // Filter By
//        filterSpinner = view.findViewById(R.id.filterBy);
//        filterAdapter = new HighLightArrayAdapter(getContext(), R.layout.spinner_filter_item, getResources().getStringArray(R.array.filter_by));
//        filterAdapter.setHighlightColor(getResources().getColor(R.color.bg_menu_select));
//        filterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        filterSpinner.setAdapter(filterAdapter);
//        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                filterAdapter.setSelection(position);
//
//                mAssetAdapter.setFilterBy(position);
//
//                if (mAssetMap.size() > 0)
//                    search();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//        filterSpinner.setSelection(pref.getInt(PREF_KEY_FILTER, 0));
//
//        // Sort By
//        sortSpinner = view.findViewById(R.id.orderBy);
//        sortAdapter = new HighLightArrayAdapter(getContext(), R.layout.spinner_sort_item, getResources().getStringArray(R.array.order_by));
//        sortAdapter.setHighlightColor(getResources().getColor(R.color.bg_menu_select));
//        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        sortSpinner.setAdapter(sortAdapter);
//        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                sortAdapter.setSelection(position);
//
//                if (position == 0)
//                    groupAssets();
//
//                updateAssetList();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//        sortSpinner.setSelection(pref.getInt(PREF_KEY_SORT, 0));

        // Progress dialog
        mProgressDlg = new ProgressDialog(getContext());
        mProgressDlg.setCancelable(false);
        mProgressDlg.setMessage(getString(R.string.progress_load));


    }

    private void refreshAlertCount(String noti_body) {
        updateAssetList();
    }

    private void showProgress() {
        if (!mProgressDlg.isShowing())
            mProgressDlg.show();
    }

    private void hideProgress() {
        if (mProgressDlg != null && mProgressDlg.isShowing())
            mProgressDlg.dismiss();
    }

    private void search() {
        mAssetAdapter.getFilter().filter(mKeyword);
    }

    private void groupAssets() {
        mAssetMap.clear();

        List<AssetItem> assets = SharedStorage.getInstance().assets;

        for (AssetItem item : assets) {
            classifyAsset(item);
        }
    }

    private void classifyAsset(AssetItem item) {
        String status = item.getStatus();
        List<AssetItem> list = getAssetList(status);
        if (item.isAlarm() && status.equals("MONITORED"))
            list.add(0, item);
        else
            list.add(item);
    }

    private void startUpdateTimer() {
        if (mUpdateTimer != null)
            return;

        int interval = AppParam.getInstance().updateInterval;

        Log.d("11111111111", String.valueOf(AppParam.getInstance().updateInterval * 1000));

        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(createUpdateTask(), interval * 1000, interval * 1000);
    }

    private void stopUpdateTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    private TimerTask createUpdateTask () {
        // for asset update
        return  new TimerTask() {
            @Override
            public void run() {
                getAssets();
            }
        };
    }

    /**
     * After monitor status changed
     * @param item
     */
    private void monitorChanged(AssetItem item) {
//        int sort = sortSpinner.getSelectedItemPosition();
//        int filter = filterSpinner.getSelectedItemPosition();
        int sort = 0;
        int filter = 0;

        if (sort == 1) {  // A-Z
            if (filter == AssetListItem.FILTER_ALL || filter == AssetListItem.FILTER_FAVOURITE) {
                mAssetAdapter.notifyItemChanged(currentPosition);
            } else {
                mAssets.remove(currentPosition);
                mAssetAdapter.notifyItemRemoved(currentPosition);
            }
        } else {            // Status
            String status = item.getOriginStatus();
            List<AssetItem> orgList = getAssetList(status);
            List<AssetItem> monList = getAssetList("MONITORED");

            if (item.isMon()) {
                orgList.remove(item);
                if (orgList.size() == 0)
                    mAssetMap.remove(status);

                if (item.isAlarm())
                    monList.add(0, item);
                else
                    monList.add(item);
            } else {
                monList.remove(item);
                if (monList.size() == 0)
                    mAssetMap.remove("MONITORED");

                orgList.add(item);
            }

            updateAssetList();
        }
    }

    /**
     * After favorite status changed
     * @param item
     */
    private void favoriteChanged(AssetItem item) {
//        int filter = filterSpinner.getSelectedItemPosition();
//        int sort = sortSpinner.getSelectedItemPosition();
        int sort = 0;
        int filter = 0;

        if (filter == AssetListItem.FILTER_FAVOURITE && !item.isFav()) {
            mAssets.remove(currentPosition);
            mAssetAdapter.notifyItemRemoved(currentPosition);
            if (mAssets.size() == 1 && sort == AssetListItem.SORT_STATUS) {
                mAssets.remove(0);
                mAssetAdapter.notifyItemRemoved(0);
            }
        } else {
            mAssetAdapter.notifyItemChanged(currentPosition);
        }
    }

    private List<AssetItem> getAssetList(String group) {
        List<AssetItem> list = mAssetMap.get(group);
        if (list == null) {
            list = new ArrayList<>();
            mAssetMap.put(group, list);
        }

        return list;
    }

    private void updateAssetList() {
        mAssets.clear();
        mAssetAdapter.resetFilter();

//        int sortBy = sortSpinner.getSelectedItemPosition();
        int sortBy = 0;
        if (sortBy == 0) {  // sort by status
            List<AssetPushItem> asset_push = SharedStorage.getInstance().asset_push;
            boolean exist = false;
            for (String status : mAssetMap.keySet()) {
                mAssets.add(new AssetHeaderItem(status));

                for (AssetItem item : mAssetMap.get(status)) {
//                    Log.d("FirebaseMsgService ",  "asset item:  " + item.id);
                    AssetDataItem dataItem = null;
                    exist = false;
                    for(int index = 0; index < asset_push.size(); index ++) {
                        if(asset_push.get(index).getAssets_id().equals(item.id)) {
                            dataItem = new AssetDataItem(item, asset_push.get(index));
                            exist = true;
                            break;
                        }
                    }
                    if(!exist) {
                        dataItem = new AssetDataItem(item, null);

                    }
                    mAssets.add(dataItem);
                }
            }
        } else {            // sort by a-z
//            List<AssetItem> assets = SharedStorage.getInstance().assets;
//            for (AssetItem item : assets) {
//                AssetDataItem dataItem = new AssetDataItem(item);
//                mAssets.add(dataItem);
//            }
//
//            Collections.sort(mAssets, new Comparator<AssetListItem>() {
//                @Override
//                public int compare(AssetListItem o1, AssetListItem o2) {
//                    return ((AssetDataItem)o1).getAsset().name.compareTo(((AssetDataItem)o2).getAsset().name);
//                }
//            });
        }

        search();
    }

    private void saveSettings() {
        SharedPreferences pref = getActivity().getSharedPreferences(PREF_SEARCH, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
//        int filter = filterSpinner.getSelectedItemPosition();
//        int sort = sortSpinner.getSelectedItemPosition();
        int sort = 0;
        int filter = 0;
        editor.putInt(PREF_KEY_FILTER, filter);
        editor.putInt(PREF_KEY_SORT, sort);

        editor.commit();
    }

    @Override
    public void onAssetBellClicked(AssetListAdapter adapter, int position) {
        mListener.onAssetBellClicked();
    }

    @Override
    public void onAssetClicked(AssetListAdapter adapter, int position) {
        SharedStorage ss = SharedStorage.getInstance();
        ss.currentAsset = ((AssetDataItem)adapter.itemList.get(position)).getAsset();
        mListener.onAssetListItemSelected();
    }

    @Override
    public void onLikeClicked(AssetListAdapter adapter, int position, boolean isfav) {
        currentPosition = position;

        AssetItem item = ((AssetDataItem) mAssets.get(position)).getAsset();
        toggleFavorite(item);
    }

    @Override
    public void onMonitorChange(AssetListAdapter adapter, int position, boolean enable) {
        currentPosition = position;

        AssetItem item = ((AssetDataItem) mAssets.get(position)).getAsset();
        if (item.isMon()) {
            attemptMonitorOff(item);
        } else {
            Intent intent = new Intent(getContext(), MonitorActivity.class);
            startActivityForResult(intent, RC_MONITOR);
        }
    }

    private void attemptMonitorOff(AssetItem item) {
        if (monitorOffConfirmEnabled())
            showMonitorOffConfirm();
        else
            toggleMonitor(item);
    }

    private void showMonitorOffConfirm() {
        Utils.showConfirmDialog(getContext(),
                getString(R.string.title_disable_monitor),
                getString(R.string.message_disable_monitor), true, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1)
                            disableMonitorOffConfirm();

                        AssetItem item = ((AssetDataItem) mAssets.get(currentPosition)).getAsset();
                        toggleMonitor(item);
                    }
                }, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mAssetAdapter.notifyItemChanged(currentPosition);
                    }
                });
    }

    private void disableMonitorOffConfirm() {
        SharedPreferences pref = getContext().getSharedPreferences(Constant.PREF_CONFIRM, Constant.PRIVATE_MODE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(Constant.KEY_MONITOR_OFF_CONFIRM, false);

        editor.commit();
    }

    private boolean monitorOffConfirmEnabled() {
        SharedPreferences pref = getContext().getSharedPreferences(Constant.PREF_CONFIRM, Constant.PRIVATE_MODE);
        return pref.getBoolean(Constant.KEY_MONITOR_OFF_CONFIRM, true);
    }

    /**
     * get asset
     */
    private void getAssets() {
        if (isAssetQuery) {
            Log.d(TAG, "Skip asset query.");
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                anim_increase.end();
                anim_increase.cancel();
            }
        });


        try{
            SessionManager sm = SessionManager.getInstance();

            JSONObject jo = new JSONObject();
            jo.put("uid", sm.username);
            jo.put("token", sm.token);

            isAssetQuery = true;

            if (SharedStorage.getInstance().assets.size() == 0)
                showProgress();

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.ASSET)
                    .post(reqBody)
                    .build();

            Log.d(TAG, request.url().toString());
            Log.d(TAG, Utils.requestBodyToString(request));

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Exception", e);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {

                        public void run() {
                            anim_increase.setDuration(AppParam.getInstance().updateInterval * 1000);
                            anim_increase.start();

                            hideProgress();
                            isAssetQuery = false;

                            SharedStorage.getInstance().connection_lost = true;

                            SharedPreferences pref = getContext().getSharedPreferences(PREF_ASSET, PRIVATE_MODE);
                            if(pref != null) {

                                Gson gson = new Gson();
                                String json = pref.getString(KEY_ASSETS_LIST, "");
                                Type type = new TypeToken<List<AssetItem>>(){}.getType();
                                List<AssetItem> assets_list_saved = gson.fromJson(json, type);

                                List<AssetItem> assets = SharedStorage.getInstance().assets;
                                if(assets != null ) {
                                    assets.clear();
                                    mAssetMap.clear();
                                }

                                for (int i = 0; i < assets_list_saved.size(); ++i) {

                                    AssetItem item = assets_list_saved.get(i);
                                    assets.add(item);

                                    // grouping
                                    String status = item.getStatus();
                                    List<AssetItem> list = getAssetList(status);
                                    if (item.isAlarm() && status.equals("MONITORED"))
                                        list.add(0, item);
                                    else
                                        list.add(item);
                                }
                                updateAssetList();
                                if(searchfilterSpinner.getSelectedItemPosition() == 0) {
                                    searchfilterAdapter.setSelection(1);
                                }
                                connect_list_layout.setVisibility(View.VISIBLE);
                                SharedStorage.getInstance().connection_lost = true;
                                etKeyword.setEnabled(false);
                                searchfilterSpinner.setEnabled(false);
                                Log.d("1111111111", String.valueOf(assets.size()));
                            }

                        }});
                    }
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    isAssetQuery = false;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                anim_increase.setDuration(AppParam.getInstance().updateInterval * 1000 - 3000);
                                anim_increase.start();

                            }
                        });
                    }


                    ResponseBody responseBody = response.body();

                    SharedStorage.getInstance().connection_lost = false;

                    if (!response.isSuccessful()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() { public void run() {
                                hideProgress();
                                Toast.makeText(getContext(), response.message(), Toast.LENGTH_SHORT).show();

                                // session out
                                SessionManager.getInstance().signOut(getContext());
                                Utils.gotoSignIn(getContext());
                                getActivity().finish();
                            }});
                        }
                        Log.e(TAG, response.toString());
                        return;
                    }

                    final String resp = responseBody.string();

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                connect_list_layout.setVisibility(View.GONE);
                                etKeyword.setEnabled(true);
                                searchfilterSpinner.setEnabled(true);

                                JSONObject jr = new JSONObject(resp);

                                List<AssetItem> assets = SharedStorage.getInstance().assets;
                                assets.clear();
                                mAssetMap.clear();

                                if (jr.getString("status").equals("1")) {
                                    JSONArray ja = jr.getJSONArray("assets");

                                    for (int i = 0; i < ja.length(); ++i) {
                                        JSONObject jo = ja.getJSONObject(i);

                                        AssetItem item = AssetItem.parse(jo);
                                        assets.add(item);

                                        // grouping
                                        String status = item.getStatus();
                                        List<AssetItem> list = getAssetList(status);
                                        if (item.isAlarm() && status.equals("MONITORED"))
                                            list.add(0, item);
                                        else
                                            list.add(item);
                                    }

                                    SharedPreferences pref = getContext().getSharedPreferences(PREF_ASSET, PRIVATE_MODE);
                                    if(pref != null) {
                                        SharedPreferences.Editor prefsEditor = pref.edit();
                                        Gson gson = new Gson();
                                        String json = gson.toJson(assets);
                                        prefsEditor.putString(KEY_ASSETS_LIST, json);
                                        prefsEditor.commit();
                                    }

                                    updateAssetList();

                                    if(searchfilterSpinner.getSelectedItemPosition() == 0) {
                                        searchfilterAdapter.setSelection(1);
                                    }

                                    Log.d(TAG, "Assets count: " + assets.size());
                                } else {
                                    String message = jr.getString("message");

                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Exception", e);
                            }

                            hideProgress();
                        }});
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    /**
     * monitor on/off
     */
    private void toggleMonitor(final AssetItem item) {
        try{
            SessionManager sm = SessionManager.getInstance();

            JSONObject jo = new JSONObject();
            jo.put("uid", sm.username);
            jo.put("token", sm.token);
            jo.put("a_id", item.id);
            jo.put("a_fav", item.isFav()? 1 : 0);
            jo.put("a_mon", item.isMon()? 0 : 1);
            if (!item.isMon()) {
                jo.put("mon_interval", monInterval);
                jo.put("mon_start_timestamp", monStartTimestamp);
                jo.put("mon_stop_timestamp", monStopTimestamp);
            }

            isAssetQuery = true;

            showProgress();

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.UPDATE_ASSET)
                    .post(reqBody)
                    .build();

            Log.d(TAG, request.url().toString());
            Log.d(TAG, Utils.requestBodyToString(request));

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Exception", e);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() { public void run() {
                            hideProgress();
                            isAssetQuery = false;
                        }});
                    }
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    isAssetQuery = false;

                    ResponseBody responseBody = response.body();

                    if (!response.isSuccessful()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() { public void run() {
                                hideProgress();
                                Toast.makeText(getContext(), response.message(), Toast.LENGTH_SHORT).show();

                                // session out
                                SessionManager.getInstance().signOut(getContext());
                                Utils.gotoSignIn(getContext());
                                getActivity().finish();
                            }});
                        }
                        Log.e(TAG, response.toString());
                        return;
                    }

                    final String resp = responseBody.string();
                    Log.d(TAG, resp);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() { public void run() {
                            try {
                                JSONObject jr = new JSONObject(resp);

                                if (jr.getString("status").equals("1")) {
                                    item.mon = 1 - item.mon;
                                    monitorChanged(item);
                                } else {
                                    String message = jr.getString("message");

                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Exception", e);
                            }

                            hideProgress();
                        }});
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    /**
     * monitor on/off
     */
    private void toggleFavorite(final AssetItem item) {
        try{
            SessionManager sm = SessionManager.getInstance();

            JSONObject jo = new JSONObject();
            jo.put("uid", sm.username);
            jo.put("token", sm.token);
            jo.put("a_id", item.id);
            jo.put("a_fav", item.isFav()? 0 : 1);
            jo.put("a_mon", item.isMon()? 1 : 0);
            if (item.isMon()) {
                jo.put("mon_interval", item.mon_interval);
                jo.put("mon_start_timestamp", item.mon_start_timestamp);
                jo.put("mon_stop_timestamp", item.mon_stop_timestamp);
            }

            isAssetQuery = true;

            showProgress();

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.UPDATE_ASSET)
                    .post(reqBody)
                    .build();

            Log.d(TAG, request.url().toString());
            Log.d(TAG, Utils.requestBodyToString(request));

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Exception", e);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() { public void run() {
                            hideProgress();
                            isAssetQuery = false;
                        }});
                    }
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    isAssetQuery = false;

                    ResponseBody responseBody = response.body();

                    if (!response.isSuccessful()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() { public void run() {
                                hideProgress();
                                Toast.makeText(getContext(), response.message(), Toast.LENGTH_SHORT).show();
                            }});
                        }
                        Log.e(TAG, response.toString());
                        return;
                    }

                    final String resp = responseBody.string();
                    Log.d(TAG, resp);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() { public void run() {
                            try {
                                JSONObject jr = new JSONObject(resp);

                                if (jr.getString("status").equals("1")) {
                                    item.fav = 1 - item.fav;

                                    favoriteChanged(item);
                                } else {
                                    String message = jr.getString("message");

                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Exception", e);
                            }

                            hideProgress();
                        }});
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    public interface Callback {
        void onAssetListItemSelected();
        void onAssetBellClicked();
    }

}
