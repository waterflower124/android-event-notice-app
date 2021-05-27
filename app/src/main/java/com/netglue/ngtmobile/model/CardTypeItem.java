package com.netglue.ngtmobile.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CardTypeItem {
    private static final String TAG = CardTypeItem.class.getSimpleName();


    public int no;
    public String type;
    public String title;
    public String item1_caption;
    public String item2_caption;
    public String item3_caption;

    public static CardTypeItem parse(JSONObject jdata) {
        CardTypeItem item = new CardTypeItem();

        try {
            item.no = Integer.parseInt(jdata.getString("a_card_no"));
            item.type = jdata.getString("a_card_type");
            item.title = jdata.getString("a_card_title");
            item.item1_caption = jdata.getString("a_card_item1_caption");
            item.item2_caption = jdata.getString("a_card_item2_caption");
            item.item3_caption = jdata.getString("a_card_item3_caption");
        } catch (JSONException e) {
            Log.e(TAG, "Exception", e);
        }

        return item;
    }

}
