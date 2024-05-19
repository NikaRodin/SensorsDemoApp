package com.rma.sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.rma.sensors.databinding.ActivitySensorBinding;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "SensorActivity";
    public static final String SENSOR_TYPE_KEY = "Tip senzora";
    private static final int DEFAULT_TYPE = Sensor.TYPE_ACCELEROMETER;
    private static final int SAMPLING_FREQUENCY = 5; //cilj: smanjiti ludiranje vrijednosti (prikaži svaki 5.)
    private int counter = SAMPLING_FREQUENCY;
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
            previewSensor(savedInstanceState);
            displayGraph(savedInstanceState);
        } else {
            Log.d(TAG, "No intent");
            finish();
        }
    }

    private void previewSensor(Bundle savedInstanceState) {
        if (sensor == null){
            Toast.makeText(this, "Senzor ne postoji na uređaju!", Toast.LENGTH_LONG).show();
        } else if(savedInstanceState == null){
            int numSensors = sensorManager.getSensorList(sensorType).size();
            Toast.makeText(this, "Imamo " + numSensors + " senzor(a) tog tipa.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        counter++;
        if(counter > SAMPLING_FREQUENCY || sensorType == Sensor.TYPE_PROXIMITY || sensorType == Sensor.TYPE_SIGNIFICANT_MOTION) {
            displaySensorData(event); // provjeri ima li ovo smisla tj blokira li svejedno
            counter = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy < 0 || accuracy > 3) accuracy = 4;

        String[] level = new String[]{"unreliable", "low", "medium", "high", "error"};
        String accuracyText = "Current accuracy: " + level[accuracy] + "\n(Last change: " + getCurrentTimeStamp() + ")";
        viewBinding.sensorAccuracy.setText(accuracyText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); //this -> SensorEventListener (implementiramo ga)
    }

    public static String getCurrentTimeStamp(){
        try {
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
            return dateFormat.format(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Timestamp error: " + e.getMessage());
            return "?";
        }
    }

    private void displaySensorData(SensorEvent event) {
        viewBinding.sensorName.setText(sensor.getName());

        StringBuilder sensorInfo = new StringBuilder();
        sensorInfo.append("Type: ").append(sensor.getStringType()).append("\n");
        sensorInfo.append("Vendor: ").append(sensor.getVendor()).append("\n");
        sensorInfo.append("Version: ").append(sensor.getVersion()).append("\n");
        sensorInfo.append("Max range: ").append(sensor.getMaximumRange()).append("\n");
        sensorInfo.append("Power: ").append(sensor.getPower()).append("\n");
        sensorInfo.append("Resolution: ").append(sensor.getResolution()).append("\n");
        sensorInfo.append("Max delay: ").append(sensor.getMaxDelay()).append("\n");
        sensorInfo.append("Min delay: ").append(sensor.getMinDelay()).append("\n");

        viewBinding.sensorInfo.setText(sensorInfo);

        StringBuilder sensorReadings = new StringBuilder();
        for (int i = 0; i < event.values.length; i++) {
            if(event.values.length > 1)
                sensorReadings.append(GlobalConstants.getTitle(i)).append(": ");

            sensorReadings.append(String.format(Locale.getDefault(),"%.5f", event.values[i])).append(" ");
            sensorReadings.append(GlobalConstants.getMeasurementUnit(sensorType)).append("\n");
        }
        viewBinding.sensorReadings.setText(sensorReadings);

        sensorViewModel.setData(event.values);
    }

    private void displayGraph(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(viewBinding.fragmentContainer.getId(), GraphFragment.class, getSensorRangeArgs())
                    .commit();
        }
    }

    private Bundle getSensorRangeArgs() {
        Bundle args = new Bundle();
        float maxRange, minRange;

        if(sensor != null) {

            switch(sensorType) {
                case(Sensor.TYPE_LIGHT):
                    maxRange = sensor.getMaximumRange()/2;
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