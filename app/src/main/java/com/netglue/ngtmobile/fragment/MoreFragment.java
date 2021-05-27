package com.netglue.ngtmobile.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.adapter.CardListAdapter;
import com.netglue.ngtmobile.adapter.MoreListAdapter;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.CardItem;
import com.netglue.ngtmobile.model.MoreItem;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MoreFragment extends Fragment {

    public static final String TAG = MoreFragment.class.getSimpleName();

    private List<MoreItem> mMoreItems = new ArrayList<>();
    private MoreListAdapter mMoreAdapter;


    public MoreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MoreFragment newInstance() {
        MoreFragment fragment = new MoreFragment();
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
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {

        RecyclerView rv = view.findViewById(R.id.more_list);
        mMoreAdapter = new MoreListAdapter(getContext(), mMoreItems);
        rv.setAdapter(mMoreAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(layoutManager);

    }

    public void getMoreItems(AssetItem selectedAsset) {
        try {
            mMoreItems.clear();
            JSONArray moreJsonArray = selectedAsset.more_items;
            for (int index = 0; index < moreJsonArray.length(); index++) {
                MoreItem moreItem = MoreItem.parse(moreJsonArray.getJSONObject(index));
                mMoreItems.add(moreItem);
            }
            mMoreAdapter.notifyDataSetChanged();
            Log.d("454545", String.valueOf(mMoreItems.size()));
        } catch(Exception e) {

        }
    }
}
