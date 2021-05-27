package com.netglue.ngtmobile.model;

public class NotificationToneItem {

    private static final String TAG = NotificationToneItem.class.getSimpleName();

    private String tone_name;
    private boolean selected;
    private String tone_uri;

    public NotificationToneItem(String tone_name, boolean selected, String tone_uri) {
        this.tone_name = tone_name;
        this.selected = selected;
        this.tone_uri = tone_uri;
    }

    public String getTone_name() {
        return tone_name;
    }

    public void setTone_name(String tone_name) {
        this.tone_name = tone_name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getTone_uri() {
        return tone_uri;
    }

    public void setTone_uri(String tone_uri) {
        this.tone_uri = tone_uri;
    }
}
