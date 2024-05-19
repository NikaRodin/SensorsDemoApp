package com.rma.sensors;

import android.content.Intent;
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

    // disable buttons if sensor doesn't exist
    // force portrait
    // smart low pass filtering?
    // izračun prave frekvencije
    // drop down izbornik možda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding viewBinding = ActivityMainBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewBinding.allSensorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DisplayAllSensorsActivity.class);
            startActivity(intent);
        });
        
        openSensorActivity(viewBinding.gravityButton, Sensor.TYPE_GRAVITY);
        openSensorActivity(viewBinding.accelerometerButton, Sensor.TYPE_ACCELEROMETER);
        openSensorActivity(viewBinding.linearAccelerationButton, Sensor.TYPE_LINEAR_ACCELERATION);
        openSensorActivity(viewBinding.gyroscopeButton, Sensor.TYPE_GYROSCOPE);
        openSensorActivity(viewBinding.rotationVectorButton, Sensor.TYPE_ROTATION_VECTOR);
        openSensorActivity(viewBinding.significantMotionButton, Sensor.TYPE_SIGNIFICANT_MOTION);
        openSensorActivity(viewBinding.stepCounterButton, Sensor.TYPE_STEP_COUNTER);
        openSensorActivity(viewBinding.stepDetectorButton, Sensor.TYPE_STEP_DETECTOR);
        openSensorActivity(viewBinding.magneticFieldButton, Sensor.TYPE_MAGNETIC_FIELD);
        openSensorActivity(viewBinding.orientationButton, Sensor.TYPE_ORIENTATION);
        openSensorActivity(viewBinding.proximityButton, Sensor.TYPE_PROXIMITY);
        openSensorActivity(viewBinding.lightButton, Sensor.TYPE_LIGHT);
        openSensorActivity(viewBinding.temperatureButton, Sensor.TYPE_AMBIENT_TEMPERATURE);
        openSensorActivity(viewBinding.pressureButton, Sensor.TYPE_PRESSURE);
        openSensorActivity(viewBinding.humidityButton, Sensor.TYPE_RELATIVE_HUMIDITY);
    }

    private void openSensorActivity(Button button, int sensorType) {
        if(button == null) return;

        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, SensorActivity.class);
            intent.putExtra(SensorActivity.SENSOR_TYPE_KEY, sensorType);
            startActivity(intent);
        });
    }
}