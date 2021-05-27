package com.netglue.ngtmobile.fragment;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.adapter.AssetDataItem;
import com.netglue.ngtmobile.adapter.AssetHeaderItem;
import com.netglue.ngtmobile.adapter.AssetListAdapter;
import com.netglue.ngtmobile.adapter.AssetListItem;
import com.netglue.ngtmobile.adapter.HighLightArrayAdapter;
import com.netglue.ngtmobile.adapter.ViewPagerAdapter;
import com.netglue.ngtmobile.common.Constant;
import com.netglue.ngtmobile.common.RestApi;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.AppParam;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.AssetPushItem;
import com.netglue.ngtmobile.model.RouteItem;
import com.netglue.ngtmobile.model.SessionManager;
import com.netglue.ngtmobile.model.SharedStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, AssetListAdapter.Callback {
    public static final String TAG = MapFragment.class.getSimpleName();

    private static final String PREF_MAP = "map";
    private static final String PREF_KEY_ZOOM = "zoom";
    private static final String PREF_KEY_CENTER_LAT = "center_lat";
    private static final String PREF_KEY_CENTER_LNG = "center_lng";

    private static final String PREF_ASSET = "assets";
    private static final int PRIVATE_MODE = 0;
    public static final String KEY_ASSETS_LIST = "assets_list";

//    private static final float MAP_ZOOM_CRITIRIA = 18.0f;
//    private static final float MAP_ZOOM_DEFAULT = 18.0f;

    private static final float MAP_ZOOM_CRITIRIA = 18.0f;
    private static final float MAP_ZOOM_DEFAULT = 18.0f;

    SupportMapFragment mapFragment;

    //private MapView mMapView;
    private GoogleMap mGoogleMap;

    private Spinner searchfilterSpinner;
    private HighLightArrayAdapter searchfilterAdapter;

    private EditText searchEditText;
    private String mKeyword = "";

    private View llInfo, searchResultView;

    private TextView tvAssetUser;
    private TextView tvAssetName;
    private TextView tvAssetAddr;

    private TextView tvAssetStatus;
    private TextView tvAssetUpdate;

    private View llDetails;

    private ToggleButton tbVisible;

    private ViewPager viewPager;
    private ViewPagerAdapter mPagerAdapter;

    private AssetItem selectedAsset;

    private InfoFragment infoFragment;
    private TripsFragment tripsFragment;
    private MoreFragment moreFragment;

    private ToggleButton tbSetting, maplabelToggle, maptrafficToggle, mapsateliteToggle;

    private ImageView googlemapImageView, shareImageView;

    private Timer mUpdateTimer;

    private Marker selectedMarker;
    private Marker routeStartMarker;
    private Polyline routeLine;
    private List<RouteItem> mRoute = new ArrayList<>();

    private Call routeCall;

    private List<Marker> mMarkers = new ArrayList<>();
    private List<Marker> allMarkers = new ArrayList<>();

    private RecyclerView searchResultRecyclerView;
    private AssetListAdapter mAssetAdapter;
    private List<AssetListItem> mAssets = new ArrayList<>();
    private Map<String, List<AssetItem>> mAssetMap = new TreeMap<>();

    private Callback mListener;

    private int selected_spinner_position = 1;

    private LinearLayout connect_list_layout;

    // connecting bar animation
    private RelativeLayout connecting_bar_increase_layout;
    private ValueAnimator anim_increase;

    // setting toggle
    LinearLayout mapSettingLayout;
    List<Marker> removedMarkerList;


    // map custom mark
    LayoutInflater inflaterLayoutter;

    // toggling marker label
    Boolean show_marker_label = false;

    // integrated rounded marker
    float prev_cameraPosZoom;
    Marker integratedMarker;
    List<Marker> integratedMarkerList = new ArrayList<>();

    // for grouping
    List<Marker> newMarkers = new ArrayList<>();
    List<List<Marker>> removedMakerListGroup = new ArrayList<>();

    boolean first_open;

    RelativeLayout settingLayout;

    float current_map_zoom = MAP_ZOOM_CRITIRIA;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("body");
            if (!TextUtils.isEmpty(title)) {

            }
        }
    };


    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
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
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SharedStorage.getInstance().connection_lost) {
            connect_list_layout.setVisibility(View.VISIBLE);
            searchfilterSpinner.setEnabled(false);
        } else {
            connect_list_layout.setVisibility(View.GONE);
            searchfilterSpinner.setEnabled(true);
        }
        startUpdateTimer();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleMap != null) {
            saveMapStatus();
        }

        stopUpdateTimer();
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




    private void initView(View view) {
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        first_open = true;

        removedMarkerList = new ArrayList<>();

        prev_cameraPosZoom = MAP_ZOOM_DEFAULT;
        integratedMarker = null;

        connect_list_layout = view.findViewById(R.id.connect_list_layout);

        //////
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


        // search keyword
        searchEditText = view.findViewById(R.id.keyword);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mKeyword = s.toString();

                search();
            }
        });
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mAssets.clear();
                    mAssetMap.clear();
                    List<AssetItem> assets = SharedStorage.getInstance().assets;
                    for (AssetItem item : assets) {
                        //                    AssetDataItem dataItem = new AssetDataItem(item);
                        //                    mAssets.add(dataItem);

                        // grouping
                        String status = item.getStatus();
                        List<AssetItem> list = getAssetList(status);
                        if (item.isAlarm() && status.equals("MONITORED"))
                            list.add(0, item);
                        else
                            list.add(item);
                    }

                    for (String status1 : mAssetMap.keySet()) {
                        mAssets.add(new AssetHeaderItem(status1));

                        for (AssetItem item1 : mAssetMap.get(status1)) {
                            AssetDataItem dataItem = new AssetDataItem(item1, null);
                            mAssets.add(dataItem);
                        }
                    }

                    mAssetAdapter.notifyDataSetChanged();
                    searchResultView.setVisibility(View.VISIBLE);

                    search();

                } else {
                    removeSearchResultView();
                }
            }
        });

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

                selected_spinner_position = position;

                if(searchResultView.getVisibility() != View.GONE) {
                    mAssetAdapter.setFilterBy(position);

                    if (mAssetMap.size() > 0)
                        search();

                } else {
                    updateAssetOverlay(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // traffic info on/off
        mapSettingLayout = view.findViewById(R.id.mapSettingLayout);
        tbSetting = view.findViewById(R.id.traffic);
        tbSetting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                mGoogleMap.setTrafficEnabled(isChecked);
                if(isChecked) {
                    mapSettingLayout.setVisibility(View.VISIBLE);
                    updateAssetOverlay(selected_spinner_position);
                } else {
                    mapSettingLayout.setVisibility(View.INVISIBLE);
                    updateAssetOverlay(selected_spinner_position);
                }
            }
        });

        show_marker_label = true;
        maplabelToggle = view.findViewById(R.id.maplabelToggle);
        maplabelToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    show_marker_label = true;
                } else {
                    show_marker_label = false;
                }
                updateAssetOverlay(selected_spinner_position);
            }
        });

        maptrafficToggle = view.findViewById(R.id.maptrafficToggle);
        maptrafficToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mGoogleMap.setTrafficEnabled(isChecked);
            }
        });

        mapsateliteToggle = view.findViewById(R.id.mapsateliteToggle);
        mapsateliteToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else {
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });

        llInfo = view.findViewById(R.id.info_layout);
        llInfo.setVisibility(View.GONE);
        llInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prevent click event to go to mapview.
            }
        });

        //  redirectin to google map
        googlemapImageView = view.findViewById(R.id.googlemapImageView);
        googlemapImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setTitle("Leave app?");
                builder1.setMessage("In order to proceed the action, you will leave the app. You can return to the app at anytime.");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String urlAddress = "http://maps.google.com/maps?q="+ String.valueOf(selectedAsset.lat)  +"," + String.valueOf(selectedAsset.lng) +"("+ selectedAsset.name + ")&iwloc=A&hl=es";
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                                intent.setPackage("com.google.android.apps.maps");
                                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                                    getContext().startActivity(intent);
                                }
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            }
        });

        // share function
        shareImageView = view.findViewById(R.id.shareImageView);
        shareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = selectedAsset.lat + " " + selectedAsset.lng;
                String shareSub = selectedAsset.address;
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share NGT"));
            }
        });

        // search result view
        searchResultView = view.findViewById(R.id.searchresult_layout);
        searchResultView.setVisibility(View.GONE);

        // search result recycler view
        searchResultRecyclerView = view.findViewById(R.id.searchResultRecyclerView);
        mAssetAdapter = new AssetListAdapter(getContext(), mAssets);
        searchResultRecyclerView.setAdapter(mAssetAdapter);
        searchResultRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        searchResultRecyclerView.setLayoutManager(layoutManager);
        mAssetAdapter.setCallback(this);

        // close
//        ImageView iv = view.findViewById(R.id.close);
//        iv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                resetCurrentItem();
//
//                llInfo.setVisibility(View.GONE);
//                if (tbVisible.isChecked())
//                    tbVisible.setChecked(false);
//            }
//        });

        tvAssetName = view.findViewById(R.id.asset_name);
        tvAssetUser = view.findViewById(R.id.asset_user);
        tvAssetAddr = view.findViewById(R.id.asset_addr);

        tvAssetStatus = view.findViewById(R.id.asset_status);
        tvAssetUpdate = view.findViewById(R.id.asset_update);

        tbVisible = view.findViewById(R.id.toggle_visible);
        tbVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                llDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);

                if (isChecked && selectedAsset != null) {
                    infoFragment.getAsset(selectedAsset.id);
                    tripsFragment.getTrip(selectedAsset.id);
                    moreFragment.getMoreItems(selectedAsset);
                }
            }
        });
        final View parent = (View) tbVisible.getParent();  // button: the view you want to enlarge hit area
        parent.post( new Runnable() {
            public void run() {
                final Rect rect = new Rect();
                tbVisible.getHitRect(rect);
                rect.top -= 50;    // increase top hit area
                rect.left -= 800;   // increase left hit area
                rect.bottom += 50; // increase bottom hit area
                rect.right += 800;  // increase right hit area
                parent.setTouchDelegate( new TouchDelegate( rect , tbVisible));
            }
        });

        View v = view.findViewById(R.id.label_layout);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tbVisible.toggle();
            }
        });

        llDetails = view.findViewById(R.id.details_layout);
        llDetails.setVisibility(View.GONE);

        viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    public void removeSearchResultView() {
        searchResultView.setVisibility(View.INVISIBLE);
        searchEditText.setText("");
        searchEditText.clearFocus();
    }

    private void initMap() {
        //mGoogleMap.setTrafficEnabled(true);

        // route
        routeLine = mGoogleMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.blue_ribbon))
                .width(16.0f)
            );

        // Start/Stop position

        routeStartMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .anchor(0.15f, 0.5f)
                .visible(false)
                .icon(BitmapDescriptorFactory.fromBitmap(loadBitmapFromView(null, false)))
        );


        updateAssetOverlay(selected_spinner_position);
    }

    private List<AssetItem> getAssetList(String group) {
        List<AssetItem> list = mAssetMap.get(group);
        if (list == null) {
            list = new ArrayList<>();
            mAssetMap.put(group, list);
        }

        return list;
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onAssetClicked(AssetListAdapter adapter, int position) {
        SharedStorage ss = SharedStorage.getInstance();
        ss.currentAsset = ((AssetDataItem)adapter.itemList.get(position)).getAsset();

        resetCurrentItem();

        llInfo.setVisibility(View.GONE);
        if (tbVisible.isChecked())
            tbVisible.setChecked(false);

        searchResultView.setVisibility(View.GONE);
        hideKeyboard(getActivity());
        searchEditText.clearFocus();

        selectedAsset = ss.currentAsset;
        showAssetInfo(true);

        // set center of map
        float zoom = mGoogleMap.getCameraPosition().zoom;
        if (zoom < MAP_ZOOM_DEFAULT) {
            zoom = MAP_ZOOM_DEFAULT;
            saveMapStatus();
        }
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(selectedAsset.lat, selectedAsset.lng), zoom));

        ss.currentAsset = null;
    }

    @Override
    public void onLikeClicked(AssetListAdapter adapter, int position, boolean isfav) {

    }

    @Override
    public void onMonitorChange(AssetListAdapter adapter, int position, boolean enable) {

    }

    @Override
    public void onAssetBellClicked(AssetListAdapter adapter, int position) {

    }

    private void search() {
        mAssetAdapter.getFilter().filter(mKeyword);
    }

    private void setupViewPager(ViewPager viewPager) {
        infoFragment = InfoFragment.newInstance();
        tripsFragment = TripsFragment.newInstance();
        moreFragment = MoreFragment.newInstance();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(getString(R.string.title_tab_info), infoFragment);
        adapter.addFragment(getString(R.string.title_tab_trips), tripsFragment);
        adapter.addFragment(getString(R.string.title_tab_more), moreFragment);
        viewPager.setAdapter(adapter);
    }

    private void updateAssetOverlay(int position) {
        if (getActivity() == null)
            return;

        removeMarkers();

        SharedPreferences pref = getActivity().getSharedPreferences(PREF_MAP, Context.MODE_PRIVATE);
        boolean zoomed = (pref.getFloat(PREF_KEY_ZOOM, 0) != 0);

        List<AssetItem> assets_origin = SharedStorage.getInstance().assets;
        List<AssetItem> assets = new ArrayList<>();
        List<AssetPushItem> asset_push = SharedStorage.getInstance().asset_push;
        int alarm_count = 0;

        for(int index = 0; index < assets_origin.size(); index ++) {
            if(position == 0 || position == 1) {
                assets.add(assets_origin.get(index));
            } else if(position == 2) {
                if(assets_origin.get(index).alarm == 1) {
                    assets.add(assets_origin.get(index));
                }
//                alarm_count = 0;
//                if(asset_push != null) {
//                    for(int iindex = 0; iindex < asset_push.size(); iindex ++) {
//                        if(asset_push.get(iindex).getAssets_id().equals(assets_origin.get(index).id)) {
//                            if(asset_push.get(iindex).getAlarm_id_list() != null) {
//                                for(int iiindex = 0; iiindex < asset_push.get(iindex).getAlarm_id_list().size(); iiindex ++) {
//                                    if( asset_push.get(iindex).getAlarm_id_list().get(iiindex) > assets_origin.get(index).a_last_ack_alarm_id) {
//                                        alarm_count ++;
//                                    }
//                                }
//                                if(alarm_count > 0) {
//                                    assets.add(assets_origin.get(index));
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
            } else if(position == 3) {
                if(assets_origin.get(index).mon == 1) {
                    assets.add(assets_origin.get(index));
                }
            } else if(position == 4) {
                if(assets_origin.get(index).stat == 1) {
                    assets.add(assets_origin.get(index));
                }
            } else if(position == 5) {
                if(assets_origin.get(index).stat == 0) {
                    assets.add(assets_origin.get(index));
                }
            }
        }


        // add assets
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        //ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

        allMarkers.clear();
        AssetItem asset;
        for (int index = 0; index < assets.size(); index ++) {
            asset = assets.get(index);
            if (!zoomed && Utils.isValidLatLng(asset.lat, asset.lng)) {
                builder.include(new LatLng(asset.lat, asset.lng));
            }

            // add marker of asset
            Marker marker;
            marker = mGoogleMap.addMarker(new MarkerOptions()
                    .anchor(0.15f, 0.5f)
                    .position(new LatLng(asset.lat, asset.lng))
                    .icon(BitmapDescriptorFactory.fromBitmap(loadBitmapFromView(asset, false)))
                    .title(asset.name)
            );
            marker.setTag(asset);

            if (asset.stat == AssetItem.STATUS_MOVE) {
//                marker.setRotation(asset.direction);

                double x = Math.sin(-asset.direction * Math.PI / 180) * 0.5 + 0.5;
                double y = -(Math.cos(-asset.direction * Math.PI / 180) * 0.5 - 0.5);
                marker.setInfoWindowAnchor((float)x, (float)y);
            }

            // show info window of selected asset
            if (selectedAsset != null && asset.id.equals(selectedAsset.id)) {
                selectedAsset = asset;
                marker.setZIndex(100);
                selectedMarker = marker;

                Log.d(TAG, String.format("ttttttttt%s, %f, %f", asset.id, asset.lat, asset.lng));
            } else {
                //mMapView.getOverlays().add(marker);
                mMarkers.add(marker);
            }

            allMarkers.add(marker);
        }

        if (selectedMarker != null) {

            selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(loadBitmapFromView((AssetItem)selectedMarker.getTag(), true)));

            mMarkers.add(selectedMarker);

//            selectedMarker.showInfoWindow();
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(selectedMarker.getPosition()));
        } else {
//            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(mMarkers.get(0).getPosition()));
            if(first_open) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMarkers.get(0).getPosition().latitude, mMarkers.get(0).getPosition().longitude), MAP_ZOOM_DEFAULT));
                llInfo.setVisibility(View.GONE);
                first_open = false;
            }
        }

        // when not zoomed, zoom to bounding box
        if (!zoomed) {
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
            mGoogleMap.moveCamera(cu);
        }

        if(searchfilterSpinner.getSelectedItemPosition() == 0) {
            searchfilterAdapter.setSelection(1);
        }


        SharedPreferences pref_group_distance = getActivity().getSharedPreferences(Constant.GROUP_DISTANCE, Context.MODE_PRIVATE);
        String group_distance_str = pref_group_distance.getString(Constant.GROUP_DISTANCE, "");
        Double group_distance =  0.0;
        if(!group_distance_str.isEmpty()) {
            group_distance = Double.parseDouble(group_distance_str);
        }


        if(current_map_zoom < MAP_ZOOM_CRITIRIA && !tbSetting.isChecked()) {
            newMarkers.clear();
            for (int i = 0; i < removedMakerListGroup.size(); i++) {
                removedMakerListGroup.get(i).clear();
            }
            removedMakerListGroup.clear();

            for (int i = 0; i < mMarkers.size(); i++) {
                if (!mMarkers.get(i).equals(selectedMarker)) {
                    newMarkers.add(mMarkers.get(i));
                }
            }

            int index = 0, subindex = 0;
            while (index < newMarkers.size()) {
                List<Marker> removeMarkerListSub = new ArrayList<>();
                subindex = index + 1;
                while (subindex < newMarkers.size()) {
                    Point p1 = mGoogleMap.getProjection().toScreenLocation(newMarkers.get(index).getPosition());
                    Point p2 = mGoogleMap.getProjection().toScreenLocation(newMarkers.get(subindex).getPosition());
                    Double distance = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
                    if (!Double.isNaN(distance) && distance < group_distance) {
                        removeMarkerListSub.add(newMarkers.get(subindex));
                        newMarkers.remove(subindex);
                    } else {
                        subindex++;
                    }
                }
                if (removeMarkerListSub.size() > 0) {
                    removeMarkerListSub.add(newMarkers.get(index));
                    newMarkers.remove(index);
                    removedMakerListGroup.add(removeMarkerListSub);
                } else {
                    index++;
                }
            }


            Double newLat = 0.0, newLng = 0.0;
            for (int i = 0; i < removedMakerListGroup.size(); i++) {
                newLat = 0.0;
                newLng = 0.0;
                for (int j = 0; j < removedMakerListGroup.get(i).size(); j++) {
                    newLat += removedMakerListGroup.get(i).get(j).getPosition().latitude;
                    newLng += removedMakerListGroup.get(i).get(j).getPosition().longitude;
                    for (int mMarkerIndex = 0; mMarkerIndex < mMarkers.size(); mMarkerIndex++) {
                        if (mMarkers.get(mMarkerIndex).getPosition().latitude == removedMakerListGroup.get(i).get(j).getPosition().latitude &&
                                mMarkers.get(mMarkerIndex).getPosition().longitude == removedMakerListGroup.get(i).get(j).getPosition().longitude) {
                            mMarkers.get(mMarkerIndex).remove();
                            break;
                        }
                    }
                }
                newLat = newLat / removedMakerListGroup.get(i).size();
                newLng = newLng / removedMakerListGroup.get(i).size();

                LayoutInflater inflaterMarkerCircle = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflaterMarkerCircle.inflate(R.layout.marker_group_circle, null);
                TextView marker_integrate_countTextView = view.findViewById(R.id.marker_integrate_countTextView);
                marker_integrate_countTextView.setText(String.valueOf(removedMakerListGroup.get(i).size()));
                Bitmap bitmapCircle = null;
                if (view.getMeasuredHeight() <= 0) {
                    view.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    bitmapCircle = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(bitmapCircle);
                    view.layout(0, 0, 150, 150);
                    view.draw(c);
                }
                if (bitmapCircle != null) {
                    integratedMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .position(new LatLng(newLat, newLng))
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmapCircle))
                            .title("Group")
                            .zIndex(100)
                    );
                    integratedMarker.setTag(null);
                    integratedMarkerList.add(integratedMarker);
                }
            }
        }
    }

    private void removeMarkers() {
//        for (Marker m : mMarkers) {
//            m.remove();
//        }
        for (Marker m : allMarkers) {
            m.remove();
        }
        for (Marker m: integratedMarkerList) {
            m.remove();
        }

        mMarkers.clear();
        integratedMarkerList.clear();
    }

    public Bitmap loadBitmapFromView(AssetItem assetItem, boolean selected) {
        inflaterLayoutter = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        view = inflaterLayoutter.inflate(R.layout.marker_label_stop, null);
        if(assetItem != null && assetItem.name != null) {
            switch (assetItem.stat) {
                case AssetItem.STATUS_IDLE:
                    view = inflaterLayoutter.inflate(R.layout.marker_label_move_idle, null);
                    break;
                case AssetItem.STATUS_MOVE:
                    view = assetItem.isAlarm() ? inflaterLayoutter.inflate(R.layout.marker_label_move_alert, null) : inflaterLayoutter.inflate(R.layout.marker_label_move, null);
                    break;
                case AssetItem.STATUS_STOP:
                    view = assetItem.isAlarm() ? inflaterLayoutter.inflate(R.layout.marker_label_stop_alert, null) : inflaterLayoutter.inflate(R.layout.marker_label_stop, null);

                    break;
            }

            if (assetItem.stat == AssetItem.STATUS_MOVE) {
                ImageView markerImageView = view.findViewById(R.id.markerImageView);
                markerImageView.setRotation(assetItem.direction);
            }
            TextView asset_nameTextView = view.findViewById(R.id.asset_name);
            asset_nameTextView.setText(assetItem.name);
            if(show_marker_label) {
                asset_nameTextView.setVisibility(View.VISIBLE);
            } else {
                asset_nameTextView.setVisibility(View.INVISIBLE);
            }
            Typeface face;
            if(selected) {
                face = ResourcesCompat.getFont(getContext(), R.font.roboto_bold);
            } else {
                face = ResourcesCompat.getFont(getContext(), R.font.roboto_medium);
            }
            asset_nameTextView.setTypeface(face);
        }

        if (view.getMeasuredHeight() <= 0) {
            view.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.draw(c);

            return b;

        }

        return null;
    }

    private int getMarkerDrawable(AssetItem asset) {
        int resId = R.drawable.ic_marker_stop;

        switch (asset.stat) {
            case AssetItem.STATUS_IDLE:
                resId = R.drawable.ic_marker_move_idle;
                break;
            case AssetItem.STATUS_MOVE:
                resId = asset.isAlarm()? R.drawable.ic_marker_move_alert : R.drawable.ic_marker_move;
                break;
            case AssetItem.STATUS_STOP:
                resId = asset.isAlarm()? R.drawable.ic_marker_stop_alert : R.drawable.ic_marker_stop;

                break;
        }

        return resId;
    }

    private void startUpdateTimer() {
        if (mUpdateTimer != null)
            return;

        int interval = AppParam.getInstance().updateInterval;

        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getAssets();
            }
        }, interval * 1000, interval * 1000);
    }

    private void stopUpdateTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    private void saveMapStatus() {
        SharedPreferences pref = getActivity().getSharedPreferences(PREF_MAP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        CameraPosition cp = mGoogleMap.getCameraPosition();
        editor.putFloat(PREF_KEY_ZOOM, cp.zoom);
        editor.putLong(PREF_KEY_CENTER_LAT, Double.doubleToRawLongBits(cp.target.latitude));
        editor.putLong(PREF_KEY_CENTER_LNG, Double.doubleToRawLongBits(cp.target.longitude));

        editor.commit();
    }

    /**
     * show asset info at the bottom
     */
    private void showAssetInfo(boolean forceShow) {

        getRoute();

        if (forceShow) {
            llInfo.setVisibility(View.VISIBLE);
        }

        tvAssetName.setText(selectedAsset.name);
        tvAssetUser.setText(selectedAsset.user);
        tvAssetAddr.setText(selectedAsset.address);

        if(selectedAsset.stat == 1) {// moving state
            tvAssetStatus.setText(selectedAsset.getOriginStatus() + " " +selectedAsset.speed + " km/h");
        } else {
            tvAssetStatus.setText(selectedAsset.getOriginStatus());
        }
        tvAssetUpdate.setText(selectedAsset.timestamp);
    }

    private void showRouteOverlay() {
        // Route
        List<LatLng> geoPoints = new ArrayList<>();
        for (RouteItem route : mRoute) {
            geoPoints.add(new LatLng(route.lat, route.lng));
        }

        //add your points here
        if (routeLine != null) {
            routeLine.setPoints(geoPoints);
            routeLine.setVisible(true);
            if(selectedMarker != null) {
                routeLine.setZIndex(selectedMarker.getZIndex());
            }
        }

        if (mRoute.size() > 0) {
            // Start/Stop position
            RouteItem startItem = mRoute.get(mRoute.size() - 1);
            RouteItem stopItem = mRoute.get(0);

            // sync
            if (selectedMarker != null) {
                selectedMarker.setPosition(new LatLng(stopItem.lat, stopItem.lng));
                Log.d(TAG, String.format("Route endpoint: %f, %f", stopItem.lat, stopItem.lng));
            }

//            routeStartMarker.setPosition(new LatLng(startItem.lat, startItem.lng));
//            routeStartMarker.setVisible(true);
        }

        //mMapView.invalidate();
    }

    /**
     * Reset selected status
     */
    private void resetCurrentItem() {
        if (selectedMarker != null) {
            selectedMarker.hideInfoWindow();
            selectedMarker = null;
        }
        selectedAsset = null;
        routeLine.setVisible(false);
        routeStartMarker.setVisible(false);
    }

    /**
     * get asset
     */
    private void getAssets() {
        try {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    anim_increase.end();
                    anim_increase.cancel();
                }
            });

            SessionManager sm = SessionManager.getInstance();

            JSONObject jo = new JSONObject();
            jo.put("uid", sm.username);
            jo.put("token", sm.token);

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
                                anim_increase.setDuration(AppParam.getInstance().updateInterval * 1000 - 3000);
                                anim_increase.start();

                                SharedStorage.getInstance().connection_lost = true;

                                SharedPreferences pref = getContext().getSharedPreferences(PREF_ASSET, PRIVATE_MODE);
                                if (pref != null) {

                                    Gson gson = new Gson();
                                    String json = pref.getString(KEY_ASSETS_LIST, "");
                                    Type type = new TypeToken<List<AssetItem>>() {
                                    }.getType();
                                    List<AssetItem> assets_list_saved = gson.fromJson(json, type);

                                    List<AssetItem> assets = SharedStorage.getInstance().assets;
                                    if (assets != null && assets.size() > 0) {

                                    } else {
                                        assets.clear();
                                        mAssetMap.clear();
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
                                        updateAssetOverlay(selected_spinner_position);

                                    }
                                    connect_list_layout.setVisibility(View.VISIBLE);
                                    searchEditText.setEnabled(false);
                                    Log.d("1111111111", String.valueOf(assets.size()));
                                }
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if(getActivity() != null) {
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

//                                Toast.makeText(getContext(), response.message(), Toast.LENGTH_SHORT).show();

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
                        getActivity().runOnUiThread(new Runnable() { public void run() {
                            try {

                                connect_list_layout.setVisibility(View.GONE);
                                searchEditText.setEnabled(true);

                                JSONObject jr = new JSONObject(resp);

                                List<AssetItem> assets = SharedStorage.getInstance().assets;
                                assets.clear();

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

                                    updateAssetOverlay(selected_spinner_position);

                                    if (selectedAsset != null) {
                                        showAssetInfo(false);
                                    }

                                    Log.d(TAG, "Assets count: " + assets.size());
                                } else {
                                    String message = jr.getString("message");

//                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Exception", e);
                            }
                        }});
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    /**
     * get route
     */
    public void getRoute() {
        try{
            if (routeCall != null && routeCall.isExecuted())
                routeCall.cancel();

            SessionManager sm = SessionManager.getInstance();

            JSONObject jo = new JSONObject();
            jo.put("uid", sm.username);
            jo.put("token", sm.token);
            jo.put("route_type", "1");  // Map
            jo.put("a_id", selectedAsset.id);

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.ROUTE)
                    .post(reqBody)
                    .build();

            Log.d(TAG, request.url().toString());
            Log.d(TAG, Utils.requestBodyToString(request));

            routeCall = client.newCall(request);
            routeCall.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Exception", e);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() { public void run() {
                        }});
                    }
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {

                    ResponseBody responseBody = response.body();

                    if (!response.isSuccessful()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() { public void run() {
                                //hideProgress();

//                                Toast.makeText(getContext(), response.message(), Toast.LENGTH_SHORT).show();

                                // session out
                                SessionManager.getInstance().signOut(getContext());
                                Utils.gotoSignIn(getContext());
                            }});
                        }
                        Log.e(TAG, response.toString());
                        return;
                    }

                    final String resp = responseBody.string();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() { public void run() {
                            try {
                                JSONObject jr = new JSONObject(resp);

                                mRoute.clear();


                                if (jr.getString("status").equals("1")) {
                                    JSONArray ja = jr.getJSONArray("position");

                                    for (int i = 0; i < ja.length(); ++i) {
                                        JSONObject jo = ja.getJSONObject(i);

                                        RouteItem item = RouteItem.parse(jo);
                                        mRoute.add(item);
                                    }

                                    showRouteOverlay();

                                    Log.d(TAG, "Route count: " + mRoute.size());
                                } else {
                                    String message = jr.getString("message");

//                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Exception", e);
                            }

                            //hideProgress();
                        }});
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
//                if (cameraPosition.zoom > MAP_ZOOM_CRITIRIA) {
//                    if (mGoogleMap.getMapType() != GoogleMap.MAP_TYPE_HYBRID) {
//                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//                    }
//                } else if (mGoogleMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL) {
//                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//                }
            }
        });


        SharedStorage ss = SharedStorage.getInstance();

        if (ss.currentAsset != null) {
            selectedAsset = ss.currentAsset;

            // set center of map
            float zoom = mGoogleMap.getCameraPosition().zoom;
            if (zoom < MAP_ZOOM_DEFAULT) {
                zoom = MAP_ZOOM_DEFAULT;
                saveMapStatus();
            }

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(selectedAsset.lat, selectedAsset.lng), zoom));

            showAssetInfo(true);

            ss.currentAsset = null;

        } else {
            SharedPreferences prefs = getActivity().getSharedPreferences(PREF_MAP, Context.MODE_PRIVATE);
            if (prefs.getFloat(PREF_KEY_ZOOM, 0) != 0) {
                float zoomLevel = prefs.getFloat(PREF_KEY_ZOOM, 0.0f);
                double lat = Double.longBitsToDouble(prefs.getLong(PREF_KEY_CENTER_LAT, Double.doubleToLongBits(0.0)));
                double lng = Double.longBitsToDouble(prefs.getLong(PREF_KEY_CENTER_LNG, Double.doubleToLongBits(0.0)));

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), (float) zoomLevel));
            }
        }

        initMap();

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(marker.getTag() != null) {
                    routeLine.setVisible(false);
                    routeStartMarker.setVisible(false);

//                marker.showInfoWindow();
                    selectedAsset = (AssetItem) marker.getTag();
                    showAssetInfo(true);


                    for (int index = 0; index < allMarkers.size(); index++) {
                        if (allMarkers.get(index).equals(selectedMarker)) {
                            if(allMarkers.get(index).getTag() != null) {
                                allMarkers.get(index).setIcon(BitmapDescriptorFactory.fromBitmap(loadBitmapFromView((AssetItem) allMarkers.get(index).getTag(), false)));
                            }
                            break;
                        }
                    }
                    selectedMarker = marker;
                    selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(loadBitmapFromView(selectedAsset, true)));
                }
                return true;
            }
        });

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition pos) {
                current_map_zoom = pos.zoom;
                if(prev_cameraPosZoom != pos.zoom) {
                    if(Math.abs(prev_cameraPosZoom - pos.zoom) > 0.1) {
                        prev_cameraPosZoom = pos.zoom;
                        updateAssetOverlay(selected_spinner_position);
                    }
                }
            }
        });

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                resetCurrentItem();

                llInfo.setVisibility(View.GONE);
                if (tbVisible.isChecked())
                    tbVisible.setChecked(false);
            }
        });
    }

    public interface Callback {
        void onAssetListItemSelected();
    }

}
