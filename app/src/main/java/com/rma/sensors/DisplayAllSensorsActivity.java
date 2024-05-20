package com.rma.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rma.sensors.databinding.ActivityDisplayAllSensorsBinding;

import java.util.List;

public class DisplayAllSensorsActivity extends AppCompatActivity {
    ActivityDisplayAllSensorsBinding viewBinding;
    private List<Sensor> sensorsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         viewBinding = ActivityDisplayAllSensorsBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorsList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        previewSensorsList();
    }

    private void previewSensorsList() {
        StringBuilder sensorsText = new StringBuilder();
        for (Sensor sensor : sensorsList) {
            sensorsText.append(sensor.toString());

            if(!sensor.getStringType().isEmpty()) {
                sensorsText.append("\n");
                sensorsText.append("Type: ");
                sensorsText.append(sensor.getStringType());
            }
            sensorsText.append("\n\n");
        }
        viewBinding.allSensorsPreview.setText(sensorsText);
    }
}