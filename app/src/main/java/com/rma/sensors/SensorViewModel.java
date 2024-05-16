package com.rma.sensors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SensorViewModel extends ViewModel {
    private final MutableLiveData<float[]> data = new MutableLiveData<>();

    public LiveData<float[]> getData() {
        return data;
    }

    public void setData(float[] value) {
        data.setValue(value);
    }
}
