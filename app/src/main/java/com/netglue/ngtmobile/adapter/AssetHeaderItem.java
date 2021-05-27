package com.netglue.ngtmobile.adapter;

import androidx.annotation.NonNull;

public class AssetHeaderItem extends AssetListItem {
    @NonNull
    private String title;

    public AssetHeaderItem(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    // here getters and setters
    // for title and so on, built
    // using date

    @Override
    public int getType() {
        return TYPE_HEADER;
    }
}
