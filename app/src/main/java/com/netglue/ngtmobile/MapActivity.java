package com.netglue.ngtmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.SharedStorage;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity {
    public static final String TAG = MapActivity.class.getSimpleName();

    private MapView mMapView;
    private ItemizedOverlayWithFocus<OverlayItem> mAssetOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    private void initView() {
        mMapView = findViewById(R.id.mapView);
        mMapView.setMultiTouchControls(true);
        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mMapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                showAssetOverlay();
            }
        });
    }

    private void showAssetOverlay() {
        AssetItem asset = SharedStorage.getInstance().currentAsset;

    }
}
