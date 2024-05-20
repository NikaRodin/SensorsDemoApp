package com.rma.sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.rma.sensors.databinding.ActivitySensorBinding;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "SensorActivity";
    public static final String SENSOR_TYPE_KEY = "Tip senzora";
    private static final int DEFAULT_TYPE = Sensor.TYPE_ACCELEROMETER;
    private static final int DISPLAY_FREQUENCY = 5;
    private final int suggestedDelay = SensorManager.SENSOR_DELAY_NORMAL;
    private int displayCounter = DISPLAY_FREQUENCY;
    private int sampleCounter = 0;
    private long lastTimestamp = 0;
    private double totalInterval = 0;
    private ActivitySensorBinding viewBinding;
    private SensorViewModel sensorViewModel;
    private SensorManager sensorManager;
    private Sensor sensor;
    private int sensorType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivitySensorBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Intent intent = getIntent();
        if (intent != null) {
            sensorType = intent.getIntExtra(SENSOR_TYPE_KEY, DEFAULT_TYPE);
            sensor = sensorManager.getDefaultSensor(sensorType);
            displaySensorInfoDetails(savedInstanceState);
            displayGraph(savedInstanceState);
        } else {
            Log.d(TAG, "Missing intent");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, suggestedDelay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); //this -> SensorEventListener
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        displayCounter++;
        if(displayCounter > DISPLAY_FREQUENCY || sensorType == Sensor.TYPE_PROXIMITY) {
            displaySensorData(event);
            sensorViewModel.setData(event.values);
            displayCounter = 0;
        }
        calculateFrequency(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy < 0 || accuracy > 3) accuracy = 4;

        String[] level = new String[]{"unreliable", "low", "medium", "high", "error"};
        String accuracyText = "Current accuracy: " + level[accuracy] + "\n(Last change: " + getCurrentTimeStamp() + ")";
        viewBinding.sensorAccuracy.setText(accuracyText);
    }
    
    private void displaySensorInfoDetails(Bundle savedInstanceState) {
        if (sensor == null){
            viewBinding.sensorNotAvailable.setVisibility(View.VISIBLE);
            viewBinding.sensorAvailable.setVisibility(View.GONE);
            
        } else {
            viewBinding.sensorNotAvailable.setVisibility(View.GONE);
            viewBinding.sensorAvailable.setVisibility(View.VISIBLE);

            viewBinding.sensorName.setText(sensor.getName());
            viewBinding.sensorInfo.setText(getSensorInfo(sensor));

            if (savedInstanceState == null) {
                int numSensors = sensorManager.getSensorList(sensorType).size();
                if (numSensors > 1) {
                    String msg = "Found " + numSensors + " sensors of this type. Displaying default sensor.";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private static StringBuilder getSensorInfo(Sensor sensor) {
        StringBuilder sensorInfo = new StringBuilder();
        sensorInfo.append("Type: ").append(sensor.getStringType()).append("\n");
        sensorInfo.append("Vendor: ").append(sensor.getVendor()).append("\n");
        sensorInfo.append("Version: ").append(sensor.getVersion()).append("\n");
        sensorInfo.append("Max range: ").append(sensor.getMaximumRange()).append("\n");
        sensorInfo.append("Power: ").append(sensor.getPower()).append("\n");
        sensorInfo.append("Resolution: ").append(sensor.getResolution()).append("\n");
        sensorInfo.append("Max delay: ").append(sensor.getMaxDelay()).append("\n");
        sensorInfo.append("Min delay: ").append(sensor.getMinDelay()).append("\n");
        return sensorInfo;
    }

    private void displaySensorData(SensorEvent event) {
        StringBuilder sensorReadings = new StringBuilder();
        for (int i = 0; i < event.values.length; i++) {
            if(event.values.length > 1)
                sensorReadings.append(GlobalConstants.getTitle(i)).append(": ");
            sensorReadings.append(String.format(Locale.getDefault(),"%.5f", event.values[i])).append(" ");
            sensorReadings.append(GlobalConstants.getMeasurementUnit(sensorType)).append("\n");
        }
        viewBinding.sensorReadings.setText(sensorReadings);
    }

    private void calculateFrequency(SensorEvent event){
        long currentTimestamp = event.timestamp;
        double suggestedFrequency = GlobalConstants.getFrequency(suggestedDelay);

        if (lastTimestamp != 0) {
            long interval = currentTimestamp - lastTimestamp;
            totalInterval += interval;
            sampleCounter++;
        }
        lastTimestamp = currentTimestamp;

        if (sampleCounter >= 100) {
            double averageInterval = totalInterval/sampleCounter;
            double actualFrequency = 1e9/averageInterval;

            if(suggestedFrequency != -1)
                viewBinding.sensorFrequency.setText(
                    String.format(Locale.getDefault(),"Suggested frequency: %.2f Hz\n", suggestedFrequency));
            else
                viewBinding.sensorFrequency.setText("Suggested frequency: max\n");

            viewBinding.sensorFrequency.append(
                    String.format(Locale.getDefault(),"Actual frequency: %.2f Hz", actualFrequency));

            sampleCounter = 0;
            totalInterval = 0;
        }
    }

    public static String getCurrentTimeStamp(){
        try {
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                                                                    DateFormat.DEFAULT,
                                                                    Locale.getDefault());
            return dateFormat.format(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Timestamp error: " + e.getMessage());
            return "?";
        }
    }

    private void displayGraph(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(viewBinding.fragmentContainer.getId(), GraphFragment.class, getSensorRangeArgs())
                    .commit();
        }


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.unregisterListener(this);




    }

    private Bundle getSensorRangeArgs() {
        Bundle args = null;

        if(sensor != null) {
            args = new Bundle();
            float maxRange, minRange;

            switch(sensorType) {
                case(Sensor.TYPE_LIGHT):
                    maxRange = 30000;
                    minRange = 0;
                    break;

                case(Sensor.TYPE_MAGNETIC_FIELD):
                    maxRange = 100;
                    minRange = -100;
                    break;

                default:
                    maxRange = sensor.getMaximumRange();
                    minRange = -sensor.getMaximumRange();
                    break;
            }

            args.putFloat(GraphFragment.MAX_RANGE_KEY, maxRange);
            args.putFloat(GraphFragment.MIN_RANGE_KEY, minRange);
        }
        return args;
    }
}