package com.netglue.ngtmobile.adapter;

import androidx.annotation.NonNull;

import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.AssetPushItem;

public class AssetDataItem extends AssetListItem {
    @NonNull
    private AssetItem asset;
    private AssetPushItem asset_push;

    public AssetDataItem(@NonNull AssetItem data, AssetPushItem asset_push) {
        this.asset = data;
        this.asset_push = asset_push;
    }

    @NonNull
    public AssetItem getAsset() {
        return asset;
    }

    public AssetPushItem getAsset_push() {
        return asset_push;
    }

    // here getters and setters
    // for title and so on, built
    // using event

    @Override
    public int getType() {
        return TYPE_ASSET;
    }
}
