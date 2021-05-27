package com.netglue.ngtmobile.adapter;

public abstract class AssetListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ASSET = 1;

    public static final int FILTER_ALL_TITLE = 0;
    public static final int FILTER_ALL = 1;
    public static final int FILTER_ALERT = 2;
    public static final int FILTER_MONITORED = 3;
    public static final int FILTER_MOVING = 4;
    public static final int FILTER_STOPPED = 5;
    public static final int FILTER_FAVOURITE = 6;
    public static final int FILTER_IDLE = 7;

    public static final int SORT_STATUS = 0;
    public static final int SORT_A_Z = 1;

    abstract public int getType();

}
