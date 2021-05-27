package com.netglue.ngtmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.netglue.ngtmobile.common.RestApi;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.AppParam;
import com.netglue.ngtmobile.model.RouteItem;
import com.netglue.ngtmobile.model.SessionManager;
import com.netglue.ngtmobile.model.SharedStorage;
import com.netglue.ngtmobile.model.TripItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = RouteActivity.class.getSimpleName();

    //private MapView mMapView;
    private GoogleMap mGoogleMap;

    private TripItem mTrip;
    public List<TripItem> listTips = new ArrayList<>();

    private String mAssetId;

    private List<RouteItem> mRoute = new ArrayList<>();

    private ToggleButton mPlayToggle;
    private Marker motionMarker;
    private Timer mMotionTimer;

    private int motionIndex = 0;

    private ProgressDialog mProgressDlg;

    private LinearLayout prevLayout, nextLayout;

    private int trip_index = 0, trips_count;

    TextView tripindexTextView, tvDate, tvStop_time, tvStop_aa, tvDist, tvDuration, tvStart_time, tvStart_loc, tvStop_loc, tvDriver;

    View labelLayoutView; // motion marker label layout
    RelativeLayout.LayoutParams layoutParams;
    int deviceWidth, deviceHeight;
    TextView markerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        mAssetId = getIntent().getStringExtra("asset");
        trip_index = getIntent().getIntExtra("position", 0);
        trips_count = getIntent().getIntExtra("trip_counts", 0);

        initView();
    }

    private void initView() {

        labelLayoutView = findViewById(R.id.motion_markerLayout);
        layoutParams = new RelativeLayout.LayoutParams(300, 70);
        markerTextView = findViewById(R.id.markerTextView);

        listTips = SharedStorage.getInstance().trips_list;
        // back button
        ImageButton ib = findViewById(R.id.back);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /*
        mMapView = findViewById(R.id.mapView);
        mMapView.setMultiTouchControls(true);
        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mMapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                getRoute();
            }
        });
        // Create a custom tile source
        final ITileSource tileSource = new XYTileSource( "HOT", 1, 20, 256, ".png",
                new String[] {AppParam.getInstance().sys_tile_server[0]},"© OpenStreetMap contributors");
        mMapView.setTileSource(tileSource);
        */

        // Play button
        mPlayToggle = findViewById(R.id.play);
        mPlayToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    playMotion();
                } else {
                    stopMotion();
                }
            }
        });

        // trip info
        mTrip = SharedStorage.getInstance().currentTrip;

        // trip index textview
        tripindexTextView = findViewById(R.id.countTextView);
        tvDate = findViewById(R.id.date);
        tvStop_time = findViewById(R.id.stop_time);
        tvStop_aa = findViewById(R.id.stop_aa);
        tvDist = findViewById(R.id.dist);
        tvDuration = findViewById(R.id.duration);
        tvStart_time = findViewById(R.id.start_time);
        tvStop_loc = findViewById(R.id.stop_loc);
        tvDriver = findViewById(R.id.driver);
        tvStart_loc = findViewById(R.id.start_loc);


        // initialize values
        initValue();

        // Progress dialog
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setCancelable(false);
        mProgressDlg.setMessage(getString(R.string.progress_load));

        prevLayout = findViewById(R.id.prevLayout);
        nextLayout = findViewById(R.id.nextLayout);
        prevLayout.setVisibility(View.VISIBLE);
        nextLayout.setVisibility(View.VISIBLE);

        prevLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trip_index > 0) {
                    stopMotion();
                    motionIndex = 0;
                    mGoogleMap.clear();
                    labelLayoutView.setVisibility(View.GONE);
                    trip_index --;
                    mTrip = listTips.get(trip_index);
                    initValue();
                    getRoute();
                    mPlayToggle.setChecked(false);
                }
            }
        });

        nextLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trip_index + 1 < trips_count) {
                    stopMotion();
                    motionIndex = 0;
                    mGoogleMap.clear();
                    labelLayoutView.setVisibility(View.GONE);
                    trip_index ++;
                    mTrip = listTips.get(trip_index);
                    initValue();
                    getRoute();
                    mPlayToggle.setChecked(false);
                }
            }
        });

    }

    private void initValue() {
        tripindexTextView.setText(String.valueOf(trips_count - trip_index));
        tvDate.setText(Utils.getDateTimeString(mTrip.stop_time, "dd/MM/yyyy"));
        tvStop_time.setText(mTrip.getStopTime());
        tvStop_aa.setText(mTrip.getStopAA());
        tvDist.setText(String.format("%s km", mTrip.dist));
        tvDuration.setText(Utils.millisecondsToTime(this, mTrip.duration * 1000));
        tvStart_time.setText(mTrip.getStartTime());
        tvStop_loc.setText(mTrip.stop_loc);
        tvDriver.setText(mTrip.driver);
        tvStart_loc.setText(mTrip.start_loc);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
    }

    private void showProgress() {
        if (!mProgressDlg.isShowing())
            mProgressDlg.show();
    }

    private void hideProgress() {
        if (mProgressDlg != null && mProgressDlg.isShowing())
            mProgressDlg.dismiss();
    }

    private void showRouteOverlay() {
        if (mGoogleMap == null)
            return;
        mGoogleMap.clear();

        // Route
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        List<LatLng> geoPoints = new ArrayList<>();
        for (RouteItem route : mRoute) {
            geoPoints.add(new LatLng(route.lat, route.lng));

            builder.include(new LatLng(route.lat, route.lng));
        }

        if(mRoute.size() == 0) {
            return;
        }

        // zoom to bounding box
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mGoogleMap.moveCamera(cu);
        /*
        BoundingBox boundingBox = new BoundingBox(maxLat, maxLong, minLat, minLong);
        mMapView.zoomToBoundingBox(boundingBox, false);
         */

        //add your points here
        Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.blue_ribbon))
                .width(16.0f));
        line.setPoints(geoPoints);

        /*
        Polyline line = new Polyline();   //see note below!
        line.setPoints(geoPoints);
        line.setColor(0xFF00BB00);
        line.setWidth(8);
        line.getPaint().setStrokeCap(Paint.Cap.ROUND);
        line.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        mMapView.getOverlayManager().add(line);
        */

        // Start/Stop position
        RouteItem startItem = mRoute.get(mRoute.size() - 1);
        RouteItem stopItem = mRoute.get(0);

        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(startItem.lat, startItem.lng))
                .anchor(0.5f, 0.5f)
                .icon(Utils.bitmapDescriptorFromVector(this, R.drawable.ic_loc_start)));

        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(stopItem.lat, stopItem.lng))
                .anchor(0.5f, 0.5f)
                .icon(Utils.bitmapDescriptorFromVector(this, R.drawable.ic_loc_stop)));

        motionMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(stopItem.lat, stopItem.lng))
                .anchor(0.5f, 0.5f)
                .icon(Utils.bitmapDescriptorFromVector(this, R.drawable.ic_motion))
                .zIndex(100)
                .visible(false));

        /*
        Marker marker = new Marker(mMapView);
        marker.setPosition(new GeoPoint(startItem.lat, startItem.lng));
        marker.setIcon(getResources().getDrawable(R.drawable.ic_loc_start));
        marker.setAnchor(0.5f, 0.5f);
        marker.setInfoWindow(null);
        mMapView.getOverlays().add(marker);

        marker = new Marker(mMapView);
        marker.setPosition(new GeoPoint(stopItem.lat, stopItem.lng));
        marker.setIcon(getResources().getDrawable(R.drawable.ic_loc_stop));
        marker.setAnchor(0.5f, 0.5f);
        marker.setInfoWindow(null);
        mMapView.getOverlays().add(marker);

        // motion marker
        motionMarker = new Marker(mMapView);
        motionMarker.setVisible(false);
        motionMarker.setIcon(getResources().getDrawable(R.drawable.ic_motion));
        motionMarker.setAnchor(0.5f, 0.5f);
        mMapView.getOverlays().add(motionMarker);
        */
    }

    /**
     * play motion
     */
    private void playMotion() {

        if(motionIndex == 0) {
            motionIndex = mRoute.size() - 1;
        }

        mMotionTimer = new Timer();
        mMotionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() { public void run() {
                    if (motionIndex < 0) {
                        mPlayToggle.setChecked(false);
                        return;
                    }
                    if(!motionMarker.isVisible()) {
                        motionMarker.setVisible(true);
                        labelLayoutView.setVisibility(View.VISIBLE);
                    }
                    RouteItem route = mRoute.get(motionIndex);
                    motionMarker.setPosition(new LatLng(route.lat, route.lng));

                    Point markerPosition = mGoogleMap.getProjection().toScreenLocation(motionMarker.getPosition());

                    layoutParams.setMargins(markerPosition.x - 300 - 30, markerPosition.y - 30, deviceWidth - (markerPosition.x - 30), deviceHeight - (markerPosition.y - 30 + 150));
                    markerTextView.setText(convert_date(route.timestamp) + " - " + route.speed + " kmph");
                    labelLayoutView.setLayoutParams(layoutParams);

                    if (!isVisible(motionMarker)) {
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(motionMarker.getPosition()));
                    }

                    --motionIndex;
                }});
            }
        }, 600, 600);
    }

    private String convert_date(String strDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date convertedDate = new Date();
        String finalDateString = "";
        try {
            convertedDate = dateFormat.parse(strDate);
            SimpleDateFormat sdfnewformat = new SimpleDateFormat("HH:mm");
            finalDateString = sdfnewformat.format(convertedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return finalDateString;
    }

    /**
     * stop motion
     */
    private void stopMotion(){
//        labelLayoutView.setVisibility(View.GONE);
        if(mMotionTimer != null) {
            mMotionTimer.cancel();
        }

//        motionMarker.setVisible(false);
    }

    /**
     * get route
     */
    public void getRoute() {
        try{
            SessionManager sm = SessionManager.getInstance();

            JSONObject jo = new JSONObject();
            jo.put("uid", sm.username);
            jo.put("token", sm.token);
            jo.put("route_type", "2");  // Trip
            jo.put("a_id", mAssetId);
            jo.put("t_id", mTrip.id);

            showProgress();

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.ROUTE)
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

                    ResponseBody responseBody = response.body();

                    if (!response.isSuccessful()) {
                        runOnUiThread(new Runnable() { public void run() {
                            hideProgress();

                            Toast.makeText(RouteActivity.this, response.message(), Toast.LENGTH_SHORT).show();

                            // session out
                            SessionManager.getInstance().signOut(RouteActivity.this);
                            Utils.gotoSignIn(RouteActivity.this);
                            finish();
                        }});
                        Log.e(TAG, response.toString());
                        return;
                    }

                    final String resp = responseBody.string();

                    runOnUiThread(new Runnable() { public void run() {
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

                                Toast.makeText(RouteActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Exception", e);
                        }

                        hideProgress();
                    }});
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        getRoute();
    }

    private boolean isVisible(Marker m) {
        LatLngBounds bounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
        return bounds.contains(m.getPosition());
    }
}
