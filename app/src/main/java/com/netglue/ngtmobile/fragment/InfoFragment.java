package com.netglue.ngtmobile.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.adapter.AssetListAdapter;
import com.netglue.ngtmobile.adapter.AssetListItem;
import com.netglue.ngtmobile.adapter.CardListAdapter;
import com.netglue.ngtmobile.common.RestApi;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.CardItem;
import com.netglue.ngtmobile.model.SessionManager;
import com.netglue.ngtmobile.model.SharedStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoFragment extends Fragment {
    public static final String TAG = InfoFragment.class.getSimpleName();

    private List<CardItem> mCards = new ArrayList<>();
    private CardListAdapter mCardAdapter;

    private ProgressBar mProgressBar;

    public InfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InfoFragment newInstance() {
        InfoFragment fragment = new InfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        // Asset list
        RecyclerView rv = view.findViewById(R.id.list);
        mCardAdapter = new CardListAdapter(getContext(), mCards);
        rv.setAdapter(mCardAdapter);
        rv.setNestedScrollingEnabled(false);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rv.setLayoutManager(layoutManager);

        mProgressBar = view.findViewById(R.id.progress_bar);
    }

    /**
     * get asset
     */
    public void getAsset(String assetId) {
        try{
            SessionManager sm = SessionManager.getInstance();

            JSONObject jo = new JSONObject();
            jo.put("uid", sm.username);
            jo.put("token", sm.token);
            jo.put("a_id", assetId);

            if (mProgressBar != null)
                mProgressBar.setVisibility(View.VISIBLE);

            OkHttpClient client = new OkHttpClient.Builder().build();

            RequestBody reqBody = RequestBody.create(RestApi.JSON, jo.toString());
            Request request = new Request.Builder()
                    .url(RestApi.ASSET)
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

                                mCards.clear();

                                if (jr.getString("status").equals("1")) {
                                    JSONArray ja = jr.getJSONArray("assets");
                                    if (ja.length() > 0) {
                                        JSONArray jc = ja.optJSONObject(0).getJSONArray("asset_cards_value");

                                        for (int i = 0; i < jc.length(); ++i) {
                                            JSONObject jo = jc.getJSONObject(i);

                                            mCards.add(CardItem.parse(jo));
                                        }

                                        mCardAdapter.notifyDataSetChanged();

                                        Log.d(TAG, "Card count: " + mCards.size());
                                    }

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
