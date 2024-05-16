package com.rma.sensors;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class GraphFragment extends Fragment {
    private static final int[] COLORS = {Color.RED, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN};
    private static final String[] TITLES = {"x", "y", "z", "k", "i", "j"};
    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private GraphView graph;
    private List<LineGraphSeries<DataPoint>> seriesList = new ArrayList<>();
    private double graphLastXValue = 0d;
    private float[] currentSensorReadings = new float[0];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);

        SensorViewModel sensorViewModel = new ViewModelProvider(requireActivity()).get(SensorViewModel.class);
        sensorViewModel.getData().observe(getViewLifecycleOwner(), newData -> {
            currentSensorReadings = newData;
        });

        graph = (GraphView) rootView.findViewById(R.id.graph);

        for (int i = 0; i < currentSensorReadings.length; i++) {
            seriesList.add(new LineGraphSeries<>());
            seriesList.get(i).setColor(COLORS[i % COLORS.length]);
            seriesList.get(i).setTitle(TITLES[i % TITLES.length]);
            graph.addSeries(seriesList.get(i));
        }

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-10);
        graph.getViewport().setMaxY(10);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(100);

        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setPadding(50);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTimer = new Runnable() {
            @Override
            public void run() {
                graphLastXValue += 1d;

                for (int i = 0; i < seriesList.size(); i++) {
                    seriesList.get(i).appendData(new DataPoint(graphLastXValue, currentSensorReadings[i]), true, 100);
                }

                if(graphLastXValue > 1000) {
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