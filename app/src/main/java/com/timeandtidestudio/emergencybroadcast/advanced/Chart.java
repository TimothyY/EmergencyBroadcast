/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package com.timeandtidestudio.emergencybroadcast.advanced;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.timeandtidestudio.emergencybroadcast.Controller.sensor.SensorData;
import com.timeandtidestudio.emergencybroadcast.R;
import com.timeandtidestudio.emergencybroadcast.achartengine.ChartFactory;
import com.timeandtidestudio.emergencybroadcast.achartengine.GraphicalView;
import com.timeandtidestudio.emergencybroadcast.achartengine.model.TimeSeries;
import com.timeandtidestudio.emergencybroadcast.achartengine.model.XYMultipleSeriesDataset;
import com.timeandtidestudio.emergencybroadcast.achartengine.renderer.XYMultipleSeriesRenderer;
import com.timeandtidestudio.emergencybroadcast.achartengine.renderer.XYSeriesRenderer;
import com.timeandtidestudio.emergencybroadcast.achartengine.tools.ZoomEvent;
import com.timeandtidestudio.emergencybroadcast.achartengine.tools.ZoomListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;


/**
 * Created by samyboy89 on 29/01/15.
 */
public class Chart implements View.OnClickListener {

    private final Activity mActivity;

    private static Random RAND = new Random();
    private static final float RATIO = 0.618033988749895f;

    private static final String TIME = "H:mm:ss";

    private static final String TAG = "Chart";

    private static final int TEN_SEC = 10000;
    private static final int TWO_SEC = 2000;

    private GraphicalView mChartView;
    private XYMultipleSeriesRenderer mRenderer;
    private XYMultipleSeriesDataset mDataset;
    private HashMap<String, TimeSeries> mSeries;
    private double mYAxisMin = Double.MAX_VALUE;
    private double mYAxisMax = Double.MIN_VALUE;
    private double mZoomLevel = 1;
    private int mYAxisPadding = 10;

    private final ZoomListener mZoomListener = new ZoomListener() {
        @Override
        public void zoomReset() {
            mZoomLevel = 1;
            scrollGraph(new Date().getTime());
        }

        @Override
        public void zoomApplied(final ZoomEvent event) {
            if (event.isZoomIn()) {
                mZoomLevel /= 2;
            }
            else {
                mZoomLevel *= 2;
            }
            scrollGraph(new Date().getTime());
        }
    };

    public Chart(Activity activity, ViewGroup chartView) {
        mActivity = activity;
        EventBus.getDefault().register(this);

        mSeries = new HashMap<>();
        mDataset = new XYMultipleSeriesDataset();
        mRenderer = new XYMultipleSeriesRenderer();

        mRenderer.setLabelsColor(Color.LTGRAY);
        mRenderer.setAxesColor(Color.LTGRAY);
        mRenderer.setGridColor(Color.rgb(136, 136, 136));
        mRenderer.setBackgroundColor(Color.BLACK);
        mRenderer.setApplyBackgroundColor(true);

        mRenderer.setLegendTextSize(20);
        mRenderer.setLabelsTextSize(20);
        mRenderer.setPointSize(8);
        mRenderer.setMargins(new int[] { 60, 60, 60, 60 });

        mRenderer.setFitLegend(true);
        mRenderer.setShowGrid(true);
        mRenderer.setZoomEnabled(true);
        mRenderer.setExternalZoomEnabled(true);
        mRenderer.setAntialiasing(true);
        mRenderer.setInScroll(true);

        // onCreateView
        if (Configuration.ORIENTATION_PORTRAIT == mActivity.getResources().getConfiguration().orientation) {
            mYAxisPadding = 9;
            mRenderer.setYLabels(15);
        }

        mChartView = ChartFactory.getTimeChartView(mActivity, mDataset, mRenderer, TIME);
        mChartView.addZoomListener(mZoomListener, true, false);
        chartView.addView(mChartView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mActivity.findViewById(R.id.zoom_in).setOnClickListener(this);
        mActivity.findViewById(R.id.zoom_out).setOnClickListener(this);
        mActivity.findViewById(R.id.zoom_reset).setOnClickListener(this);
    }

    /*
    public void onEvent(SensorAlgorithmPack pack) {
        HashMap<SensorSession, List<SensorData>> sensorData = pack.getSensorData();
        for (SensorSession session : sensorData.keySet()) {
            TimeSeries series;
            if (mSeries.containsKey(session.getId())) {
                series = mSeries.get(session.getId());
            } else {
                series = new TimeSeries(session.getId());
                mSeries.put(session.getId(), series);
                mDataset.addSeries(series);
                mRenderer.addSeriesRenderer(getSeriesRenderer(randomColor()));
            }

            for (SensorData data : sensorData.get(session)) {
                    Double x = Double.longBitsToDouble(data.getTimeCaptured());
                    Double y = (double) data.getSensorData().getValues()[0];
                    series.add(x, y);
            }
        }
        // scrollGraph(data.getTimeCaptured());

        mChartView.repaint();
    }*/


    /*
    public void onEvent(SensorAlgorithmPack pack) {
        HashMap<SensorSession, List<SensorData>> sensorData = pack.getSensorData();
        for (SensorSession session : sensorData.keySet()) {
            TimeSeries series;
            boolean insert = false;

            if (mSeries.containsKey(session.getId())) {
                series = mSeries.get(session.getId());
            } else {
                insert = true;
                series = new TimeSeries(session.getId());
            }

            for (SensorData data : sensorData.get(session)) {
                series.add(data.getTimeCaptured(), data.getSensorData().getValues()[0]);
            }

            if (insert) {
                mSeries.put(session.getId(), series);
                mDataset.addSeries(series);
                mRenderer.addSeriesRenderer(getSeriesRenderer(randomColor()));
            }
        }
        // scrollGraph(data.getTimeCaptured());

        mChartView.repaint();
    }*/

    @Subscribe
    public void onEvent(SensorData data) {
//        Log.d(TAG, data.toString());
        if (mSeries.containsKey(data.getSensorSession().getId())) {
            TimeSeries series = mSeries.get(data.getSensorSession().getId());
            series.add(data.getTimeCaptured(), data.getSensorData().getValues()[0]);
        } else {
            TimeSeries series = new TimeSeries(data.getSensorSession().getId());
            series.add(data.getTimeCaptured(), data.getSensorData().getValues()[0]);
            mSeries.put(data.getSensorSession().getId(), series);
            mDataset.addSeries(series);
            mRenderer.addSeriesRenderer(getSeriesRenderer(randomColor()));
        }
//        scrollGraph(data.getTimeCaptured()); // timestamps of data differ (bt comm), results in jittery graph
        scrollGraph(System.currentTimeMillis());
        mChartView.repaint();
    }



    private void scrollGraph(final long time) {
        final double[] limits = new double[] { time - TEN_SEC * mZoomLevel, time + TWO_SEC * mZoomLevel, mYAxisMin - mYAxisPadding,
                mYAxisMax + mYAxisPadding };
        mRenderer.setRange(limits);
    }

    private XYSeriesRenderer getSeriesRenderer(final int color) {
        final XYSeriesRenderer r = new XYSeriesRenderer();
        r.setDisplayChartValues(false);
        r.setPointStrokeWidth(0);
        r.setColor(color);
        r.setFillPoints(false);
        r.setLineWidth(4);
        return r;
    }


    private static int randomColor() {
        final float hue = (RAND.nextInt(360) + RATIO);
        return Color.HSVToColor(new float[] { hue, 0.8f, 0.9f });
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.zoom_in:
                mChartView.zoomIn();
                break;

            case R.id.zoom_out:
                mChartView.zoomOut();
                break;

            case R.id.zoom_reset:
                mChartView.zoomReset();
                break;

            default:
                break;
        }

    }
}
