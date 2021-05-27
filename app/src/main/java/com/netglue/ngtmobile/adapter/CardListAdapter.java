package com.netglue.ngtmobile.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.holder.CardAHolder;
import com.netglue.ngtmobile.holder.CardBHolder;
import com.netglue.ngtmobile.holder.CardCHolder;
import com.netglue.ngtmobile.holder.CardDHolder;
import com.netglue.ngtmobile.model.AppParam;
import com.netglue.ngtmobile.model.CardItem;
import com.netglue.ngtmobile.model.CardTypeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ??? on 21-Jan-18.
 */

public class CardListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = CardListAdapter.class.getSimpleName();

    public static final int TYPE_NONE = 0;
    public static final int TYPE_A = 1;
    public static final int TYPE_B = 2;
    public static final int TYPE_C = 3;
    public static final int TYPE_D = 4;
    public static final int TYPE_E = 5;

    private Context context;
    public List<CardItem> itemList;

    private Callback mListener;

    public CardListAdapter(Context context, List<CardItem> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_A: {
                View itemView = inflater.inflate(R.layout.item_asset_card_a, parent, false);
                return new CardAHolder(itemView);
            }
            case TYPE_B: {
                View itemView = inflater.inflate(R.layout.item_asset_card_b, parent, false);
                return new CardBHolder(itemView);
            }
            case TYPE_C: {
                View itemView = inflater.inflate(R.layout.item_asset_card_c, parent, false);
                return new CardCHolder(itemView);
            }
            case TYPE_D: {
                View itemView = inflater.inflate(R.layout.item_asset_card_d, parent, false);
                return new CardDHolder(itemView);
            }
            case TYPE_E: {
                View itemView = inflater.inflate(R.layout.item_asset_card_e, parent, false);
                return new CardAHolder(itemView);
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        CardItem card = itemList.get(position);
        CardTypeItem cardType = AppParam.getInstance().getCardType(card.no);

        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_A: {
                CardAHolder holder = (CardAHolder) viewHolder;
                holder.tvTitle.setText(cardType.title);

                holder.tvItem1.setText(card.item1);
                holder.tvItem1.setTextColor(card.item1_color);

                holder.tvNote.setText(card.note);
                break;
            }
            case TYPE_B: {
                CardBHolder holder = (CardBHolder) viewHolder;
                holder.tvTitle.setText(cardType.title);

                holder.tvItem1Caption.setText(cardType.item1_caption);
                holder.tvItem1.setText(card.item1);
                holder.tvItem1.setTextColor(card.item1_color);

                holder.tvItem2Caption.setText(cardType.item2_caption);
                holder.tvItem2.setText(card.item2);
                holder.tvItem2.setTextColor(card.item2_color);

                holder.tvNote.setText(card.note);
                break;
            }
            case TYPE_C: {
                CardCHolder holder = (CardCHolder) viewHolder;
                holder.tvTitle.setText(cardType.title);

                holder.tvItem1Caption.setText(cardType.item1_caption);
                holder.tvItem1.setText(card.item1);
                holder.tvItem1.setTextColor(card.item1_color);

                holder.tvItem2.setText(card.item2);
                holder.tvItem2.setTextColor(card.item2_color);
                if (!card.item2.isEmpty())
                    holder.csbValue2.setValue(Float.parseFloat(card.item2.replaceAll("[\\D]","")));

                holder.tvNote.setText(card.note);
                break;
            }
            case TYPE_D: {
                CardDHolder holder = (CardDHolder) viewHolder;
                holder.tvTitle.setText(cardType.title);

                holder.tvNote.setText(card.note);

                holder.tvItem1Caption.setText(cardType.item1_caption);
                holder.tvItem1.setText(card.item1);
                holder.tvItem1.setTextColor(card.item1_color);

                holder.tvItem2Caption.setText(cardType.item2_caption);
                holder.tvItem2.setText(card.item2);
                holder.tvItem2.setTextColor(card.item2_color);

                holder.tvItem3Caption.setText(cardType.item3_caption);
                holder.tvItem3.setText(card.item3);
                holder.tvItem3.setTextColor(card.item3_color);

                List<PieEntry> pieEntries = new ArrayList<>();
                if (!card.item1.isEmpty())
                    pieEntries.add(new PieEntry(Float.parseFloat(card.item1.replaceAll("[\\D]","")), ""));
                if (!card.item2.isEmpty())
                    pieEntries.add(new PieEntry(Float.parseFloat(card.item2.replaceAll("[\\D]","")), ""));
                if (!card.item3.isEmpty())
                    pieEntries.add(new PieEntry(Float.parseFloat(card.item3.replaceAll("[\\D]","")), ""));

                PieDataSet dataSet = new PieDataSet(pieEntries, "");
                dataSet.setColors(new int[] { card.item1_color, card.item2_color, card.item3_color});
                dataSet.setDrawValues(false);
                PieData data = new PieData(dataSet);

                holder.chart.setData(data);
                holder.chart.invalidate();

                break;
            }
            case TYPE_E: {
                CardAHolder holder = (CardAHolder) viewHolder;
                holder.tvTitle.setText(cardType.title);

                holder.tvItem1.setText(Html.fromHtml(String.format("<a href=\"%s\">%s</a>", card.item2, card.item1)));
                holder.tvItem1.setTextColor(card.item1_color);

                holder.tvNote.setText(card.note);
                break;
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        int cardNo = itemList.get(position).no;
        CardTypeItem cardType = AppParam.getInstance().getCardType(cardNo);

        int viewType = TYPE_NONE;

        switch (cardType.type) {
            case "A":
                viewType = TYPE_A;
                break;

            case "B":
                viewType = TYPE_B;
                break;

            case "C":
                viewType = TYPE_C;
                break;

            case "D":
                viewType = TYPE_D;
                break;

            case "E":
                viewType = TYPE_E;
                break;
        }

        return viewType;
    }

    public void setCallback(Callback listener) {
        mListener = listener;
    }

    public interface Callback {
        void onAssetClicked(CardListAdapter adapter, int position);
    }
}