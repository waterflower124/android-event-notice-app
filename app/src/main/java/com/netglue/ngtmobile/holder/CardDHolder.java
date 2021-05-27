package com.netglue.ngtmobile.holder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.netglue.ngtmobile.R;


/**
 * Created by ??? on 2018-01-21.
 */

public class CardDHolder extends RecyclerView.ViewHolder {

    public TextView tvTitle;
    public TextView tvItem1;
    public TextView tvItem2;
    public TextView tvItem3;
    public TextView tvItem1Caption;
    public TextView tvItem2Caption;
    public TextView tvItem3Caption;
    public TextView tvNote;

    public PieChart chart;

    public CardDHolder(View view) {
        super(view);

        tvTitle = view.findViewById(R.id.title);

        tvItem1Caption = view.findViewById(R.id.item1_caption);
        tvItem2Caption = view.findViewById(R.id.item2_caption);
        tvItem3Caption = view.findViewById(R.id.item3_caption);

        tvItem1 = view.findViewById(R.id.item1);
        tvItem2 = view.findViewById(R.id.item2);
        tvItem3 = view.findViewById(R.id.item3);

        tvNote = view.findViewById(R.id.note);

        chart = view.findViewById(R.id.chart);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
    }
}
