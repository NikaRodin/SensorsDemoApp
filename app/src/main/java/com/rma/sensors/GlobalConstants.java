package com.rma.sensors;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.HashMap;
import java.util.Map;

public class GlobalConstants {
    private static final Map<Integer, String> measurementUnits;
    private static final Map<Integer, Double> frequencies;
    private static final int[] colors = {Color.RED, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN};
    private static final String[] titles = {"x", "y", "z", "x'", "y'", "z'"};

    static {
        measurementUnits = new HashMap<>();
        measurementUnits.put(Sensor.TYPE_GRAVITY, "m/s²");
        measurementUnits.put(Sensor.TYPE_ACCELEROMETER, "m/s²");
        measurementUnits.put(Sensor.TYPE_LINEAR_ACCELERATION, "m/s²");
        measurementUnits.put(Sensor.TYPE_GYROSCOPE, "rad/s");
        measurementUnits.put(Sensor.TYPE_ROTATION_VECTOR, "");
        measurementUnits.put(Sensor.TYPE_STEP_COUNTER, "steps");

        measurementUnits.put(Sensor.TYPE_MAGNETIC_FIELD, "µT");
        measurementUnits.put(Sensor.TYPE_ORIENTATION, "°");
        measurementUnits.put(Sensor.TYPE_PROXIMITY, "cm");

        measurementUnits.put(Sensor.TYPE_LIGHT, "lx");
        measurementUnits.put(Sensor.TYPE_AMBIENT_TEMPERATURE, "°C");
        measurementUnits.put(Sensor.TYPE_PRESSURE, "hPa");
        measurementUnits.put(Sensor.TYPE_RELATIVE_HUMIDITY, "%");

        frequencies = new HashMap<>();
        frequencies.put(SensorManager.SENSOR_DELAY_FASTEST, -1.0d);
        frequencies.put(SensorManager.SENSOR_DELAY_GAME, 50.0d);    // 20 ms
        frequencies.put(SensorManager.SENSOR_DELAY_UI, 16.67d);     // 60 ms
        frequencies.put(SensorManager.SENSOR_DELAY_NORMAL, 5.0d);   // 200 ms
    }

    public static int getColor(int index){
        return colors[index % colors.length];
    }

    public static String getTitle(int index){
        return titles[index % titles.length];
    }

    public static String getMeasurementUnit(int sensorType){
        String unit = measurementUnits.get(sensorType);
        return unit != null ? unit : "";
    }

    public static double getFrequency(int sensorDelay){
        Double fq = frequencies.get(sensorDelay);
        return fq != null ? fq : 1f/((float)sensorDelay/1000000f);
    }
}
