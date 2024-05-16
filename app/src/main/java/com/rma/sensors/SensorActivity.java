package com.rma.sensors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.rma.sensors.databinding.ActivityMainBinding;
import com.rma.sensors.databinding.ActivitySensorBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    public static final String SENSOR_TYPE_KEY = "Tip senzora";
    private static final int DEFAULT_TYPE = Sensor.TYPE_ACCELEROMETER;
    // pročitaj svaku drugu vrijednost koju vrati onSensorChanged metoda
    // cilj toga je samo smanjiti prebrzo variranje vrijednosti koje prikazujemo
    private static final int SAMPLING_FREQUENCY = 5;
    private int counter = SAMPLING_FREQUENCY;
    private int sensorType;
    private SensorManager sensorManager;
    private Sensor sensor;
    private List<Sensor> sensorsList;
    private ActivitySensorBinding viewBinding;
    private SensorViewModel sensorViewModel;
    private String measurementUnit;


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

            if(sensorType == Sensor.TYPE_ALL) {
                // popis svih dostupnih senzora (TYPE_ALL)
                sensorsList = sensorManager.getSensorList(sensorType);
                previewSensorsList();
            } else {
                sensor = sensorManager.getDefaultSensor(sensorType);
                previewSensor(savedInstanceState);
            }

        } else {
            finish();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container, GraphFragment.class, null)
                    //ovdje uguravamo SimpleFragment u fragment_containter_view
                    .commit();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MeasurementUnits", MODE_PRIVATE);
        measurementUnit = sharedPreferences.getString(String.valueOf(sensorType), "");
    }

    private void previewSensor(Bundle savedInstanceState) {
        //provjera imamo li specifičan senzor
        //if a device has more than one sensor of a given type, one of the sensors must be
        // designated as the default sensor
        if (sensor == null){
            Toast.makeText(this, "Senzor ne postoji na uređaju!", Toast.LENGTH_LONG).show();
        } else if(savedInstanceState == null){
            int numSensors = sensorManager.getSensorList(sensorType).size();
            Toast.makeText(this, "Imamo " + numSensors + " senzor(a) tog tipa.", Toast.LENGTH_LONG).show();
        }
    }

    private void previewSensorsList() {
        StringBuilder sensorsText = new StringBuilder();
        for (Sensor sensor : sensorsList) {
            sensorsText.append("{ ");
            sensorsText.append(sensor.toString());
            sensorsText.append(sensor.getStringType());
            sensorsText.append(" }, \n");
        }
        viewBinding.sensorReadings.setText(sensorsText);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        counter++;
        if(counter > SAMPLING_FREQUENCY || sensorType == Sensor.TYPE_PROXIMITY || sensorType == Sensor.TYPE_SIGNIFICANT_MOTION) {
            displaySensorData(event); // provjeri ima li ovo smisla tj blokira li svejedno
            counter = 0;
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
        for (float value : event.values) {
            sensorReadings.append(String.format(Locale.getDefault(),"%.5f", value)).append(measurementUnit).append("\n");
        }
        viewBinding.sensorReadings.setText(sensorReadings);

        sensorViewModel.setData(event.values);
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
        sensorManager.registerListener(this, sensor, 5000000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); //this -> SensorEventListener (implementiramo ga)
    }

    @Nullable
    public static String getCurrentTimeStamp(){
        try {
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
            return dateFormat.format(new Date());
        } catch (Exception e) {
            Log.e("SENSORS_APP", "Timestamp error: " + e.getMessage());
            return null;
        }
    }
}