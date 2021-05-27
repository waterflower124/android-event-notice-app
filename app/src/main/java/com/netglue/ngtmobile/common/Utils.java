package com.netglue.ngtmobile.common;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.netglue.ngtmobile.MainActivity;
import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.SignInActivity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Request;
import okio.Buffer;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String requestBodyToString(final Request request){
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    /**
     * time string to updateAt with time zone
     * @param dateString
     * @param pattern
     * @param tz
     * @return
     */
    public static Date string2Date(String dateString, String pattern, TimeZone tz) {
        Date date = null;

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(tz);
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Exception", e);
        }

        return date;
    }

    public static String getDateTimeString(Date date, String pattern) {
        if (date == null)
            return "-";

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static String millisecondsToTime(Context context, long miliseconds) {
        long totalMins = miliseconds / 1000 / 60;
        int hours = (int)(totalMins / 60);
        int mins = (int)(totalMins - hours * 60);

        String travelTime = "";

        if (hours > 0) {
            travelTime += String.format("%d%s", hours, context.getString(R.string.unit_hour));
            travelTime += " ";
        }
        if (mins > 0) {
            travelTime += String.format("%d%s", mins, context.getString(R.string.unit_minute));
        }

        if (hours == 0 && mins == 0) {
            int seconds = (int)(miliseconds / 1000);
            travelTime += String.format("%d%s", seconds, context.getString(R.string.unit_second));
        }

        if (travelTime.isEmpty()) {
            travelTime = "-";
        }

        return travelTime;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


    public static void gotoMain(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void gotoSignIn(Context context) {
        Intent intent = new Intent(context, SignInActivity.class);
        context.startActivity(intent);
    }

    public static boolean isValidLatLng(double lat, double lng){
        if (lat < -90 || lat > 90) {
            return false;
        }

        if (lng < -180 || lng > 180) {
            return false;
        }

        return true;
    }

    public static void alertMsg(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(context.getString(android.R.string.ok), null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }

        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    public static void showConfirmDialog(Context context, String title, String message, boolean checkboxEnabled,
                                         final DialogInterface.OnClickListener positiveListener,
                                         final DialogInterface.OnCancelListener negativeListener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm);

        TextView tv = dialog.findViewById(R.id.title);
        tv.setText(title);

        tv = dialog.findViewById(R.id.message);
        tv.setText(message);

        // Do not show this again
        final CheckBox cb = dialog.findViewById(R.id.check);
        cb.setVisibility(checkboxEnabled? View.VISIBLE : View.GONE);

        // yes
        View v = dialog.findViewById(R.id.yes);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (positiveListener != null)
                    positiveListener.onClick(dialog, cb.isChecked()? 1 : 0);
            }
        });

        // no
        v = dialog.findViewById(R.id.no);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (negativeListener != null)
                    negativeListener.onCancel(dialog);
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        dialog.show();
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
