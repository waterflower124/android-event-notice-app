package com.netglue.ngtmobile;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.AssetItem;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MarkerWithLabel extends Marker {
    private Paint textPaint = null;
    private Paint bgPaint = null;

    private String mLabel = null;
    private AssetItem asset;

    private float paddingH;
    private float paddingV;
    private float marginRight;
    private float round;

    public MarkerWithLabel(MapView mapView, AssetItem asset) {
        super(mapView);

        this.asset = asset;

        paddingH = Utils.convertDpToPixel(14f, mapView.getContext());
        paddingV = Utils.convertDpToPixel(10f, mapView.getContext());
        marginRight = Utils.convertDpToPixel(40f, mapView.getContext());
        round = Utils.convertDpToPixel(10f, mapView.getContext());

        textPaint = new Paint();
        textPaint.setColor(0xFF303030);
        textPaint.setTextSize(Utils.convertDpToPixel(15f, mapView.getContext()));
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.LEFT);

        bgPaint = new Paint();
        bgPaint.setColor(0xFFBAF25C);
    }

    public void draw(final Canvas c, final MapView osmv, boolean shadow) {
        draw(c, osmv);
    }

    public void draw(final Canvas c, final MapView osmv) {
        super.draw(c, osmv, false);

        Point p = this.mPositionPixels;  // already provisioned by Marker

        Rect bounds = new Rect();
        textPaint.getTextBounds(asset.name, 0, asset.name.length(), bounds);

        int start = bounds.left;
        int width = bounds.width();
        int height = bounds.height();

        bounds.left += (p.x - start - width - marginRight);
        bounds.top += (p.y + height / 2);
        bounds.right += (p.x - start - width - marginRight);
        bounds.bottom += (p.y + height / 2);

        bgPaint.setColor(getBgColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            c.drawRoundRect((float) bounds.left - paddingH, (float)bounds.top - paddingV,
                    (float)bounds.right + paddingH, (float)bounds.bottom + paddingV, round, round, bgPaint);
        } else {
            c.drawRect((float) bounds.left - paddingH, (float)bounds.top - paddingV,
                    (float)bounds.right + paddingH, (float)bounds.bottom + paddingV, bgPaint);
        }

        textPaint.setColor(getColor());
        c.drawText(asset.name, bounds.left, (bounds.top + bounds.bottom + height) / 2, textPaint);
    }

    private int getColor() {
        int color = 0xFF303030;
        if (asset.stat == AssetItem.STATUS_MOVE && asset.isAlarm())
            color = 0xFFFFFFFF;

        return color;
    }

    private int getBgColor() {
        int color = 0xFFFFC700;

        if (asset.stat == AssetItem.STATUS_MOVE && asset.isAlarm())
            color = 0xFFFF0000;

        if (asset.stat == AssetItem.STATUS_MOVE)
            color = 0xFFBAF25C;

        if (asset.stat == AssetItem.STATUS_STOP)
            color = 0xFFFFC700;

        return color;
    }
}
