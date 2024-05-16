package com.rma.sensors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.hardware.Sensor;
import android.widget.Button;

import com.rma.sensors.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MeasurementUnits", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(String.valueOf(Sensor.TYPE_ACCELEROMETER), "m/s²");
        editor.apply();

        openSensorActivityWhenButtonIsPressed(viewBinding.allSensorsButton, Sensor.TYPE_ALL);
        // izdvoji u uzasebni activity
        openSensorActivityWhenButtonIsPressed(viewBinding.gravityButton, Sensor.TYPE_GRAVITY);
        openSensorActivityWhenButtonIsPressed(viewBinding.accelerometerButton, Sensor.TYPE_ACCELEROMETER);
        openSensorActivityWhenButtonIsPressed(viewBinding.linearAccelerationButton, Sensor.TYPE_LINEAR_ACCELERATION);
        openSensorActivityWhenButtonIsPressed(viewBinding.gyroscopeButton, Sensor.TYPE_GYROSCOPE);
        openSensorActivityWhenButtonIsPressed(viewBinding.rotationVectorButton, Sensor.TYPE_ROTATION_VECTOR);
        openSensorActivityWhenButtonIsPressed(viewBinding.significantMotionButton, Sensor.TYPE_SIGNIFICANT_MOTION);
        openSensorActivityWhenButtonIsPressed(viewBinding.stepCounterButton, Sensor.TYPE_STEP_COUNTER);
        openSensorActivityWhenButtonIsPressed(viewBinding.stepDetectorButton, Sensor.TYPE_STEP_DETECTOR);
        openSensorActivityWhenButtonIsPressed(viewBinding.magneticFieldButton, Sensor.TYPE_MAGNETIC_FIELD);
        openSensorActivityWhenButtonIsPressed(viewBinding.orientationButton, Sensor.TYPE_ORIENTATION);
        openSensorActivityWhenButtonIsPressed(viewBinding.proximityButton, Sensor.TYPE_PROXIMITY);
        openSensorActivityWhenButtonIsPressed(viewBinding.lightButton, Sensor.TYPE_LIGHT);
        openSensorActivityWhenButtonIsPressed(viewBinding.temperatureButton, Sensor.TYPE_AMBIENT_TEMPERATURE);
        openSensorActivityWhenButtonIsPressed(viewBinding.pressureButton, Sensor.TYPE_PRESSURE);
        openSensorActivityWhenButtonIsPressed(viewBinding.humidityButton, Sensor.TYPE_RELATIVE_HUMIDITY);
    }


    // disable buttons if sensor doesn't exist
    // force portrait
    // smart low pass filtering?
    // izračun prave frekvencije
    // drop down izbornik možda

    private void openSensorActivityWhenButtonIsPressed(Button button, int sensorType) {
        if(button == null) return;

        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, SensorActivity.class);
            intent.putExtra(SensorActivity.SENSOR_TYPE_KEY, sensorType);
            startActivity(intent);
        });
    }
}