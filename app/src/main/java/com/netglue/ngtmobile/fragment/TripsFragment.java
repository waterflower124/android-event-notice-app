package com.netglue.ngtmobile.fragment;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.netglue.ngtmobile.PermissionActivity;
import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.RouteActivity;
import com.netglue.ngtmobile.adapter.CardListAdapter;
import com.netglue.ngtmobile.adapter.TripListAdapter;
import com.netglue.ngtmobile.common.RestApi;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.CardItem;
import com.netglue.ngtmobile.model.SessionManager;
import com.netglue.ngtmobile.model.SharedStorage;
import com.netglue.ngtmobile.model.TripItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripsFragment extends Fragment implements TripListAdapter.Callback {
    public static final String TAG = TripsFragment.class.getSimpleName();

    private List<TripItem> mTrips = new ArrayList<>();
    private TripListAdapter mTripAdapter;

    private ProgressBar mProgressBar;

    private Date mTripDate = new Date();
    private Button mDatePicker;

    private String mAssetId;

    public TripsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TripsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TripsFragment newInstance() {
        TripsFragment fragment = new TripsFragment();
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
        View view = inflater.inflate(R.layout.fragment_trips, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mDatePicker = view.findViewById(R.id.date_picker);
        mDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        ImageButton ib = view.findViewById(R.id.prev_date);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTripDate.setTime(mTripDate.getTime() - 24 * 3600 * 1000);
                updateTripDate();

                getTrip(mAssetId);
            }
        });
        ib = view.findViewById(R.id.next_date);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTripDate.after(new Date()))
                    return;

                mTripDate.setTime(mTripDate.getTime() + 24 * 3600 * 1000);
                updateTripDate();

                getTrip(mAssetId);
            }
        });

        updateTripDate();

        // Asset list
        RecyclerView rv = view.findViewById(R.id.list);
        mTripAdapter = new TripListAdapter(getContext(), mTrips);
        rv.setAdapter(mTripAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(layoutManager);
        mTripAdapter.setCallback(this);

        mProgressBar = view.findViewById(R.id.progress_bar);
    }

    private void updateTripDate() {
        mDatePicker.setText(Utils.getDateTimeString(mTripDate, "dd/MM/yyyy"));
    }

    /**
     *
     */
    private void showDatePickerDialog() {
        Calendar cldr = Calendar.getInstance();
        cldr.setTime(mTripDate);

        // DatePickerDialog
        DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar cldr = Calendar.getInstance();
                cldr.set(year, month, dayOfMonth);

                mTripDate = cldr.getTime();
                updateTripDate();

                getTrip(mAssetId);
            }
        }, cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH), cldr.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(new Date().getTime());
        dialog.show();
    }

    @Override
    public void onTripClicked(int position) {
        SharedStorage ss = SharedStorage.getInstance();
        ss.currentTrip = mTrips.get(position);

        Intent intent = new Intent(getContext(), RouteActivity.class);
        intent.putExtra("asset", mAssetId);
        intent.putExtra("position", position);
        intent.putExtra("trip_counts", mTrips.size());
        startActivity(intent);
    }

    /**
     * get asset
     */
    public void getTrip(String assetId) {
        mAssetId = assetId;

        try{
            SessionManager sm = SessionManager.getInstance();

            JSONObject jo = new JSONObject();
            jo.put("uid", sm.username);
            jo.put("token", sm.token);
            jo.put("a_id", assetId);
            jo.put("ts_date", Utils.getDateTimeString(mTripDate, "yyyy-MM-dd"));

            if (mProgressBar != null)
                mProgressBar.setVisibility(View.VISIBLE);

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.TRIP)
                    .post(reqBody)
                    .build();

            Log.d(TAG, request.url().toString());
            Log.d(TAG, Utils.requestBodyToString(request));

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Exception", e);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() { public void run() {
                            mProgressBar.setVisibility(View.GONE);
                        }});
                    }
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {

                    ResponseBody responseBody = response.body();

                    if (!response.isSuccessful()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() { public void run() {
                                mProgressBar.setVisibility(View.GONE);
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
                        getActivity().runOnUiThread(new Runnable() { public void run() {
                            try {
                                JSONObject jr = new JSONObject(resp);

                                mTrips.clear();

                                if (jr.getString("status").equals("1")) {
                                    JSONArray ja = jr.getJSONArray("trip_list");

                                    for (int i = 0; i < ja.length(); ++i) {
                                        JSONObject jo = ja.getJSONObject(i);

                                        TripItem item = TripItem.parse(jo);
                                        mTrips.add(item);
                                    }

                                    SharedStorage.getInstance().trips_list = mTrips;

                                    mTripAdapter.notifyDataSetChanged();

                                    Log.d(TAG, "Trip count: " + mTrips.size());
                                } else {
                                    String message = jr.getString("message");

                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Exception", e);
                            }

                            mProgressBar.setVisibility(View.GONE);
                        }});
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

}
