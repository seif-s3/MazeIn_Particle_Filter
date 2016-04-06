package com.mazein.mazeinparticlefilter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by Seif3 on 3/31/2016.
 */
// Class for aggregating sensor data and triggering step events.

public class StepDetector
        implements IAccelerometerListner, IMagnetometerListner,
        SensorEventListener
{
    public StepDetector()
    {
    }

    @Override
    public void onReceiveAccelerometer(float[] rawData)
    {

    }

    @Override
    public void onReceiveMagnetometer(float[] rawData)
    {

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR)
        {

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}
