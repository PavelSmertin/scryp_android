package com.start.crypto.android;

import android.content.Intent;
import android.os.Bundle;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;

public class PieActivity extends BaseActivity {

    public static final String EXTRA_PIE_DATA = "pie data";

    @BindView(R.id.chart) PieChart mPieChart;

    @Override
    protected void setupLayout() {
        setContentView(R.layout.pie_activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        HashMap<String, Double> hashMap = (HashMap<String, Double>)intent.getSerializableExtra(EXTRA_PIE_DATA);


        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : hashMap.entrySet()) {
            double value = entry.getValue();
            entries.add(new PieEntry((float)value, entry.getKey()));
        }

        PieDataSet dataset = new PieDataSet(entries, "# of Coins");
        dataset.setColors(ColorTemplate.MATERIAL_COLORS);


        PieData data = new PieData(dataset);
        mPieChart.setData(data);
        Description description = new Description();
        description.setText("");
        mPieChart.setDescription(description);

        mPieChart.animateY(1000);
        mPieChart.setDrawEntryLabels(false);

    }


}
