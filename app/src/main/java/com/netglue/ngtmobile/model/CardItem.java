package com.netglue.ngtmobile.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CardItem {
    private static final String TAG = CardItem.class.getSimpleName();

    public int no;
    public String note;
    public String item1;
    public int item1_color;
    public String item2;
    public int item2_color;
    public String item3;
    public int item3_color;

    public static CardItem parse(JSONObject jdata) {
        CardItem item = new CardItem();
        String value;
        try {
            item.no = Integer.parseInt(jdata.getString("a_card_no"));

            item.note = jdata.getString("a_card_note");

            item.item1 = jdata.getString("a_card_item1");
            value = jdata.getString("a_card_item1_color");
            if (!value.isEmpty())
                item.item1_color = Integer.parseInt(value, 16) | 0xFF000000;

            item.item2 = jdata.getString("a_card_item2");
            value = jdata.getString("a_card_item2_color");
            if (!value.isEmpty())
                item.item2_color = Integer.parseInt(value, 16) | 0xFF000000;

            item.item3 = jdata.getString("a_card_item3");
            value = jdata.getString("a_card_item3_color");
            if (!value.isEmpty())
                item.item3_color = Integer.parseInt(value, 16) | 0xFF000000;
        } catch (JSONException e) {
            Log.e(TAG, "Exception", e);
        }

        return item;
    }

}
