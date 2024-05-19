package com.rma.sensors;

import android.graphics.Color;
import android.hardware.Sensor;
import java.util.HashMap;
import java.util.Map;

public class GlobalConstants {
    private static final Map<Integer, String> measurementUnits;
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
}
