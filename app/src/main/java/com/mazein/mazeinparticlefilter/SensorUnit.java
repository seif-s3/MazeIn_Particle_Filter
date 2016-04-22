package com.mazein.mazeinparticlefilter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Seif3 on 3/30/2016.
 */
public class SensorUnit implements SensorEventListener
{
    public static final String LOG_TAG = "SensorUnit";
    private static SensorData mSensorData;
    float[] inR = new float[16];
    float[] I = new float[16];
    private SensorManager mSensorManager = null;
    private boolean isPositioningOn = false;
    private Context context;
    // for handling rotation sensor
    private float rotationMatrix[] = new float[9];
    private float rotationAngle = (float) (180.0 / Math.PI);
    private int mDataReady;
    private IStepTrigger mStepListener;

    public SensorUnit(Context ctx)
    {
        this.context = ctx;
        mSensorData = new SensorData();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public static SensorData getSensorData()
    {
        return mSensorData;
    }

    public static double getMagneticMagnitude()
    {
        return Math.sqrt(mSensorData.mag[0] * mSensorData.mag[0]
                + mSensorData.mag[1] * mSensorData.mag[1]
                + mSensorData.mag[2] * mSensorData.mag[2]
        );
    }

    /**
     * Pause the position processing
     */
    public void pause() {
        if (isPositioningOn) {
            Log.i(LOG_TAG, "pause");
            isPositioningOn = false;
            mSensorManager.unregisterListener(this);
        }
    }

    /**
     * Resume the position processing
     */
    public void resume() {
        if (!isPositioningOn) {
            Log.i(LOG_TAG, "resume");
            isPositioningOn = true;
            registerSensors();
        }
    }

    // register sensors
    private void registerSensors() {
        // get orientation sensor
        Sensor orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if (orientation != null) {
            mSensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(LOG_TAG, "Orientation sensor: " + orientation.getName());
        }
        Sensor step = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if(step != null)
        {
            mSensorManager.registerListener(this, step, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(LOG_TAG, "StepDetector: " + step.getName());
        }

		Sensor gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (gyroscope != null) {
			mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
			Log.i(LOG_TAG, "Gyroscope: " + gyroscope.getName());
		}

        Sensor acc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (acc != null) {
            mSensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(LOG_TAG, "Accelerometer: " + acc.getName());
        }

		Sensor mag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		if (mag != null) {
			mSensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_FASTEST);
			Log.i(LOG_TAG, "Magnetic field: " + mag.getName());
		}

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            mSensorData.mag[0] = event.values[0];
            mSensorData.mag[1] = event.values[1];
            mSensorData.mag[2] = event.values[2];
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float alpha = 0.8f;

            mSensorData.acc[0] = alpha * mSensorData.acc[0] + (1 - alpha) * event.values[0];
            mSensorData.acc[1] = alpha * mSensorData.acc[1] + (1 - alpha) * event.values[1];
            mSensorData.acc[2] = alpha * mSensorData.acc[2] + (1 - alpha) * event.values[2];
        }

        rotationMatrix = new float[16];
        SensorManager.getRotationMatrix(rotationMatrix, null, mSensorData.acc, mSensorData.mag);
        SensorManager.getOrientation(rotationMatrix, mSensorData.ori);

        //double Heading=vals[0] * (180/Math.PI);

        /*
        switch (event.sensor.getType())
        {
            case Sensor.TYPE_ROTATION_VECTOR: {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                SensorManager.getOrientation(rotationMatrix, event.values);
                for (int i = 0; i < 3; i++) {
                    event.values[i] *= rotationAngle;
                }
                // no break, fall through
            }

//            case Sensor.TYPE_ORIENTATION:
//                mSensorData.ori[0] = event.values[0];
//                mSensorData.ori[1] = event.values[1];
//                mSensorData.ori[2] = event.values[2];
//                mDataReady |= 1;
//                //triggerOrientationListeners(mSensorData.ori);
//                break;

            case Sensor.TYPE_GYROSCOPE:
                mSensorData.gyr[0] = event.values[0];
                mSensorData.gyr[1] = event.values[1];
                mSensorData.gyr[2] = event.values[2];
                mDataReady |= 2;
                break;

            case Sensor.TYPE_ACCELEROMETER:
                mSensorData.acc[0] = event.values[0];
                mSensorData.acc[1] = event.values[1];
                mSensorData.acc[2] = event.values[2];
                mDataReady |= 4;
//                ((IAccelerometerListner)(mStepListener)).onReceiveAccelerometer(mSensorData.acc);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                mSensorData.mag[0] = event.values[0];
                mSensorData.mag[1] = event.values[1];
                mSensorData.mag[2] = event.values[2];
                mDataReady |= 8;
//                ((IMagnetometerListner)(mStepListener)).onReceiveMagnetometer(mSensorData.mag);
                break;

            case Sensor.TYPE_STEP_DETECTOR:
                // Call the Particle Filter function.
                if(mStepListener != null)
                    mStepListener.onStepDetected(mSensorData);
                Log.i("StepDetector", "Step Detected...");
                break;

        }

        if(mSensorData.acc != null && mSensorData.mag != null)
        {
            boolean success = SensorManager.getRotationMatrix(inR, I,
                    mSensorData.acc, mSensorData.mag);
            if (success)
            {
                SensorManager.getOrientation(inR, mSensorData.ori);
            }
        }
        */
    }

    public void registerStepListner(IStepTrigger listener)
    {
        mStepListener = listener;
    }

    /**
     * Get MagnetismInformation
     *
     * @return the current magnetism information
     */

    public float[] getMagnetism() {
        float[] magInfo = { mSensorData.mag[0], mSensorData.mag[1], mSensorData.mag[2] };
        return magInfo;
    }

    /**
     * Get Unfiltered heading
     *
     * @return the current heading in degrees
     */
    public float getRAWHeading() {
        return mSensorData.ori[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}
