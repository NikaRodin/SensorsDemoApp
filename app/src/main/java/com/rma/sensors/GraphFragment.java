package com.rma.sensors;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

public class GraphFragment extends Fragment {
    public static final String MAX_RANGE_KEY = "max_range";
    public static final String MIN_RANGE_KEY = "min_range";
    private final Handler mHandler = new Handler();
    private final List<LineGraphSeries<DataPoint>> seriesList = new ArrayList<>();
    private Runnable mTimer;
    private GraphView graph;
    private double graphLastXValue = 0d;
    private float maxRange, minRange;
    private boolean areBoundsManual = false;
    private boolean graphInitialized = false;
    private float[] currentSensorReadings = new float[0];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        graph = rootView.findViewById(R.id.graph);

        if (getArguments() != null) {
            maxRange = getArguments().getFloat(MAX_RANGE_KEY);
            minRange = getArguments().getFloat(MIN_RANGE_KEY);
            areBoundsManual = true;
        }

        SensorViewModel sensorViewModel = new ViewModelProvider(requireActivity()).get(SensorViewModel.class);
        sensorViewModel.getData().observe(getViewLifecycleOwner(), newData -> {
            currentSensorReadings = newData;
            if (!graphInitialized)
                initGraph();
        });

        return rootView;
    }

    private void initGraph() {
        for (int i = 0; i < currentSensorReadings.length; i++) {
            seriesList.add(new LineGraphSeries<>());
            seriesList.get(i).setColor(GlobalConstants.getColor(i));
            seriesList.get(i).setTitle(GlobalConstants.getTitle(i));
            graph.addSeries(seriesList.get(i));
        }

        if (areBoundsManual) {
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMaxY(maxRange);
            graph.getViewport().setMinY(minRange);
        }

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(100);

        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setPadding(50);

        if (currentSensorReadings.length > 1) {
            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        }

        graphInitialized = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTimer = new Runnable() {
            @Override
            public void run() {
                graphLastXValue += 1d;

                for (int i = 0; i < seriesList.size(); i++) {
                    seriesList.get(i).appendData(new DataPoint(graphLastXValue, currentSensorReadings[i]),
                                                    true, 100);
                }

                if (graphLastXValue > 10000) {
                    graphLastXValue = 0d;
                    graph.removeAllSeries();

                    for (int i = 0; i < seriesList.size(); i++) {
                        DataPoint[] values = new DataPoint[]{new DataPoint(graphLastXValue, currentSensorReadings[i])};
                        seriesList.get(i).resetData(values);
                        graph.addSeries(seriesList.get(i));
                    }
                }

                mHandler.postDelayed(this, 100);
            }
        };

        mHandler.postDelayed(mTimer, 1000);
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer);
        super.onPause();
    }
}